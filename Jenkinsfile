pipeline {
    agent any

    options {
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
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
                    docker run --rm -v "$PWD:/project" \
                        -v trivy-cache:/root/.cache/ \
                        aquasec/trivy:latest fs --timeout 15m --scanners vuln \
                        --severity HIGH,CRITICAL --ignore-unfixed \
                        --exit-code 1 /project/pom.xml
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
                        -v trivy-cache:/root/.cache/ \
                        aquasec/trivy:latest image --timeout 15m --scanners vuln \
                        --severity HIGH,CRITICAL --ignore-unfixed \
                        --exit-code 1 $IMAGE
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