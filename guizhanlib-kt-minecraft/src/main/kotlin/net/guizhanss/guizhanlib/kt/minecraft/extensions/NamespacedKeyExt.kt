package net.guizhanss.guizhanlib.kt.minecraft.extensions

import net.guizhanss.guizhanlib.common.utils.StringUtil
import org.bukkit.NamespacedKey
import java.util.Locale

/**
 * Create a Minecraft [NamespacedKey] from the string.
 */
fun String.toMinecraftKey() =
    NamespacedKey.minecraft(StringUtil.dehumanize(this).lowercase(Locale.ENGLISH))
