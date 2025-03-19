package net.guizhanss.guizhanlib.kt.slimefun.items.builder

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.utils.HeadTexture
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils
import org.bukkit.Material as BukkitMaterial
import org.bukkit.inventory.ItemStack as BukkitItemStack

/**
 * Represents the different types of material a [SlimefunItemStack] can have.
 *
 * Modified from [sf4k by Seggan](https://github.com/Seggan/sf4k/blob/master/src/main/kotlin/io/github/seggan/sf4k/item/builder/MaterialType.kt).
 */
sealed interface MaterialType {

    /**
     * Converts this [MaterialType] into an [BukkitItemStack]
     */
    fun convert(): BukkitItemStack

    /**
     * A simple [BukkitMaterial]
     */
    class Material(private val material: BukkitMaterial) : MaterialType {

        override fun convert() = BukkitItemStack(material)
    }

    /**
     * A full on [BukkitItemStack]
     */
    class ItemStack(private val itemStack: BukkitItemStack) : MaterialType {

        override fun convert() = itemStack
    }

    /**
     * A player head.
     */
    class Head(private val texture: String) : MaterialType {

        override fun convert() = SlimefunUtils.getCustomHead(texture)
    }
}

/**
 * Converts this [BukkitMaterial] into a [MaterialType].
 */
fun BukkitMaterial.asMaterialType() = MaterialType.Material(this)

/**
 * Converts this [BukkitItemStack] into a [MaterialType].
 */
fun BukkitItemStack.asMaterialType() = MaterialType.ItemStack(this)

/**
 * Converts this head texture [String] into a [MaterialType].
 */
fun String.asMaterialType(): MaterialType = MaterialType.Head(this)

/**
 * Converts Slimefun's [HeadTexture] into a [MaterialType].
 */
fun HeadTexture.asMaterialType(): MaterialType = MaterialType.Head(this.texture)
