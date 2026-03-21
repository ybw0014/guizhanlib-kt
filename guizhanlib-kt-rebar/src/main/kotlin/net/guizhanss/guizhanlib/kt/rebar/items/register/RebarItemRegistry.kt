package net.guizhanss.guizhanlib.kt.rebar.items.register

import io.github.pylonmc.rebar.addon.RebarAddon

/**
 * A simple registry class holding the registration of rebar items.
 *
 * By calling the DSL registering functions:
 * - [item]: automatically registers the associated [io.github.pylonmc.rebar.item.RebarItem].
 * - [block]: automatically registers the associated [io.github.pylonmc.rebar.block.RebarBlock]. the nested [io.github.pylonmc.rebar.item.RebarItem] class named `Item` is also registered.
 *
 * If their companion objects are [org.bukkit.event.Listener], they will be automatically registered.
 */
abstract class RebarItemRegistry(
    val addon: RebarAddon,
)
