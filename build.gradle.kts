import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}
group = "com.dedztbh"
version = "1.4.4"

val projectRoot = "${group}.kuantum"
val projectRootExclude = "/${projectRoot.replace('.', '/')}"

val ejmlVersion = "0.39"
val jblasVersion = "1.2.5"

repositories {
    mavenCentral()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://jitpack.io")
}

kotlin {
    sourceSets {
        dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
        }
    }
}

dependencies {
    implementation("org.ejml:ejml-core:${ejmlVersion}")
    implementation("org.ejml:ejml-zdense:${ejmlVersion}")
    implementation("org.jblas:jblas:${jblasVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("com.github.cvb941:kotlin-parallel-operations:1.3")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.11.1")
    implementation("org.slf4j:slf4j-nop:1.7.30")
    testImplementation("org.ejml:ejml-cdense:${ejmlVersion}")
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
    register("shadowJarEjml", ShadowJar::class) {
        archiveBaseName.set("Kuantum-ejml")
        from(sourceSets.main.get().output)
        configurations.add(project.configurations.runtime.get())
        configurations.add(project.configurations.implementation.get().apply {
            isCanBeResolved = true
        })
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "${projectRoot}.ejml.MainKt"))
        }
        dependencies {
            exclude(dependency("org.jblas:jblas:${jblasVersion}"))
        }
        exclude("${projectRootExclude}/jblas")
        minimize()
    }

    register("shadowJarJBLAS", ShadowJar::class) {
        archiveBaseName.set("Kuantum-jblas")
        from(sourceSets.main.get().output)
        configurations.add(project.configurations.runtime.get())
        configurations.add(project.configurations.implementation.get().apply {
            isCanBeResolved = true
        })
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "${projectRoot}.jblas.MainKt"))
        }
        dependencies {
            exclude(dependency("org.ejml:ejml-core:${ejmlVersion}"))
            exclude(dependency("org.ejml:ejml-zdense:${ejmlVersion}"))
        }
        exclude("${projectRootExclude}/ejml")
        minimize()
    }
}