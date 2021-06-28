// Readme @ http://gitlab.iex.ec:30000/iexec/jenkins-library
@Library('jenkins-library') _

def nativeImage = buildSimpleDocker_v2(dockerfilePath: "docker/Dockerfile", buildContext: '.',
        dockerImageRepositoryName: 'tee-worker-pre-compute', imageprivacy: 'public')
sconeBuildAllTee(nativeImage: nativeImage, targetImageRepositoryName: 'tee-worker-pre-compute',
        sconifyArgsPath: './docker/sconify.args')
