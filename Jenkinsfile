pipeline {
    agent any

    parameters {
        string(name: 'SCENARIO_JSON', defaultValue: '', description: 'JSON scenario to execute')
    }

    environment {
        GIT_REPO = 'https://github.com/malek-soukeh/testmanagement.git'
        GIT_BRANCH = 'master'
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
        
        stage('Build Project') {
            steps {
                sh """
                    mvn clean package -DskipTests
                """
            }
        }
        
        stage('Run Selenium Tests') {
            steps {
                script {
                    if (!params.SCENARIO_JSON || params.SCENARIO_JSON.trim().isEmpty()) {
                        error("SCENARIO_JSON parameter is required")
                    }
                    
                    echo 'Running Selenium Runner...'
                    echo "Scenario JSON length: ${params.SCENARIO_JSON.length()}"
                    
                    // Sauvegarder le JSON dans un fichier temporaire pour éviter les problèmes d'échappement
                    writeFile file: '/tmp/scenario.json', text: params.SCENARIO_JSON
                    
                    // Exécuter TestExecutor avec le fichier JSON
                    sh """
                        mvn exec:java -Dexec.mainClass="com.example.testmanagement.seleniumrunner.TestExecutor" -Dexec.args="/tmp/scenario.json"
                    """
                }
            }
        }
        
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/selenium-runs/**/*', allowEmptyArchive: true
                junit 'target/selenium-runs/**/junit-report.xml'
            }
        }
    }
}

