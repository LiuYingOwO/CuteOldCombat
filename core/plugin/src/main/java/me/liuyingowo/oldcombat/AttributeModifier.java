package me.liuyingowo.oldcombat;

import me.liuyingowo.oldcombat.nms.NmsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AttributeModifier {

    private final CuteOldCombat plugin;

    public AttributeModifier(CuteOldCombat plugin) {
        this.plugin = plugin;
    }

    public void initializeAttributes() {
        if (plugin.getConfig().getBoolean("enable")) {
            applyAllAttributesForAllPlayer();
            plugin.getLogger().info("Legacy Attack Attributes applied!");
        } else {
            restoreAllAttributesForAllPlayer();
            plugin.getLogger().warning("CuteOldCombat is disabled via config; combat attributes have been restored.");
        }
    }

    public void applyAllAttributesForAllPlayer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyAttributes(player);
        }
    }

    public void restoreAllAttributesForAllPlayer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            restoreAttributes(player);
        }
    }

    public void applyAttributes(Player player) {
        if (player == null || !NmsManager.isInstalled()) {
            return;
        }
        var adapter = NmsManager.getAdapter();
        if (plugin.getConfig().getBoolean("reach.enabled")) {
            adapter.applyLegacyEntityInteractionRange(player, plugin.getConfig().getDouble("reach.range"));
        }
        adapter.applyLegacyAttackSpeed(player);
    }

    public void restoreAttributes(Player player) {
        if (player == null || !NmsManager.isInstalled()) {
            return;
        }
        var adapter = NmsManager.getAdapter();

        adapter.restoreLegacyEntityInteractionRange(player);
        adapter.restoreLegacyAttackSpeed(player);
    }
}
