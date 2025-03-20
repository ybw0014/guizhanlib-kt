@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.slimefun.extensions

import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu
import org.bukkit.Location
import org.bukkit.block.Block

/**
 * Create a [BlockPosition] from this [Block].
 */
val Block.position: BlockPosition
    get() = BlockPosition(this)

/**
 * Create a [BlockPosition] from this [Location].
 */
val Location.position: BlockPosition
    get() = BlockPosition(this)

/**
 * Create a [BlockPosition] from this [BlockMenu].
 */
val BlockMenu.position: BlockPosition
    get() = BlockPosition(location)

/**
 * Get the [Location] from this [BlockPosition].
 */
val BlockPosition.location: Location
    get() = toLocation()
