plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("io.github.fstaudt.hugo:io.github.fstaudt.hugo.gradle.plugin:0.12.0")
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:8.4.0")
}

gradlePlugin {
    plugins {
        create("bookPlugin") {
            id = "com.andrewnplus.book"
            displayName = "Andrew's Hugo Book Convention Plugin"
            description = "Convention plugin for Hugo Book repositories: pins Hugo version, applies Spotless markdown formatting, and registers the stage task."
            implementationClass = "com.andrewnplus.book.BookPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Andrewnplus/book-gradle-conventions")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String? ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String? ?: ""
            }
        }
    }
}

kotlin {
    jvmToolchain(21)
}
