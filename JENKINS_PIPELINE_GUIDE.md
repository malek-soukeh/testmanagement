# Guide du Pipeline Jenkins pour Tests de Performance

## Vue d'ensemble

Ce pipeline Jenkins (`Jenkinsfile-Performance`) :
1. ✅ Récupère `template.xml` depuis Git
2. ✅ Parse le scénario JSON reçu du backend
3. ✅ Extrait les paramètres et parse l'URL
4. ✅ Exécute JMeter avec `template.xml`
5. ✅ Génère un rapport HTML
6. ✅ Parse les résultats JTL
7. ✅ Envoie le callback au backend avec les métriques

## Configuration requise

### Plugins Jenkins nécessaires

- **Pipeline** (déjà inclus)
- **Git** (pour le checkout)
- **HTTP Request Plugin** (pour le callback)
- **HTML Publisher Plugin** (pour publier les rapports)

### Outils nécessaires sur l'agent Jenkins

- **JMeter** installé et dans le PATH
- **Java** (pour JMeter)
- **Git** (pour le checkout)

## Installation

### 1. Créer un nouveau Job Pipeline

1. Dans Jenkins, allez dans **New Item**
2. Choisissez **Pipeline**
3. Nommez-le (ex: `performance-test`)

### 2. Configurer le Pipeline

1. Dans la section **Pipeline**, choisissez :
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: `https://github.com/malek-soukeh/testmanagement.git`
   - **Branch**: `*/master`
   - **Script Path**: `Jenkinsfile-Performance`

OU

Copiez le contenu de `Jenkinsfile-Performance` directement dans le script Pipeline.

### 3. Configurer les variables d'environnement

Dans le Jenkinsfile, modifiez ces variables selon votre environnement :

```groovy
environment {
    GIT_REPO = 'https://github.com/malek-soukeh/testmanagement.git'
    GIT_BRANCH = 'master'
    BACKEND_URL = 'http://192.168.56.1:8081'
    CALLBACK_TOKEN = 'your-secret-token-here'
}
```

### 4. Vérifier JMeter

Assurez-vous que JMeter est installé et accessible :

```bash
which jmeter
jmeter -v
```

## Utilisation

### Depuis le Backend

Le backend appelle automatiquement ce pipeline via :

```java
POST http://jenkins-url/job/performance-test/buildWithParameters
Body (form-data):
  SCENARIO_JSON: {...}
  TEST_RESULT_ID: 123
```

### Depuis Jenkins UI

1. Allez sur le job `performance-test`
2. Cliquez sur **Build with Parameters**
3. Entrez le `SCENARIO_JSON` et `TEST_RESULT_ID`
4. Cliquez sur **Build**

### Exemple de SCENARIO_JSON

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

## Étapes du Pipeline

### 1. Checkout Git Repository
- Clone le repo Git
- Récupère `template.xml`
- Vérifie que le fichier existe

### 2. Parse JSON and Extract Parameters
- Parse le JSON reçu
- Extrait l'URL et la décompose (protocol, domain, port, path)
- Extrait les paramètres de performance
- Crée les répertoires de résultats

### 3. Run JMeter Test
- Exécute JMeter en mode non-GUI avec `template.xml`
- Passe tous les paramètres via `-J`
- Génère le fichier JTL

### 4. Generate HTML Report
- Génère un rapport HTML depuis le JTL
- Stocke dans `reports/report_{BUILD_NUMBER}/`

### 5. Parse Results and Send Callback
- Parse le fichier JTL
- Extrait les métriques (avg, max, p95, errorRate)
- Envoie le callback au backend

### 6. Archive Artifacts
- Archive les fichiers JTL et rapports HTML

## Format du Callback

Le pipeline envoie au backend :

```json
{
  "status": "PASSED",
  "avgResponseTimeMs": 250.5,
  "maxResponseTimeMs": 1200.0,
  "p95ResponseTimeMs": 450.0,
  "errorRatePercent": 0.5,
  "jmeterReportUrl": "http://jenkins/artifact/reports/report_123/index.html"
}
```

## Dépannage

### Problème : template.xml non trouvé

**Solution** : Vérifiez que le fichier existe dans le repo Git et que le checkout fonctionne.

### Problème : JMeter non trouvé

**Solution** : Installez JMeter et ajoutez-le au PATH, ou utilisez le chemin complet :
```groovy
sh '/opt/jmeter/bin/jmeter -n -t template.xml ...'
```

### Problème : Callback échoue

**Solution** : 
- Vérifiez que `BACKEND_URL` est correct
- Vérifiez que `CALLBACK_TOKEN` correspond à celui du backend
- Vérifiez que le backend est accessible depuis Jenkins

### Problème : Parsing JTL échoue

**Solution** : Vérifiez le format du fichier JTL. Le parser attend le format standard JMeter.

## Améliorations possibles

1. **Parallélisation** : Exécuter plusieurs tests en parallèle
2. **Notifications** : Envoyer des emails/Slack en cas d'échec
3. **Historique** : Stocker les métriques dans une base de données
4. **Graphiques** : Générer des graphiques de tendance
5. **Comparaison** : Comparer avec les tests précédents

## Notes importantes

- Le pipeline nécessite que JMeter soit installé sur l'agent Jenkins
- Le token `CALLBACK_TOKEN` doit correspondre à celui configuré dans le backend
- Le backend doit être accessible depuis Jenkins pour le callback
- Le format JTL doit être standard (CSV avec les colonnes attendues)

