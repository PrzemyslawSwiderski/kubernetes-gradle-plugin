package com.pswidersk.gradle.kubernetes

import com.palantir.gradle.docker.DockerExtension
import com.palantir.gradle.docker.DockerRunExtension
import com.pswidersk.gradle.yamlsecrets.YamlSecretsData
import com.pswidersk.gradle.yamlsecrets.YamlSecretsResolver
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.jvm.tasks.Jar


/**
 * Creates a [Property] to hold values of the given type.
 *
 * @param T the type of the property
 * @return the property
 */
internal inline fun <reified T : Any> ObjectFactory.property(): Property<T> =
        property(T::class.javaObjectType)

/**
 * Gets the [KubernetesPluginExtension] that is installed on the project.
 */
internal val Project.kubernetesPlugin: KubernetesPluginExtension
    get() = extensions.getByType(KubernetesPluginExtension::class.java)

internal val Project.secrets: YamlSecretsResolver
    get() = extensions.getByType(YamlSecretsResolver::class.java)

internal val Project.docker: DockerExtension
    get() = extensions.getByType(DockerExtension::class.java)

internal val Project.dockerRun: DockerRunExtension
    get() = extensions.getByType(DockerRunExtension::class.java)

internal val Project.envsToDeploy: List<YamlSecretsData>
    get() = secrets.getNames().filter { it.endsWith("-env") }.map { secrets.getSecretsData(it) }

internal fun checkIfBootJarTaskAvailable(project: Project): Boolean {
    val bootJarTask = project.tasks.findByName(BOOT_JAR_TASK)
    return bootJarTask != null && bootJarTask is Jar
}

internal fun buildDockerImageTag(project: Project, dockerRepo: String): String = with(project) {
    val dockerImageName = kubernetesPlugin.dockerImageName
    val dockerImageVersion = kubernetesPlugin.dockerImageVersion
    val repoSep = if (dockerRepo.isNotEmpty()) "/" else ""
    val versionSep = if (dockerImageName.isNotEmpty()) ":" else ""
    return "$dockerRepo$repoSep$dockerImageName$versionSep$dockerImageVersion"
}