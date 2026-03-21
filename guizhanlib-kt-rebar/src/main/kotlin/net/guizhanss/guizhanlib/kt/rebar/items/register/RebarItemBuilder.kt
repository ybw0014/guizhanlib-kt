@file:Suppress("unused")
package net.guizhanss.guizhanlib.kt.rebar.items.register

import io.github.pylonmc.rebar.block.RebarBlock
import io.github.pylonmc.rebar.item.RebarItem
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import net.guizhanss.guizhanlib.kt.common.utils.RequiredProperty
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf

/**
 * DSL class to construct and register a [RebarItem].
 */
open class RebarItemBuilder(protected val registry: RebarItemRegistry) {

    var key: NamespacedKey by RequiredProperty()
    var material: Material? = null
    var stack: ItemStack? = null

    private var builderConfig: (ItemStackBuilder.() -> Unit)? = null
    private var postRegister: ((ItemStack) -> Unit)? = null

    /**
     * Configures the [ItemStackBuilder] with full access to all its methods.
     * This is applied after creating the builder.
     */
    fun builder(block: ItemStackBuilder.() -> Unit) {
        this.builderConfig = block
    }

    /**
     * Sets a callback to be invoked after the item is registered.
     * The callback receives the built ItemStack.
     */
    fun postRegister(block: (ItemStack) -> Unit) {
        this.postRegister = block
    }

    /**
     * Builds the ItemStack and registers the item.
     */
    fun <T : RebarItem> build(
        clazz: KClass<T>,
        register: ((ItemStack) -> Unit)? = null
    ): ItemStack {
        val builder = when {
            stack != null -> ItemStackBuilder.rebar(stack!!, key)
            material != null -> ItemStackBuilder.rebar(material!!, key)
            else -> error("Either material or stack must be set")
        }

        builderConfig?.let { config ->
            builder.apply(config)
        }

        val builtStack = builder.build()

        // Use custom register if provided, otherwise default
        register?.invoke(builtStack)
            ?: RebarItem.register(clazz.java, builtStack, null)

        postRegister?.invoke(builtStack)
        return builtStack
    }
}

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
    crossinline builder: RebarItemBuilder.() -> Unit
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
    crossinline builder: RebarItemBuilder.() -> Unit
) = PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, ItemStack>> { _, _ ->
    val itemBuilder = RebarItemBuilder(this)
    itemBuilder.apply(builder)

    val key = itemBuilder.key
    val material = itemBuilder.material ?: error("material is required for block registration")

    RebarBlock.register(key, material, B::class.java)

    val itemClass = B::class.nestedClasses
        .firstOrNull { it.isSubclassOf(RebarItem::class) && it.simpleName == "Item" }
        ?.let { it as KClass<out RebarItem> }
        ?: error("${B::class.simpleName} must have a nested RebarItem class named 'Item'")

    val stack = itemBuilder.build(itemClass) { builtStack ->
        RebarItem.register(itemClass.java, builtStack, key)
    }

    B::class.companionObjectInstance
        ?.takeIf { it is Listener }
        ?.let { Bukkit.getPluginManager().registerEvents(it as Listener, addon.javaPlugin) }

    ReadOnlyProperty { _, _ -> stack }
}
