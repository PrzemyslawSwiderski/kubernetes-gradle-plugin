package com.pswidersk.gradle.kubernetes

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar

open class ExplodeBootJarTask : DefaultTask() {

    init {
        group = "docker"
        description = "Explode bootJar archive to a $EXPLODED_JAR_DIR in project's buildDir"
    }

    private val defaultExplodedBootJarDir = project.layout.projectDirectory.dir(project.buildDir.resolve(EXPLODED_JAR_DIR).path)

    @OutputDirectory
    val explodedBootJarDir: DirectoryProperty = project.objects.directoryProperty().convention(defaultExplodedBootJarDir)

    @TaskAction
    fun execute() = with(project) {
        val appProject = project.kubernetesPlugin.applicationProject
        val bootJarTask = appProject.tasks.getByName(BOOT_JAR_TASK) as Jar
        val explodedJarDir = buildDir.resolve(EXPLODED_JAR_DIR)
        copy {
            it.from(zipTree(bootJarTask.archiveFile))
            it.into(explodedJarDir)
        }
        docker.files(explodedJarDir)
    }

}