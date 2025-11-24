# Alternative : Pipeline sans plugins supplémentaires

## Problème

Le pipeline `Jenkinsfile-Performance` utilise :
- `readJSON` → nécessite le plugin **Pipeline Utility Steps**
- `httpRequest` → nécessite le plugin **HTTP Request Plugin**

Si ces plugins ne sont pas installés, vous obtiendrez des erreurs comme :
```
No such DSL method 'readJSON' found
No such DSL method 'httpRequest' found
```

## Solution : Utiliser `Jenkinsfile-Performance-Simple`

J'ai créé une version simplifiée qui utilise uniquement :
- **Python 3** (pour parser le JSON)
- **curl** (pour les appels HTTP)
- **Commandes shell** (pour tout le reste)

### Avantages

✅ Aucun plugin supplémentaire nécessaire  
✅ Fonctionne avec une installation Jenkins de base  
✅ Plus facile à maintenir et déboguer  

### Prérequis

- Python 3 installé sur l'agent Jenkins
- curl installé sur l'agent Jenkins
- JMeter installé et dans le PATH

### Installation

1. **Copiez le contenu de `Jenkinsfile-Performance-Simple`** dans votre job Jenkins
2. **OU** configurez le job pour utiliser le fichier depuis Git :
   - Script Path: `Jenkinsfile-Performance-Simple`

### Configuration

Le pipeline utilise Python pour :
- Parser le JSON du scénario
- Extraire les paramètres (URL, performance, etc.)
- Parser les résultats JTL
- Calculer les métriques (avg, max, p95, errorRate)
- Envoyer le callback au backend avec curl

### Exemple de sortie Python

Le script Python génère un fichier `jenkins_env.sh` avec toutes les variables :
```bash
export PROTOCOL=http
export DOMAIN=192.168.56.1
export PORT=4200
export PATH=/auth/login
export USERS=10
export RAMP_UP=10
export DURATION=60
...
```

Ces variables sont ensuite utilisées par JMeter.

### Callback HTTP

Le callback est envoyé avec `curl` :
```bash
curl -X POST \
  http://192.168.56.1:8081/api/performance/results/123/callback \
  -H "Content-Type: application/json" \
  -H "X-JENKINS-TOKEN: your-token" \
  -d '{"status":"PASSED","avgResponseTimeMs":250,...}'
```

## Migration depuis `Jenkinsfile-Performance`

Si vous utilisez déjà `Jenkinsfile-Performance` et voulez migrer :

1. Remplacez le script Pipeline par le contenu de `Jenkinsfile-Performance-Simple`
2. Vérifiez que Python 3 et curl sont installés
3. Testez avec un build

## Installation des plugins (optionnel)

Si vous préférez utiliser `Jenkinsfile-Performance` avec les plugins :

1. Allez dans **Manage Jenkins** > **Plugins**
2. Installez :
   - **Pipeline Utility Steps** (pour `readJSON`)
   - **HTTP Request Plugin** (pour `httpRequest`)
3. Redémarrez Jenkins

Mais **`Jenkinsfile-Performance-Simple` est recommandé** car il ne nécessite aucun plugin supplémentaire.

