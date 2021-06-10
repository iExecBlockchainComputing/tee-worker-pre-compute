//Readme @ http://gitlab.iex.ec:30000/iexec/jenkins-library
@Library('jenkins-library@1.0.2') _

// Build native docker image
buildSimpleDocker(imageprivacy: 'public')

// Build tee docker images
node('docker') {

    def gitShortCommit = sh(
            script: 'git rev-parse --short HEAD',
            returnStdout: true)
            .trim()
    def gitTag = sh(
            script: 'git tag --points-at HEAD|tail -n1',
            returnStdout: true)
            .trim()
    def imageTag = ("$gitTag" =~ /^\d{1,}\.\d{1,}\.\d{1,}$/)
            ? "$gitTag"
            : "$gitShortCommit-dev"

    def imageRegistry = 'docker.io'
    def imageName = 'iexechub/tee-worker-pre-compute'
    def sconifyToolImageName = 'scone-production/iexec-sconify-image'
    def sconifyToolImageVersion = '5.3.5'
    def sconifyToolArgsPath = './docker/sconify.args'

    // /!\ UNLOCKED VERSION /!\
    stage('Build TEE debug unlocked image') {
        sconeSigning(
                IMG_FROM: "$imageRegistry/$imageName:$imageTag",
                IMG_TO: "nexus.iex.ec/$imageName-unlocked:$imageTag-debug",
                SCRIPT_CONFIG: "$sconifyToolArgsPath",
                SCONE_IMG_NAME: 'sconecuratedimages/iexec-sconify-image',
                SCONE_IMG_VERS: '5.3.3',
                FLAVOR: 'DEBUG'
        )
    }

    stage('Build TEE debug image') {
        sconeSigning(
                IMG_FROM: "$imageRegistry/$imageName:$imageTag",
                IMG_TO: "$imageRegistry/$imageName:$imageTag-debug",
                SCRIPT_CONFIG: "$sconifyToolArgsPath",
                SCONE_IMG_NAME: "$sconifyToolImageName",
                SCONE_IMG_VERS: "$sconifyToolImageVersion",
                FLAVOR: 'DEBUG'
        )
    }

    stage('Build TEE production image') {
        if (env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'main') {
            sconeSigning(
                    IMG_FROM: "$imageRegistry/$imageName:$imageTag",
                    IMG_TO: "$imageRegistry/$imageName:$imageTag-production",
                    SCRIPT_CONFIG: "$sconifyToolArgsPath",
                    SCONE_IMG_NAME: "$sconifyToolImageName",
                    SCONE_IMG_VERS: "$sconifyToolImageVersion",
                    FLAVOR: 'PROD'
            )
        }
    }
}


