package me.liuyingowo.oldcombat.loader;

import me.liuyingowo.oldcombat.nms.adapter.NmsAdapter;
import me.liuyingowo.oldcombat.nms.NmsManager;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.lang.instrument.Instrumentation;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PatchInstaller {

    private static ResettableClassFileTransformer transformer;

    private PatchInstaller() {}

    public static synchronized boolean hasTransformer() {
        return transformer != null;
    }

    public static synchronized void install(Instrumentation instrumentation, Logger logger, FileConfiguration config) {
        if (!NmsManager.install(logger)) {
            logger.severe("Failed to load Nms-adapter. NMS patches disabled.");
            return;
        }
        if (!config.getBoolean("enable")) {
            logger.info("Patch is disabled in config.");
            return;
        }
        try {
            resetCurrentTransformer(instrumentation, logger);
            NmsAdapter adapter = NmsManager.getAdapter();

            AgentBuilder agentBuilder = new AgentBuilder.Default()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .disableClassFormatChanges()
                    .with(new AgentBuilder.Listener.Adapter() {
                        @Override
                        public void onTransformation(@NotNull TypeDescription typeDescription,
                                                     ClassLoader classLoader,
                                                     JavaModule module,
                                                     boolean loaded,
                                                     @NotNull DynamicType dynamicType) {
                            logger.info("Patched " + typeDescription.getName());
                        }

                        @Override
                        public void onError(@NotNull String typeName,
                                            ClassLoader classLoader,
                                            JavaModule module,
                                            boolean loaded,
                                            @NotNull Throwable throwable) {
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

            logger.info("Loading Complete. >w<");
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "Could not install NMS patches.", throwable);
        }
    }

    public static synchronized void uninstall(Instrumentation instrumentation, Logger logger) {
        resetCurrentTransformer(instrumentation, logger);
    }
    
    private static void resetCurrentTransformer(Instrumentation instrumentation, Logger logger) {
        if (transformer == null || instrumentation == null) {
            return;
        }

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
