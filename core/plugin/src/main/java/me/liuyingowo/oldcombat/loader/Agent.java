package me.liuyingowo.oldcombat.loader;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

public final class Agent {

    private static volatile Instrumentation instrumentation;

    private Agent() {}

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
    }

    public static void agentmain(String args, Instrumentation inst) {
        instrumentation = inst;
    }

    private static Instrumentation localInstrumentation() {
        return instrumentation;
    }

    public static Instrumentation findInstrumentation() {
        try {
            Class<?> agentClass = ClassLoader.getSystemClassLoader().loadClass(Agent.class.getName());

            Method method = agentClass.getDeclaredMethod("localInstrumentation");
            method.setAccessible(true);

            Object value = method.invoke(null);
            if (value instanceof Instrumentation inst) {
                return inst;
            }

            return null;
        } catch (ClassNotFoundException ignored) {
            return null;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read CuteOldCombat javaagent instrumentation.", exception);
        }
    }

    public static boolean isAgentLoaded() {
        return findInstrumentation() != null;
    }
}