@file:Suppress("unused", "deprecation")

package net.guizhanss.guizhanlib.kt.minecraft.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

private val legacySectionSerializer = LegacyComponentSerializer.legacySection()

fun String.asLegacyComponent(): Component = legacySectionSerializer.deserialize(ChatColor.translateAlternateColorCodes('&', this))

fun CommandSender.sendMessage(message: ComponentLike) {
    sendMessage(message.asComponent())
}
