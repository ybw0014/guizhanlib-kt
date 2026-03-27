package net.guizhanss.guizhanlib.kt.rebar.items.register

import io.github.pylonmc.rebar.item.RebarItem
import io.github.pylonmc.rebar.item.builder.ItemStackBuilder
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

/**
 * DSL class to construct and register a [RebarItem] as a weapon.
 */
open class RebarWeaponBuilder(registry: RebarItemRegistry) : RebarItemBuilder(registry) {

    var hasDurability: Boolean = false
    var hasKnockback: Boolean = false
    var disablesShield: Boolean = false

    /**
     * Builds the ItemStack using [ItemStackBuilder.rebarWeapon] and registers the item.
     */
    override fun <T : RebarItem> build(
        clazz: KClass<T>,
        register: ((ItemStack) -> Unit)?,
    ): ItemStack {
        val builder = when {
            stack != null -> ItemStackBuilder.rebarWeapon(
                stack!!,
                key,
                hasDurability,
                hasKnockback,
                disablesShield,
            )

            material != null -> ItemStackBuilder.rebarWeapon(
                material!!,
                key,
                hasDurability,
                hasKnockback,
                disablesShield,
            )

            else -> error("Either material or stack must be set")
        }

        builderConfig?.let { config ->
            builder.apply(config)
        }

        val builtStack = builder.build()

        register?.invoke(builtStack)
            ?: RebarItem.register(clazz.java, builtStack, null)

        postRegister?.invoke(builtStack)
        return builtStack
    }
}
