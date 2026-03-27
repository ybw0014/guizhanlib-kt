package net.guizhanss.guizhanlib.kt.rebar.items.register

import io.github.pylonmc.rebar.addon.RebarAddon
import io.github.pylonmc.rebar.block.RebarBlock
import io.github.pylonmc.rebar.item.RebarItem
import org.bukkit.event.Listener

/**
 * A simple registry class holding the registration of rebar items.
 *
 * By calling the DSL registering functions:
 * - [item]: automatically registers the associated [RebarItem].
 * - [block]: automatically registers the associated [RebarBlock]. the nested [RebarItem] class named `Item` is also registered.
 * - [blockOnly]: automatically registers only the [RebarBlock] without an associated item.
 * - [weapon]: automatically registers the associated [RebarItem] but with weapon settings.
 *
 * If their companion objects are [Listener], they will be automatically registered.
 */
abstract class RebarItemRegistry(
    val addon: RebarAddon,
)
