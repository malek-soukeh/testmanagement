pipeline {
    agent any

    environment {
        GIT_REPO = 'https://github.com/malek-soukeh/testmanagement.git'
        GIT_BRANCH = 'master'
        TEST_RUNNER = 'com.example.testmanagement.seleniumrunner.TestExecutor'
        MAVEN_HOME = '/usr/share/maven'
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Clonage du dépôt Git...'
                git branch: "${GIT_BRANCH}", url: "${GIT_REPO}"
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building Spring Boot project with Maven...'
                script {
                    // Option 1: Si vous avez un repository Maven local ou proxy
                    // Décommentez ces lignes et configurez votre settings.xml
                    // sh "cp /path/to/settings.xml ~/.m2/settings.xml"
                    
                    // Option 2: Utiliser un JAR précompilé (recommandé si problème réseau)
                    // Si vous avez déjà un JAR compilé, décommentez cette section:
                    /*
                    sh """
                        if [ -f target/testmanagement-0.0.1-SNAPSHOT.jar ]; then
                            echo 'JAR already exists, skipping build'
                        else
                            echo 'Building with Maven...'
                            ${MAVEN_HOME}/bin/mvn clean package -DskipTests
                        fi
                    """
                    */
                    
                    // Option 3: Build normal (nécessite accès Internet)
                    try {
                        sh "${MAVEN_HOME}/bin/mvn clean package -DskipTests"
                    } catch (Exception e) {
                        echo "Build failed, trying with offline mode or using pre-built JAR"
                        // Si le build échoue, vous pouvez utiliser un JAR précompilé
                        // ou configurer un repository Maven local
                        error("Build failed. Please ensure Maven can access repositories or use a pre-built JAR.")
                    }
                }
            }
        }
        
        stage('Copy Dependencies') {
            steps {
                echo 'Copying Maven dependencies for classpath...'
                sh "${MAVEN_HOME}/bin/mvn dependency:copy-dependencies -DoutputDirectory=target/dependency"
            }
        }
        
        stage('Run Selenium Tests') {
            steps {
                echo 'Running Selenium Runner...'
                script {
                    // Vérifier que le JAR existe
                    def jarExists = sh(
                        script: "test -f target/testmanagement-0.0.1-SNAPSHOT.jar",
                        returnStatus: true
                    ) == 0
                    
                    if (!jarExists) {
                        error("JAR file not found. Build must succeed first.")
                    }
                    
                    // Construire le classpath: JAR principal + toutes les dépendances
                    def classpath = "target/testmanagement-0.0.1-SNAPSHOT.jar"
                    sh "find target/dependency -name '*.jar' | tr '\\n' ':' | sed 's/:$//' > target/classpath.txt || echo '' > target/classpath.txt"
                    def depClasspath = sh(
                        script: "cat target/classpath.txt",
                        returnStdout: true
                    ).trim()
                    
                    if (depClasspath) {
                        classpath = "${depClasspath}:${classpath}"
                    }
                    
                    // Décoder le SCENARIO_JSON si nécessaire
                    def scenarioJson = "${SCENARIO_JSON}"
                    
                    sh """
                        java -cp "${classpath}" \
                        ${TEST_RUNNER} \
                        '${scenarioJson}'
                    """
                }
            }
        }

        stage('Publish Reports') {
            steps {
                echo 'Publishing test reports...'
                script {
                    // Créer le dossier screenshots s'il n'existe pas
                    sh "mkdir -p screenshots || true"
                    
                    // Archiver les screenshots
                    archiveArtifacts artifacts: 'screenshots/*.png', allowEmptyArchive: true
                    archiveArtifacts artifacts: 'target/selenium-runs/**/*', allowEmptyArchive: true
                    
                    // Publier les rapports JUnit si disponibles
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
    }

    post {
        success {
            echo '✅ Tests exécutés avec succès.'
        }
        failure {
            echo '❌ Échec des tests.'
            // Optionnel: Envoyer une notification
        }
        always {
            // Nettoyer les fichiers temporaires si nécessaire
            cleanWs()
        }
    }
}

