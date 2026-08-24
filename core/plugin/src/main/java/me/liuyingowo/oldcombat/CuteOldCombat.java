package me.liuyingowo.oldcombat;

import me.liuyingowo.oldcombat.loader.Installer;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class CuteOldCombat extends JavaPlugin {

    private ReloadCommand oldCombatCommand;
    private AttributeModifier attributeModifier;
    private LegacyCombatListener legacyCombatListener;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        Installer.install(getLogger(), getConfig());
    }

    @Override
    public void onEnable() {
        oldCombatCommand = new ReloadCommand(this);
        attributeModifier = new AttributeModifier(this);
        legacyCombatListener = new LegacyCombatListener(this, attributeModifier);

        attributeModifier.initializeAttributes();

        if (getConfig().getBoolean("enable")) {
            getServer().getPluginManager().registerEvents(legacyCombatListener, this);
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);

        Installer.uninstall(getLogger());

        if (attributeModifier != null) {
            attributeModifier.restoreAllAttributesForAllPlayer();
        }
        if (oldCombatCommand != null) {
            oldCombatCommand.unregister(this);
        }
        legacyCombatListener = null;
        attributeModifier = null;
    }

    public void reload() {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);

        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        Installer.uninstall(getLogger());

        if (getConfig().getBoolean("enable")) {
            Installer.install(getLogger(), getConfig());
            attributeModifier = new AttributeModifier(this);
            legacyCombatListener = new LegacyCombatListener(this, attributeModifier);
            attributeModifier.initializeAttributes();
            getServer().getPluginManager().registerEvents(legacyCombatListener, this);
        } else {
            attributeModifier = new AttributeModifier(this);
            attributeModifier.initializeAttributes();
            legacyCombatListener = null;
        }
    }
}