package net.guizhanss.guizhanlib.kt.slimefun.config

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ConfigField<T>(
    private val loader: () -> T
) : ReadOnlyProperty<Any?, T> {

    private var cached: T = loader()

    fun reload() {
        cached = loader()
    }

    val value: T
        get() = cached

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = cached
}
