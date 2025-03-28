@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.slimefun.items

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import net.guizhanss.guizhanlib.kt.common.utils.invoke
import net.guizhanss.guizhanlib.kt.minecraft.items.ItemStackEditor
import net.guizhanss.guizhanlib.kt.minecraft.items.edit
import net.guizhanss.guizhanlib.kt.slimefun.items.ConversionHandler.handler
import org.bukkit.inventory.ItemStack

private object ConversionHandler {

    val handler = if (ItemStack::class.java.isAssignableFrom(SlimefunItemStack::class.java)) {
        { sfis: SlimefunItemStack -> (sfis as ItemStack).clone() }
    } else {
        { sfis: SlimefunItemStack ->
            sfis.invoke<ItemStack>("item") ?: error("Cannot get ItemStack from SlimefunItemStack")
        }
    }
}

/**
 * Get the [ItemStack] from this [SlimefunItemStack].
 *
 * Result is always cloned.
 */
fun SlimefunItemStack.toItem(): ItemStack {
    return handler(this)
}

/**
 * Edit the given [SlimefunItemStack] and get the edited [ItemStack].
 */
fun SlimefunItemStack.edit(block: ItemStackEditor.() -> Unit): ItemStack =
    toItem().edit(block)

/**
 * Edit the given [SlimefunItemStack] and get the edited [SlimefunItemStack].
 */
fun SlimefunItemStack.editSf(block: ItemStackEditor.() -> Unit): SlimefunItemStack {
    val item = this.edit(block)
    val id = Slimefun.getItemDataService().getItemData(item).get()
    return SlimefunItemStack(id, item)
}
