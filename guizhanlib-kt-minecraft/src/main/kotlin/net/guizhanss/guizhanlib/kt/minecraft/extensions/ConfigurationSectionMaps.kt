@file:Suppress("unused", "deprecation")

package net.guizhanss.guizhanlib.kt.minecraft.extensions

import net.guizhanss.guizhanlib.kt.common.extensions.valueOfOrNull
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import java.util.Locale
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.collections.toMap
import kotlin.text.uppercase

/**
 * Loads a map from the [ConfigurationSection].
 *
 * @param keyParser The parser function to convert the key string to the desired type.
 * @param valueParser The parser function to convert the value to the desired type.
 * @param valuePredicate The predicate function to filter the values. Default is to accept all values.
 */
inline fun <reified K, reified V> ConfigurationSection?.loadMap(
    keyParser: (String) -> K? = { key -> key as? K },
    valueParser: (Any?) -> V? = { value -> value as? V },
    valuePredicate: (V) -> Boolean = { true }
): Map<K, V> {
    if (this == null) return emptyMap()

    val result = mutableMapOf<K, V>()
    getKeys(false).forEach { keyStr ->
        val key = keyParser(keyStr) ?: return@forEach
        val value = valueParser(this[keyStr]) ?: return@forEach
        if (!valuePredicate(value)) return@forEach
        result[key] = value
    }
    return result.toMap()
}

/**
 * Loads a map from the [ConfigurationSection] with enum keys.
 */
inline fun <reified K : Enum<K>, reified V> ConfigurationSection?.loadEnumKeyMap(
    valueParser: (Any?) -> V? = { value -> value as? V },
    valuePredicate: (V) -> Boolean = { true }
): Map<K, V> = loadMap(
    keyParser = { keyStr -> valueOfOrNull<K>(keyStr.uppercase(Locale.getDefault())) },
    valueParser,
    valuePredicate
)

/**
 * Loads a map from the [ConfigurationSection] with string keys and integer values.
 */
fun ConfigurationSection?.loadIntMap(valuePredicate: (Int) -> Boolean = { true }) =
    loadMap<String, Int>(valuePredicate = valuePredicate)

/**
 * Loads a map from the [ConfigurationSection] with string keys and long values.
 */
fun ConfigurationSection?.loadDoubleMap(valuePredicate: (Double) -> Boolean = { true }) =
    loadMap<String, Double>(valuePredicate = valuePredicate)

/**
 * Loads a map from the [ConfigurationSection] with string keys and boolean values.
 */
fun ConfigurationSection?.loadBooleanMap() = loadMap<String, Boolean>()

/**
 * Loads a map from the [ConfigurationSection] with string keys and string values.
 */
fun ConfigurationSection?.loadStringMap(valuePredicate: (String) -> Boolean = { true }) =
    loadMap<String, String>(valueParser = { it.toString() }, valuePredicate = valuePredicate)

/**
 * Loads a map from the [ConfigurationSection] with string keys and [ConfigurationSection] values.
 */
fun ConfigurationSection?.loadSectionMap() = loadMap<String, ConfigurationSection>()

/**
 * Loads a map from the [ConfigurationSection] with [Enchantment] keys and values as their levels.
 */
fun ConfigurationSection?.loadEnchantmentKeyMap() =
    loadMap<Enchantment, Int>({ key -> Enchantment.getByName(key) }, valuePredicate = { it >= 1 })

