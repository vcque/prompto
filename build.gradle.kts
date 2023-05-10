plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.13.3"
    id("io.freefair.lombok") version "6.5.1"
}

group = "com.vcque"
version = "0.5.0"

repositories {
    mavenCentral()
    mavenLocal()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.4")
    type.set("IU") // Target IDE Platform

    plugins.set(listOf(
            "com.intellij.java",
            "JavaScript",
            "com.intellij.database",
            ))
}

dependencies {
    implementation("com.theokanning.openai-gpt3-java:service:0.12.0")
    implementation("org.projectlombok:lombok:1.18.26")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.assertj:assertj-core:3.6.1")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(File("./sign/chain.crt").readText(Charsets.UTF_8))
        privateKey.set(File("./sign/private.pem").readText(Charsets.UTF_8))
        password.set(File("./sign/password").readText(Charsets.UTF_8))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
