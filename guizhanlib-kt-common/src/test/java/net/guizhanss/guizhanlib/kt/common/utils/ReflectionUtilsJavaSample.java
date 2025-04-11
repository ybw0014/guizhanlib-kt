package net.guizhanss.guizhanlib.kt.common.utils;

class ReflectionUtilsJavaSample {

    public String value = "hello";

    private int secretNumber = 100;

    public static final String STATIC_VALUE = "world";

    public ReflectionUtilsJavaSample(String value) {
        this.value = value;
    }

    public int multiply(int x, int y) {
        return x * y;
    }

    public int getSecretNumber() {
        return secretNumber;
    }
}
