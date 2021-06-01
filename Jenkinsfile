//Readme @ http://gitlab.iex.ec:30000/iexec/jenkins-library
@Library('jenkins-library@1.0.0') _
// Build default docker image
buildSimpleDocker(imageprivacy: 'public')
// Build tee debug docker image
node('docker'){

    def DOCKER_IMG_BASENAME = 'docker.io/iexechub/tee-worker-pre-compute'
    def SCONIFY_ARGS_PATH = '5.3.3'
    def SCONIFY_TOOL_IMG_VERSION = '5.3.3'
    def TAG

    stage('Trigger TEE debug image build') {
        GIT_SHORT_COMMIT = sh(script: 'git rev-parse --short HEAD',
                returnStdout: true).trim()
        GIT_TAG = sh(script: 'git tag --points-at HEAD|tail -n1',
                returnStdout: true).trim()
        TAG = "$GIT_SHORT_COMMIT" + '-dev' //no tag match
        if ("$GIT_TAG" =~ /^\d{1,}\.\d{1,}\.\d{1,}$/) {
            TAG = "$GIT_TAG" //tag match
        }

        sconeSigning(
                IMG_FROM: "$DOCKER_IMG_BASENAME:$TAG",
                IMG_TO: "$DOCKER_IMG_BASENAME:$TAG-debug",
                SCRIPT_CONFIG: "$SCONIFY_ARGS_PATH",
                SCONE_IMG_VERS: "$SCONIFY_TOOL_IMG_VERSION",
                FLAVOR: 'DEBUG'
        )
    }

    stage('Trigger TEE production image build') {
        when {
            branch 'master'
        }
        sconeSigning(
                IMG_FROM: "$DOCKER_IMG_BASENAME:$TAG",
                IMG_TO: "$DOCKER_IMG_BASENAME:$TAG-production",
                SCRIPT_CONFIG: "$SCONIFY_ARGS_PATH",
                SCONE_IMG_VERS: "$SCONIFY_TOOL_IMG_VERSION",
                FLAVOR: 'PROD'
        )
    }

}


