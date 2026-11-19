package me.liuyingowo.oldcombat.loader;

import net.bytebuddy.agent.ByteBuddyAgent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstrumentOperator {

    private static volatile Instrumentation instrumentation;

    public static synchronized Instrumentation getInstrumentation(Logger logger) {
        if (instrumentation != null) {
            logger.info("Reusing existing instrumentation");
            return instrumentation;
        }

        Instrumentation fromAgent = null;
        try {
            Class<?> agentClass = Class.forName(
                    "me.liuyingowo.oldcombat.loader.Agent",
                    false,
                    ClassLoader.getSystemClassLoader()
            );
            Field instField = agentClass.getDeclaredField("instrumentation");
            instField.setAccessible(true);

            fromAgent = (Instrumentation) instField.get(null);

            if (fromAgent != null) {
                logger.info("Using Instrumentation from -javaagent.");
            } else {
                logger.info("Using Instrumentation by dynamic attach.");
            }

        } catch (ClassNotFoundException e) {
            logger.info("Agent class not found in SystemClassLoader, using dynamic attach.");
        } catch (ReflectiveOperationException e) {
            logger.log(Level.WARNING, "Agent class exists but instrumentation field is unreadable, using dynamic attach.", e);
        }

        instrumentation = (fromAgent != null) ? fromAgent : ByteBuddyAgent.install();
        return instrumentation;
    }

    public static synchronized void redefineModule(Logger logger) {
        if (instrumentation == null) {
            logger.info("Instrumentation is null, call getInstrumentation first.");
            return;
        }
    }
}
