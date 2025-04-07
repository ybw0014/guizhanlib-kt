package net.guizhanss.guizhanlib.kt.minecraft.extensions

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.ItemMock
import org.mockbukkit.mockbukkit.world.WorldMock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.collections.filterIsInstance

class ItemStackExtTest {

    private lateinit var server: ServerMock
    private lateinit var world: WorldMock

    @BeforeTest
    fun beforeTest() {
        server = MockBukkit.mock()
        world = server.addSimpleWorld("world")
    }

    @AfterTest
    fun afterTest() {
        MockBukkit.unmock()
    }

    @Test
    fun testToItem() {
        val material = Material.DIAMOND
        val amount = 5
        val itemStack = material.toItem(amount)

        assertEquals(material, itemStack.type)
        assertEquals(amount, itemStack.amount)
    }

    @Test
    fun testIsAir() {
        // null
        var itemStack: ItemStack? = null
        assertTrue(itemStack.isAir())

        // air
        itemStack = ItemStack(Material.AIR)
        assertTrue(itemStack.isAir())

        // normal item
        itemStack = ItemStack(Material.DIAMOND)
        assertFalse(itemStack.isAir())
    }

    @Test
    fun testDrop() {
        val location = world.spawnLocation
        val itemStack = ItemStack(Material.DIAMOND, 1)

        // drop 70 items, expect 64 + 6
        itemStack.drop(location, 70)

        val droppedItems = world.entities
            .filterIsInstance<ItemMock>()
            .map { it.itemStack.amount }
            .sorted()

        assertEquals(2, droppedItems.size)
        assertEquals(listOf(6, 64), droppedItems)
    }
}
