val guizhanLibVersion: String by rootProject.extra

dependencies {
    fun compileOnlyAndTestImpl(dep: Any) {
        compileOnly(dep)
        testImplementation(dep)
    }

    api(project(":guizhanlib-kt-common", configuration = "shadow"))
    api(project(":guizhanlib-kt-minecraft", configuration = "shadow"))
    compileOnlyAndTestImpl("net.guizhanss:guizhanlib-slimefun:$guizhanLibVersion")
    compileOnlyAndTestImpl("com.github.slimefun:Slimefun4:experimental-SNAPSHOT")
}
