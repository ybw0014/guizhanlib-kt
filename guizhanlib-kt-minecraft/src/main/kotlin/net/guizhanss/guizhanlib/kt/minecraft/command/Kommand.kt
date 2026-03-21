@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.minecraft.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Field
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val usageArgPattern = Regex("<[^>]*>|\\[[^]]*]|\\S+")

/**
 * Sender type restriction for command execution.
 */
enum class SenderType {
    ANY,
    PLAYER_ONLY,
    CONSOLE_ONLY
}

/**
 * Cooldown manager for command cooldowns.
 */
internal object KommandCooldowns {
    private val cooldowns = ConcurrentHashMap<UUID, MutableMap<String, Long>>()

    fun isOnCooldown(playerId: UUID, commandKey: String, cooldownMs: Long): Boolean {
        val now = System.currentTimeMillis()
        val playerCooldowns = cooldowns.getOrPut(playerId) { mutableMapOf() }
        val lastUse = playerCooldowns[commandKey] ?: 0L
        return now - lastUse < cooldownMs
    }

    fun setCooldown(playerId: UUID, commandKey: String) {
        cooldowns.getOrPut(playerId) { mutableMapOf() }[commandKey] = System.currentTimeMillis()
    }

    fun getRemainingCooldown(playerId: UUID, commandKey: String, cooldownMs: Long): Long {
        val now = System.currentTimeMillis()
        val playerCooldowns = cooldowns[playerId] ?: return 0L
        val lastUse = playerCooldowns[commandKey] ?: return 0L
        val remaining = cooldownMs - (now - lastUse)
        return maxOf(0L, remaining)
    }
}

sealed class Kommand(
    open val parent: Kommand?,
    val name: String,
    val description: ComponentLike,
    val usage: String,
    val permission: String?,
    val permissionMessage: ComponentLike,
    internal val executor: KommandExecutor?,
    internal val tabCompleter: KommandTabCompleter?,
    internal val customHelpFormatter: KommandHelpFormatter?,
    val senderType: SenderType,
    val senderTypeMessage: ComponentLike,
    val cooldown: Long,
    val cooldownMessage: ComponentLike
) {

    internal var subCommands: List<SubKommand> = emptyList()

    val hasParent: Boolean
        get() = parent != null

    val hasSubCommands: Boolean
        get() = subCommands.isNotEmpty()

    val fullPermission: String?
        get() {
            val p = parent ?: return permission
            val parentPerm = p.fullPermission ?: return permission
            return if (p.permission != null) {
                "$parentPerm.$name"
            } else {
                permission
            }
        }

    fun hasPermission(sender: CommandSender): Boolean {
        return permission == null || sender.hasPermission(permission)
    }

    fun hasFullPermission(sender: CommandSender): Boolean {
        val perm = fullPermission ?: return true
        return sender.hasPermission(perm)
    }

    private fun checkSenderType(sender: CommandSender): Boolean {
        return when (senderType) {
            SenderType.ANY -> true
            SenderType.PLAYER_ONLY -> sender is Player
            SenderType.CONSOLE_ONLY -> sender !is Player
        }
    }

    private fun checkCooldown(sender: CommandSender): Boolean {
        if (cooldown <= 0) return true
        if (sender !is Player) return true
        
        val commandKey = fullPath()
        return !KommandCooldowns.isOnCooldown(sender.uniqueId, commandKey, cooldown)
    }

    fun fullPath(): String {
        val path = generateSequence(this) { it.parent }
            .toList()
            .asReversed()
            .joinToString(".") { it.name }
        return path
    }

    fun fullUsage(label: String): String {
        val path = generateSequence(this) { it.parent }
            .toList()
            .asReversed()
            .joinToString(" ") { if (it is BaseKommand) label else it.name }

        return if (usage.isBlank()) {
            "/$path"
        } else {
            "/$path $usage"
        }
    }

    fun helpEntries(sender: CommandSender): List<Kommand> {
        if (!hasPermission(sender)) {
            return emptyList()
        }

        val children = subCommands.flatMap { it.helpEntries(sender) }
        return buildList {
            if (executor != null || (!hasSubCommands && children.isEmpty())) {
                add(this@Kommand)
            }
            addAll(children)
        }
    }

    internal fun execute(context: KommandContext) {
        val sender = context.sender

        if (!checkSenderType(sender)) {
            val message = when (senderType) {
                SenderType.PLAYER_ONLY -> {
                    if (senderTypeMessage.asComponent() == Component.empty()) {
                        Component.text("This command can only be used by players.", NamedTextColor.RED)
                    } else {
                        senderTypeMessage.asComponent()
                    }
                }
                SenderType.CONSOLE_ONLY -> {
                    if (senderTypeMessage.asComponent() == Component.empty()) {
                        Component.text("This command can only be used from console.", NamedTextColor.RED)
                    } else {
                        senderTypeMessage.asComponent()
                    }
                }
                SenderType.ANY -> senderTypeMessage.asComponent()
            }
            sender.sendMessage(message)
            return
        }

        if (!hasPermission(sender)) {
            sender.sendMessage(permissionMessage)
            return
        }

        if (!checkCooldown(sender)) {
            val remaining = if (sender is Player) {
                KommandCooldowns.getRemainingCooldown(sender.uniqueId, fullPath(), cooldown)
            } else {
                0L
            }
            val message = if (cooldownMessage.asComponent() == Component.empty()) {
                Component.text("Please wait ${remaining / 1000}s before using this command again.", NamedTextColor.RED)
            } else {
                cooldownMessage.asComponent()
            }
            sender.sendMessage(message)
            return
        }

        findSubCommand(context.args.firstOrNull())?.let { subCommand ->
            subCommand.execute(context.child(subCommand, context.args.drop(1)))
            return
        }

        if (executor != null && usage.isValid(context.args)) {
            if (sender is Player && cooldown > 0) {
                KommandCooldowns.setCooldown(sender.uniqueId, fullPath())
            }
            executor.execute(context)
            return
        }

        sendHelp(context.sender, context.label)
    }

    internal fun complete(context: KommandContext): List<String> {
        if (!hasPermission(context.sender)) {
            return emptyList()
        }

        val child = findSubCommand(context.args.firstOrNull())
        if (child != null && context.args.isNotEmpty()) {
            return child.complete(context.child(child, context.args.drop(1)))
        }

        val childSuggestions = if (context.args.size <= 1) {
            visibleSubCommands(context.sender)
                .map(SubKommand::name)
                .filter { suggestion ->
                    val input = context.args.firstOrNull().orEmpty()
                    suggestion.startsWith(input, ignoreCase = true)
                }
        } else {
            emptyList()
        }

        val executorSuggestions = tabCompleter?.complete(context)
            ?.filter { suggestion ->
                val input = context.args.lastOrNull().orEmpty()
                suggestion.startsWith(input, ignoreCase = true)
            }
            .orEmpty()

        return (childSuggestions + executorSuggestions).distinct()
    }

    fun sendHelp(sender: CommandSender, label: String) {
        val commands = helpEntries(sender)
        if (commands.isEmpty()) {
            sender.sendMessage(Component.text("No commands available.", NamedTextColor.RED))
            return
        }

        resolveHelpFormatter().send(sender, label, commands)
    }

    private fun visibleSubCommands(sender: CommandSender): List<SubKommand> {
        return subCommands.filter { it.hasPermission(sender) }
    }

    private fun findSubCommand(name: String?): SubKommand? {
        return name?.let { requested ->
            subCommands.firstOrNull { it.name.equals(requested, ignoreCase = true) }
        }
    }

    private fun resolveHelpFormatter(): KommandHelpFormatter {
        return generateSequence(this) { it.parent }
            .mapNotNull(Kommand::customHelpFormatter)
            .firstOrNull()
            ?: DefaultKommandHelpFormatter
    }
}

class BaseKommand internal constructor(
    val command: PluginCommand,
    val aliases: List<String>,
    description: ComponentLike,
    usage: String,
    permission: String?,
    permissionMessage: ComponentLike,
    executor: KommandExecutor?,
    tabCompleter: KommandTabCompleter?,
    customHelpFormatter: KommandHelpFormatter?,
    senderType: SenderType,
    senderTypeMessage: ComponentLike,
    cooldown: Long,
    cooldownMessage: ComponentLike
) : Kommand(
    parent = null,
    name = command.name,
    description = description,
    usage = usage,
    permission = permission,
    permissionMessage = permissionMessage,
    executor = executor,
    tabCompleter = tabCompleter,
    customHelpFormatter = customHelpFormatter,
    senderType = senderType,
    senderTypeMessage = senderTypeMessage,
    cooldown = cooldown,
    cooldownMessage = cooldownMessage
), CommandExecutor, TabCompleter {

    init {
        bind()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        execute(KommandContext(this, sender, command, label, args.toList()))
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return complete(KommandContext(this, sender, command, alias, args.toList()))
    }

    private fun bind() {
        command.aliases = aliases
        command.setExecutor(this)
        command.tabCompleter = this
        syncCommandAliases(command, aliases)
    }
}

internal fun resolvePluginCommand(plugin: JavaPlugin, name: String, aliases: List<String>): PluginCommand {
    val existingCommand = plugin.getCommand(name)
    if (existingCommand != null) {
        if (aliases.isNotEmpty()) {
            existingCommand.aliases = aliases
        }
        return existingCommand
    }

    val command = createPluginCommand(plugin, name)
    if (aliases.isNotEmpty()) {
        command.aliases = aliases
    }
    resolveCommandMap().register(plugin.name.lowercase(), command)
    return command
}

class SubKommand internal constructor(
    override val parent: Kommand,
    name: String,
    description: ComponentLike,
    usage: String,
    permission: String?,
    permissionMessage: ComponentLike,
    executor: KommandExecutor?,
    tabCompleter: KommandTabCompleter?,
    customHelpFormatter: KommandHelpFormatter?,
    senderType: SenderType,
    senderTypeMessage: ComponentLike,
    cooldown: Long,
    cooldownMessage: ComponentLike
) : Kommand(
    parent = parent,
    name = name,
    description = description,
    usage = usage,
    permission = permission,
    permissionMessage = permissionMessage,
    executor = executor,
    tabCompleter = tabCompleter,
    customHelpFormatter = customHelpFormatter,
    senderType = senderType,
    senderTypeMessage = senderTypeMessage,
    cooldown = cooldown,
    cooldownMessage = cooldownMessage
)

private fun String.parseUsage(): List<String> {
    return usageArgPattern.findAll(trim()).map { it.value }.toList()
}

private fun String.requiredArgsCount(): Int {
    return parseUsage().count { it.startsWith("<") }
}

private fun String.isValid(args: List<String>): Boolean {
    val usageParts = parseUsage()
    if (args.size < requiredArgsCount()) {
        return false
    }

    return args.size <= usageParts.size
}

private fun createPluginCommand(plugin: JavaPlugin, name: String): PluginCommand {
    val constructor = PluginCommand::class.java.getDeclaredConstructor(String::class.java, Plugin::class.java)
    constructor.isAccessible = true
    return constructor.newInstance(name, plugin)
}

private fun syncCommandAliases(command: PluginCommand, aliases: List<String>) {
    if (aliases.isEmpty()) {
        return
    }

    val knownCommands = resolveKnownCommands(resolveCommandMap()) ?: return
    val normalizedAliases = aliases.map(String::lowercase).toSet()
    val rootKeys = setOf(command.name.lowercase())

    val staleKeys = knownCommands.entries
        .filter { (key, value) ->
            key is String && value === command && key.lowercase() !in rootKeys && key.lowercase() !in normalizedAliases
        }
        .mapNotNull { it.key as? String }

    val removeMethod = knownCommands.javaClass.getMethod("remove", Any::class.java)
    val putMethod = knownCommands.javaClass.getMethod("put", Any::class.java, Any::class.java)

    staleKeys.forEach { key ->
        removeMethod.invoke(knownCommands, key)
    }
    normalizedAliases.forEach { alias ->
        putMethod.invoke(knownCommands, alias, command)
    }
}

private fun resolveCommandMap(): CommandMap {
    findPublicCommandMap()?.let { return it }

    val server = Bukkit.getServer()
    val commandMapField = findCommandMapField(server.javaClass)
        ?: error("Unable to resolve Bukkit command map for dynamic command registration.")

    commandMapField.isAccessible = true
    return when (val value = commandMapField.get(server)) {
        is CommandMap -> value
        else -> error("Bukkit command map field did not contain a CommandMap instance.")
    }
}

private fun findPublicCommandMap(): CommandMap? {
    val bukkitMethod = Bukkit::class.java.methods.firstOrNull {
        it.name == "getCommandMap" && it.parameterCount == 0
    }
    if (bukkitMethod != null) {
        when (val value = bukkitMethod.invoke(null)) {
            is CommandMap -> return value
        }
    }

    val server = Bukkit.getServer()
    val serverMethod = server.javaClass.methods.firstOrNull {
        it.name == "getCommandMap" && it.parameterCount == 0
    }
    if (serverMethod != null) {
        when (val value = serverMethod.invoke(server)) {
            is CommandMap -> return value
        }
    }

    return null
}

private fun findCommandMapField(type: Class<*>): Field? {
    var currentType: Class<*>? = type
    while (currentType != null) {
        val field = currentType.declaredFields.firstOrNull {
            it.name == "commandMap" && CommandMap::class.java.isAssignableFrom(it.type)
        }
        if (field != null) {
            return field
        }
        currentType = currentType.superclass
    }
    return null
}

private fun resolveKnownCommands(commandMap: CommandMap): MutableMap<*, *>? {
    var currentType: Class<*>? = commandMap.javaClass
    while (currentType != null) {
        val field = currentType.declaredFields.firstOrNull {
            it.name == "knownCommands" && MutableMap::class.java.isAssignableFrom(it.type)
        }
        if (field != null) {
            field.isAccessible = true
            return when (val value = field.get(commandMap)) {
                is MutableMap<*, *> -> value
                else -> null
            }
        }
        currentType = currentType.superclass
    }
    return null
}
