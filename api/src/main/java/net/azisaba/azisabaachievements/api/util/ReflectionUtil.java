package net.azisaba.azisabaachievements.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

public final class ReflectionUtil {
    private ReflectionUtil() { throw new AssertionError(); }

    // 1. look for method on superclass
    // 2. look for method on interfaces, then interfaces in interface...

    /**
     * Finds the method recursively with the given method (uses name and parameter types for finding a method).
     * @param clazz the base class to look for the method
     * @param m the method to look for
     * @return the method if found; null otherwise
     */
    @Nullable
    public static Method findMethod(@NotNull Class<?> clazz, @NotNull Method m) {
        try {
            return clazz.getDeclaredMethod(m.getName(), m.getParameterTypes());
        } catch (ReflectiveOperationException ignore) {}
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            Method method = findMethod(superClass, m);
            if (method != null) return method;
        }
        for (Class<?> c : clazz.getInterfaces()) {
            Method method = findMethod(c, m);
            if (method != null) return method;
        }
        return null;
    }

    @NotNull
    public static Class<?> getCallerClass() {
        return getCallerClass(3); // 2 + this method
    }

    @NotNull
    public static Class<?> getCallerClass(int offset) {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i = 1 + offset; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(ReflectionUtil.class.getName()) && !ste.getClassName().contains("java.lang.Thread")) {
                try {
                    return Class.forName(ste.getClassName());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        throw new NoSuchElementException("elements: " + stElements.length + ", offset: " + offset);
    }
}
