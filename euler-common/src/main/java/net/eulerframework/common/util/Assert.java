package net.eulerframework.common.util;

import java.lang.reflect.Constructor;

public class Assert {
    
    private static <T extends RuntimeException> void throwException(Class<T> exceptionClass, String message) {
        try {
            Constructor<T> constructor = exceptionClass.getDeclaredConstructor(String.class);
            throw constructor.newInstance(message);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static <T extends RuntimeException> void isTrue(boolean expression, String message) {
        Assert.isTrue(expression, IllegalArgumentException.class, message);
    }

    public static <T extends RuntimeException> void isTrue(boolean expression, Class<T> exceptionClass, String message) {
        Assert.isFalse(!expression, exceptionClass, message);
    }
    
    public static <T extends RuntimeException> void isFalse(boolean expression, String message) {
        Assert.isFalse(expression, IllegalArgumentException.class, message);
    }

    public static <T extends RuntimeException> void isFalse(boolean expression, Class<T> exceptionClass, String message) {
        if (expression) {
            Assert.throwException(exceptionClass, message);
        }
    }
    
    public static <T extends RuntimeException> void isNotNull(Object object, String message) {
        Assert.isNotNull(object, IllegalArgumentException.class, message);
    }

    public static <T extends RuntimeException> void isNotNull(Object object, Class<T> exceptionClass, String message) {
        if (object == null) {
            Assert.throwException(exceptionClass, message);
        }
    }
    
    public static <T extends RuntimeException> void isNull(Object object, String message) {
        Assert.isNull(object, IllegalArgumentException.class, message);
    }

    public static <T extends RuntimeException> void isNull(Object object, Class<T> exceptionClass, String message) {
        if (object != null) {
            Assert.throwException(exceptionClass, message);
        }
    }
}
