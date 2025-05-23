package net.guizhanss.guizhanlib.kt.slimefun.config

import net.guizhanss.guizhanlib.slimefun.addon.AddonConfig

/**
 * A wrapper class for easy management of config fields in [AddonConfig].
 *
 * @see ConfigField
 * @see ConfigBuilder
 */
class ConfigContainer(val config: AddonConfig) {

    private val _fields = mutableListOf<ConfigField<*>>()
    val fields: List<ConfigField<*>>
        get() = _fields

    fun reload() {
        config.reload()
        _fields.forEach { it.reload() }
    }

    fun save() {
        config.save()
    }

    internal fun <T> register(loader: () -> T): ConfigField<T> {
        return ConfigField(loader).also { _fields += it }
    }

}
