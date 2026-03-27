@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.rebar.items.register

import io.github.pylonmc.rebar.block.RebarBlock
import net.guizhanss.guizhanlib.kt.common.utils.RequiredProperty
import org.bukkit.Material
import org.bukkit.NamespacedKey
import kotlin.reflect.KClass

/**
 * DSL class to construct and register a [RebarBlock] without an associated item.
 * This is used for blocks that only exist as block types without corresponding item types.
 */
open class RebarBlockBuilder(protected val registry: RebarItemRegistry) {

    var key: NamespacedKey by RequiredProperty()
    var material: Material by RequiredProperty()

    /**
     * Builds and registers the block.
     *
     * @param blockClass The block class to register
     */
    open fun <B : RebarBlock> build(blockClass: KClass<B>) {
        RebarBlock.register(key, material, blockClass.java)
    }
}
