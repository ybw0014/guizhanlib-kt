@file:Suppress("unused", "deprecation")

package net.guizhanss.guizhanlib.kt.minecraft.items

import net.guizhanss.guizhanlib.minecraft.utils.ChatUtil
import org.bukkit.inventory.ItemStack

/**
 * An [ItemStack] editor.
 */
class ItemStackEditor(private val original: ItemStack) {

    private var amount: Int? = null
    private var name: String? = null
    private val newLore = mutableListOf<String>()
    private var clearLore = false

    /**
     * Set the amount of the item.
     */
    fun amount(value: Int) {
        amount = value
    }

    /**
     * Set the display name of the item.
     */
    fun name(value: String) {
        name = value
    }

    /**
     * Add a single line to the lore.
     */
    operator fun String.unaryPlus() {
        newLore += this
    }

    /**
     * Add multiple lines to the lore.
     */
    fun lore(vararg lines: String) {
        newLore.addAll(lines)
    }

    /**
     * Clear the existing lore and set new lines.
     */
    fun setLore(vararg lines: String) {
        clearLore = true
        newLore.clear()
        newLore.addAll(lines)
    }

    /**
     * Construct the final [ItemStack] with the applied changes.
     */
    internal fun build(): ItemStack {
        val result = original.clone()

        // update amount
        amount?.let { result.amount = it }

        // update itemMeta
        result.itemMeta = result.itemMeta?.apply {
            // update name
            name?.let { setDisplayName(ChatUtil.color(it)) }

            // update lore
            if (clearLore) {
                lore = ChatUtil.color(newLore)
            } else if (newLore.isNotEmpty()) {
                lore = (lore ?: mutableListOf()).apply { addAll(ChatUtil.color(newLore)) }
            }
        }

        return result
    }
}

/**
 * Edit the given [ItemStack].
 */
fun ItemStack.edit(block: ItemStackEditor.() -> Unit): ItemStack {
    val editor = ItemStackEditor(this.clone())
    editor.block()
    return editor.build()
}
