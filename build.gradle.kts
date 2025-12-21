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

// Task to generate lexer from .flex file
val generateLexer = tasks.register<GenerateLexerTask>("generateLexer") {
    sourceFile.set(file("src/main/java/org/clabs/superdb/SuperSQL.flex"))
    targetOutputDir.set(file("src/main/gen/org/clabs/superdb"))
    purgeOldFiles.set(true)
}

// Task to generate parser from .bnf file
val generateParser = tasks.register<GenerateParserTask>("generateParser") {
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
}

// Make compile depend on generation tasks
tasks.compileJava {
    dependsOn(generateLexer, generateParser)
}

tasks.compileKotlin {
    dependsOn(generateLexer, generateParser)
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.1.7")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf(/* Plugin Dependencies */))
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
}

// Clean generated sources
tasks.clean {
    delete("src/main/gen")
}
