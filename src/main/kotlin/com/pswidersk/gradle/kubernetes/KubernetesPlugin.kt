package com.pswidersk.gradle.kubernetes

import com.palantir.gradle.docker.DockerComposePlugin
import com.palantir.gradle.docker.DockerRunPlugin
import com.palantir.gradle.docker.PalantirDockerPlugin
import com.pswidersk.gradle.helm.HelmPlugin
import com.pswidersk.gradle.helm.HelmTask
import com.pswidersk.gradle.yamlsecrets.YamlSecretsData
import com.pswidersk.gradle.yamlsecrets.YamlSecretsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import java.time.Instant

class KubernetesPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        val externalPluginsToApply = listOf(
                BasePlugin::class,
                PalantirDockerPlugin::class,
                DockerRunPlugin::class,
                DockerComposePlugin::class,
                YamlSecretsPlugin::class,
                HelmPlugin::class
        )
        externalPluginsToApply.forEach { pluginManager.apply(it.java) }
        extensions.create(KUBERNETES_PLUGIN_EXTENSION_NAME, KubernetesPluginExtension::class.java, this)
        explodeBootJar(project)
        configureEnvironments(project)
        configureDocker(project)
    }

    private fun explodeBootJar(project: Project) = with(project) {
        val appProject = kubernetesPlugin.applicationProject
        val explodeBootJarTask = tasks.register("explodeBootJar", ExplodeBootJarTask::class.java) { task ->
            task.dependsOn(appProject.tasks.getByName(BOOT_JAR_TASK))
            task.onlyIf { checkIfBootJarTaskAvailable(appProject) && !task.explodedBootJarDir.get().asFile.exists() }
        }
        tasks.getByName("dockerPrepare") {
            it.dependsOn(explodeBootJarTask)
            it.doFirst {
                val explodedBootJarDir = explodeBootJarTask.get().explodedBootJarDir.get().asFile
                if (explodedBootJarDir.exists())
                    docker.files(explodedBootJarDir)
            }
        }
    }

    private fun configureDocker(project: Project) = with(project) {
        val imageName = kubernetesPlugin.dockerImageName
        docker.name = imageName
        dockerRun.name = imageName
        dockerRun.image = imageName
        envsToDeploy.forEach { envSecretsData ->
            val dockerRepo = secrets.get<String>(envSecretsData.secretsName, DOCKER_REPO_PROP_NAME)
            val tag = buildDockerImageTag(this, dockerRepo)
            docker.tag("-${envSecretsData.secretsName}", tag)
        }
    }


    private fun configureEnvironments(project: Project) = with(project) {
        envsToDeploy.forEach { env ->
            generateHelmTasks(env, project)
        }
    }

    private fun generateHelmTasks(envSecretsData: YamlSecretsData, project: Project) = with(project.tasks) {
        val releaseName = envSecretsData.secretsName
        val additionalInstallArgs = project.kubernetesPlugin.additionalInstallArgs
        val deploymentName = project.kubernetesPlugin.deploymentName
        val chartRef = project.kubernetesPlugin.chartRef
        val kubeContext = project.secrets.get<String>(releaseName, KUBE_CONTEXT_PROP_NAME)
        val commonSecrets = project.secrets.getSecretsData("common")

        register("helmTest-$releaseName", HelmTask::class.java) {
            it.description = "Runs release tests for \"$releaseName\" environment."
            it.args("test", releaseName, "--namespace", "$releaseName-${deploymentName.get()}")
        }

        register("helmUpgradeOrInstall-$releaseName", HelmTask::class.java) {
            it.description = "Display the status of the named release for \"$releaseName\" environment."
            val installArgs = listOf("upgrade", "--install", releaseName, chartRef.get(),
                    "--namespace", "$releaseName-${deploymentName.get()}",
                    "--create-namespace", "--kube-context", kubeContext,
                    "--values", commonSecrets.propertiesFile,
                    "--values", envSecretsData.propertiesFile,
                    "--set", "deployTimeUTC=${Instant.now()}",
                    "--set", "dockerImageName=${project.kubernetesPlugin.dockerImageName}",
                    "--set", "dockerImageVersion=${project.kubernetesPlugin.dockerImageVersion}"
            ) + additionalInstallArgs.get()
            it.args(installArgs)
            it.doFirst {
                project.logger.quiet("Executing helm with args: $installArgs")
            }
            if (project.kubernetesPlugin.pushImageBeforeInstall.get())
                it.dependsOn("dockerPush-${envSecretsData.secretsName}")
        }

        register("helmStatus-$releaseName", HelmTask::class.java) {
            it.description = "Display the status of the named release for \"$releaseName\" environment."
            it.args("status", releaseName, "--namespace", "$releaseName-${deploymentName.get()}")
        }

        register("helmLint-$releaseName", HelmTask::class.java) {
            it.description = "Examine a chart for possible issues for \"$releaseName\" environment."
            it.args("lint", chartRef.get(), "--namespace", "$releaseName-${deploymentName.get()}")
        }

        register("helmTemplate-$releaseName", HelmTask::class.java) {
            it.description = "Locally render template for \"$releaseName\" environment."
            it.args(
                    "template", chartRef.get(), "--namespace", "$releaseName-${deploymentName.get()}",
                    "--values", commonSecrets.propertiesFile,
                    "--values", envSecretsData.propertiesFile,
                    "--set", "deployTimeUTC=${Instant.now()}",
                    "--set", "dockerImageName=${project.kubernetesPlugin.dockerImageName}",
                    "--set", "dockerImageVersion=${project.kubernetesPlugin.dockerImageVersion}"
            )
        }

        register("helmUninstall-$releaseName", HelmTask::class.java) {
            it.description = "Uninstall a release for \"$releaseName\" environment."
            it.args("uninstall", releaseName, "--namespace", "$releaseName-${deploymentName.get()}")
        }

    }

}
