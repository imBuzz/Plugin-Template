plugins {
    java
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

val pluginName: String by project
val pluginMain: String by project
val pluginAuthors: String by project
val pluginDescription: String by project
val javaVersion = libs.versions.java.get().toInt()
val basePackage = pluginMain.substringBeforeLast('.')

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
        content { includeGroupAndSubgroups("io.papermc"); includeGroup("com.mojang"); includeGroup("net.md-5") }
    }
}

// Downloaded by Paper at runtime (plugin.yml "libraries"): compiled against, not shaded.
val runtimeLibraries = listOf(libs.configme, libs.hikari, libs.mariadb, libs.h2, libs.caffeine)

dependencies {
    // paper-api also exposes Adventure, Gson, Guava and fastutil: they are already on the server.
    compileOnly(libs.paper.api)
    runtimeLibraries.forEach { compileOnly(it) }

    // Shaded + relocated into the final jar.
    implementation(libs.bundles.lamp)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.params)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.paper.api)
    runtimeLibraries.forEach { testImplementation(it) }
    testImplementation(libs.sqlite)
}

// API-only jar (it/buzz/template/api/**) that other plugins can depend on with compileOnly.
val apiJar by tasks.registering(Jar::class) {
    archiveBaseName = pluginName
    archiveClassifier = "api"
    from(sourceSets.main.get().output) { include(basePackage.replace('.', '/') + "/api/**") }
}

publishing {
    publications {
        create<MavenPublication>("api") {
            artifactId = "${pluginName.lowercase()}-api"
            artifact(apiJar) { classifier = null }
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = javaVersion
        options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all,-processing,-serial", "-Werror"))
    }

    processResources {
        val props = mapOf(
            "name" to pluginName,
            "version" to project.version.toString(),
            "main" to pluginMain,
            "description" to pluginDescription,
            "apiVersion" to libs.versions.minecraft.get(),
            "authors" to pluginAuthors.split(',').joinToString(", ", "[", "]") { "\"${it.trim()}\"" },
            "libraries" to runtimeLibraries.joinToString("\n") { lib ->
                val dep = lib.get()
                "  - \"${dep.module.group}:${dep.module.name}:${dep.version}\""
            },
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") { expand(props) }
    }

    jar { enabled = false }

    shadowJar {
        archiveBaseName = pluginName
        archiveClassifier = ""
        relocate("revxrsal.commands", "$basePackage.libs.lamp")
        mergeServiceFiles()
        exclude("META-INF/maven/**", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    assemble { dependsOn(shadowJar, apiJar) }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        jvmArgs("-Dcom.mojang.eula.agree=true", "-Dpaper.disablePluginRemapping=true")
    }
}
