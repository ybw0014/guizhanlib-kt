val guizhanLibVersion: String by rootProject.extra

dependencies {
    fun compileOnlyAndTestImpl(dep: Any) {
        compileOnly(dep)
        testImplementation(dep)
    }

    compileOnlyAndTestImpl("net.guizhanss:guizhanlib-minecraft:$guizhanLibVersion")
    api(project(":guizhanlib-kt-common", configuration = "shadow"))
}
