package net.guizhanss.guizhanlib.kt.common.utils

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters

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
    val argTypes = args.map { it::class.java }.toTypedArray()

    // find handle in class and superclasses
    var handle = lookup.findMethodHandle(this.javaClass, name, T::class.java, argTypes)

    // find handle in interfaces
    if (handle == null) {
        val interfaces = this.javaClass.interfaces + this.javaClass.superclass?.interfaces.orEmpty()
        for (iface in interfaces) {
            handle = lookup.findMethodHandle(iface, name, T::class.java, argTypes)
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
