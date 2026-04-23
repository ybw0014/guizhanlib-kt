import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    `maven-publish`
    signing
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "8.3.6"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.jetbrains.dokka") version "1.9.10"
    id("com.diffplug.spotless") version "8.3.0"
}

group = "net.guizhanss"
extra["guizhanLibVersion"] = "3.0.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.alessiodp.com/releases/")
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            ktlint().editorConfigOverride(
                mapOf(
                    "ktlint_code_style" to "intellij_idea",
                    "ktlint_standard_no-unused-imports" to "enabled"
                )
            )
        }
    }

    dependencies {
        fun compileOnlyAndTestImpl(dep: Any) {
            compileOnly(dep)
            testImplementation(dep)
        }

        compileOnlyAndTestImpl(kotlin("stdlib"))
        compileOnlyAndTestImpl(kotlin("reflect"))
        compileOnlyAndTestImpl("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.1")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.12.1")
        testImplementation("com.github.MockBukkit:MockBukkit:v1.21-SNAPSHOT")
        testImplementation(kotlin("test"))
    }

    java {
        disableAutoTargetJvm()
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        compilerOptions {
            javaParameters = true
            jvmTarget = JvmTarget.JVM_21
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    val sourcesJar by tasks.creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    val javadocJar by tasks.creating(Jar::class) {
        archiveClassifier.set("javadoc")
        from(tasks.named("dokkaHtml"))
        dependsOn("dokkaHtml")
    }

    tasks.withType<ShadowJar> {
        archiveClassifier = ""
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["shadow"])

                groupId = rootProject.group.toString()
                artifactId = project.name
                version = rootProject.version.toString()

                artifact(sourcesJar)
                artifact(javadocJar)

                pom {
                    name.set("guizhanlib-kt")
                    description.set("A Kotlin library for Slimefun addon development.")
                    url.set("https://github.com/ybw0014/guizhanlib-kt")

                    licenses {
                        license {
                            name.set("GPL-3.0 license")
                            url.set("https://github.com/ybw0014/guizhanlib-kt/blob/master/LICENSE")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            name.set("ybw0014")
                            url.set("https://ybw0014.dev/")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/ybw0014/guizhanlib-kt.git")
                        developerConnection.set("scm:git:ssh://github.com:ybw0014/guizhanlib-kt.git")
                        url.set("https://github.com/ybw0014/guizhanlib-kt/tree/master")
                    }
                }
            }
        }
    }

    signing {
        sign(publishing.publications["maven"])
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}
