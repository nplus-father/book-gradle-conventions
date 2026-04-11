# book-gradle-conventions

Gradle convention plugin for Andrew's Hugo Book repositories.

## Usage

```kotlin
// build.gradle.kts
plugins {
    id("com.andrewnplus.book") version "0.1.x"
}
```

That's it. The plugin pins the Hugo version, configures Spotless markdown formatting, registers the `stage` task, and wires `hugoBuild` / `hugoServer` to run `spotlessCheck` first.

## What's inside

- `io.github.fstaudt.hugo` with Hugo binary version pinned in `BookPlugin.kt`
- `com.diffplug.spotless` with markdown prettier config targeting `site/content/**/*.md`
- `stage` task: copies `build/hugo/publish` → `build/dist`

## Versioning

Every push to `main` triggers a CI publish with version `0.1.${GITHUB_RUN_NUMBER}`. Renovate on consumer repos automatically picks up new versions.

## Updating Hugo version

Renovate detects new Hugo releases via the `// renovate:` comment above `HUGO_VERSION` and opens an automerged PR. No manual intervention.
