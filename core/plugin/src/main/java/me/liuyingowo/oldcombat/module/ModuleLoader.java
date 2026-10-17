package me.liuyingowo.oldcombat.module;

import java.util.*;
import java.util.logging.Level;

public class ModuleLoader {

    private final Map<Class<? extends Module>, Module> modules = new LinkedHashMap<>();

    public <T extends Module> void register(T module) {
        Module previous = modules.putIfAbsent(module.getClass(), module);

        if (previous != null) {
            throw new IllegalArgumentException("Module already registered: " + module.getClass().getSimpleName());
        }
    }

    public void enable(Class<? extends Module> type) {
        require(type).load();
    }

    public void disable(Class<? extends Module> type) {
        require(type).unload();
    }

    public void reload(Class<? extends Module> type) {
        require(type).reload();
    }

    public void shutdown() {
        List<Module> reverse = new ArrayList<>(modules.values());
        Collections.reverse(reverse);

        for (Module module : reverse) {
            try {
                module.unload();
            } catch (RuntimeException exception) {
                module.plugin.getLogger().log(Level.WARNING,
                        "Failed to unload Module: " + module.getClass().getSimpleName(),
                        exception
                );
            }
        }
    }

    private Module require(Class<? extends Module> type) {
        Module module = modules.get(type);

        if (module == null) {
            throw new IllegalArgumentException("Module not registered: " + type.getName());
        }
        return module;
    }
}
