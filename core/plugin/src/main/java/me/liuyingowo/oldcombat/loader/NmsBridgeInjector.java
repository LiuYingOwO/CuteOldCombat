package me.liuyingowo.oldcombat.loader;

import net.bytebuddy.dynamic.loading.ClassInjector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.instrument.Instrumentation;
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

    private static final String BASE_BRIDGE_CLASS_NAME = "me.liuyingowo.oldcombat.nms.bridge.KnockbackBridge";
    private static final String BRIDGE_CLASS_RESOURCE_PATH = "me/liuyingowo/oldcombat/nms/bridge";
    private static volatile boolean injected;

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
            File file = path.toFile();

            try (var jar = new JarFile(file)) {
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
