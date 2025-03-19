package net.guizhanss.guizhanlib.kt.slimefun.items.builder

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon

/**
 * A class that allows the use of item builder DSL.
 *
 * Modified from [sf4k by Seggan](https://github.com/Seggan/sf4k/blob/master/src/main/kotlin/io/github/seggan/sf4k/item/builder/ItemRegistry.kt).
 */
abstract class ItemRegistry(
    val addon: SlimefunAddon,
    val prefix: String = addon.javaPlugin.name.uppercase()
)
