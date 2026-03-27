@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.rebar.items.register

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.pylonmc.rebar.item.RebarItem
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf

/**
 * DSL function to register a RebarItem, returning the ItemStack.
 *
 * Usage:
 * ```kotlin
 * class MyRegistry(addon: RebarAddon) : ItemRegistry(addon) {
 *     val MY_ITEM by item<MyItem> {
 *         key = addon.key("my_item")
 *         material = Material.DIAMOND
 *         // or
 *         stack = ItemStack(Material.DIAMOND)
 *
 *         // modify the full builder
 *         builder {
 *             set(DataComponentTypes.MAX_STACK_SIZE, 1)
 *         }
 *
 *         // Do something after registration
 *         postRegister { stack ->
 *         }
 *     }
 * }
 * ```
 */
inline fun <reified I : RebarItem> RebarItemRegistry.item(
    crossinline builder: RebarItemBuilder.() -> Unit,
) = PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, ItemStack>> { _, _ ->
    val itemBuilder = RebarItemBuilder(this)
    itemBuilder.apply(builder)
    val stack = itemBuilder.build(I::class)

    I::class.companionObjectInstance
        ?.takeIf { it is Listener }
        ?.let { Bukkit.getPluginManager().registerEvents(it as Listener, addon.javaPlugin) }

    ReadOnlyProperty { _, _ -> stack }
}

/**
 * DSL function to register a [RebarBlock] without an associated item.
 *
 * This is used for blocks that only exist as block types without corresponding item types
 * (e.g., technical blocks, unbreakable blocks that shouldn't be obtainable as items).
 *
 * Usage:
 * ```kotlin
 * class MyRegistry(addon: RebarAddon) : ItemRegistry(addon) {
 *     val TECHNICAL_BLOCK by blockOnly<TechnicalBlock> {
 *         key = addon.key("technical_block")
 *         material = Material.STONE
 *         // or
 *         stack = ItemStack(Material.STONE)
 *     }
 * }
 * ```
 */
inline fun <reified B : RebarBlock> RebarItemRegistry.blockOnly(
    crossinline builder: RebarBlockBuilder.() -> Unit,
) = PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, NamespacedKey>> { _, _ ->
    val blockBuilder = RebarBlockBuilder(this)
    blockBuilder.apply(builder)
    blockBuilder.build(B::class)

    B::class.companionObjectInstance
        ?.takeIf { it is Listener }
        ?.let { Bukkit.getPluginManager().registerEvents(it as Listener, addon.javaPlugin) }

    ReadOnlyProperty { _, _ -> blockBuilder.key }
}

/**
 * DSL function to register a RebarBlock along with its associated RebarItem.
 *
 * The block class must have a nested `Item` class that extends `RebarItem`.
 *
 * Usage:
 * ```kotlin
 * class MyRegistry(addon: RebarAddon) : ItemRegistry(addon) {
 *     val MY_BLOCK by block<MyBlock> {
 *         key = addon.key("my_block")
 *         material = Material.AMETHYST_BLOCK
 *         // or
 *         stack = ItemStack(Material.AMETHYST_BLOCK)
 *
 *         // modify the item stack builder
 *         builder {
 *             set(DataComponentTypes.MAX_STACK_SIZE, 1)
 *         }
 *
 *         // Do something after registration
 *         postRegister { stack ->
 *         }
 *     }
 * }
 *
 * // MyBlock must have a nested Item class:
 * // class MyBlock(...) : RebarBlock(...) {
 * //     class Item : RebarItem { ... }
 * // }
 * ```
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified B : RebarBlock> RebarItemRegistry.block(
    crossinline builder: RebarItemBuilder.() -> Unit,
) = PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, ItemStack>> { _, _ ->
    val itemBuilder = RebarItemBuilder(this)
    itemBuilder.apply(builder)

    // Use BlockOnlyBuilder logic to register the block with resolved material
    val blockBuilder = RebarBlockBuilder(this).apply {
        key = itemBuilder.key
        material = itemBuilder.material ?: error("Material is not set")
    }
    blockBuilder.build(B::class)

    val itemClass = B::class.nestedClasses
        .firstOrNull { it.isSubclassOf(RebarItem::class) && it.simpleName == "Item" }
        ?.let { it as KClass<out RebarItem> }
        ?: error("${B::class.simpleName} must have a nested RebarItem class named 'Item'")

    val stack = itemBuilder.build(itemClass) { builtStack ->
        RebarItem.register(itemClass.java, builtStack, itemBuilder.key)
    }

    B::class.companionObjectInstance
        ?.takeIf { it is Listener }
        ?.let { Bukkit.getPluginManager().registerEvents(it as Listener, addon.javaPlugin) }

    ReadOnlyProperty { _, _ -> stack }
}

/**
 * DSL function to register a RebarItem as a weapon, returning the ItemStack.
 *
 * This uses [ItemStackBuilder.rebarWeapon] to create the item with weapon attributes
 * like attack damage, attack speed, and durability.
 *
 * Usage:
 * ```kotlin
 * class MyRegistry(addon: RebarAddon) : ItemRegistry(addon) {
 *     val MY_WEAPON by weapon<MyWeapon> {
 *         key = addon.key("my_weapon")
 *         material = Material.DIAMOND_SWORD
 *
 *         // Weapon-specific settings
 *         hasDurability = true
 *         hasKnockback = true
 *         disablesShield = true
 *
 *         // modify the full builder
 *         builder {
 *             set(DataComponentTypes.MAX_STACK_SIZE, 1)
 *         }
 *
 *         // Do something after registration
 *         postRegister { stack ->
 *         }
 *     }
 * }
 * ```
 */
inline fun <reified I : RebarItem> RebarItemRegistry.weapon(
    crossinline builder: RebarWeaponBuilder.() -> Unit,
) = PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, ItemStack>> { _, _ ->
    val weaponBuilder = RebarWeaponBuilder(this)
    weaponBuilder.apply(builder)
    val stack = weaponBuilder.build(I::class)

    I::class.companionObjectInstance
        ?.takeIf { it is Listener }
        ?.let { Bukkit.getPluginManager().registerEvents(it as Listener, addon.javaPlugin) }

    ReadOnlyProperty { _, _ -> stack }
}
