@file:Suppress("UNCHECKED_CAST")

package net.guizhanss.guizhanlib.kt.common.utils

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters
import java.lang.Enum as JavaEnum

fun Class<*>.toPrimitiveType() =
    when (this) {
        java.lang.Integer::class.java -> Int::class.javaPrimitiveType ?: this
        java.lang.Long::class.java -> Long::class.javaPrimitiveType ?: this
        java.lang.Short::class.java -> Short::class.javaPrimitiveType ?: this
        java.lang.Byte::class.java -> Byte::class.javaPrimitiveType ?: this
        java.lang.Double::class.java -> Double::class.javaPrimitiveType ?: this
        java.lang.Float::class.java -> Float::class.javaPrimitiveType ?: this
        java.lang.Character::class.java -> Char::class.javaPrimitiveType ?: this
        java.lang.Boolean::class.java -> Boolean::class.javaPrimitiveType ?: this
        else -> this
    }

/**
 * Find a Kotlin constructor of the class that matches the given arguments.
 */
fun <T : Any> KClass<T>.getConstructor(vararg args: Any?): KFunction<T>? =
    this.constructors.firstOrNull { constructor ->
        constructor.valueParameters.size == args.size && constructor.valueParameters.zip(args).all { (param, arg) ->
            val classifier = param.type.classifier as? KClass<*> ?: return@all false
            if (arg == null) param.type.isMarkedNullable
            else classifier.isInstance(arg)
        }
    }

/**
 * Invoke a method on the given object with specified name, arguments, and return type.
 */
inline fun <reified T> Any.invoke(name: String, vararg args: Any): T? {
    val lookup = MethodHandles.lookup()
    val argTypes = args.map { it::class.java.toPrimitiveType() }.toTypedArray()
    val returnType = T::class.java.toPrimitiveType()

    // find handle in class and superclasses
    var handle = lookup.findMethodHandle(this.javaClass, name, returnType, argTypes)

    // find handle in interfaces
    if (handle == null) {
        val interfaces = this.javaClass.interfaces + this.javaClass.superclass?.interfaces.orEmpty()
        for (iface in interfaces) {
            handle = lookup.findMethodHandle(iface, name, returnType, argTypes)
            if (handle != null) break
        }
    }

    return handle?.invokeWithArguments(this, *args) as? T
}

/**
 * Fina a method handle in the class and its superclasses.
 */
fun <T> MethodHandles.Lookup.findMethodHandle(
    clazz: Class<*>?,
    name: String,
    returnType: Class<T>,
    argTypes: Array<Class<*>>
): MethodHandle? {
    var currentClass = clazz
    while (currentClass != null) {
        try {
            val methodType = MethodType.methodType(returnType, argTypes)
            return findVirtual(currentClass, name, methodType)
        } catch (_: ReflectiveOperationException) {
            currentClass = currentClass.superclass
        }
    }
    return null
}

/**
 * Get a field value from the given object instance.
 */
inline fun <reified T> Any.getField(fieldName: String): T? {
    val lookup = MethodHandles.privateLookupIn(this.javaClass, MethodHandles.lookup())

    return try {
        val varHandle = lookup.findVarHandle(this.javaClass, fieldName, T::class.java.toPrimitiveType())
        varHandle.get(this) as? T
    } catch (_: Throwable) {
        null
    }
}

/**
 * Get a static field value from the given class.
 */
inline fun <reified T> KClass<*>.getStaticField(fieldName: String): T? {
    val clazz = this.java
    return if (clazz.isEnum) {
        try {
            JavaEnum.valueOf(clazz.asSubclass(Enum::class.java), fieldName) as? T
        } catch (_: Exception) {
            null
        }
    } else {
        try {
            val lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
            val varHandle = lookup.findStaticVarHandle(clazz, fieldName, T::class.java.toPrimitiveType())
            varHandle.get() as? T
        } catch (_: Throwable) {
            null
        }
    }
}
