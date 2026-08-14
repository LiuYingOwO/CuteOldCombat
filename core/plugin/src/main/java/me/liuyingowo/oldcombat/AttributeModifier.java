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
        if (plugin.getConfig().getBoolean("reach.enabled")) {
            NmsManager.getAdapter().applyLegacyEntityInteractionRange(player, plugin.getConfig().getDouble("reach.range"));
        }
        NmsManager.getAdapter().applyLegacyAttackSpeed(player);
    }

    public void restoreAttributes(Player player) {
        NmsManager.getAdapter().restoreLegacyEntityInteractionRange(player);
        NmsManager.getAdapter().restoreLegacyAttackSpeed(player);
    }
}
