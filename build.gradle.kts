import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "1.3.72"
    id("com.gradle.plugin-publish") version "0.11.0"
    id("net.researchgate.release") version "2.8.1"
}

repositories {
    mavenLocal()
    jcenter()
    maven {
        setUrl("https://plugins.gradle.org/m2/")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.palantir.gradle.docker:gradle-docker:0.25.0")
    implementation("com.pswidersk:yaml-secrets-gradle-plugin:1.0.8")
    implementation("com.pswidersk:helm-gradle-plugin:1.0.4")


    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
}

tasks {
    test {
        useJUnitPlatform()
    }
    "afterReleaseBuild"{
        dependsOn("publish", "publishPlugins")
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
gradlePlugin {
    plugins {
        create("kubernetes-gradle-plugin") {
            id = "com.pswidersk.kubernetes-plugin"
            implementationClass = "com.pswidersk.gradle.kubernetes.KubernetesPlugin"
            displayName = "Plugin to deploy charts to kubernetes clusters by docker image. https://github.com/PrzemyslawSwiderski/kubernetes-gradle-plugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/PrzemyslawSwiderski/kubernetes-gradle-plugin"
    vcsUrl = "https://github.com/PrzemyslawSwiderski/kubernetes-gradle-plugin"
    description = "Plugin to deploy charts to kubernetes clusters by docker image."
    tags = listOf("kubernetes", "k8s", "helm", "docker", "yaml-secrets", "envs")
}

publishing {
    repositories {
        mavenLocal()
    }
}