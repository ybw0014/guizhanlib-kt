package net.guizhanss.guizhanlib.kt.minecraft.config

import net.guizhanss.guizhanlib.kt.minecraft.config.migration.MigrationManager
import net.guizhanss.guizhanlib.minecraft.config.YamlConfig
import org.bukkit.plugin.java.JavaPlugin

/**
 * A DSL builder to build a [ConfigContainer].
 *
 * @param config The [YamlConfig] instance.
 */
@Suppress("unused")
class ConfigBuilder(private val config: YamlConfig) {

    private val container = ConfigContainer(config)

    fun boolean(path: String, default: Boolean) = container.register { config.getBoolean(path, default) }

    fun int(path: String, default: Int, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE) =
        container.register { config.getInt(path, default).coerceIn(min, max) }

    fun long(path: String, default: Long, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE) =
        container.register { config.getLong(path, default).coerceIn(min, max) }

    fun double(path: String, default: Double, min: Double = Double.MAX_VALUE, max: Double = Double.MAX_VALUE) =
        container.register { config.getDouble(path, default).coerceIn(min, max) }

    fun string(path: String, default: String) = container.register { config.getString(path) ?: default }

    fun stringList(path: String) = container.register { config.getStringList(path) }

    fun section(path: String) = container.register { config.getConfigurationSection(path) }

    fun <T> custom(loader: (YamlConfig) -> T) = container.register { loader(config) }

    fun build(): ConfigContainer = container
}

/**
 * Build a [ConfigContainer] in DSL style.
 *
 * @param plugin The [YamlConfig] instance.
 * @param file The path to the config file.
 * @param migrations Optional [MigrationManager] for migrations.s
 * @param builder DSL builder.
 */
fun yamlConfig(
    plugin: JavaPlugin, file: String, migrations: MigrationManager? = null, builder: ConfigBuilder.() -> Unit
): ConfigContainer {
    val ymlConfig = YamlConfig(plugin, file)

    migrations?.migrate(ymlConfig)

    val b = ConfigBuilder(ymlConfig)
    builder(b)
    return b.build()
}
