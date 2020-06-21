package com.pswidersk.gradle.kubernetes

import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject


open class KubernetesPluginExtension @Inject constructor(project: Project,
                                                         objects: ObjectFactory) {

    internal val applicationProject: Project = if (project.parent != null) project.parent!! else project

    internal val dockerImageName: Property<String> = objects.property<String>().convention(applicationProject.name)

    internal val dockerImageVersion: Property<String> = objects.property<String>().convention(applicationProject.version.toString())

    val chartRef: Property<String> = objects.property<String>().convention("./${applicationProject.name}")

    val deploymentName: Property<String> = objects.property<String>().convention(applicationProject.name)

    val additionalInstallArgs: ListProperty<String> = objects.listProperty(String::class.java)

}