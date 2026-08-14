package me.liuyingowo.oldcombat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class ReloadCommand extends Command implements PluginIdentifiableCommand {

    private final CuteOldCombat plugin;
    private ReloadCommand oldCombatCommand;

    public ReloadCommand(CuteOldCombat plugin) {
        super(
                "cuteoldcombat",
                "Reload CuteOldCombat configuration and NMS hooks.",
                "/cuteoldcombat reload",
                List.of("oldcombat")
        );

        this.plugin = plugin;
        setPermission("cuteoldcombat.reload");

        if (plugin.getServer().getPluginManager().getPermission("cuteoldcombat.reload") == null) {
            plugin.getServer().getPluginManager().addPermission(
                    new Permission("cuteoldcombat.reload", PermissionDefault.OP)
            );
        }
        oldCombatCommand = this;

        plugin.getServer().getCommandMap().register(
                getName().toLowerCase(Locale.ROOT),
                oldCombatCommand
        );
    }

    public void unregister(CuteOldCombat plugin) {
        if (oldCombatCommand != null) {
            oldCombatCommand.unregister(plugin.getServer().getCommandMap());
            oldCombatCommand = null;
        }

    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!testPermission(sender)) {
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

            plugin.onDisable();
            plugin.onLoad();
            plugin.onEnable();

            sender.sendMessage("§a插件重载成功!");
            return true;
        }

        sender.sendMessage("§eUsage: /" + label + " reload");
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(
            @NotNull CommandSender sender,
            @NotNull String alias,
            @NotNull String[] args
    ) throws IllegalArgumentException {
        if (!sender.hasPermission("cuteoldcombat.reload")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("reload"), new ArrayList<>());
        }

        return Collections.emptyList();
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }
}