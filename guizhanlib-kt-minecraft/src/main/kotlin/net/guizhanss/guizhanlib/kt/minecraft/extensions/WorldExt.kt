@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.minecraft.extensions

import org.bukkit.World

/**
 * Check if the world is day.
 */
fun World.isDay() = time in 0 until 13000

/**
 * Check if the world is night.
 */
fun World.isNight() = time in 13000..24000
