package net.guizhanss.guizhanlib.kt.minecraft.config.migration

import net.guizhanss.guizhanlib.minecraft.config.YamlConfig

/**
 * A single migration to handle configuration changes between 2 versions.
 */
class ConfigMigration(val fromVersion: Int, val toVersion: Int) {

    private val operations = mutableListOf<(YamlConfig) -> Unit>()

    /**
     * The config field has changed path.
     */
    fun move(oldPath: String, newPath: String) {
        operations.add { config ->
            if (config.contains(oldPath)) {
                val value = config.get(oldPath)
                config.set(newPath, value)
                config.set(oldPath, null) // Remove old path
            }
        }
    }

    /**
     * The config field has changed value type.
     */
    fun transform(path: String, transformer: (Any?) -> Any?) {
        operations.add { config ->
            if (config.contains(path)) {
                val value = config.get(path)
                config.set(path, transformer(value))
            }
        }
    }

    /**
     * The config field has been removed.
     */
    fun remove(path: String) {
        operations.add { config ->
            config.set(path, null)
        }
    }

    internal fun applyTo(config: YamlConfig) {
        operations.forEach { it(config) }
        config.set("version", toVersion)
    }
}
