package net.guizhanss.guizhanlib.kt.slimefun.config

import net.guizhanss.guizhanlib.kt.slimefun.config.migration.MigrationManager
import net.guizhanss.guizhanlib.slimefun.addon.AbstractAddon
import net.guizhanss.guizhanlib.slimefun.addon.AddonConfig

/**
 * A DSL builder to build a [ConfigContainer].
 *
 * @param config The [AddonConfig] instance.
 */
class ConfigBuilder(private val config: AddonConfig) {

    private val container = ConfigContainer(config)

    fun boolean(path: String, default: Boolean) =
        container.register { config.getBoolean(path, default) }

    fun int(path: String, default: Int, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE) =
        container.register { config.getInt(path, default).coerceIn(min, max) }

    fun long(path: String, default: Long, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE) =
        container.register { config.getLong(path, default).coerceIn(min, max) }

    fun double(path: String, default: Double, min: Double = Double.MAX_VALUE, max: Double = Double.MAX_VALUE) =
        container.register { config.getDouble(path, default).coerceIn(min, max) }

    fun string(path: String, default: String) =
        container.register { config.getString(path) ?: default }

    fun stringList(path: String) =
        container.register { config.getStringList(path) }

    fun section(path: String) =
        container.register { config.getConfigurationSection(path) }

    fun <T> custom(loader: (AddonConfig) -> T) =
        container.register { loader(config) }

    fun build(): ConfigContainer = container
}

/**
 * Build a [ConfigContainer] in DSL style.
 *
 * @param addon The [AbstractAddon] instance.
 * @param file The path to the config file.
 * @param migrations Optional [MigrationManager] for migrations.s
 * @param builder DSL builder.
 */
fun addonConfig(
    addon: AbstractAddon,
    file: String,
    migrations: MigrationManager? = null,
    builder: ConfigBuilder.() -> Unit
): ConfigContainer {
    val addonConfig = AddonConfig(addon, file)

    migrations?.migrate(addonConfig)

    val b = ConfigBuilder(addonConfig)
    builder(b)
    return b.build()
}
