pipeline {
  agent any

  tools {
    jdk 'jdk17'
    maven 'maven3'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }
    stage('Build') {
      steps {
        sh 'mvn -B -DskipTests package'
      }
    }
    stage('Docker Compose Config') {
      steps {
        sh 'docker compose config >/tmp/llcb-compose.yml'
      }
    }
  }
}
