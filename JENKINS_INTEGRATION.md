# Guide d'intégration Jenkins pour les Tests de Performance

## Vue d'ensemble

Ce document explique comment configurer Jenkins pour transformer le JSON reçu du backend en paramètres JMeter et exécuter le test plan `template.xml`.

## Paramètres reçus par Jenkins

Le backend envoie à Jenkins deux paramètres via `buildWithParameters` :

1. **SCENARIO_JSON** : JSON contenant le scénario de test
2. **TEST_RESULT_ID** : ID du résultat de test pour le callback

### Format de SCENARIO_JSON

```json
{
  "testCaseId": 1,
  "title": "Test de performance - Page d'accueil",
  "url": "https://example.com/api/users",
  "performance": {
    "testType": "LOAD_TEST",
    "numberOfUsers": 100,
    "durationSeconds": 300,
    "rampUpSeconds": 60,
    "requestsPerSecond": 50,
    "timeoutMs": 5000,
    "additionalParams": {
      "method": "GET",
      "authorization": "Bearer token123",
      "contentType": "application/json"
    }
  }
}
```

## Transformation JSON → Paramètres JMeter

Jenkins doit parser le JSON et extraire les paramètres pour JMeter. Voici le mapping :

### Mapping des paramètres

| Champ JSON | Variable JMeter | Description | Exemple |
|------------|----------------|-------------|---------|
| `performance.numberOfUsers` | `USERS` | Nombre d'utilisateurs virtuels | `100` |
| `performance.rampUpSeconds` | `RAMP_UP` | Temps de montée en charge (secondes) | `60` |
| `performance.durationSeconds` | `DURATION` | Durée du test (secondes) | `300` |
| `url` (parsed) | `PROTOCOL` | Protocole (http/https) | `https` |
| `url` (parsed) | `DOMAIN` | Domaine | `example.com` |
| `url` (parsed) | `PORT` | Port (vide si défaut) | `` |
| `url` (parsed) | `PATH` | Chemin | `/api/users` |
| `performance.additionalParams.method` | `METHOD` | Méthode HTTP | `GET` |
| `performance.additionalParams.authorization` | `AUTHORIZATION` | Token d'authentification | `Bearer token123` |
| `performance.additionalParams.contentType` | `CONTENT_TYPE` | Type de contenu | `application/json` |
| `performance.testType` | `USE_DURATION` | Active le scheduler si LOAD_TEST | `true` |
| `performance.timeoutMs` | `TIMEOUT` | Timeout en millisecondes | `5000` |
| `performance.requestsPerSecond` | `REQUESTS_PER_SECOND` | Nombre de requêtes par seconde (optionnel) | `50` |
| `performance.additionalParams.assertContains` | `ASSERT_CONTAINS` | Assertion optionnelle | `"success"` |
| - | `RESULT_FILE` | Fichier de résultats | `results/result_${BUILD_NUMBER}.jtl` |

### Parsing de l'URL

L'URL complète doit être parsée pour extraire les composants :

```groovy
// Exemple en Groovy (Jenkins Pipeline)
def url = json.url // "https://example.com:8080/api/users"
def urlObj = new URL(url)

PROTOCOL = urlObj.protocol  // "https"
DOMAIN = urlObj.host        // "example.com"
PORT = urlObj.port != -1 ? urlObj.port.toString() : ""  // "8080" ou ""
PATH = urlObj.path          // "/api/users"
```

### Gestion du testType

Le `testType` influence certains paramètres :

- **LOAD_TEST** : `USE_DURATION=true`, utilise `durationSeconds`
- **STRESS_TEST** : `USE_DURATION=true`, peut augmenter progressivement les utilisateurs
- **SPIKE_TEST** : `USE_DURATION=true`, montée rapide puis descente
- **ENDURANCE_TEST** : `USE_DURATION=true`, durée longue

## Script Jenkins Pipeline (exemple)

```groovy
pipeline {
    agent any
    
    parameters {
        string(name: 'SCENARIO_JSON', description: 'JSON scenario from backend')
        string(name: 'TEST_RESULT_ID', description: 'Test result ID for callback')
    }
    
    stages {
        stage('Parse JSON and Extract Parameters') {
            steps {
                script {
                    def json = readJSON text: params.SCENARIO_JSON
                    def perf = json.performance
                    def url = new URL(json.url)
                    
                    // Extract URL components
                    env.PROTOCOL = url.protocol
                    env.DOMAIN = url.host
                    env.PORT = url.port != -1 ? url.port.toString() : ""
                    env.PATH = url.path ?: "/"
                    
                    // Extract performance parameters
                    env.USERS = perf.numberOfUsers?.toString() ?: "10"
                    env.RAMP_UP = perf.rampUpSeconds?.toString() ?: "10"
                    env.DURATION = perf.durationSeconds?.toString() ?: "60"
                    env.USE_DURATION = "true"
                    env.TIMEOUT = perf.timeoutMs?.toString() ?: "5000"
                    env.REQUESTS_PER_SECOND = perf.requestsPerSecond?.toString() ?: ""
                    
                    // Extract additional params
                    env.METHOD = perf.additionalParams?.method ?: "GET"
                    env.AUTHORIZATION = perf.additionalParams?.authorization ?: ""
                    env.CONTENT_TYPE = perf.additionalParams?.contentType ?: "application/json"
                    env.ASSERT_CONTAINS = perf.additionalParams?.assertContains ?: ""
                    
                    // Result file
                    env.RESULT_FILE = "results/result_${BUILD_NUMBER}.jtl"
                }
            }
        }
        
        stage('Run JMeter Test') {
            steps {
                    sh '''
                    jmeter -n -t template.xml \
                        -JUSERS=${USERS} \
                        -JRAMP_UP=${RAMP_UP} \
                        -JDURATION=${DURATION} \
                        -JUSE_DURATION=${USE_DURATION} \
                        -JPROTOCOL=${PROTOCOL} \
                        -JDOMAIN=${DOMAIN} \
                        -JPORT=${PORT} \
                        -JPATH=${PATH} \
                        -JMETHOD=${METHOD} \
                        -JAUTHORIZATION="${AUTHORIZATION}" \
                        -JCONTENT_TYPE=${CONTENT_TYPE} \
                        -JASSERT_CONTAINS="${ASSERT_CONTAINS}" \
                        -JTIMEOUT=${TIMEOUT} \
                        -JREQUESTS_PER_SECOND=${REQUESTS_PER_SECOND} \
                        -JRESULT_FILE=${RESULT_FILE} \
                        -l ${RESULT_FILE}
                '''
            }
        }
        
        stage('Generate Report and Send Callback') {
            steps {
                script {
                    // Générer le rapport HTML (si jmeter-plugins est installé)
                    sh '''
                        if [ -f ${RESULT_FILE} ]; then
                            jmeter -g ${RESULT_FILE} -o reports/report_${BUILD_NUMBER}
                        fi
                    '''
                    
                    // Parser les résultats JTL pour extraire les métriques
                    def metrics = parseJTLResults("${env.RESULT_FILE}")
                    
                    // Envoyer le callback au backend
                    def callbackUrl = "http://your-backend:8081/api/performance/results/${params.TEST_RESULT_ID}/callback"
                    def callbackPayload = [
                        status: metrics.status,
                        avgResponseTimeMs: metrics.avgResponseTime,
                        maxResponseTimeMs: metrics.maxResponseTime,
                        p95ResponseTimeMs: metrics.p95ResponseTime,
                        errorRatePercent: metrics.errorRate,
                        jmeterReportUrl: "${env.BUILD_URL}reports/report_${BUILD_NUMBER}/index.html"
                    ]
                    
                    httpRequest(
                        url: callbackUrl,
                        httpMode: 'POST',
                        contentType: 'APPLICATION_JSON',
                        customHeaders: [[name: 'X-JENKINS-TOKEN', value: 'your-secret-token']],
                        requestBody: groovy.json.JsonOutput.toJson(callbackPayload)
                    )
                }
            }
        }
    }
}

// Fonction helper pour parser les résultats JTL
def parseJTLResults(String jtlFile) {
    // Parser le fichier JTL pour extraire les métriques
    // Format JTL: timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect
    def metrics = [:]
    def lines = readFile(jtlFile).split('\n')
    def responseTimes = []
    def errors = 0
    def total = 0
    
    lines.each { line ->
        if (line.startsWith('timeStamp')) return // Skip header
        def cols = line.split(',')
        if (cols.size() > 3) {
            total++
            def elapsed = cols[1].toInteger()
            responseTimes.add(elapsed)
            if (cols[7] == 'false') errors++
        }
    }
    
    responseTimes.sort()
    metrics.avgResponseTime = responseTimes.sum() / responseTimes.size()
    metrics.maxResponseTime = responseTimes.max()
    metrics.p95ResponseTime = responseTimes[(int)(responseTimes.size() * 0.95)]
    metrics.errorRate = (errors / total) * 100
    metrics.status = metrics.errorRate < 5 ? "PASSED" : "FAILED"
    
    return metrics
}
```

## Configuration du Job Jenkins

1. **Créer un Job de type "Pipeline"**
2. **Configurer les paramètres** :
   - `SCENARIO_JSON` (String parameter)
   - `TEST_RESULT_ID` (String parameter)
3. **Configurer le Pipeline** avec le script ci-dessus
4. **Installer les plugins nécessaires** :
   - Pipeline
   - HTTP Request Plugin (pour le callback)
   - HTML Publisher Plugin (pour les rapports)

## Améliorations possibles du template.xml

Le template actuel supporte déjà les paramètres de base. Pour supporter plus de types de tests, on pourrait ajouter :

1. **Constant Throughput Timer** pour `requestsPerSecond`
2. **Different Thread Groups** selon le `testType`
3. **Stepping Thread Group** pour les tests de stress/spike
4. **Response Times Over Time** listener

## Notes importantes

- Le template.xml doit être accessible depuis Jenkins (dans le workspace ou dans un repo)
- Les résultats JTL doivent être parsés pour extraire les métriques
- Le callback doit être envoyé même en cas d'erreur (avec status FAILED)
- Le token `X-JENKINS-TOKEN` doit correspondre à celui configuré dans le backend

