package me.liuyingowo.oldcombat.loader;

import me.liuyingowo.oldcombat.nms.Inject;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.pool.TypePool;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NmsInjector {

    private static volatile boolean injected;

    private NmsInjector() {}

    public static synchronized void injectIfNeeded(Instrumentation instrumentation,
                                                   ClassLoader targetLoader,
                                                   Logger logger) {

        if (instrumentation == null) {
            throw new IllegalArgumentException("instrumentation could not be null");
        }

        if (injected) {
            injected = true;
            return;
        }

        try {
            Map<String, byte[]> classes = readInjectClasses(logger);
            if (classes.isEmpty()) {
                throw new RuntimeException("Inject Class is Empty.");
            }
            ClassInjector.UsingUnsafe.Factory.resolve(instrumentation)
                    .make(targetLoader)
                    .injectRaw(classes);
            logger.info("All classes requiring injection have been injected into " + targetLoader);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to inject the NMS bridge classes into " + targetLoader + ".", e);
        }
    }

    private static Map<String, byte[]> readInjectClasses(Logger logger) throws IOException {

        record InjectClassInfo(String className, Inject.InjectType type) {}
        List<InjectClassInfo> injectClasses = new ArrayList<>();
        Map<String, byte[]> map = new HashMap<>();
        Map<String, byte[]> orderedMap = new LinkedHashMap<>();

        try {
            URL url = NmsInjector.class.getProtectionDomain().getCodeSource().getLocation();
            String packageName = Inject.class.getPackageName().replace('.', '/');
            Path path = Path.of(url.toURI());
            File file = path.toFile();

            try (var jar = new JarFile(file)) {
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();

                    if (entry.isDirectory() || !entry.getName().endsWith(".class") || !entry.getName().startsWith(packageName + "/")) {
                        continue;
                    }

                    String className = entry.getName()
                            .substring(0, entry.getName().length() - ".class".length())
                            .replace('/', '.');

                    try (InputStream in = jar.getInputStream(entry)) {
                        map.put(className, in.readAllBytes());
                    }
                }
            }
            try (ClassFileLocator locator = ClassFileLocator.ForJarFile.of(file)) {
                TypePool pool = TypePool.Default.of(locator);

                for (Map.Entry<String, byte[]> entry : map.entrySet()) {
                    String className = entry.getKey();
                    pool.describe(className)
                            .resolve()
                            .getDeclaredAnnotations()
                            .stream()
                            .filter(a -> a.getAnnotationType().represents(Inject.class))
                            .findFirst()
                            .ifPresent(a -> {
                                Inject inject = a.prepare(Inject.class).load();
                                injectClasses.add(new InjectClassInfo(className, inject.injectType()));
                            });
                }
            }
            injectClasses.sort(Comparator.comparingInt(info -> info.type().ordinal()));

            for (InjectClassInfo info : injectClasses) {
                String className = info.className();
                byte[] bytes = map.get(className);

                orderedMap.put(className, bytes);
                logger.info("[Inject] [" + info.type().name() + "] Found Class: " + className);
            }
            return orderedMap;
        } catch (URISyntaxException e3) {
            throw new IOException(e3);
        }
    }
}
