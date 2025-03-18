@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.slimefun.extensions

import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu

val BlockMenu.position: BlockPosition get() = BlockPosition(location)
