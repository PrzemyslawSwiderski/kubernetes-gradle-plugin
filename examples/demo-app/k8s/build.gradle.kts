plugins {
    base
    id("com.pswidersk.kubernetes-plugin") version "1.0.0"
}
kubernetesPlugin {
    additionalInstallArgs.set(listOf("--atomic"))
}
docker {
    buildArgs(mapOf(
            "MAIN_CLASS" to "com.pswidersk.demoapp.DemoAppApplicationKt" // passing arg to Dockerfile
    ))
    files("dev-env.sec.yml") // additional files (besides exploded jar) can be defined
}

dockerRun {
    clean = true // remove container if stopped
}
