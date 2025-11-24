# Configuration du Job Jenkins pour Tests de Performance

## Problème actuel

Jenkins retourne une erreur 500 car le job `JmeterTest` n'existe pas ou n'est pas configuré correctement.

## Solution : Créer le Job Jenkins

### Étape 1 : Créer un nouveau Job Pipeline

1. **Connectez-vous à Jenkins** : `http://10.0.0.15:8080`
2. Cliquez sur **"New Item"** dans le menu de gauche
3. Entrez le nom : **`JmeterTest`**
4. Sélectionnez **"Pipeline"**
5. Cliquez sur **"OK"**

### Étape 2 : Configurer le Pipeline

**IMPORTANT** : Utilisez `Jenkinsfile-Performance-Simple` qui ne nécessite pas de plugins supplémentaires.

1. Dans la section **"Pipeline"**, choisissez :
   - **Definition**: Pipeline script from SCM
   - **SCM**: Git
   - **Repository URL**: `https://github.com/malek-soukeh/testmanagement.git`
   - **Branch**: `*/master`
   - **Script Path**: `Jenkinsfile-Performance-Simple`

   **OU**

   Copiez le contenu de `Jenkinsfile-Performance-Simple` directement dans le script Pipeline (recommandé).

### Étape 3 : Configurer les Paramètres du Build

1. Cochez **"This project is parameterized"**
2. Ajoutez les paramètres suivants :
   - **String Parameter** :
     - Name: `SCENARIO_JSON`
     - Description: `JSON scenario from backend`
     - Default Value: (laisser vide)
   - **String Parameter** :
     - Name: `TEST_RESULT_ID`
     - Description: `Test result ID for callback`
     - Default Value: (laisser vide)

### Étape 4 : Vérifier les Prérequis

Assurez-vous que sur l'agent Jenkins :
- **JMeter** est installé et dans le PATH
- **Java** est installé (pour JMeter)
- **Git** est installé
- **Python 3** est installé (pour parser le JSON et envoyer le callback)
- **curl** est installé (pour envoyer le callback HTTP)

Pour vérifier :
```bash
which jmeter
jmeter -v
java -version
git --version
python3 --version
curl --version
```

**Note** : `Jenkinsfile-Performance-Simple` utilise Python et curl au lieu de plugins Jenkins, donc aucun plugin supplémentaire n'est nécessaire.

### Étape 5 : Tester le Job

1. Allez sur le job `JmeterTest`
2. Cliquez sur **"Build with Parameters"**
3. Entrez un JSON de test dans `SCENARIO_JSON` :
   ```json
   {
     "testCaseId": 1,
     "title": "Test de performance",
     "url": "http://192.168.56.1:4200/auth/login",
     "performance": {
       "testType": "LOAD_TEST",
       "numberOfUsers": 10,
       "durationSeconds": 60,
       "rampUpSeconds": 10,
       "timeoutMs": 5000
     }
   }
   ```
4. Entrez un ID dans `TEST_RESULT_ID` : `1`
5. Cliquez sur **"Build"**

## Alternative : Utiliser un Job Freestyle

Si vous préférez un job Freestyle au lieu d'un Pipeline :

1. Créez un nouveau job **Freestyle project** nommé `JmeterTest`
2. Dans **"Build"**, ajoutez :
   - **Execute shell** (Linux/Mac) ou **Execute Windows batch command** (Windows)
3. Utilisez le script `run-performance-test.sh` ou adaptez-le pour votre environnement

## Vérification

Une fois le job créé, testez depuis le backend :
- Le backend devrait pouvoir appeler : `http://10.0.0.15:8080/job/JmeterTest/buildWithParameters`
- Le job devrait démarrer et exécuter le pipeline

## Dépannage

### Erreur : "Job not found"
- Vérifiez que le job s'appelle exactement `JmeterTest`
- Vérifiez l'URL dans `application.properties` : `jenkins.job-url`

### Erreur : "Permission denied"
- Vérifiez les credentials Jenkins dans `application.properties`
- Vérifiez que l'utilisateur a les permissions pour déclencher des builds

### Erreur : "JMeter not found"
- Installez JMeter sur l'agent Jenkins
- Ajoutez JMeter au PATH ou utilisez le chemin complet dans le pipeline

## Configuration dans application.properties

Assurez-vous que ces valeurs sont correctes :

```properties
jenkins.job-url=http://10.0.0.15:8080/job/JmeterTest/buildWithParameters
jenkins.user=admin
jenkins.token=11d1741c72084f7b0ebd2144638320e8d2
```

