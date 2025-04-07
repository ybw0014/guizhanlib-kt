dependencies {
    fun compileOnlyAndTestImpl(dep: Any) {
        compileOnly(dep)
        testImplementation(dep)
    }

    compileOnlyAndTestImpl("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    api(project(":guizhanlib-kt-common", configuration = "shadow"))
    api("net.guizhanss:guizhanlib-minecraft:2.2.0")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.45.0")
}

tasks.test {
    dependsOn(":guizhanlib-kt-common:jar")
}

tasks.shadowJar {
    dependsOn(":guizhanlib-kt-common:jar")
}
