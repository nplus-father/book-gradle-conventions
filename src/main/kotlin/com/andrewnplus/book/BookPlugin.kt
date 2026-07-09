package com.andrewnplus.book

import com.diffplug.gradle.spotless.SpotlessExtension
import io.github.fstaudt.hugo.HugoPluginExtension
import io.github.fstaudt.hugo.tasks.HugoBuild
import io.github.fstaudt.hugo.tasks.HugoCommand
import io.github.fstaudt.hugo.tasks.HugoServer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import java.io.File
import java.nio.file.Files

/**
 * Convention plugin for Andrew's Hugo Book repositories.
 *
 * Applies:
 * - io.github.fstaudt.hugo (pinned to [HUGO_VERSION] on Linux/CI; uses the locally-installed
 *   Hugo on macOS — see [configureLocalHugoForMacOs])
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

        if (isMacOs()) {
            configureLocalHugoForMacOs(project)
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

    private fun isMacOs() = System.getProperty("os.name").startsWith("Mac", ignoreCase = true)

    /**
     * Use the locally-installed Hugo on macOS instead of the one io.github.fstaudt.hugo downloads.
     *
     * Hugo stopped publishing macOS `.tar.gz` builds at v0.153.0 (macOS now ships only `.pkg`),
     * and the fstaudt plugin can *only* download + extract a tarball, so any Hugo >= 0.153.0 makes
     * `hugoDownload` fail with a 404 on macOS. Rather than pin every book repo to an old Hugo, we
     * point the Hugo command tasks at the Hugo already on the developer's PATH (`brew install hugo`).
     * This keeps macOS dev on the latest Hugo and needs no per-repo workaround. Linux/CI is untouched
     * and keeps downloading the pinned [HUGO_VERSION] tarball.
     */
    private fun configureLocalHugoForMacOs(project: Project) {
        val localBinDir = project.layout.buildDirectory.dir("hugo/local-bin")
        val pathVar = project.providers.environmentVariable("PATH")

        // Symlink the PATH-resolved Hugo into build/hugo/local-bin so the command tasks can run
        // `<localBinDir>/hugo`. Always re-runs so it tracks whatever `brew` currently has installed.
        val linkLocalHugo = project.tasks.register("linkLocalHugo") {
            group = "hugo"
            description = "Link the locally-installed Hugo binary into the build dir (macOS)."
            outputs.dir(localBinDir)
            outputs.upToDateWhen { false }
            doLast {
                val hugo = pathVar.orNull
                    ?.split(File.pathSeparator)
                    ?.map { File(it, "hugo") }
                    ?.firstOrNull { it.isFile && it.canExecute() }
                    ?: error(
                        "Hugo was not found on PATH. On macOS this plugin uses the locally-installed " +
                            "Hugo because Hugo no longer ships macOS tarballs. Install it with: brew install hugo",
                    )
                val dir = localBinDir.get().asFile
                dir.mkdirs()
                val link = File(dir, "hugo").toPath()
                Files.deleteIfExists(link)
                Files.createSymbolicLink(link, hugo.toPath())
            }
        }

        // Re-pointing hugoBinaryDirectory replaces fstaudt's `hugoDownload.flatMap { ... }` provider,
        // which is what carried the implicit dependency on hugoDownload — so hugoDownload is no longer
        // pulled in. We depend on linkLocalHugo instead to populate the directory first.
        project.tasks.named<HugoCommand>("hugo") {
            hugoBinaryDirectory.set(localBinDir)
            dependsOn(linkLocalHugo)
        }
        project.tasks.named<HugoBuild>("hugoBuild") {
            hugoBinaryDirectory.set(localBinDir)
            dependsOn(linkLocalHugo)
        }
        project.tasks.named<HugoServer>("hugoServer") {
            hugoBinaryDirectory.set(localBinDir)
            dependsOn(linkLocalHugo)
        }

        // Nothing depends on hugoDownload on macOS anymore; disable it so a direct invocation
        // doesn't fail with a 404 for the non-existent macOS tarball.
        project.tasks.named("hugoDownload").configure { enabled = false }
    }

    companion object {
        // renovate: datasource=github-releases depName=gohugoio/hugo extractVersion=^v(?<version>.+)$
        const val HUGO_VERSION = "0.164.0"
    }
}
