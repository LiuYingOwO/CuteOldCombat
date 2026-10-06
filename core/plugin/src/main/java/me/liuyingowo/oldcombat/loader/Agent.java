package me.liuyingowo.oldcombat.loader;

import net.bytebuddy.agent.ByteBuddyAgent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;

public final class Agent {

    // do not direct use, use reflect + SystemClassLoader
    private static volatile Instrumentation instrumentation;
    private static volatile boolean isPremain = false;
    private static volatile boolean isAgentMain = false;

    private Agent() {}

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        isPremain = true;
    }

    public static void agentmain(String args, Instrumentation inst) {
        instrumentation = inst;
        isAgentMain = true;
    }

    private static volatile Instrumentation currentInstrumentation;

    private static Instrumentation getInstrumentationFromJavaAgent() {
        try {
            Class<?> agentClass = ClassLoader.getSystemClassLoader().loadClass(Agent.class.getName());

            Field premainField = agentClass.getDeclaredField("isPremain");
            premainField.setAccessible(true);

            boolean isPremain = (boolean) premainField.get(null);

            if (isPremain) {
                System.out.println("Using Instrumentation from -javaagent.");

                Field field = agentClass.getDeclaredField("instrumentation");
                field.setAccessible(true);

                return (Instrumentation) field.get(null);
            }

            System.out.println("Agent was loaded in System ClassLoader, but is not premain.");
            System.out.println("Using Dynamic Attach instead.");
            return ByteBuddyAgent.install();

        } catch (ClassNotFoundException e) {
            return null;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read CuteOldCombat javaagent instrumentation.", e);
        }
    }

    public static Instrumentation getInstrumentation() {
        Instrumentation inst = currentInstrumentation;
        if (inst != null) {
            System.out.println("Reusing existing Instrumentation.");
            return inst;
        }

        synchronized (Agent.class) {
            inst = currentInstrumentation;
            if (inst != null) {
                return inst;
            }

            inst = getInstrumentationFromJavaAgent();
            String source = "-javaagent";

            if (inst == null) {
                inst = ByteBuddyAgent.install();
                source = "dynamic agent";
            }

            currentInstrumentation = inst;
            System.out.println("Using Instrumentation from " + source + ".");
            return inst;
        }
    }

    public static boolean isAgentLoaded() {
        return getInstrumentationFromJavaAgent() != null;
    }
}