pipeline {
    agent any

    environment {
        PATH = "/usr/bin:/usr/local/bin:${env.PATH}"
    }

    stages {

        stage('Pull Docker Image') {
            steps {
                sh 'docker pull nikhilsinghal2004/maven_test:latest'
            }
        }

        stage('Run Container') {
            steps {
                sh 'docker run --rm nikhilsinghal2004/maven_test:latest'
            }
        }
    }
}