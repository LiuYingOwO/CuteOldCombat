package me.liuyingowo.oldcombat;

import me.liuyingowo.oldcombat.loader.Installer;
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
        loadOldCombatConfig();
        installed = Installer.install(getLogger(), getConfig());
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new LegacyCombatListener(this), this);
        registerCommand();

        if (installed) {
            getLogger().info("Loading Complete >w<.");
        } else {
            getLogger().warning("NMS patches were not installed.");
        }
    }

    @Override
    public void onDisable() {
        if (oldCombatCommand != null) {
            oldCombatCommand.unregister(getServer().getCommandMap());
            oldCombatCommand = null;
        }

        Installer.uninstall(getLogger());
    }

    public boolean reloadOldCombat() {
        loadOldCombatConfig();
        installed = Installer.install(getLogger(), getConfig());
        return installed;
    }

    public boolean isInstalled() {
        return installed;
    }

    private void loadOldCombatConfig() {
        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void registerCommand() {
        if (getServer().getPluginManager().getPermission("cuteoldcombat.reload") == null) {
            getServer().getPluginManager().addPermission(
                    new Permission("cuteoldcombat.reload", PermissionDefault.OP)
            );
        }

        oldCombatCommand = new ReloadCommand(this);
        getServer().getCommandMap().register(
                getName().toLowerCase(Locale.ROOT),
                oldCombatCommand
        );
    }
}