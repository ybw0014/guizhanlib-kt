@file:Suppress("unused")

package net.guizhanss.guizhanlib.kt.slimefun.sf4kext

import io.github.seggan.sf4k.item.builder.MaterialType
import io.github.thebusybiscuit.slimefun4.utils.HeadTexture

fun String.asMaterialType(): MaterialType = MaterialType.Head(this)
fun HeadTexture.asMaterialType(): MaterialType = MaterialType.Head(this.texture)
