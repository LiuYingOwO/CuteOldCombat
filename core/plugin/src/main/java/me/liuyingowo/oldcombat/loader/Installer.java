package me.liuyingowo.oldcombat.loader;

import me.liuyingowo.oldcombat.nms.adapter.NmsAdapter;
import me.liuyingowo.oldcombat.nms.NmsManager;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Installer {

    private static Instrumentation instrumentation;
    private static ResettableClassFileTransformer transformer;

    private Installer() {
    }

    public static synchronized boolean install(Logger logger) {
        try {
            resetCurrentTransformer(logger);

            if (!NmsManager.install(logger)) {
                logger.severe("[OldCombat] NmsManager failed to load adapter. NMS patches disabled.");
                return false;
            }

            NmsAdapter adapter = NmsManager.getAdapter();
            if (instrumentation == null) {
                instrumentation = Agent.getInstrumentation();

                if (instrumentation != null) {
                    logger.info("[OldCombat] Using Instrumentation from -javaagent.");
                }
            }

            if (instrumentation == null) {
                logger.info("[OldCombat] Installing ByteBuddy agent dynamically...");
                instrumentation = ByteBuddyAgent.install();
            } else {
                logger.info("[OldCombat] Reusing existing Instrumentation.");
            }

            AgentBuilder agentBuilder = new AgentBuilder.Default()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .disableClassFormatChanges()
                    .with(new AgentBuilder.Listener.Adapter() {
                        @Override
                        public void onTransformation(TypeDescription typeDescription,
                                                     ClassLoader classLoader,
                                                     JavaModule module,
                                                     boolean loaded,
                                                     DynamicType dynamicType) {
                            logger.info("[OldCombat] Patched " + typeDescription.getName());
                        }

                        @Override
                        public void onError(String typeName,
                                            ClassLoader classLoader,
                                            JavaModule module,
                                            boolean loaded,
                                            Throwable throwable) {
                            if (typeName.startsWith("net.minecraft.world.entity")) {
                                logger.log(Level.SEVERE, "[OldCombat] Failed to patch " + typeName, throwable);
                            }
                        }
                    })
                    .ignore(ElementMatchers.nameStartsWith("net.bytebuddy.")
                            .or(ElementMatchers.nameStartsWith("java."))
                            .or(ElementMatchers.nameStartsWith("jdk."))
                            .or(ElementMatchers.nameStartsWith("sun.")));

            agentBuilder = adapter.apply(agentBuilder, logger);
            transformer = agentBuilder.installOn(instrumentation);

            return true;
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "[OldCombat] Could not install NMS patches.", throwable);
            return false;
        }
    }

    public static synchronized void uninstall(Logger logger) {
        resetCurrentTransformer(logger);
    }

    private static void resetCurrentTransformer(Logger logger) {
        if (transformer == null || instrumentation == null) {
            return;
        }

        try {
            boolean reset = transformer.reset(instrumentation, AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
            if (reset) {
                logger.info("[OldCombat] Previous NMS patches reset.");
            } else {
                logger.warning("[OldCombat] Previous NMS patches could not be fully reset.");
            }
        } catch (Throwable throwable) {
            logger.log(Level.WARNING, "[OldCombat] Failed to reset previous NMS patches.", throwable);
        } finally {
            transformer = null;
        }
    }
}
