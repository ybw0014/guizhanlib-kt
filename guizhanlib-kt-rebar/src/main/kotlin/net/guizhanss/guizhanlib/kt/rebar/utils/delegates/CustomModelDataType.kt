package net.guizhanss.guizhanlib.kt.rebar.utils.delegates

interface CustomModelDataType<T> {

    fun toString(value: T): String

    fun fromString(value: String): T

    companion object {

        @JvmField
        val STRING: CustomModelDataType<String> = object : CustomModelDataType<String> {
            override fun toString(value: String) = value
            override fun fromString(value: String) = value
        }

        @JvmField
        val INTEGER: CustomModelDataType<Int> = object : CustomModelDataType<Int> {
            override fun toString(value: Int) = value.toString()
            override fun fromString(value: String) = value.toInt()
        }

        @JvmField
        val LONG: CustomModelDataType<Long> = object : CustomModelDataType<Long> {
            override fun toString(value: Long) = value.toString()
            override fun fromString(value: String) = value.toLong()
        }

        @JvmField
        val DOUBLE: CustomModelDataType<Double> = object : CustomModelDataType<Double> {
            override fun toString(value: Double) = value.toString()
            override fun fromString(value: String) = value.toDouble()
        }

        @JvmField
        val FLOAT: CustomModelDataType<Float> = object : CustomModelDataType<Float> {
            override fun toString(value: Float) = value.toString()
            override fun fromString(value: String) = value.toFloat()
        }

        @JvmField
        val BOOLEAN: CustomModelDataType<Boolean> = object : CustomModelDataType<Boolean> {
            override fun toString(value: Boolean) = value.toString()
            override fun fromString(value: String) = value.toBooleanStrict()
        }
    }
}
