package me.liuyingowo.oldcombat.loader;

import java.lang.instrument.Instrumentation;

public final class Agent {

    private static final String INSTRUMENTATION_KEY =
            "me.liuyingowo.oldcombat.loader.instrumentation";

    private Agent() {}

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        install(instrumentation);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        install(instrumentation);
    }

    private static void install(Instrumentation instrumentation) {
        System.getProperties().put(INSTRUMENTATION_KEY, instrumentation);
        System.setProperty(INSTRUMENTATION_KEY + ".active", "true");
    }

    public static Instrumentation getInstrumentation() {
        Object value = System.getProperties().get(INSTRUMENTATION_KEY);
        if (value instanceof Instrumentation instrumentation) {
            return instrumentation;
        }
        return null;
    }

    public static boolean isAgentLoaded() {
        return Boolean.getBoolean(INSTRUMENTATION_KEY + ".active");
    }
}
