package net.guizhanss.guizhanlib.kt.rebar.utils.delegates

import io.github.pylonmc.rebar.item.RebarItem
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegation to read/set value in the bound stack's PersistentDataContainer.
 */
@JvmSynthetic
inline fun <reified T : RebarItem, V> T.persistentItemData(
    key: NamespacedKey,
    type: PersistentDataType<*, V & Any>,
    crossinline default: () -> V,
) = object : ReadWriteProperty<T, V> {
    override fun getValue(thisRef: T, property: KProperty<*>): V = thisRef.stack.persistentDataContainer.get(key, type) ?: default()

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        if (value == null) {
            thisRef.stack.editPersistentDataContainer { it.remove(key) }
        } else {
            thisRef.stack.editPersistentDataContainer { it.set(key, type, value) }
        }
    }
}

/**
 * Property delegation to read/set value in the bound stack's PersistentDataContainer.
 */
@JvmSynthetic
inline fun <reified T : RebarItem, V> T.persistentItemData(
    key: NamespacedKey,
    type: PersistentDataType<*, V & Any>,
    default: V,
) = persistentItemData(key, type) { default }
