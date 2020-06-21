package com.pswidersk.gradle.kubernetes

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject


open class KubernetesPluginExtension @Inject constructor(project: Project,
                                                         objects: ObjectFactory) {

    internal val applicationProject: Project = if (project.parent != null) project.parent!! else project

    internal val dockerImageName: String = applicationProject.name

    internal val dockerImageVersion: String = applicationProject.version.toString()

    val chartRef: Property<String> = objects.property<String>().convention("./${applicationProject.name}")

    val deploymentName: Property<String> = objects.property<String>().convention(applicationProject.name)

    val additionalInstallArgs: ListProperty<String> = objects.listProperty(String::class.java)

    val pushImageBeforeInstall: Property<Boolean> = objects.property<Boolean>().convention(true)

}