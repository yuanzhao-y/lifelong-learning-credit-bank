pipeline {
  agent any

  tools {
    jdk 'JDK 17'
    maven 'Maven 3.9.6'
    nodejs 'NodeJS 22'
  }

  environment {
    PATH = "/var/jenkins_home/tools/bin:${env.PATH}"
  }

  options {
    timestamps()
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh 'git log -1 --oneline'
      }
    }
    stage('Toolchain Evidence') {
      steps {
        sh 'java -version'
        sh 'mvn -version'
      }
    }
    stage('Backend Test And Package') {
      steps {
        sh 'mvn -B clean verify'
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
      }
    }
    stage('Frontend Build') {
      steps {
        dir('credit-bank-frontend') {
          sh 'npm ci'
          sh 'npm run build'
        }
      }
    }
    stage('Docker Compose Evidence') {
      steps {
        withEnv([
          'MYSQL_PASSWORD=ci-compose-check-only',
          'MYSQL_ROOT_PASSWORD=ci-compose-check-only',
          'REDIS_PASSWORD=ci-compose-check-only',
          'JWT_SECRET=ci-compose-check-only-at-least-32-characters',
          'DATA_CRYPTO_SECRET=ci-compose-check-only-at-least-32-characters',
          'ADMIN_PASSWORD=ci-compose-check-only',
          'TOS_ACCESS_KEY_FILE=/tmp/ci-compose-check-access-key'
        ]) {
          sh '''
          docker-compose config --quiet
          sed -n "1,180p" docker-compose.yml \
            | sed -E 's/(MYSQL_PASSWORD|MYSQL_ROOT_PASSWORD|JWT_SECRET|DATA_CRYPTO_SECRET|ADMIN_PASSWORD): .*/\\1: ****/g' \
            | sed -E 's#\\$\\{TOS_ACCESS_KEY_FILE:-[^}]+\\}#\\${TOS_ACCESS_KEY_FILE:-****}#g'
          '''
        }
      }
    }
    stage('Archive Artifact') {
      steps {
        archiveArtifacts artifacts: 'target/*.jar,target/surefire-reports/**,target/site/jacoco/**,test-evidence/**', allowEmptyArchive: true, fingerprint: true
      }
    }
  }
}
