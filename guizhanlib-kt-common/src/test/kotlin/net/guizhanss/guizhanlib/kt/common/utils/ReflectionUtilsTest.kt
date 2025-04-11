package net.guizhanss.guizhanlib.kt.common.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ReflectionUtilsTest {

    class Sample(val a: String) {

        var b: Int = 0

        constructor(a: String, b: Int) : this(a) {
            this.b = b
        }

        fun multiply(x: Int, y: Int) = x * y

        private val secret: String = "hidden"
    }

    enum class Color {
        RED, GREEN, BLUE
    }

    class Constants {
        companion object {

            @JvmStatic
            val VERSION = "1.0.0"
        }
    }

    @Test
    fun testGetConstructor() {
        val primaryConstructor = Sample::class.getConstructor("hello")
        assertNotNull(primaryConstructor)
        val sample1 = primaryConstructor?.call("hello")
        assertEquals("hello", sample1?.a)

        val secondaryConstructor = Sample::class.getConstructor("hello", 123)
        assertNotNull(secondaryConstructor)
        val sample2 = secondaryConstructor?.call("hello", 123)
        assertEquals("hello", sample2?.a)
        assertEquals(123, sample2?.b)

        val nonExisting = Sample::class.getConstructor(123)
        assertNull(nonExisting)
    }

    @Test
    fun testInvokeMethod() {
        val sample = Sample("test", 42)
        val result = sample.invoke<Int>("multiply", 3, 4)
        assertEquals(12, result)
    }

    @Test
    fun testGetInstanceField() {
        val sample = Sample("private", 0)
        val secret = sample.getField<String>("secret")
        assertEquals("hidden", secret)
    }

    @Test
    fun testGetStaticFieldForConstants() {
        val version = Constants::class.getStaticField<String>("VERSION")
        assertEquals("1.0.0", version)
    }

    @Test
    fun testGetStaticFieldForEnum() {
        val color = Color::class.getStaticField<Color>("GREEN")
        assertNotNull(color)
        assertEquals(Color.GREEN, color)
    }

    @Test
    fun testJavaClassInvokeMethod() {
        val javaSample = ReflectionUtilsJavaSample("test")
        val result = javaSample.invoke<Int>("multiply", 3, 4)
        assertEquals(12, result)
    }

    @Test
    fun testJavaClassGetInstanceField() {
        val javaSample = ReflectionUtilsJavaSample("initial")
        val value = javaSample.getField<String>("value")
        assertEquals("initial", value)

        val secret = javaSample.getField<Int>("secretNumber")
        assertEquals(javaSample.secretNumber, secret)
    }

    @Test
    fun testJavaClassGetStaticField() {
        val staticValue = ReflectionUtilsJavaSample::class.getStaticField<String>("STATIC_VALUE")
        assertEquals("world", staticValue)
    }
}
