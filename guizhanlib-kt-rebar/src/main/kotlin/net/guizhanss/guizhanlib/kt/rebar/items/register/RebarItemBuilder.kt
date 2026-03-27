@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.rebar.items.register

import io.github.pylonmc.rebar.item.RebarItem
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import net.guizhanss.guizhanlib.kt.common.utils.RequiredProperty
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

/**
 * DSL class to construct and register a [RebarItem].
 */
open class RebarItemBuilder(protected val registry: RebarItemRegistry) {

    var key: NamespacedKey by RequiredProperty()
    var material: Material? = null
    var stack: ItemStack? = null

    protected var builderConfig: (ItemStackBuilder.() -> Unit)? = null
    protected var postRegister: ((ItemStack) -> Unit)? = null

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
    open fun <T : RebarItem> build(
        clazz: KClass<T>,
        register: ((ItemStack) -> Unit)? = null,
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
