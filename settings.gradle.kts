rootProject.name = "kubernetes-gradle-plugin"

include("examples:demo-app", "examples:demo-app:k8s")

pluginManagement {
    repositories {
        mavenLocal()
        jcenter()
        maven {
            setUrl("https://plugins.gradle.org/m2/")
        }
    }
}