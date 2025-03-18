dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    api(project(":guizhanlib-kt-common", configuration = "shadow"))
    api("net.guizhanss:guizhanlib-minecraft:2.2.0")
}
