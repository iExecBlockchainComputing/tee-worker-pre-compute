//Readme @ http://gitlab.iex.ec:30000/iexec/jenkins-library

@Library('jenkins-library@1.0.0') _

buildSimpleDocker(imageprivacy: 'public')

node('docker'){

    stage('Build TEE debug image') {

        //get short commit
        GIT_SHORT_COMMIT = sh(
                script: 'git rev-parse --short HEAD',
                returnStdout: true
        ).trim()

        //get tag
        GIT_TAG = sh(
                script: 'git tag --points-at HEAD|tail -n1',
                returnStdout: true
        ).trim()
        //GIT_TAG = "0.0.1" //debug

        if ("$GIT_TAG" =~ /^\d{1,}\.\d{1,}\.\d{1,}$/) {
            ARTEFACT_VERSION = GIT_TAG //tag match
        } else {
            ARTEFACT_VERSION = 'dev' //no tag match
        }

        def TAG = "$GIT_SHORT_COMMIT" + '-' + "$ARTEFACT_VERSION"

        sconeSigning(
                IMG_FROM: "docker.io/iexechub/tee-worker-pre-compute:$TAG",
                IMG_TO: "docker.io/iexechub/tee-worker-pre-compute:$TAG-debug",
                SCRIPT_CONFIG: './docker/sconify.args',
                SCONE_IMG_VERS: '5.3.3',
                FLAVOR: 'DEBUG'
        )
    }

}


