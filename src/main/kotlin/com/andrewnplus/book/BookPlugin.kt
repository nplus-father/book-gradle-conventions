package com.andrewnplus.book

import com.diffplug.gradle.spotless.SpotlessExtension
import io.github.fstaudt.hugo.HugoPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

/**
 * Convention plugin for Andrew's Hugo Book repositories.
 *
 * Applies:
 * - io.github.fstaudt.hugo (pinned to [HUGO_VERSION])
 * - com.diffplug.spotless (markdown prettier formatting)
 * - a `stage` task that copies the Hugo build output to build/dist
 * - hugoBuild / hugoServer depend on spotlessCheck
 */
class BookPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("io.github.fstaudt.hugo")
        project.pluginManager.apply("com.diffplug.spotless")

        project.extensions.configure<HugoPluginExtension> {
            version.set(HUGO_VERSION)
            sourceDirectory.set("site")
        }

        project.extensions.configure<SpotlessExtension> {
            format("markdown") {
                target("site/content/**/*.md")
                prettier().config(mapOf("parser" to "markdown"))
            }
        }

        project.tasks.register<Copy>("stage") {
            dependsOn("hugoBuild")
            from(project.layout.buildDirectory.dir("hugo/publish"))
            into(project.layout.buildDirectory.dir("dist"))
        }

        project.tasks.named("spotlessCheck").configure {
            outputs.upToDateWhen { false }
        }

        project.tasks.matching { it.name in listOf("hugoBuild", "hugoServer") }
            .configureEach { dependsOn("spotlessCheck") }
    }

    companion object {
        // renovate: datasource=github-releases depName=gohugoio/hugo extractVersion=^v(?<version>.+)$
        const val HUGO_VERSION = "0.160.1"
    }
}
