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
                    
                    // Décoder le JSON encodé en URL
                    String decodedJson = java.net.URLDecoder.decode(params.SCENARIO_JSON, "UTF-8")
                    
                    // Sauvegarder le JSON décodé dans un fichier temporaire
                    writeFile file: '/tmp/scenario.json', text: decodedJson
                    
                    // Vérifier que le fichier a été créé correctement
                    sh """
                        echo "First 100 chars of JSON file:"
                        head -c 100 /tmp/scenario.json || true
                        echo ""
                    """
                    
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

