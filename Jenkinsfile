pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        IMAGE = "payments:${env.BUILD_NUMBER}"
    }

    stages {

        stage('Secret scan (Gitleaks)') {
            steps {
                sh '''
                    docker run --rm -v "$PWD:/repo" zricethezav/gitleaks:latest \
                        detect --source /repo --redact --verbose
                '''
            }
        }

        stage('Build and unit tests') {
            steps {
                // Only the DB-free unit test runs here. The default Spring
                // context test needs a database, so it is excluded for now.
                sh '''
                    docker run --rm --user "$(id -u):$(id -g)" -e HOME=/tmp \
                        -v "$PWD:/app" -w /app maven:3.9-eclipse-temurin-21 \
                        mvn -B -Dmaven.repo.local=/tmp/m2 test -Dtest=IbanValidatorTest
                '''
            }
        }

        stage('SAST (Semgrep)') {
            steps {
                sh '''
                    docker run --rm -v "$PWD:/src" -w /src semgrep/semgrep \
                        semgrep scan --config p/java --config p/owasp-top-ten \
                        --metrics=off --error src
                '''
            }
        }

        stage('Dependency scan (Trivy)') {
            steps {
                sh '''
                    docker run --rm -v "$PWD:/project" aquasec/trivy:latest \
                        fs --scanners vuln --severity HIGH,CRITICAL \
                        --ignore-unfixed --exit-code 1 /project/pom.xml
                '''
            }
        }

        stage('Docker build') {
            steps {
                sh 'docker build -t $IMAGE .'
            }
        }

        stage('Image scan (Trivy)') {
            steps {
                sh '''
                    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
                        aquasec/trivy:latest image --severity HIGH,CRITICAL \
                        --ignore-unfixed --exit-code 1 $IMAGE
                '''
            }
        }
    }

    post {
        always {
            sh 'docker rmi $IMAGE || true'
        }
    }
}