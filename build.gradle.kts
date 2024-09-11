plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = "com.codealike.client.intellij"

version = "1.7.3.0"

val libs: Configuration by configurations.creating

repositories {
    mavenCentral()
    intellijPlatform{
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {

    pluginConfiguration{
        ideaVersion {
            sinceBuild = "222"
            untilBuild = "233.*"
        }
    }

    publishing{
        token = System.getenv("PUBLISH_TOKEN")
    }

    signing{
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2023.3.3")
        bundledPlugin("com.intellij.java")
        instrumentationTools()
    }

    libs("joda-time:joda-time:2.12.2")
    configurations.implementation.get().extendsFrom(libs)
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(libs.map { if (it.isDirectory) it else zipTree(it) })
    }
}
