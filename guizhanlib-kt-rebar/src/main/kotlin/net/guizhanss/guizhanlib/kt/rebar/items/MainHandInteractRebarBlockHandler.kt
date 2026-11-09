package net.guizhanss.guizhanlib.kt.rebar.items

import io.github.pylonmc.rebar.block.interfaces.InteractRebarBlockHandler
import io.github.pylonmc.rebar.event.api.annotation.MultiHandler
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * A [InteractRebarBlockHandler] which the interaction always happen with main hand.
 */
interface MainHandInteractRebarBlockHandler : InteractRebarBlockHandler {

    /**
     * Override [onMainHandInteractedWith] instead.
     */
    @MultiHandler([EventPriority.NORMAL, EventPriority.MONITOR])
    override fun onInteractedWith(
        event: PlayerInteractEvent,
        priority: EventPriority,
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

        onMainHandInteractedWith(event)
    }

    fun onMainHandInteractedWith(event: PlayerInteractEvent)
}
