val guizhanLibVersion: String by rootProject.extra

dependencies {
    fun compileOnlyAndTestImpl(dep: Any) {
        compileOnly(dep)
        testImplementation(dep)
    }

    api(project(":guizhanlib-kt-common", configuration = "shadow"))
    api(project(":guizhanlib-kt-minecraft", configuration = "shadow"))
    compileOnlyAndTestImpl("net.guizhanss:guizhanlib-slimefun:$guizhanLibVersion")
    compileOnlyAndTestImpl("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnlyAndTestImpl("com.github.slimefun:Slimefun4:experimental-SNAPSHOT")
}
