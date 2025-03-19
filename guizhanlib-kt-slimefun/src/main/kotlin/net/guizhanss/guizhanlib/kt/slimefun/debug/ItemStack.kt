@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.slimefun.debug

import net.guizhanss.guizhanlib.kt.slimefun.extensions.getSlimefunItem
import net.guizhanss.guizhanlib.kt.slimefun.extensions.isSlimefunItem
import org.bukkit.inventory.ItemStack

/**
 * Returns the debug message of the [ItemStack] array.
 */
fun Array<out ItemStack?>.debugMessage(): String {
    return this.joinToString(", ") { itemStack ->
        if (itemStack == null) {
            return@joinToString "null"
        }

        if (itemStack.isSlimefunItem()) {
            val sfItem = itemStack.getSlimefunItem()
            return@joinToString sfItem.item.toString()
        }

        return@joinToString itemStack.toString()
    }
}
