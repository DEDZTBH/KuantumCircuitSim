import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}
group = "com.dedztbh"
version = "0.2"

val ejmlVersion = "0.39"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ejml:ejml-core:${ejmlVersion}")
    implementation("org.ejml:ejml-zdense:${ejmlVersion}")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf(
        "-Xno-param-assertions",
        "-Xno-call-assertions",
        "-Xno-receiver-assertions"
    )
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("kuantum_circuit_sim")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "MainKt"))
        }
        minimize()
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}