plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.3.1"
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
    website.set("https://github.com/Andrewnplus/book-gradle-conventions")
    vcsUrl.set("https://github.com/Andrewnplus/book-gradle-conventions.git")

    plugins {
        create("bookPlugin") {
            id = "com.andrewnplus.book"
            displayName = "Andrew's Hugo Book Convention Plugin"
            description = "Convention plugin for Hugo Book repositories: pins Hugo version, applies Spotless markdown formatting, and registers the stage task."
            tags.set(listOf("hugo", "hugo-book", "convention", "markdown"))
            implementationClass = "com.andrewnplus.book.BookPlugin"
        }
    }
}

kotlin {
    jvmToolchain(21)
}
