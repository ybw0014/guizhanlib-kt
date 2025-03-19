@file:Suppress("unused", "DEPRECATION")

package net.guizhanss.guizhanlib.kt.minecraft.extensions

import org.bukkit.Registry
import org.bukkit.enchantments.Enchantment

/**
 * Retrieve the [Enchantment] by the name.
 *
 * The name is automatically converted to [org.bukkit.NamespacedKey] first.
 */
fun getEnchantment(name: String): Enchantment? = Registry.ENCHANTMENT.get(name.toMinecraftKey())
