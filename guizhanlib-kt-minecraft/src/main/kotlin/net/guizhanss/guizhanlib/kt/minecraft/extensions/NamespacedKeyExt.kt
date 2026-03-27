package net.guizhanss.guizhanlib.kt.minecraft.extensions

import net.guizhanss.guizhanlib.common.utils.StringUtil
import org.bukkit.NamespacedKey
import java.util.Locale

/**
 * Create a [NamespacedKey] from the string.
 */
fun String.toNamespacedKey() = NamespacedKey.fromString(StringUtil.dehumanize(this).lowercase(Locale.ENGLISH))

/**
 * Create a Minecraft [NamespacedKey] from the string.
 */
fun mcKey(key: String) = key.toNamespacedKey()
