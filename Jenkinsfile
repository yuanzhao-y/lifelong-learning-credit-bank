pipeline {
  agent any

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh 'git log -1 --oneline'
      }
    }
    stage('Backend Package') {
      steps {
        sh 'mvn -B -DskipTests package'
      }
    }
    stage('Docker Compose Evidence') {
      steps {
        sh 'sed -n "1,180p" docker-compose.yml'
      }
    }
    stage('Archive Artifact') {
      steps {
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }
  }
}
