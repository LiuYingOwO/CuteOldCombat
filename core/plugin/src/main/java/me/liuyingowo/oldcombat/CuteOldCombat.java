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

        attributeModifier.restoreAllAttributesForAllPlayer();

        if (legacyCombatListener != null) {
            legacyCombatListener = null;
        }
        oldCombatCommand.unregister(this);
        attributeModifier = null;
    }
}