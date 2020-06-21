# Kubernetes Gradle Plugin

Plugin to deploy charts to kubernetes clusters by docker image.

## What it does?

1. Bundles the following plugins:
    * Docker Palantir [plugin](https://github.com/palantir/gradle-docker) `com.palantir.gradle.docker:gradle-docker` to create docker image, run in container or push to registry.
    * Yaml secrets [plugin](https://github.com/PrzemyslawSwiderski/yaml-secrets-gradle-plugin) `com.pswidersk:yaml-secrets-gradle-plugin` to load local properties in Yaml format.
    * Helm gradle [plugin](https://github.com/PrzemyslawSwiderski/helm-gradle-plugin) `com.pswidersk:helm-gradle-plugin` to execute and register Helm tasks.
2. Create Docker and Helm tasks by Yaml files with defined environments.
3. Exports the following values to Helm charts:
    * `dockerRepository` - pointer to a docker registry where image have to be pushed for a specific environment. It is read from env file.
    * `dockerImageName` - docker image name which was pushed to a registry. It is a parent project name by default.
    * `dockerImageVersion` - docker image version which was pushed to a registry. It is a parent project version by default.
    * `deployTimeUTC` - current date time in a standard format, for example: `2020-06-21T20:13:12.010784300Z`


## Applying to a project

1. 