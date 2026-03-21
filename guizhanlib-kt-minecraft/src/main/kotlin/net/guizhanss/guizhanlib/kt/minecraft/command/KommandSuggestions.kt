@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.minecraft.command

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.Permissible

fun interface SuggestionsProvider {
    fun suggest(context: KommandContext, currentArg: String): Iterable<String>
}

@KommandDsl
class TabBuilder {
    private val suggestionsByPosition = mutableMapOf<Int, SuggestionsProvider>()

    fun arg(position: Int, provider: SuggestionsProvider) {
        suggestionsByPosition[position] = provider
    }

    fun arg(position: Int, block: (context: KommandContext, currentArg: String) -> Iterable<String>) {
        suggestionsByPosition[position] = SuggestionsProvider(block)
    }

    fun args(vararg positions: Int, provider: SuggestionsProvider) {
        positions.forEach { suggestionsByPosition[it] = provider }
    }

    fun args(vararg positions: Int, block: (context: KommandContext, currentArg: String) -> Iterable<String>) {
        val provider = SuggestionsProvider(block)
        positions.forEach { suggestionsByPosition[it] = provider }
    }

    internal fun build(): KommandTabCompleter {
        return KommandTabCompleter { context ->
            val currentArg = context.args.lastOrNull().orEmpty()
            suggestionsByPosition[context.size - 1]
                ?.suggest(context, currentArg)
                ?.filter { it.startsWith(currentArg, ignoreCase = true) }
                ?.toList()
                ?: emptyList()
        }
    }
}

fun suggestPlayers(): SuggestionsProvider = SuggestionsProvider { _, _ ->
    Bukkit.getOnlinePlayers().map { it.name }
}

fun suggestVisiblePlayers(): SuggestionsProvider = SuggestionsProvider { context, _ ->
    Bukkit.getOnlinePlayers()
        .filter { context.sender.canSeePlayer(it) }
        .map { it.name }
}

fun suggestWorlds(): SuggestionsProvider = SuggestionsProvider { _, _ ->
    Bukkit.getWorlds().map { it.name }
}

fun suggestMaterials(): SuggestionsProvider = SuggestionsProvider { _, _ ->
    Material.values().map { it.name.lowercase() }
}

fun suggestItems(): SuggestionsProvider = SuggestionsProvider { _, _ ->
    Material.values().filter { it.isItem }.map { it.name.lowercase() }
}

fun suggestBlocks(): SuggestionsProvider = SuggestionsProvider { _, _ ->
    Material.values().filter { it.isBlock }.map { it.name.lowercase() }
}

inline fun <reified T : Enum<T>> suggestEnum(): SuggestionsProvider = SuggestionsProvider { _, _ ->
    enumValues<T>().map { it.name.lowercase() }
}

fun suggest(vararg values: String): SuggestionsProvider = SuggestionsProvider { _, _ ->
    values.toList()
}

fun suggest(values: Iterable<String>): SuggestionsProvider = SuggestionsProvider { _, _ ->
    values.toList()
}

fun suggestWithPermission(vararg values: String, permissionPrefix: String): SuggestionsProvider =
    SuggestionsProvider { context, _ ->
        val permissible: Permissible = context.sender
        values.filter { permissible.hasPermission("$permissionPrefix.$it") }
    }

fun suggestAny(vararg providers: SuggestionsProvider): SuggestionsProvider =
    SuggestionsProvider { context, currentArg ->
        providers.flatMap { it.suggest(context, currentArg) }
    }

fun suggestIf(
    condition: (context: KommandContext) -> Boolean,
    ifTrue: SuggestionsProvider,
    ifFalse: SuggestionsProvider = SuggestionsProvider { _, _ -> emptyList() }
): SuggestionsProvider = SuggestionsProvider { context, currentArg ->
    if (condition(context)) {
        ifTrue.suggest(context, currentArg)
    } else {
        ifFalse.suggest(context, currentArg)
    }
}

private fun CommandSender.canSeePlayer(player: Player): Boolean {
    return when (this) {
        is Player -> this.canSee(player)
        else -> true
    }
}

fun suggestPlayerThenActions(vararg actions: String): TabBuilder.() -> Unit = {
    arg(0, suggestPlayers())
    arg(1, suggest(*actions))
}