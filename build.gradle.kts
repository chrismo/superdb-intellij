import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
}

group = "org.clabs"

// Versioning scheme: <superdb-version>.<plugin-release>
//
// The first two parts mirror the SuperDB version this plugin is built against.
// The third part is the plugin-specific release number, starting at 0.
//
// Pre-release era (current):
//   SuperDB uses 0.5MMDD (e.g., 0.51222 for Dec 22, 2025)
//   Plugin: 0.51222.0, 0.51222.1, etc.
//
// Post-release era (when SuperDB ships 1.0+):
//   SuperDB uses standard semver (e.g., 1.0.0, 1.1.0)
//   Plugin: 1.0.0.0, 1.0.0.1, etc. (four parts: major.minor.patch.plugin-release)
//
// Version is derived from git tags (e.g., v0.51222.0).
// Use ./build.sh release <version> to create a release tag.
// Dev builds show: <version>-dev+<commit> or "0.0.0-dev" if no tags exist.
//
// Uses providers.exec() for configuration cache compatibility.
//
val gitDescribe: Provider<String> = providers.exec {
    commandLine("git", "describe", "--tags", "--match", "v*", "--always")
}.standardOutput.asText.map { it.trim() }

val gitVersion: Provider<String> = gitDescribe.map { output ->
    when {
        // Exact tag match: v0.51222.0 -> 0.51222.0
        output.matches(Regex("^v[0-9].*")) && !output.contains("-") ->
            output.removePrefix("v")
        // Commits after tag: v0.51222.0-3-gabcdef -> 0.51222.0-dev+abcdef
        output.matches(Regex("^v[0-9].*-[0-9]+-g[a-f0-9]+$")) -> {
            val parts = output.split("-")
            val baseVersion = parts[0].removePrefix("v")
            val commit = parts.last().removePrefix("g").take(7)
            "$baseVersion-dev+$commit"
        }
        // No tags, just commit hash: abcdef -> 0.0.0-dev+abcdef
        output.matches(Regex("^[a-f0-9]+$")) ->
            "0.0.0-dev+${output.take(7)}"
        else -> "0.0.0-dev"
    }
}

version = gitVersion.getOrElse("0.0.0-dev")

repositories {
    mavenCentral()
}

// Configure Grammar-Kit
grammarKit {
    jflexRelease.set("1.9.1")
}

// Register or configure lexer generation task
val generateLexer = if (tasks.findByName("generateLexer") != null) {
    tasks.named<GenerateLexerTask>("generateLexer")
} else {
    tasks.register<GenerateLexerTask>("generateLexer")
}
generateLexer.configure {
    sourceFile.set(file("src/main/java/org/clabs/superdb/SuperSQL.flex"))
    targetOutputDir.set(file("src/main/gen/org/clabs/superdb"))
    purgeOldFiles.set(true)
}

// Register or configure parser generation task
val generateParser = if (tasks.findByName("generateParser") != null) {
    tasks.named<GenerateParserTask>("generateParser")
} else {
    tasks.register<GenerateParserTask>("generateParser")
}
generateParser.configure {
    sourceFile.set(file("src/main/java/org/clabs/superdb/supersql.bnf"))
    targetRootOutputDir.set(file("src/main/gen"))
    pathToParser.set("org/clabs/superdb/parser/SuperSQLParser.java")
    pathToPsiRoot.set("org/clabs/superdb/psi")
    purgeOldFiles.set(true)
}

// Add generated sources to source sets
sourceSets {
    main {
        java {
            srcDirs("src/main/gen")
        }
    }
    test {
        java {
            srcDirs("src/test/java")
        }
        resources {
            srcDirs("src/test/testData")
        }
    }
}

// Dependencies for testing
dependencies {
    testImplementation("junit:junit:4.13.2")
}

// Make compile depend on generation tasks
tasks.compileJava {
    dependsOn("generateLexer", "generateParser")
}

tasks.compileKotlin {
    dependsOn("generateLexer", "generateParser")
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
val ideVersion: String by project.extra {
    findProperty("ideVersion")?.toString() ?: "2024.1.7"
}

// LSP4IJ version - check for latest at https://plugins.jetbrains.com/plugin/23257-lsp4ij
// Note: 0.17.0+ is needed for IntelliJ 2024.x compatibility
val lsp4ijVersion: String by project.extra {
    findProperty("lsp4ijVersion")?.toString() ?: "0.17.0"
}

intellij {
    version.set(ideVersion)
    type.set("IC") // Target IDE Platform

    // Plugin dependencies:
    // - LSP4IJ for Language Server Protocol support
    plugins.set(listOf(
        "com.redhat.devtools.lsp4ij:$lsp4ijVersion"
    ))
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
        sinceBuild.set("241")
        untilBuild.set("")  // Empty string = no upper bound
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    runPluginVerifier {
        ideVersions.set(listOf("2024.1", "2024.2", "2024.3", "2025.1", "2025.2", "2025.3"))
    }
}

// Clean generated sources
tasks.clean {
    delete("src/main/gen")
}

// Configure test task
tasks.test {
    useJUnit()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
    // Fork a new JVM for each test class to ensure isolation
    forkEvery = 1
    // Ensure tests run after code generation
    dependsOn("generateLexer", "generateParser")
    // Pass system properties for test data regeneration
    systemProperty("idea.tests.overwrite.data", System.getProperty("idea.tests.overwrite.data") ?: "false")
}

// Exclude LSP4IJ from test sandbox - it can't handle IntelliJ's in-memory TempFileSystem
// See: https://platform.jetbrains.com/t/how-to-make-configurebyfile-write-files-to-disk/2309
tasks.prepareTestingSandbox {
    pluginDependencies.set(emptyList())
}

// LSP Configuration
val lspRepo: String by project.extra {
    findProperty("lspRepo")?.toString() ?: "chrismo/superdb-syntaxes"
}
val lspVersion: String by project.extra {
    findProperty("lspVersion")?.toString() ?: "latest"
}

// Task to download LSP binary for current platform
tasks.register<Exec>("downloadLsp") {
    group = "lsp"
    description = "Download SuperSQL LSP binary for current platform"

    workingDir = projectDir
    commandLine("bash", "scripts/download-lsp.sh", lspVersion)

    environment("LSP_REPO", lspRepo)
    environment("GITHUB_TOKEN", System.getenv("GITHUB_TOKEN") ?: "")
}

// Task to download LSP binaries for all platforms (for distribution)
tasks.register<Exec>("downloadLspAll") {
    group = "lsp"
    description = "Download SuperSQL LSP binaries for all platforms"

    workingDir = projectDir
    commandLine("bash", "scripts/download-all-platforms.sh", lspVersion)

    environment("LSP_REPO", lspRepo)
    environment("GITHUB_TOKEN", System.getenv("GITHUB_TOKEN") ?: "")
}

// Make buildPlugin depend on LSP download for distribution builds
// Uncomment when LSP releases are available:
// tasks.buildPlugin {
//     dependsOn("downloadLspAll")
// }
