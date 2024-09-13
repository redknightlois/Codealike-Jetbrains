plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

group = "com.codealike.client.intellij"

version = "1.8.0.1"

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
            sinceBuild = "233"
            untilBuild = "241.*"
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
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
