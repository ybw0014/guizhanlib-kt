package net.guizhanss.guizhanlib.kt.slimefun.config

import net.guizhanss.guizhanlib.slimefun.addon.AbstractAddon
import net.guizhanss.guizhanlib.slimefun.addon.AddonConfig

class ConfigBuilder(private val config: AddonConfig) {

    private val container = ConfigContainer(config)

    fun boolean(path: String, default: Boolean) =
        container.register { config.getBoolean(path, default) }

    fun int(path: String, default: Int) =
        container.register { config.getInt(path, default) }

    fun string(path: String, default: String) =
        container.register { config.getString(path) ?: default }

    fun <T> custom(loader: () -> T) =
        container.register(loader)

    fun build(): ConfigContainer = container
}

fun addonConfig(addon: AbstractAddon, file: String, builder: ConfigBuilder.() -> Unit): ConfigContainer {
    val addonConfig = AddonConfig(addon, file)
    val b = ConfigBuilder(addonConfig)
    builder(b)
    return b.build()
}
