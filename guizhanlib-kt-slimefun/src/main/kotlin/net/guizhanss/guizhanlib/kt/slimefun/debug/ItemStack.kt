@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.slimefun.debug

import io.github.seggan.sf4k.extensions.getSlimefun
import io.github.seggan.sf4k.extensions.isSlimefun
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import org.bukkit.inventory.ItemStack

/**
 * Returns the debug message of the [ItemStack] array.
 */
fun Array<out ItemStack?>.debugMessage(): String {
    return this.joinToString(", ") { itemStack ->
        if (itemStack == null) {
            return@joinToString "null"
        }

        if (itemStack.isSlimefun()) {
            val sfItem = itemStack.getSlimefun<SlimefunItem>()!!
            return@joinToString sfItem.item.toString()
        }

        return@joinToString itemStack.toString()
    }
}
