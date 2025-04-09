val guizhanLibVersion: String by rootProject.extra

dependencies {
    api(project(":guizhanlib-kt-common", configuration = "shadow"))
    api(project(":guizhanlib-kt-minecraft", configuration = "shadow"))
    api("net.guizhanss:guizhanlib-slimefun:$guizhanLibVersion")
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("com.github.slimefun:Slimefun4:3ea21da4fe")
}
