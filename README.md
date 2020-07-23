# Kubernetes Gradle Plugin

Plugin to deploy application to kubernetes clusters by docker image.

## Requirements
* JRE 8 or higher to run Gradle wrapper
* Docker
* Kubernetes cluster context available

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
4. In case of spring boot [plugin](https://plugins.gradle.org/plugin/org.springframework.boot) applied and `bootJar` task available in app project,
Kubernetes plugin explode jar to boost pushing of docker image. Exploded jar files are added to docker build.

**Check example demo application [example](https://github.com/PrzemyslawSwiderski/kubernetes-gradle-plugin/tree/master/examples/demo-app) to see how to use this plugin.**   

## Applying to a project

1. Create new child subproject in your application project. Can be named `k8s`. 
2. Apply a plugin to a project as described on [gradle portal](https://plugins.gradle.org/plugin/com.pswidersk.kubernetes-plugin).
3. Create:
    * `Dockerfile` with image definition
    * `common.sec.yml` with secrets for every environment
    *  add `deployer: <FILL_WITH_YOUR_NAME>` line to `common.sec.yml` file 
    * `dev-env.sec.yml` with a sample secrets of dev environment (must have `-env` suffix)
    * add lines `kubeContext: <TO_BE_FILLED>` and `dockerRepository: <TO_BE_FILLED>`
    * new charts catalog with name same as your application name
    * define helm charts in a catalog from previous step
4. Reimport / refresh gradle project to apply plugins and create secret files (`.common.sec.yml`, `.dev-env.sec.yml`).
5. New tasks such as `dockerPush-dev-env` or `helmUpgradeOrInstall-dev-env` should be now available. 
6. Fill files: `.common.sec.yml` and `.dev-env.sec.yml` with necessary properties.
7. Use `dockerRepository`, `dockerImageName` and `dockerImageVersion` in image reference in Helm's Deployment.
8. Run `helmUpgradeOrInstall-dev-env` task to deploy application to a k8s cluster. 
Application will be pushed to a `dockerRepository` specified in a `.dev-env.sec.yml` file and kubernetes cluster pointed by `kubeContext` property.

Note that kubernetes context must be available in kube config file (`~/.kube/config`).

## Plugin extension properties
This Plugin can be configured by setting the following values in `kubernetesPlugin {}` block:
* `chartRef` - sets chart reference where chart files are located. Used in `helmUpgradeOrInstall*` tasks. Default -> application (parent) project name as dir, for example `./demo-app`.
* `chartName` - sets chart name which will be used to locate deployment in k8s cluster by helm tasks. Default ->  application (parent) project name.
* `additionalInstallArgs` - additional args which will be passed to a `helmUpgradeOrInstall*` tasks. Default -> empty list.
* `pushImageBeforeInstall` - boolean property which indicates if docker image should be pushed to repo before deployment. Default -> true.

## [Changelog](./CHANGELOG.md)