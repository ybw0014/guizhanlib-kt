import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    `maven-publish`
    signing
    kotlin("jvm") version "2.1.20"
    id("com.gradleup.shadow") version "8.3.6"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

group = "net.guizhanss"

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

    dependencies {
        compileOnly(kotlin("stdlib"))
        compileOnly(kotlin("reflect"))
        testImplementation(kotlin("test"))
    }

    java {
        disableAutoTargetJvm()
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        withJavadocJar()
        withSourcesJar()
    }

    kotlin {
        compilerOptions {
            javaParameters = true
            jvmTarget = JvmTarget.JVM_17
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<ShadowJar> {
        archiveClassifier = ""
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                project.shadow.component(this)

                artifact(tasks.named("javadocJar").get())
                artifact(tasks.named("sourcesJar").get())

                groupId = rootProject.group as String
                artifactId = project.name
                version = rootProject.version as String

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
                        developerConnection.set("scm:git:ssh://github.com:ybw0014/guizhanlib.git")
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
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}
