package net.guizhanss.guizhanlib.kt.minecraft.config.migration

import net.guizhanss.guizhanlib.minecraft.config.YamlConfig

class MigrationManager {

    private val migrations = mutableMapOf<Int, ConfigMigration>()

    fun add(fromVersion: Int, toVersion: Int, init: ConfigMigration.() -> Unit) {
        val migration = ConfigMigration(fromVersion, toVersion)
        init(migration)
        migrations[fromVersion] = migration
    }

    internal fun migrate(config: YamlConfig) {
        val currentVersion = config.getInt("version", 1)

        var version = currentVersion
        while (migrations.containsKey(version)) {
            val migration = migrations[version]!!
            migration.applyTo(config)
            version = migration.toVersion
        }

        if (version != currentVersion) {
            config.save()
        }
    }
}

fun configMigrations(init: MigrationManager.() -> Unit): MigrationManager {
    val manager = MigrationManager()
    init(manager)
    return manager
}
