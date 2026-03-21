val guizhanLibVersion: String by rootProject.extra

repositories {
    maven("https://repo.xenondevs.xyz/releases")
}

dependencies {
    fun compileOnlyAndTestImpl(dep: Any) {
        compileOnly(dep)
        testImplementation(dep)
    }

    api(project(":guizhanlib-kt-common", configuration = "shadow"))
    api(project(":guizhanlib-kt-minecraft", configuration = "shadow"))
    compileOnlyAndTestImpl("net.guizhanss:guizhanlib-rebar:$guizhanLibVersion")
    compileOnlyAndTestImpl("io.github.pylonmc:rebar:0.36.2")
}
