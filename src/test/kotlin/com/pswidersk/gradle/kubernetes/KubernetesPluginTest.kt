package com.pswidersk.gradle.kubernetes

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class KubernetesPluginTest {

    @Test
    fun `test if main external plugins were successfully applied`() {
        val project: Project = ProjectBuilder.builder().build()
        project.pluginManager.apply(KubernetesPlugin::class.java)

        assertEquals(8, project.plugins.size)
    }

    @Test
    fun `test if tasks were added`() {
        val testProjectPath = File(this.javaClass.classLoader.getResource("testProject")!!.path)
        val project: Project = ProjectBuilder.builder().withProjectDir(testProjectPath).build()
        project.pluginManager.apply(KubernetesPlugin::class.java)

        assertEquals(33, project.tasks.size)
    }

}