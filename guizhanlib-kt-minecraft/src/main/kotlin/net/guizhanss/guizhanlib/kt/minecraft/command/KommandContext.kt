@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.minecraft.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

data class KommandContext(
    val kommand: Kommand,
    val sender: CommandSender,
    val command: Command,
    val label: String,
    val args: List<String>
) {

    val argsArray: Array<String>
        get() = args.toTypedArray()

    val size: Int
        get() = args.size

    operator fun get(index: Int): String = args[index]

    fun argOrNull(index: Int): String? = args.getOrNull(index)

    internal fun child(childKommand: Kommand, childArgs: List<String>): KommandContext {
        return copy(kommand = childKommand, args = childArgs)
    }
}
