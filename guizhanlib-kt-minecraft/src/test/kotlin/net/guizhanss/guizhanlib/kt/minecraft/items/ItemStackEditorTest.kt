package net.guizhanss.guizhanlib.kt.minecraft.items

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.mockbukkit.mockbukkit.MockBukkit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ItemStackEditorTest {

    @BeforeTest
    fun beforeTest() {
        MockBukkit.mock()
    }

    @AfterTest
    fun afterTest() {
        MockBukkit.unmock()
    }

    @Test
    fun testAmount() {
        val itemStack = ItemStack(Material.DIAMOND_SWORD)
        val editedItemStack = itemStack.edit {
            amount(5)
        }
        assertEquals(5, editedItemStack.amount)
    }

    @Test
    fun testName() {
        val itemStack = ItemStack(Material.DIAMOND_SWORD)
        val componentName = Component.text("Epic Sword")
        val editedItemStack = itemStack.edit {
            name(componentName)
        }
        assertEquals(componentName, editedItemStack.itemMeta?.displayName())
    }

    @Test
    fun testLore() {
        val itemStack = ItemStack(Material.DIAMOND_SWORD)
        val loreLine = "This is a legendary sword"
        val editedItemStack = itemStack.edit {
            +loreLine
        }
        assertEquals(Component.text(loreLine), editedItemStack.itemMeta?.lore()?.first())
    }

    @Test
    fun testSetLore() {
        val itemStack = ItemStack(Material.DIAMOND_SWORD).apply {
            val meta = itemMeta!!
            meta.lore(listOf(Component.text("Old Lore")))
            itemMeta = meta
        }

        val loreLines = listOf("Line 1", "Line 2")
        val editedItemStack = itemStack.edit {
            setLore(loreLines)
        }
        assertEquals(loreLines.size, editedItemStack.itemMeta?.lore()?.size)
        assertEquals(Component.text(loreLines[0]), editedItemStack.itemMeta?.lore()?.get(0))
        assertEquals(Component.text(loreLines[1]), editedItemStack.itemMeta?.lore()?.get(1))
    }
}
