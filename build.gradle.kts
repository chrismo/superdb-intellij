import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
}

group = "org.clabs"
version = "1.0-SNAPSHOT"

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
val lsp4ijVersion: String by project.extra {
    findProperty("lsp4ijVersion")?.toString() ?: "0.8.1"
}

intellij {
    version.set(ideVersion)
    type.set("IC") // Target IDE Platform

    // LSP4IJ for Language Server Protocol support
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
        untilBuild.set("243.*")
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
        ideVersions.set(listOf("2024.1", "2024.2", "2024.3"))
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
    // Ensure tests run after code generation
    dependsOn("generateLexer", "generateParser")
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
