@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.minecraft.command

fun interface KommandExecutor {
    fun execute(context: KommandContext)
}

fun interface KommandTabCompleter {
    fun complete(context: KommandContext): Iterable<String>
}
