@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.minecraft.extensions

import org.bukkit.block.Block
import org.bukkit.block.BlockFace

/**
 * Check if the [Block] has light from the sky.
 */
fun Block.hasLightFromSky() = getRelative(BlockFace.UP).lightFromSky.toInt() == 15
