package me.liuyingowo.oldcombat.loader;

import me.liuyingowo.oldcombat.nms.adapter.KnockbackInstaller;
import me.liuyingowo.oldcombat.nms.adapter.NmsAdapter;
import me.liuyingowo.oldcombat.nms.NmsManager;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.instrument.Instrumentation;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Installer {

    private static Instrumentation instrumentation;
    private static ResettableClassFileTransformer transformer;
    private static boolean resolvedFromJavaAgent = false;

    private Installer() {}

    public static synchronized boolean install(Logger logger, FileConfiguration config) {
        try {
            resetCurrentTransformer(logger);

            if (!NmsManager.install(logger)) {
                logger.severe("Failed to load Nms-adapter. NMS patches disabled.");
                return false;
            }

            NmsAdapter adapter = NmsManager.getAdapter();

            if (instrumentation == null) {
                instrumentation = Agent.findInstrumentation();

                if (instrumentation != null) {
                    resolvedFromJavaAgent = true;
                    logger.info("Using Instrumentation from -javaagent.");
                }
            }

            if (instrumentation == null) {
                logger.info("Installing agent dynamically...");

                instrumentation = ByteBuddyAgent.install();
                resolvedFromJavaAgent = true;

                logger.info("Using Instrumentation from dynamic agent.");
            } else if (!resolvedFromJavaAgent) {
                logger.info("Reusing existing Instrumentation.");
            }

            KnockbackInstaller.injectIfNeeded(instrumentation, logger);
            syncKnockbackBridge(config, logger);

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
                            logger.info("Patched " + typeDescription.getName());
                        }

                        @Override
                        public void onError(String typeName,
                                            ClassLoader classLoader,
                                            JavaModule module,
                                            boolean loaded,
                                            Throwable throwable) {
                            if (typeName.startsWith("net.minecraft.world.entity")) {
                                logger.log(Level.SEVERE, "Failed to patch " + typeName, throwable);
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
            logger.log(Level.SEVERE, "Could not install NMS patches.", throwable);
            return false;
        }
    }

    public static synchronized void uninstall(Logger logger) {
        resetCurrentTransformer(logger);
    }

    private static void syncKnockbackBridge(FileConfiguration config, Logger logger) {
        boolean enabled = config.getBoolean("knockback.enabled", KnockbackInstaller.DEFAULT_ENABLED);
        double horizontal = config.getDouble("knockback.horizontal", KnockbackInstaller.DEFAULT_HORIZONTAL);
        double vertical = config.getDouble("knockback.vertical", KnockbackInstaller.DEFAULT_VERTICAL);
        double verticalLimit = config.getDouble("knockback.vertical-limit", KnockbackInstaller.DEFAULT_VERTICAL_LIMIT);
        double friction = config.getDouble("knockback.friction", KnockbackInstaller.DEFAULT_FRICTION);
        double minDirectionLength = config.getDouble("knockback.min-direction-length", KnockbackInstaller.DEFAULT_MIN_DIRECTION_LENGTH);
        boolean applyResistance = config.getBoolean("knockback.apply-resistance", KnockbackInstaller.DEFAULT_APPLY_RESISTANCE);

        KnockbackInstaller.update(
                enabled,
                horizontal,
                vertical,
                verticalLimit,
                friction,
                minDirectionLength,
                applyResistance
        );

        logger.info("Knockback bridge updated: enabled=" + enabled
                + ", horizontal=" + horizontal
                + ", vertical=" + vertical
                + ", verticalLimit=" + verticalLimit
                + ", friction=" + friction
                + ", minDirectionLength=" + minDirectionLength
                + ", applyResistance=" + applyResistance);
    }
    
    private static void resetCurrentTransformer(Logger logger) {
        if (transformer == null || instrumentation == null) {
            return;
        }

        resolvedFromJavaAgent = false;

        try {
            boolean reset = transformer.reset(instrumentation, AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
            if (reset) {
                logger.info("Previous NMS patches reset.");
            } else {
                logger.warning("Previous NMS patches could not be fully reset.");
            }
        } catch (Throwable throwable) {
            logger.log(Level.WARNING, "Failed to reset previous NMS patches.", throwable);
        } finally {
            transformer = null;
        }
    }
}
