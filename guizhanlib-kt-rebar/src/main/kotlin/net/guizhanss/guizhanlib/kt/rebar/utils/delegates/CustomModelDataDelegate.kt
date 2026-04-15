@file:Suppress("UnstableApiUsage")

package net.guizhanss.guizhanlib.kt.rebar.utils.delegates

import io.github.pylonmc.rebar.item.RebarItem
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.CustomModelData
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegation to read/set a prefixed string value in the bound stack's
 * CustomModelData strings list.
 *
 * Treats CMD strings like a key-value store where the key is the [prefix].
 * For example, with prefix `"myitem:variant:"`, the CMD strings `["myitem:variant:active"]`
 * stores the value `"active"`.
 *
 * When writing, the existing CMD component's floats/flags/colors are preserved.
 *
 * Usage:
 * ```
 * class MyItem(...) : RebarItem(...) {
 *     var variant by customModelDataString("myitem:variant:", "default")
 * }
 * ```
 */
@JvmSynthetic
inline fun <reified T : RebarItem, V> T.customModelDataString(
    prefix: String,
    crossinline deserialize: (String) -> V,
    crossinline serialize: (V) -> String,
) = object : ReadWriteProperty<T, V?> {
    override fun getValue(thisRef: T, property: KProperty<*>): V? {
        val cmd = thisRef.stack.getData(DataComponentTypes.CUSTOM_MODEL_DATA) ?: return null
        val entry = cmd.strings().firstOrNull { it.startsWith(prefix) } ?: return null
        return deserialize(entry.removePrefix(prefix))
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V?) {
        val stack = thisRef.stack
        val existing = stack.getData(DataComponentTypes.CUSTOM_MODEL_DATA)

        if (value == null) {
            val cmd = existing ?: return
            val oldStrings = cmd.strings().toMutableList()
            val idx = oldStrings.indexOfFirst { it.startsWith(prefix) }
            if (idx < 0) return
            oldStrings.removeAt(idx)
            if (oldStrings.isEmpty()
                && cmd.floats().isEmpty()
                && cmd.flags().isEmpty()
                && cmd.colors().isEmpty()
            ) {
                stack.unsetData(DataComponentTypes.CUSTOM_MODEL_DATA)
            } else {
                stack.setData(
                    DataComponentTypes.CUSTOM_MODEL_DATA,
                    CustomModelData.customModelData()
                        .addFloats(cmd.floats())
                        .addStrings(oldStrings)
                        .addFlags(cmd.flags())
                        .addColors(cmd.colors())
                        .build(),
                )
            }
            return
        }

        val oldStrings = existing?.strings()?.toMutableList() ?: mutableListOf()
        val newEntry = prefix + serialize(value)

        val idx = oldStrings.indexOfFirst { it.startsWith(prefix) }
        if (idx >= 0) {
            oldStrings[idx] = newEntry
        } else {
            oldStrings.add(newEntry)
        }

        stack.setData(
            DataComponentTypes.CUSTOM_MODEL_DATA,
            CustomModelData.customModelData()
                .addFloats(existing?.floats() ?: emptyList())
                .addStrings(oldStrings)
                .addFlags(existing?.flags() ?: emptyList())
                .addColors(existing?.colors() ?: emptyList())
                .build(),
        )
    }
}

@JvmSynthetic
inline fun <reified T : RebarItem, V> T.customModelDataString(
    prefix: String,
    crossinline deserialize: (String) -> V,
    crossinline serialize: (V) -> String,
    crossinline default: () -> V,
) = object : ReadWriteProperty<T, V> {
    override fun getValue(thisRef: T, property: KProperty<*>): V {
        val nullable = thisRef.customModelDataString<T, V>(prefix, deserialize, serialize)
        return nullable.getValue(thisRef, property) ?: default()
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        val nullable = thisRef.customModelDataString<T, V>(prefix, deserialize, serialize)
        nullable.setValue(thisRef, property, value)
    }
}

@JvmSynthetic
inline fun <reified T : RebarItem, V> T.customModelDataString(
    prefix: String,
    crossinline deserialize: (String) -> V,
    crossinline serialize: (V) -> String,
    default: V,
) = customModelDataString(prefix, deserialize, serialize) { default }

@JvmSynthetic
inline fun <reified T : RebarItem> T.customModelDataString(
    prefix: String,
) = customModelDataString<T, String>(
    prefix = prefix,
    deserialize = { it },
    serialize = { it },
)

@JvmSynthetic
inline fun <reified T : RebarItem> T.customModelDataString(
    prefix: String,
    default: String,
) = customModelDataString<T, String>(
    prefix = prefix,
    deserialize = { it },
    serialize = { it },
    default = default,
)
