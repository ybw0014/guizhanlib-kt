package net.guizhanss.guizhanlib.kt.common.utils

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters

/**
 * Find a constructor of the class that matches the given arguments.
 */
fun <T : Any> KClass<T>.getConstructor(vararg args: Any?): KFunction<T>? =
    this.constructors.firstOrNull { constructor ->
        constructor.valueParameters.size == args.size && constructor.valueParameters.zip(args).all { (param, arg) ->
            val classifier = param.type.classifier as? KClass<*> ?: return@all false
            if (arg == null) param.type.isMarkedNullable
            else classifier.isInstance(arg)
        }
    }
