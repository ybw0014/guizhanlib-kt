@file:Suppress("unused", "deprecation")

package net.guizhanss.guizhanlib.kt.minecraft.items

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * An [ItemStack] editor.
 */
class ItemStackEditor(private val original: ItemStack) {

    private var amount: Int? = null
    private var name: Component? = null
    private val newLore = mutableListOf<Component>()
    private var clearLore = false
    private var editMeta: (ItemMeta.() -> Unit)? = null

    private fun deserialize(text: String): Component {
        return LegacyComponentSerializer.legacySection().deserialize(ChatColor.translateAlternateColorCodes('&', text))
    }

    /**
     * Set the amount of the item.
     */
    fun amount(value: Int) {
        amount = value
    }

    /**
     * Set the display name [Component] of the item.
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
     * Append a single line [String] to the lore.
     */
    operator fun String.unaryPlus() {
        newLore += deserialize(this)
    }

    /**
     * Append a single line [Component] to the lore.
     */
    operator fun Component.unaryPlus() {
        newLore += this
    }

    /**
     * Append multiple line [String]s to the lore.
     */
    fun lore(vararg lines: String) {
        lore(lines.toList())
    }

    /**
     * Append multiple line [String]s to the lore.
     */
    @JvmName("loreStr")
    fun lore(lines: List<String>) {
        lore(lines.map { deserialize(it) })
    }

    /**
     * Append multiple line [Component]s to the lore.
     */
    fun lore(vararg lines: Component) {
        lore(lines.toList())
    }

    /**
     * Append multiple line [Component]s to the lore.
     */
    @JvmName("loreComponent")
    fun lore(lines: List<Component>) {
        newLore.addAll(lines)
    }

    /**
     * Clear the existing lore and set new line [String]s.
     */
    fun setLore(vararg lines: String) {
        setLore(lines.toList())
    }

    /**
     * Clear the existing lore and set new line [String]s.
     */
    @JvmName("setLoreStr")
    fun setLore(lines: List<String>) {
        clearLore = true
        newLore.clear()
        newLore.addAll(lines.map { deserialize(it) })
    }

    /**
     * Clear the existing lore and set new line [Component]s.
     */
    fun setLore(vararg lines: Component) {
        setLore(lines.toList())
    }

    /**
     * Clear the existing lore and set new line [Component]s.
     */
    @JvmName("setLoreComponent")
    fun setLore(lines: List<Component>) {
        clearLore = true
        newLore.clear()
        newLore.addAll(lines)
    }

    /**
     * Edit [ItemMeta].
     */
    fun meta(block: ItemMeta.() -> Unit) {
        editMeta = block
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

            // apply additional meta update
            editMeta?.let { it() }
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
