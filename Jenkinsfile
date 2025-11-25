pipeline {
    agent any

    parameters {
        string(name: 'SCENARIO_JSON', defaultValue: '', description: 'JSON scenario to execute')
        string(name: 'TEST_RESULT_ID', defaultValue: '', description: 'Backend test result identifier')
    }

    environment {
        GIT_REPO = 'https://github.com/malek-soukeh/testmanagement.git'
        GIT_BRANCH = 'master'
        MAVEN_HOME = '/usr/share/maven'
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        BACKEND_URL = 'http://192.168.56.1:8081'
        CALLBACK_TOKEN = '5c7af1e8-ba95-442f-a5e2-d0f18c7051e6'
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
                    if (!params.TEST_RESULT_ID || params.TEST_RESULT_ID.trim().isEmpty()) {
                        error("TEST_RESULT_ID parameter is required")
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
                    int exitCode = sh(
                        script: "mvn exec:java -Dexec.mainClass=\"com.example.testmanagement.seleniumrunner.TestExecutor\" -Dexec.args=\"/tmp/scenario.json\"",
                        returnStatus: true
                    )
                    env.SELENIUM_STATUS = exitCode == 0 ? "PASSED" : "FAILED"
                    if (exitCode != 0) {
                        echo "Selenium run failed with exit code ${exitCode}"
                        currentBuild.result = 'FAILURE'
                    }
                    String summaryFile = sh(
                        script: "ls -t target/selenium-runs/*/summary.json 2>/dev/null | head -n 1 || true",
                        returnStdout: true
                    ).trim()
                    env.SUMMARY_FILE = summaryFile
                }
            }
        }
        
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/selenium-runs/**/*', allowEmptyArchive: true
                junit 'target/selenium-runs/**/junit-report.xml'
            }
        }

        stage('Send Callback') {
            when {
                expression { params.TEST_RESULT_ID?.trim() }
            }
            steps {
                script {
                    def summaryContent = null
                    if (env.SUMMARY_FILE?.trim()) {
                        summaryContent = readFile(env.SUMMARY_FILE)
                    }
                    def callbackPayload = [
                        status: env.SELENIUM_STATUS ?: 'FAILED',
                        summaryJson: summaryContent,
                        artifactUrl: env.SUMMARY_FILE ? "${env.BUILD_URL}artifact/${env.SUMMARY_FILE}" : null
                    ]
                    writeJSON file: 'callback-payload.json', json: callbackPayload, pretty: 2
                    sh """
                        curl -X POST \\
                            -H "Content-Type: application/json" \\
                            -H "X-JENKINS-TOKEN: ${env.CALLBACK_TOKEN}" \\
                            --data @callback-payload.json \\
                            ${env.BACKEND_URL}/api/tests/results/${params.TEST_RESULT_ID}/callback
                    """
                }
            }
        }
    }
}

