@Library('global-jenkins-library@support/2.7.4') _

String repositoryName = 'tee-worker-pre-compute'

buildInfo = buildJavaProject(
        shouldPublishJars: false,
        shouldPublishDockerImages: true)

// add parameters for non-PR builds when branch is not develop or production branch
boolean addParameters = !buildInfo.isPullRequestBuild && !buildInfo.isDevelopBranch && !buildInfo.isProductionBranch

// Override properties defined in getBuildInfo and add parameters
if (addParameters) {
  properties([
    buildDiscarder(logRotator(numToKeepStr: '10')),
    parameters([booleanParam(description: 'Build TEE images', name: 'BUILD_TEE')])
  ])
}

// BUILD_TEE parameter only exists if addParameters is true
// If BUILD_TEE is false, TEE builds won't be executed and we return here
if (addParameters && !params.BUILD_TEE) {
    return
}

sconeBuildUnlocked(
        nativeImage:     "docker-regis.iex.ec/$repositoryName:$buildInfo.imageTag",
        imageName:       repositoryName,
        imageTag:        buildInfo.imageTag,
        sconifyArgsPath: './docker/sconify.args',
        sconifyVersion:  '5.7.6'
)

sconeBuildUnlocked(
        nativeImage:     "docker-regis.iex.ec/$repositoryName:$buildInfo.imageTag",
        imageName:       repositoryName,
        imageTag:        buildInfo.imageTag,
        sconifyArgsPath: './docker/sconify.args',
        sconifyVersion:  '5.9.0'
)
