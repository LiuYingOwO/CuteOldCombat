package me.liuyingowo.oldcombat;

import me.liuyingowo.oldcombat.loader.LegacyCombatInstaller;
import me.liuyingowo.oldcombat.nms.adapter.KnockbackSettings;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class CuteOldCombat extends JavaPlugin {

    private boolean installed;
    private Command oldCombatCommand;

    @Override
    public void onLoad() {
        loadKnockbackSettings();
        installed = LegacyCombatInstaller.install(getLogger());
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new LegacyCombatListener(this), this);
        registerCommand();

        if (installed) {
            getLogger().info("[OldCombat] 1.8 combat patches enabled.");
        } else {
            getLogger().warning("[OldCombat] NMS patches were not installed. Only Bukkit-side fallbacks are active.");
        }
    }

    @Override
    public void onDisable() {
        if (oldCombatCommand != null) {
            oldCombatCommand.unregister(getServer().getCommandMap());
            oldCombatCommand = null;
        }

        LegacyCombatInstaller.uninstall(getLogger());
    }

    public boolean reloadOldCombat() {
        loadKnockbackSettings();
        installed = LegacyCombatInstaller.install(getLogger());
        return installed;
    }

    public boolean isInstalled() {
        return installed;
    }

    public KnockbackSettings.Snapshot getKnockbackSettings() {
        return KnockbackSettings.current();
    }

    private void loadKnockbackSettings() {
        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        KnockbackSettings.Snapshot settings = new KnockbackSettings.Snapshot(
                getConfig().getBoolean("knockback.enabled", KnockbackSettings.DEFAULT.isEnabled()),
                getConfig().getDouble("knockback.horizontal", KnockbackSettings.DEFAULT_HORIZONTAL),
                getConfig().getDouble("knockback.vertical", KnockbackSettings.DEFAULT_VERTICAL),
                getConfig().getDouble("knockback.vertical-limit", KnockbackSettings.DEFAULT_VERTICAL_LIMIT),
                getConfig().getDouble("knockback.friction", KnockbackSettings.DEFAULT_FRICTION),
                getConfig().getDouble("knockback.min-direction-length", KnockbackSettings.DEFAULT_MIN_DIRECTION_LENGTH),
                getConfig().getBoolean("knockback.apply-resistance", KnockbackSettings.DEFAULT_APPLY_RESISTANCE)
        );

        KnockbackSettings.update(settings);
        getLogger().info("[OldCombat] Knockback settings loaded: " + settings);
    }

    private void registerCommand() {
        if (getServer().getPluginManager().getPermission("cuteoldcombat.reload") == null) {
            getServer().getPluginManager().addPermission(
                    new Permission("cuteoldcombat.reload", PermissionDefault.OP)
            );
        }

        oldCombatCommand = new OldCombatCommand(this);
        getServer().getCommandMap().register(
                getName().toLowerCase(Locale.ROOT),
                oldCombatCommand
        );
    }
}