# Configuration Jenkins - Guide de résolution des problèmes

## Problème identifié

L'erreur principale est que Jenkins ne peut pas accéder à `repo.maven.apache.org` (problème de réseau/DNS).

## Solutions possibles

### Solution 1: Utiliser un JAR précompilé (Recommandé si problème réseau persistant)

1. Compilez le projet localement sur une machine avec accès Internet:
   ```bash
   mvn clean package -DskipTests
   ```

2. Uploadez le JAR dans Jenkins:
   - Allez dans la configuration du job Jenkins
   - Ajoutez une étape "Copy Artifacts" ou utilisez un plugin de stockage
   - Ou copiez le JAR dans le workspace Jenkins

3. Modifiez le pipeline pour utiliser le JAR précompilé:
   ```groovy
   stage('Build') {
       steps {
           echo 'Using pre-built JAR...'
           sh "mkdir -p target || true"
           // Copiez votre JAR ici ou utilisez un plugin
       }
   }
   ```

### Solution 2: Configurer un proxy dans Jenkins

1. Allez dans **Manage Jenkins** > **Manage Plugins** > **Advanced**
2. Configurez le proxy HTTP:
   - HTTP Proxy: `http://proxy.example.com:8080`
   - No Proxy Host: `localhost,127.0.0.1`

3. Ou configurez Maven settings.xml:
   ```bash
   cp maven-settings.xml ~/.m2/settings.xml
   # Éditez le fichier pour ajouter votre proxy
   ```

### Solution 3: Utiliser un repository Maven local/mirror

1. Configurez un repository Maven local (Nexus, Artifactory, etc.)
2. Modifiez `maven-settings.xml` pour pointer vers votre repository
3. Copiez le fichier dans Jenkins:
   ```groovy
   stage('Build') {
       steps {
           sh "cp maven-settings.xml ~/.m2/settings.xml"
           sh "${MAVEN_HOME}/bin/mvn clean package -DskipTests -s ~/.m2/settings.xml"
       }
   }
   ```

### Solution 4: Vérifier la connectivité réseau

1. Vérifiez que Jenkins peut résoudre les DNS:
   ```bash
   ping repo.maven.apache.org
   nslookup repo.maven.apache.org
   ```

2. Vérifiez la connectivité HTTP:
   ```bash
   curl -I https://repo.maven.apache.org/maven2
   ```

3. Si le problème persiste, contactez votre administrateur réseau pour:
   - Configurer un proxy
   - Ouvrir les ports nécessaires
   - Configurer les DNS

## Fichiers créés

1. **TestExecutor.java**: Classe principale pour exécuter les tests Selenium depuis Jenkins
2. **Jenkinsfile**: Pipeline Jenkins amélioré avec gestion d'erreurs
3. **maven-settings.xml**: Configuration Maven avec support proxy/mirror

## Utilisation du pipeline

Le pipeline Jenkins utilise maintenant:
- `com.example.testmanagement.seleniumrunner.TestExecutor` comme classe principale
- Gestion automatique du classpath avec toutes les dépendances
- Support pour JAR précompilé en cas d'échec du build

## Test du pipeline

1. Assurez-vous que le problème réseau est résolu (une des solutions ci-dessus)
2. Configurez le job Jenkins avec le `Jenkinsfile`
3. Ajoutez le paramètre `SCENARIO_JSON` dans la configuration du job
4. Lancez le build

## Notes importantes

- Le JAR Spring Boot est un "fat JAR" mais ne peut pas être exécuté directement avec une classe main différente
- Il faut utiliser `java -cp` avec le JAR et toutes les dépendances
- Le pipeline copie automatiquement les dépendances dans `target/dependency/`

