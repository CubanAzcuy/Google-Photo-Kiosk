import java.lang.invoke.MethodHandles.invoker

val invoker: Configuration by configurations.creating

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    application
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.github.microutils:kotlin-logging:3.0.4")
    implementation("com.google.cloud.functions:functions-framework-api:1.0.4")
    invoker("com.google.cloud.functions.invoker:java-function-invoker:1.2.0")

    implementation("com.google.api-client:google-api-client:2.0.1")
    implementation("com.google.http-client:google-http-client-jackson2:1.42.3")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.mockito:mockito-core:4.8.0")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("com.google.guava:guava-testlib:31.1-jre")
}

application {
    mainClassName = "gives.robert.kiosk.gphotos.functions.AppKt"
}

task<JavaExec>("runFunction") {
    main = "com.google.cloud.functions.invoker.runner.Invoker"
    classpath(invoker)
    inputs.files(configurations.runtimeClasspath, sourceSets["main"].output)
    args(
        "--target", project.findProperty("runFunction.target") ?: "gives.robert.kiosk.gphotos.functions.App",
        "--port", project.findProperty("runFunction.port") ?: 8080
    )
    doFirst {
        args("--classpath", files(configurations.runtimeClasspath, sourceSets["main"].output).asPath)
    }
}

tasks.named("build") {
    dependsOn(":shadowJar")
}

task("buildFunction") {
    dependsOn("build")
    copy {
        from("build/libs/" + rootProject.name + "-all.jar")
        into("build/deploy")
    }
}
