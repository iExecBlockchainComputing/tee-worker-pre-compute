@Library('global-jenkins-library@feature/standalone-scone-build') _

buildInfo = getBuildInfo()

def nativeImage = buildSimpleDocker_v2(
  buildInfo:                 buildInfo,
  dockerfileDir:             './docker',
  buildContext:              '.',
  dockerImageRepositoryName: 'tee-worker-pre-compute',
  imageprivacy:              'private'
)

sconeBuildUnlocked(
  nativeImage:     nativeImage,
  imageName:       'tee-worker-pre-compute',
  imageTag:        buildInfo.imageTag,
  sconifyArgsPath: './docker/sconify.args'
)
