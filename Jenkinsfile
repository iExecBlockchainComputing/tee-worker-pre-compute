@Library('global-jenkins-library@1.9.0') _

buildInfo = getBuildInfo()

def nativeImage = buildSimpleDocker_v2(
  buildInfo:                 buildInfo,
  dockerfileDir:             './docker',
  buildContext:              '.',
  dockerImageRepositoryName: 'tee-worker-pre-compute',
  imageprivacy:              'public'
)

sconeBuildUnlocked(
  nativeImage:     nativeImage,
  imageName:       'tee-worker-pre-compute',
  imageTag:        buildInfo.imageTag,
  sconifyArgsPath: './docker/sconify.args'
)
