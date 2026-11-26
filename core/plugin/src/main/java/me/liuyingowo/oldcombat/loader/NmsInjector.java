package me.liuyingowo.oldcombat.loader;

import me.liuyingowo.oldcombat.nms.Inject;
import net.bytebuddy.description.annotation.AnnotationDescription;
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

        Objects.requireNonNull(instrumentation, "instrumentation could not be null");
        Objects.requireNonNull(targetLoader, "targetLoader could not be null");
        Objects.requireNonNull(logger, "logger could not be null");

        if (injected) {
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
            injected = true;

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to inject the NMS bridge classes into " + targetLoader + ".", e);
        }
    }

    private static Map<String, byte[]> readInjectClasses(Logger logger) throws IOException {

        record InjectClassInfo(String className, Inject.InjectType type, byte[] bytes) {}
        List<InjectClassInfo> injectClasses = new ArrayList<>();

        Path path;
        try {
            URL url = NmsInjector.class.getProtectionDomain().getCodeSource().getLocation();
            path = Path.of(url.toURI());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        File file = path.toFile();

        try (var jar = new JarFile(file);
             var locator = new ClassFileLocator.ForJarFile(jar)) {

            Enumeration<JarEntry> entries = jar.entries();
            TypePool pool = TypePool.Default.of(locator);

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.isDirectory()
                        || !entry.getName().endsWith(".class")
                        || entry.getName().startsWith("META-INF/")
                        || entry.getName().equals("module-info.class")) {
                    continue;
                }

                String className = entry.getName()
                        .substring(0, entry.getName().length() - ".class".length())
                        .replace('/', '.');

                Optional<AnnotationDescription> annotation = pool
                        .describe(className)
                        .resolve()
                        .getDeclaredAnnotations()
                        .stream()
                        .filter(a -> a.getAnnotationType().represents(Inject.class))
                        .findFirst();

                if (annotation.isEmpty()) {
                    continue;
                }

                Inject inject = annotation.get().prepare(Inject.class).load();
                byte[] bytes;
                try (InputStream in = jar.getInputStream(entry)) {
                    bytes = in.readAllBytes();
                }

                injectClasses.add(new InjectClassInfo(className, inject.type(), bytes));
            }

            injectClasses.sort(Comparator.comparingInt(info -> info.type().ordinal()));

            Map<String, byte[]> map = new LinkedHashMap<>(injectClasses.size());

            for (InjectClassInfo info : injectClasses) {
                map.put(info.className, info.bytes);
                logger.info("[Inject] [" + info.type().name() + "] Found Class: " + info.className);
            }

            return map;
        }
    }
}
