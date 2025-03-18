@file:Suppress("unused", "deprecation")

package net.guizhanss.guizhanlib.kt.minecraft.extensions

import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

/**
 * Retrieve the [PotionEffectType] by the name. For 1.20.5+ compatibility.
 */
fun getPotionEffectType(name: String) = PotionEffectType.getByKey(name.toMinecraftKey())

/**
 * Build a [PotionEffect] by the name, duration, and amplifier.
 */
fun buildPotionEffect(name: String, duration: Int, amplifier: Int) = PotionEffect(
    getPotionEffectType(name) ?: throw IllegalArgumentException("Invalid potion effect type: $name"),
    duration,
    amplifier,
)

/**
 * Build a hidden [PotionEffect] by the name, duration, and amplifier.
 */
fun buildHiddenPotionEffect(name: String, duration: Int, amplifier: Int) = PotionEffect(
    getPotionEffectType(name) ?: throw IllegalArgumentException("Invalid potion effect type: $name"),
    duration,
    amplifier,
    false,
    false,
    false,
)
