@file:JvmName("KubernetesPluginConstants")

package com.pswidersk.gradle.kubernetes

/**
 * Name of helm plugin extension in projects.
 */
const val KUBERNETES_PLUGIN_EXTENSION_NAME = "kubernetesPlugin"


const val BOOT_JAR_TASK = "bootJar"

const val EXPLODED_JAR_DIR = "explodedBootJar"

// OBLIGATORY VALUES IN ENV DEFINITIONS

/**
 * Repository where docker image should be pushed and pulled by kubernetes in deployment. Will be used in image's tag.
 */
const val DOCKER_REPO_PROP_NAME = "dockerRepository"

/**
 * Name of docker image. Will be used in image's tag.
 */
const val DOCKER_IMAGE_NAME_PROP_NAME = "dockerImageName"

/**
 * Version of docker Image. Will be used in image's tag.
 */
const val DOCKER_IMAGE_VERSION_PROP_NAME = "dockerImageVersion"

/**
 * Name of kubernetes context to which helm chart should be applied.
 */
const val KUBE_CONTEXT_PROP_NAME = "kubeContext"
