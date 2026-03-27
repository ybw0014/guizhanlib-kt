@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.minecraft.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender

fun interface KommandHelpFormatter {
    fun send(sender: CommandSender, label: String, commands: List<Kommand>)
}

internal object DefaultKommandHelpFormatter : KommandHelpFormatter {
    override fun send(sender: CommandSender, label: String, commands: List<Kommand>) {
        commands.forEach { command ->
            sender.sendMessage(command.defaultHelpLine(label))
        }
    }
}

internal fun Kommand.defaultHelpLine(label: String): ComponentLike = Component.text(fullUsage(label), NamedTextColor.YELLOW)
    .append(Component.text(" - ", NamedTextColor.WHITE))
    .append(description.asComponent())
