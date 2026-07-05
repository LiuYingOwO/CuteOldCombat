package me.liuyingowo.oldcombat.nms.adapter;

import net.bytebuddy.dynamic.loading.ClassInjector;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

public final class KnockbackInstaller {

    private static final String BRIDGE_CLASS_NAME = "me.liuyingowo.oldcombat.nms.adapter.KnockbackBridge";
    private static final String BRIDGE_RESOURCE = "me/liuyingowo/oldcombat/nms/adapter/KnockbackBridge.class";

    private static volatile boolean injected;

    public static final boolean DEFAULT_ENABLED = false;
    public static final double DEFAULT_HORIZONTAL = 1.0D;
    public static final double DEFAULT_VERTICAL = 0.4000000059604645D;
    public static final double DEFAULT_VERTICAL_LIMIT = 0.4000000059604645D;
    public static final double DEFAULT_FRICTION = 0.5D;
    public static final double DEFAULT_MIN_DIRECTION_LENGTH = 1.0E-5D;
    public static final boolean DEFAULT_APPLY_RESISTANCE = true;

    private KnockbackInstaller() {}

    public static synchronized void injectIfNeeded(Instrumentation instrumentation, Logger logger) throws IOException {
        if (instrumentation == null) {
            throw new IllegalArgumentException("instrumentation");
        }

        if (injected || isBootstrapBridgePresent()) {
            injected = true;
            return;
        }

        byte[] bridgeBytes = readBridgeBytes();
        Path temp = Files.createTempDirectory("cuteoldcombat-bridge");
        temp.toFile().deleteOnExit();

        ClassInjector.UsingInstrumentation
                .of(temp.toFile(), ClassInjector.UsingInstrumentation.Target.BOOTSTRAP, instrumentation)
                .injectRaw(Map.of(BRIDGE_CLASS_NAME, bridgeBytes));

        injected = true;
        logger.info("Knockback bridge injected into bootstrap classloader.");
    }

    public static void update(boolean enabled,
                              double horizontal,
                              double vertical,
                              double verticalLimit,
                              double friction,
                              double minDirectionLength,
                              boolean applyResistance) {

        try {
            Class<?> bridgeClass = Class.forName(BRIDGE_CLASS_NAME, true, null);
            Method update = bridgeClass.getMethod(
                    "update",
                    boolean.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    boolean.class
            );

            update.invoke(
                    null,
                    enabled,
                    finiteOrDefault(horizontal, DEFAULT_HORIZONTAL),
                    finiteOrDefault(vertical, DEFAULT_VERTICAL),
                    finiteOrDefault(verticalLimit, DEFAULT_VERTICAL_LIMIT),
                    finiteOrDefault(friction, DEFAULT_FRICTION),
                    Math.max(0.0D, finiteOrDefault(minDirectionLength, DEFAULT_MIN_DIRECTION_LENGTH)),
                    applyResistance
            );
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Knockback bridge is not available in bootstrap classloader.", exception);
        }
    }

    private static double finiteOrDefault(double value, double defaultValue) {
        return Double.isFinite(value) ? value : defaultValue;
    }

    private static boolean isBootstrapBridgePresent() {
        try {
            Class.forName(BRIDGE_CLASS_NAME, false, null);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static byte[] readBridgeBytes() throws IOException {
        ClassLoader loader = KnockbackInstaller.class.getClassLoader();
        try (InputStream input = loader.getResourceAsStream(BRIDGE_RESOURCE)) {
            if (input == null) {
                throw new IOException("Missing class resource: " + BRIDGE_RESOURCE);
            }

            return input.readAllBytes();
        }
    }
}
