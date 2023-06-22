# Changelog

All notable changes to this project will be documented in this file.

## [[8.1.1]](https://github.com/iExecBlockchainComputing/tee-worker-pre-compute/releases/tag/v8.1.1) 2023-06-22

### Features
-  Retry dataset download on several IPFS gateways. (#58)
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
