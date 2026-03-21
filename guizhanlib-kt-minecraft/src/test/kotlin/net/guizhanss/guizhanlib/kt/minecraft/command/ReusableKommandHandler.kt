package net.guizhanss.guizhanlib.kt.minecraft.command

internal fun reusableKommandHandler(block: KommandContext.() -> Unit): KommandExecutor {
    return KommandExecutor(block)
}
