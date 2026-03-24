package net.guizhanss.guizhanlib.kt.rebar.items

import io.github.pylonmc.rebar.block.base.RebarInteractBlock
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * A [RebarInteractBlock] which the interaction always happen with main hand.
 */
interface RebarMainHandInteractBlock : RebarInteractBlock {

    /**
     * Override [onMainHandInteract] instead.
     */
    @MultiHandler([EventPriority.NORMAL, EventPriority.MONITOR])
    override fun onInteract(
        event: PlayerInteractEvent,
        priority: EventPriority
    ) {
        if (event.action != Action.RIGHT_CLICK_BLOCK || event.useInteractedBlock() == Event.Result.DENY ||
            event.hand != EquipmentSlot.HAND
        ) {
            return
        }

        if (priority == EventPriority.NORMAL) {
            event.setUseItemInHand(Event.Result.DENY)
            return
        } else {
            event.setUseInteractedBlock(Event.Result.DENY)
        }

        onMainHandInteract(event)
    }

    fun onMainHandInteract(event: PlayerInteractEvent)
}
