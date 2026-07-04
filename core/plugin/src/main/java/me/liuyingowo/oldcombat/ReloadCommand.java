package me.liuyingowo.oldcombat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReloadCommand extends Command implements PluginIdentifiableCommand {

    private final CuteOldCombat plugin;

    public ReloadCommand(CuteOldCombat plugin) {
        super(
                "cuteoldcombat",
                "Reload CuteOldCombat configuration and NMS hooks.",
                "/cuteoldcombat reload",
                List.of("oldcombat", "coc")
        );

        this.plugin = plugin;
        setPermission("cuteoldcombat.reload");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!testPermission(sender)) {
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            boolean installed = plugin.reloadOldCombat();

            if (installed) {
                sender.sendMessage("§a[OldCombat] Reloaded. NMS patches enabled.");
            } else {
                sender.sendMessage("§e[OldCombat] Reloaded, but NMS patches were not installed. Only Bukkit-side fallbacks are active.");
            }

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