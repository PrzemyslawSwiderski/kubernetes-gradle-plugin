package com.pswidersk.gradle.kubernetes

import com.palantir.gradle.docker.DockerRunPlugin
import com.palantir.gradle.docker.PalantirDockerPlugin
import com.pswidersk.gradle.helm.HelmPlugin
import com.pswidersk.gradle.helm.HelmTask
import com.pswidersk.gradle.yamlsecrets.YamlSecretsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin

class KubernetesPlugin : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        val externalPluginsToApply = listOf(
                BasePlugin::class,
                DockerRunPlugin::class,
                PalantirDockerPlugin::class,
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
        val imageName = kubernetesPlugin.dockerImageName.get()
        docker.name = imageName
        dockerRun.name = imageName
        dockerRun.image = imageName
        envsToDeploy.forEach { env ->
            val dockerRepo = secrets.get<String>(env, DOCKER_REPO_PROP_NAME)
            val tag = buildDockerImageTag(this, dockerRepo)
            docker.tag(env, tag)
        }
    }


    private fun configureEnvironments(project: Project) = with(project) {
        envsToDeploy.forEach { env ->
            generateHelmTasks(env, project)
        }
    }

    private fun generateHelmTasks(env: String, project: Project) = with(project.tasks) {
        val chartName = project.kubernetesPlugin.deploymentName
        val chartRef = project.kubernetesPlugin.chartRef
        val kubeContext = project.secrets.get<String>(env, KUBE_CONTEXT_PROP_NAME)
        val additionalArgs = project.kubernetesPlugin.additionalInstallArgs

        register("helmTest-$env", HelmTask::class.java) {
            it.args("test", chartName.get(), "--namespace", "$env-${chartName.get()}")
        }

        register("helmUpgradeOrInstall-$env", HelmTask::class.java) {
            val installArgs = listOf("upgrade", "--install", chartName.get(), chartRef.get(),
                    "--namespace", "$env-${chartName.get()}",
                    "--create-namespace", "--kube-context", kubeContext,
                    "--values", ".$env.sec.yml",
                    "--set", "dockerImageName=${project.kubernetesPlugin.dockerImageName.get()}",
                    "--set", "dockerImageVersion=${project.kubernetesPlugin.dockerImageVersion.get()}"
            ) + additionalArgs.get()
            it.args(installArgs)
            it.doFirst {
                project.logger.quiet("Executing helm with args: $installArgs")
            }
        }

        register("helmStatus-$env", HelmTask::class.java) {
            it.args("status", chartName.get(), "--namespace", "$env-${chartName.get()}")
        }

        register("helmLint-$env", HelmTask::class.java) {
            it.args("lint", chartName.get(), "--namespace", "$env-${chartName.get()}")
        }

        register("helmTemplate-$env", HelmTask::class.java) {
            it.args(
                    "template", chartName.get(), "--namespace", "$env-${chartName.get()}",
                    "--values", ".$env.sec.yml",
                    "--set", "dockerImageName=${project.kubernetesPlugin.dockerImageName.get()}",
                    "--set", "dockerImageVersion=${project.kubernetesPlugin.dockerImageVersion.get()}"
            )
        }

        register("helmUninstall-$env", HelmTask::class.java) {
            it.args("uninstall", chartName.get(), "--namespace", "$env-${chartName.get()}")
        }

    }

}
