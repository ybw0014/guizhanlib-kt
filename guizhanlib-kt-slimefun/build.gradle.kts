dependencies {
    api("io.github.seggan:sf4k:0.8.2") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "com.github.Slimefun")
    }
    api(project(":guizhanlib-kt-minecraft", configuration = "shadow"))
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("com.github.slimefun:Slimefun4:3ea21da4fe")
}
