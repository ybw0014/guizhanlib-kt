@file:Suppress("unused", "deprecation")

package net.guizhanss.guizhanlib.kt.minecraft.items

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack

private val legacySerializer = LegacyComponentSerializer.legacySection()

/**
 * An [ItemStack] editor.
 */
class ItemStackEditor(private val original: ItemStack) {

    private var amount: Int? = null
    private var name: Component? = null
    private val newLore = mutableListOf<Component>()
    private var clearLore = false

    private fun deserialize(text: String): Component {
        return legacySerializer.deserialize(ChatColor.translateAlternateColorCodes('&', text))
    }

    /**
     * Set the amount of the item.
     */
    fun amount(value: Int) {
        amount = value
    }

    /**
     * Set the display name component of the item.
     */
    fun name(component: Component) {
        name = component
    }

    /**
     * Set the display name of the item.
     */
    fun name(value: String) {
        name(deserialize(value))
    }

    /**
     * Add a single line to the lore.
     */
    operator fun String.unaryPlus() {
        newLore += deserialize(this)
    }

    /**
     * Add a single line to the lore.
     */
    operator fun Component.unaryPlus() {
        newLore += this
    }

    /**
     * Add multiple lines to the lore.
     */
    fun loreStr(vararg lines: String) {
        loreStr(lines.toList())
    }

    /**
     * Add multiple lines to the lore.
     */
    fun loreStr(lines: List<String>) {
        lore(lines.map { deserialize(it) })
    }

    /**
     * Add multiple lines to the lore.
     */
    fun lore(vararg lines: Component) {
        lore(lines.toList())
    }

    /**
     * Add multiple lines to the lore.
     */
    fun lore(lines: List<Component>) {
        newLore.addAll(lines)
    }

    /**
     * Clear the existing lore and set new lines.
     */
    fun setLore(vararg lines: String) {
        setLore(lines.toList())
    }

    /**
     * Clear the existing lore and set new lines.
     */
    fun setLore(lines: List<String>) {
        clearLore = true
        newLore.clear()
        newLore.addAll(lines.map { deserialize(it) })
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
            name?.let { displayName(it) }

            // update lore
            if (clearLore) {
                lore(newLore)
            } else if (newLore.isNotEmpty()) {
                val existingLore = if (hasLore()) lore()!! else mutableListOf()
                existingLore.addAll(newLore)
                lore(existingLore)
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
