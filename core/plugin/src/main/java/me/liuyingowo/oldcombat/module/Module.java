package me.liuyingowo.oldcombat.module;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Module implements Listener {

    protected final JavaPlugin plugin;
    private boolean enabled;

    protected Module(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    protected abstract void onLoad();

    protected abstract void onUnload();

    public final void load() {
        if (enabled) {
            return;
        }
        onLoad();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        enabled = true;
        plugin.getLogger().info("Loaded Module: " + this.getClass().getSimpleName());
    }

    public final void unload() {
        if (!enabled) {
            return;
        }
        HandlerList.unregisterAll(this);
        enabled = false;
        onUnload();
        plugin.getLogger().info("Unloaded Module: " + this.getClass().getSimpleName());
    }

    public final void reload() {
        if (!enabled) {
            return;
        }
        unload();
        load();
    }

    public final boolean isEnabled() {
        return enabled;
    }
}
