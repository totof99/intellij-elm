import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

//Based on https://github.com/JetBrains/intellij-platform-plugin-template/tree/v1.11.3

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.16.1"
    // GrammarKit Plugin
    id("org.jetbrains.grammarkit") version "2022.3.2.1"
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Configure project's dependencies
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.colormath:colormath:2.1.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.20-M1")
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
//                                             read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")
}

//Gradle Grammar-Kit Plugin - read more: https://github.com/JetBrains/gradle-grammar-kit-plugin
val generateSpecParser = tasks.create<GenerateParserTask>("generateElmParser") {
    sourceFile.set(file("$projectDir/src/main/grammars/ElmParser.bnf"))
    targetRoot.set("$projectDir/src/main/gen")
    pathToParser.set("/org/elm/lang/core/parser/ElmParser.java")
    pathToPsiRoot.set("/org/elm/lang/core/psi")
    purgeOldFiles.set(true)
}

val generateSpecLexer = tasks.create<GenerateLexerTask>("generateElmLexer") {
    sourceFile.set(file("$projectDir/src/main/grammars/ElmLexer.flex"))
    skeleton.set(file("$projectDir/src/main/grammars/lexer.skeleton"))
    targetDir.set("$projectDir/src/main/gen/org/elm/lang/core/lexer/")
    targetClass.set("_ElmLexer")
    purgeOldFiles.set(true)
}

val generateGrammars = tasks.register("generateGrammars") {
    dependsOn(generateSpecParser, generateSpecLexer)
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }

    sourceSets {       
        java.sourceSets["main"].java {
            srcDir("src/main/gen")
        }
    }
    
    // Set the JVM compatibility versions
    withType<KotlinCompile>().configureEach {
        dependsOn(generateGrammars)
        compilerOptions.jvmTarget = properties("javaVersion").map {
            JvmTarget.fromTarget(it)
        }
    }

    withType<JavaCompile>().configureEach {
        sourceCompatibility = properties("javaVersion").get()
        targetCompatibility = properties("javaVersion").get()
    }
    
    patchPluginXml {
        version = properties("pluginVersion")
        sinceBuild = properties("pluginSinceBuild")
        untilBuild = properties("pluginUntilBuild")
    }
    
    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }
}

