# Changelog

All notable changes to this project will be documented in this file.

## [[8.5.0]](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/releases/tag/v8.5.0) 2024-06-18

### Quality

- Configure Gradle JVM Test Suite Plugin. (#82)

### Dependency Upgrades

- Upgrade to Gradle 8.7. (#83)
- Upgrade to `eclipse-temurin:11.0.22_7-jre-focal`. (#84)
- Upgrade to `iexec-commons-poco` 4.1.0. (#85)
- Upgrade to `iexec-common` 8.5.0. (#85)

## [[8.4.0]](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/releases/tag/v8.4.0) 2024-02-29

### Dependency Upgrades

- Upgrade to scone 5.7.6. (#78)
- Upgrade to `iexec-common` 8.4.0. (#79)

## [[8.3.0]](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/releases/tag/v8.3.0) 2024-01-12

### Dependency Upgrades

- Upgrade to `eclipse-temurin:11.0.21_9-jre-focal`. (#74)
- Upgrade to `jenkins-library` 2.7.4. (#73)
- Upgrade to `iexec-commons-poco` 3.2.0. (#75)
- Upgrade to `iexec-common` 8.3.1. (#75)

## [[8.2.0]](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/releases/tag/v8.2.0) 2023-09-28

### Quality

- Remove `nexus.intra.iex.ec` repository. (#62)
- Parameterize build of TEE applications while PR is not started. This allows faster builds. (#63 #64)
- Update `sconify.sh` script and rename `buildTeeImage` task to `buildSconeImage`. (#65)
- Upgrade to Gradle 8.2.1 with up-to-date plugins. (#67)
- Rename base package to `com.iexec.worker.compute.pre`. (#69)
- Rename worker REST api package to `com.iexec.worker.api`. (#69)

### Dependency Upgrades

- Upgrade to `jenkins-library` 2.7.3. (#63 #68)
- Upgrade to `eclipse-temurin` 11.0.20. (#66)
- Upgrade to `iexec-commons-poco` 3.1.0. (#70)
- Upgrade to `iexec-common` 8.3.0. (#70)

## [[8.1.3]](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/releases/tag/v8.1.3) 2023-06-23

### Bug Fixes

- Add missing `ipfsGateway` URL in dataset download URL. (#60)

## [[8.1.2]](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/releases/tag/v8.1.2) 2023-06-23

### Dependency Upgrades

- Upgrade to `iexec-common` 8.2.1. (#59)
- Upgrade to `iexec-commons-poco` 3.0.4. (#59)

## [[8.1.1]](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/releases/tag/v8.1.1) 2023-06-22

### Features

- Retry dataset download on several IPFS gateways. (#58)

### Dependency Upgrades

- Upgrade to `iexec-commons-poco` 3.0.3. (#58)

## [[8.1.0]](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/releases/tag/v8.1.0) 2023-06-06

### Dependency Upgrades

- Upgrade to `iexec-common` 8.2.0. (#54 #55)
- Add new `iexec-commons-poco` 3.0.0 dependency. (#54 #55)

## [[8.0.0]](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/releases/tag/v8.0.0) 2023-03-08

### New Features

* #44 Set scone heap to 3G.
* #43 Upgrade to scone 5.7.

### Quality

* #48 Remove deprecated Palantir Docker Gradle plugin.
* #39 #37 Improve code coverage.
* #33 Improve Task Feedback support.
* #45 #35 Add sonar support.
* #34 Only build internal scone image in this pipeline.

### Dependency Upgrades

* #50 Upgrade to `iexec-common` 7.0.0.
* #51 #46 #42 #38 #36 Upgrade to `jenkins-library` 2.5.0.
* #49 Replace the deprecated `openjdk` Docker base image with `eclipse-temurin` and upgrade to Java 11.0.16 patch.
* #47 Upgrade to Gradle 7.6.
