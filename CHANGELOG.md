# Changelog

All notable changes to this project will be documented in this file.

## 9.0.1 (2025-05-13)


### Bug Fixes

* Add missing logback-classic dependency ([#104](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/issues/104)) ([e1c0ee0](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/commit/e1c0ee00111b9c17e03f38fd220eb7e3404728f1))


### Miscellaneous Chores

* release 9.0.1 ([#106](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/issues/106)) ([ef36656](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/commit/ef36656d069a700a46e9e086091c0d48acad7c6a))

## [[9.0.0]](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/releases/tag/v9.0.0) 2025-03-28

### New Features

- Update build to run application in Scone framework 5.7 and 5.9 enlaves. (#96)
- Decrypt base64 encoded key before calling CipherUtils to decrypt a dataset to embrace API breaking changes in iexec-common. (#98)
- Add authorization proof to requests sending data to the worker. (#99)

### Dependency Upgrades

- Upgrade to `eclipse-temurin:17.0.13_11-jre-focal`. (#95)
- Upgrade to Spring Boot 3.3.8. (#97)
- Upgrade to `iexec-common` 9.0.0. (#101)
- Upgrade to `iexec-commons-poco` 5.0.0. (#101)

## [[8.6.0]](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/releases/tag/v8.6.0) 2024-12-20

### New Features

- Use new `FileHashUtils` API. (#90)

### Quality

- Keep `itest` task empty in `build.gradle` to avoid a warning during build. (#91)

### Dependency Upgrades

- Upgrade to `eclipse-temurin:11.0.24_8-jre-focal`. (#88)
- Upgrade to Gradle 8.10.2. (#89)
- Upgrade to `iexec-commons-poco` 4.2.0. (#92)
- Upgrade to `iexec-common` 8.6.0. (#92)

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
