package net.guizhanss.guizhanlib.kt.common.extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GeneralExtTest {

    private enum class TestEnum {
        ONE, TWO, THREE
    }

    @Test
    fun `test valueOfOrNull with valid enum names`() {
        assertEquals(TestEnum.ONE, valueOfOrNull<TestEnum>("ONE"))
        assertEquals(TestEnum.TWO, valueOfOrNull<TestEnum>("TWO"))
        assertEquals(TestEnum.THREE, valueOfOrNull<TestEnum>("THREE"))
    }

    @Test
    fun `test valueOfOrNull with invalid enum names`() {
        assertNull(valueOfOrNull<TestEnum>("FOUR"))
        assertNull(valueOfOrNull<TestEnum>(""))
        assertNull(valueOfOrNull<TestEnum>("one")) // test case sensitivity
    }

    @Test
    fun `test valueOfOrNull with special characters`() {
        assertNull(valueOfOrNull<TestEnum>("ONE "))
        assertNull(valueOfOrNull<TestEnum>(" ONE"))
        assertNull(valueOfOrNull<TestEnum>("ONE\n"))
    }

    @Test
    fun `test Pair matches with equal objects`() {
        val pair = Pair("a", "b")

        assertTrue(pair.matches("a", "b"))
        assertTrue(pair.matches("b", "a"))
    }

    @Test
    fun `test Pair matches with different objects`() {
        val pair = Pair("a", "b")

        assertFalse(pair.matches("a", "c"))
        assertFalse(pair.matches("c", "b"))
        assertFalse(pair.matches("c", "d"))
    }

    @Test
    fun `test Pair matches with null values`() {
        val pairWithNull = Pair<String?, String?>(null, "b")

        assertTrue(pairWithNull.matches(null, "b"))
        assertTrue(pairWithNull.matches("b", null))
        assertFalse(pairWithNull.matches(null, null))
        assertFalse(pairWithNull.matches("a", "b"))
    }

    @Test
    fun `test Pair matches with same objects`() {
        val pair = Pair("a", "a")

        assertTrue(pair.matches("a", "a"))
        assertFalse(pair.matches("a", "b"))
    }

    @Test
    fun `test Pair matches with different types`() {
        val pair = Pair(1, 2)

        assertTrue(pair.matches(1, 2))
        assertTrue(pair.matches(2, 1))
        assertFalse(pair.matches(1, 3))
    }

    @Test
    fun `test Pair matches with custom objects`() {
        data class TestObject(val value: String)

        val obj1 = TestObject("1")
        val obj2 = TestObject("2")
        val pair = Pair(obj1, obj2)

        assertTrue(pair.matches(obj1, obj2))
        assertTrue(pair.matches(obj2, obj1))
        assertFalse(pair.matches(obj1, TestObject("3")))
    }
}
