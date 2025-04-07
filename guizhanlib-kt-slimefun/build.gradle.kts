dependencies {
    api(project(":guizhanlib-kt-common", configuration = "shadow"))
    api(project(":guizhanlib-kt-minecraft", configuration = "shadow"))
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("com.github.slimefun:Slimefun4:3ea21da4fe")
}

tasks.test {
    dependsOn(":guizhanlib-kt-minecraft:jar")
}

tasks.shadowJar {
    dependsOn(":guizhanlib-kt-minecraft:jar")
}
