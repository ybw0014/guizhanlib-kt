package net.guizhanss.guizhanlib.kt.common.extensions

/**
 * Get the enum from the name, or null if not found.
 */
inline fun <reified T : Enum<T>> valueOfOrNull(name: String): T? = enumValues<T>().firstOrNull { it.name == name }

/**
 * Check if the given two objects matches the two objects in a [Pair],
 * regardless of the order.
 */
fun <T> Pair<T, T>.matches(obj1: T, obj2: T) =
    (first == obj1 && second == obj2) || (first == obj2 && second == obj1)
