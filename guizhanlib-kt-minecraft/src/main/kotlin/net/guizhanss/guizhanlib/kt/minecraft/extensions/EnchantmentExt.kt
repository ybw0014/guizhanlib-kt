@file:Suppress("unused", "DEPRECATION")

package net.guizhanss.guizhanlib.kt.minecraft.extensions

import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.enchantments.Enchantment

/**
 * Retrieve the [Enchantment] from registry by [NamespacedKey].
 */
fun getEnchantment(key: NamespacedKey): Enchantment? = Registry.ENCHANTMENT[key]

/**
 * Retrieve the [Enchantment] by the name.
 *
 * The name is automatically converted to [org.bukkit.NamespacedKey] first.
 */
fun getEnchantment(name: String): Enchantment? = getEnchantment(name.toNamespacedKey() ?: error("Invalid NamespacedKey: $name"))
