package me.liuyingowo.oldcombat.module;

import me.liuyingowo.oldcombat.nms.NmsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LegacyAttributeModule extends Module {

    public LegacyAttributeModule(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onLoad() {
        initializeAttributes();
    }

    @Override
    public void onUnload() {
        for (var player : Bukkit.getOnlinePlayers()) {
            restoreAttributes(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> applyAttributes(player), null, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> applyAttributes(player), null, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        var player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> applyAttributes(player), null, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        var player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> applyAttributes(player), null, 1L);
    }

    public void initializeAttributes() {
        if (plugin.getConfig().getBoolean("enable")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getScheduler().run(plugin, scheduledTask -> applyAttributes(player), null);
            }
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getScheduler().run(plugin, scheduledTask -> restoreAttributes(player), null);
            }
        }
    }

    public void applyAttributes(Player player) {
        if (player == null || !NmsManager.isInstalled()) {
            return;
        }
        var adapter = NmsManager.getAdapter();
        var config = plugin.getConfig();

        if (config.getBoolean("legacy_combat.enabled")) {
            adapter.applyLegacyEntityInteractionRange(player, config.getDouble("legacy_combat.reach-range"));
            adapter.applyLegacyAttackSpeed(player, config.getDouble("legacy_combat.attack-cooldown"));
        } else {
            adapter.restoreLegacyEntityInteractionRange(player);
            adapter.restoreLegacyAttackSpeed(player);
        }
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
