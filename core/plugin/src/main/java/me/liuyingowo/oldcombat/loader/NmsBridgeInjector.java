package me.liuyingowo.oldcombat.loader;

import net.bytebuddy.dynamic.loading.ClassInjector;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NmsBridgeInjector {

    private static final String BASE_BRIDGE_CLASS_NAME = "me.liuyingowo.oldcombat.nms.adapter.KnockbackBridge";
    private static final String BRIDGE_CLASS_RESOURCE_PATH = "me/liuyingowo/oldcombat/nms/adapter/KnockbackBridge";

    private static volatile boolean injected;

    public static final boolean DEFAULT_ENABLED = false;
    public static final double DEFAULT_HORIZONTAL = 1.0D;
    public static final double DEFAULT_VERTICAL = 0.4000000059604645D;
    public static final double DEFAULT_VERTICAL_LIMIT = 0.4000000059604645D;
    public static final double DEFAULT_FRICTION = 0.5D;
    public static final double DEFAULT_MIN_DIRECTION_LENGTH = 1.0E-5D;
    public static final boolean DEFAULT_APPLY_RESISTANCE = true;

    private NmsBridgeInjector() {}

    public static synchronized void injectIfNeeded(Instrumentation instrumentation, Logger logger) {
        if (instrumentation == null) {
            throw new IllegalArgumentException("instrumentation");
        }

        if (injected || isBootstrapBridgePresent()) {
            injected = true;
            return;
        }

        try {
            Path temp = Files.createTempDirectory("cuteoldcombat-bridge");
            temp.toFile().deleteOnExit();

            var injector = ClassInjector.UsingInstrumentation
                    .of(
                            temp.toFile(),
                            ClassInjector.UsingInstrumentation.Target.BOOTSTRAP,
                            instrumentation
                    );

            injector.injectRaw(readAllBridges(logger));

            injected = true;
            logger.info("All Bridge injected into bootstrap classloader.");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create files", e);
        }

    }

    public static void sync(FileConfiguration config, Logger logger) {
        boolean enabled = config.getBoolean("knockback.enabled", NmsBridgeInjector.DEFAULT_ENABLED);
        double horizontal = config.getDouble("knockback.horizontal", NmsBridgeInjector.DEFAULT_HORIZONTAL);
        double vertical = config.getDouble("knockback.vertical", NmsBridgeInjector.DEFAULT_VERTICAL);
        double verticalLimit = config.getDouble("knockback.vertical-limit", NmsBridgeInjector.DEFAULT_VERTICAL_LIMIT);
        double friction = config.getDouble("knockback.friction", NmsBridgeInjector.DEFAULT_FRICTION);
        double minDirectionLength = config.getDouble("knockback.min-direction-length", NmsBridgeInjector.DEFAULT_MIN_DIRECTION_LENGTH);
        boolean applyResistance = config.getBoolean("knockback.apply-resistance", NmsBridgeInjector.DEFAULT_APPLY_RESISTANCE);

        NmsBridgeInjector.update(
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

    public static void update(boolean enabled,
                              double horizontal,
                              double vertical,
                              double verticalLimit,
                              double friction,
                              double minDirectionLength,
                              boolean applyResistance) {

        try {
            Class<?> bridgeClass = Class.forName(BASE_BRIDGE_CLASS_NAME, true, null);
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
            Class.forName(BASE_BRIDGE_CLASS_NAME, false, null);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static Map<String, byte[]> readAllBridges(Logger logger) throws IOException {
        Map<String, byte[]> map = new HashMap<>();

        try {
            URL url = NmsBridgeInjector.class.getProtectionDomain().getCodeSource().getLocation();
            Path path = Path.of(url.toURI());

            try (var jar = new JarFile(path.toFile())) {
                jar.stream()
                        .filter(jarEntry -> !jarEntry.isDirectory())
                        .filter(e -> e.getName().startsWith(BRIDGE_CLASS_RESOURCE_PATH))
                        .filter(e -> e.getName().endsWith(".class"))
                        .forEach(e -> {
                            try (InputStream in = jar.getInputStream(e)) {
                                String binName = e.getName()
                                        .replace('/', '.')
                                        .replaceAll("\\.class$", "");

                                logger.info("Find Bridge Class: " + binName);
                                map.put(binName, in.readAllBytes());
                            } catch (IOException ex) {
                                throw new UncheckedIOException(ex);
                            }
                        });
            }
            if (map.isEmpty()) {
                throw new IllegalStateException("Bridge class is not found.");
            }
            return map;

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
