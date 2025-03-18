@file:Suppress("unused", "deprecation")

package net.guizhanss.guizhanlib.kt.minecraft.extensions

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Create an [ItemStack] from the [Material] with the given amount (default 1).
 */
fun Material.toItem(amount: Int = 1): ItemStack = ItemStack(this, amount)

/**
 * A shortcut to check if an [ItemStack] is null or air.
 */
@OptIn(ExperimentalContracts::class)
fun ItemStack?.isAir(): Boolean {
    contract {
        returns(false) implies (this@isAir != null)
    }
    return this?.type?.isAir != false
}

/**
 * Drop the [ItemStack] with the given amount at the [Location].
 */
fun ItemStack.dropItem(loc: Location, amount: Int = 1) {
    val fullStacks = amount / maxStackSize
    val remaining = amount % maxStackSize
    repeat(fullStacks) {
        val item = clone().apply { this.amount = amount }
        loc.world.dropItem(loc, item)
    }
    if (remaining > 0) {
        val item = clone().apply { this.amount = remaining }
        loc.world.dropItem(loc, item)
    }
}
