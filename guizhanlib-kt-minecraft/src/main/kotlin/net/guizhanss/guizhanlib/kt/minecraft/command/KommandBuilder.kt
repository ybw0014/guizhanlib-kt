@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.minecraft.command

import net.guizhanss.guizhanlib.kt.common.utils.RequiredProperty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

private val defaultPermissionMessage = Component.text("You do not have permission to use this command.")
private val defaultPlayerOnlyMessage = Component.text("This command can only be used by players.")
private val defaultConsoleOnlyMessage = Component.text("This command can only be used from console.")
private val defaultCooldownMessage = Component.text("Please wait before using this command again.")

@DslMarker
internal annotation class KommandDsl

abstract class KommandNodeBuilder {
    var description: ComponentLike = Component.empty()
    var usage: String = ""
    var permission: String? = null
    var permissionMessage: ComponentLike = defaultPermissionMessage
    var senderType: SenderType = SenderType.ANY
    var senderTypeMessage: ComponentLike = Component.empty()
    var cooldown: Long = 0
    var cooldownMessage: ComponentLike = Component.empty()
    var inheritParentPermission: Boolean = false

    protected var executor: KommandExecutor? = null
    protected var tabCompleter: KommandTabCompleter? = null
    protected var helpFormatter: KommandHelpFormatter? = null
    protected val subCommandBuilders = mutableListOf<SubKommandBuilder>()

    fun description(value: String) {
        description = value.asLegacyComponent()
    }

    fun descriptionTranslatable(key: String, vararg args: Any) {
        description = Component.translatable(key, *args.map { Component.text(it.toString()) }.toTypedArray())
    }

    fun permissionMessage(value: String) {
        permissionMessage = value.asLegacyComponent()
    }

    fun playerOnly() {
        senderType = SenderType.PLAYER_ONLY
    }

    fun consoleOnly() {
        senderType = SenderType.CONSOLE_ONLY
    }

    fun cooldown(seconds: Long) {
        cooldown = seconds * 1000
    }

    fun cooldownMs(milliseconds: Long) {
        cooldown = milliseconds
    }

    fun cooldownMessage(value: String) {
        cooldownMessage = value.asLegacyComponent()
    }

    fun execute(block: KommandContext.() -> Unit) {
        executor = KommandExecutor(block)
    }

    fun execute(handler: KommandExecutor) {
        executor = handler
    }

    fun execute(block: (sender: CommandSender, args: Array<String>) -> Unit) {
        executor = KommandExecutor { block(it.sender, it.argsArray) }
    }

    fun tabComplete(block: KommandContext.() -> Iterable<String>) {
        tabCompleter = KommandTabCompleter(block)
    }

    fun tabComplete(block: (sender: CommandSender, args: Array<String>) -> Iterable<String>) {
        tabCompleter = KommandTabCompleter { block(it.sender, it.argsArray) }
    }

    fun tab(block: TabBuilder.() -> Unit) {
        val builder = TabBuilder()
        builder.apply(block)
        tabCompleter = builder.build()
    }

    fun helpFormat(block: (sender: CommandSender, label: String, commands: List<Kommand>) -> Unit) {
        helpFormatter = KommandHelpFormatter(block)
    }

    fun customHelp(block: (sender: CommandSender, label: String, commands: List<Kommand>) -> Unit) {
        helpFormat(block)
    }
}

class BaseKommandBuilder internal constructor(
    private val plugin: JavaPlugin,
    private val commandName: String,
) : KommandNodeBuilder() {

    var aliases: List<String> = emptyList()

    fun subCommand(name: String, block: SubKommandBuilder.() -> Unit) {
        val builder = SubKommandBuilder()
        builder.name = name
        builder.apply(block)
        subCommandBuilders += builder
    }

    internal fun build(): BaseKommand {
        val command = resolvePluginCommand(plugin, commandName, aliases)
        val resolvedAliases = if (aliases.isEmpty()) command.aliases else aliases
        val root = BaseKommand(
            command = command,
            aliases = resolvedAliases,
            description = description,
            usage = usage,
            permission = permission,
            permissionMessage = permissionMessage,
            executor = executor,
            tabCompleter = tabCompleter,
            customHelpFormatter = helpFormatter,
            senderType = senderType,
            senderTypeMessage = senderTypeMessage,
            cooldown = cooldown,
            cooldownMessage = cooldownMessage,
        )

        root.subCommands = subCommandBuilders
            .map { it.build(root, permission) }
            .sortedBy(SubKommand::name)

        return root
    }
}

class SubKommandBuilder internal constructor() : KommandNodeBuilder() {

    var name: String by RequiredProperty()

    fun subCommand(name: String, block: SubKommandBuilder.() -> Unit) {
        val builder = SubKommandBuilder()
        builder.name = name
        builder.apply(block)
        subCommandBuilders += builder
    }

    internal fun build(parent: Kommand, parentPermission: String?): SubKommand {
        val effectivePermission = if (inheritParentPermission && parentPermission != null) {
            "$parentPermission.$name"
        } else {
            permission
        }

        val current = SubKommand(
            parent = parent,
            name = name,
            description = description,
            usage = usage,
            permission = effectivePermission,
            permissionMessage = permissionMessage,
            executor = executor,
            tabCompleter = tabCompleter,
            customHelpFormatter = helpFormatter,
            senderType = senderType,
            senderTypeMessage = senderTypeMessage,
            cooldown = cooldown,
            cooldownMessage = cooldownMessage,
        )

        current.subCommands = subCommandBuilders
            .map { it.build(current, effectivePermission) }
            .sortedBy(SubKommand::name)

        return current
    }
}

fun baseCommand(plugin: JavaPlugin, name: String, block: BaseKommandBuilder.() -> Unit): BaseKommand = BaseKommandBuilder(plugin, name)
    .apply(block)
    .build()
