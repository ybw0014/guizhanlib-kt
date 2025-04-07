val guizhanLibVersion: String by rootProject.extra

dependencies {
    fun compileOnlyAndTestImpl(dep: Any) {
        compileOnly(dep)
        testImplementation(dep)
    }

    compileOnlyAndTestImpl("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    api(project(":guizhanlib-kt-common", configuration = "shadow"))
    api("net.guizhanss:guizhanlib-minecraft:$guizhanLibVersion")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.45.0")
}
