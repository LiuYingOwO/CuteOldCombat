package me.liuyingowo.oldcombat;

import me.liuyingowo.oldcombat.nms.NmsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public final class LegacyCombatListener implements Listener {

    private final CuteOldCombat plugin;

    public LegacyCombatListener(CuteOldCombat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        applyLegacyAttackSpeed(event.getPlayer());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> applyLegacyAttackSpeed(event.getPlayer()), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> applyLegacyAttackSpeed(event.getPlayer()), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        applyLegacyAttackSpeed(event.getPlayer());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> applyLegacyAttackSpeed(event.getPlayer()), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> applyLegacyAttackSpeed(event.getPlayer()), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSweepDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            event.setCancelled(true);
        }
    }

    private void applyLegacyAttackSpeed(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        NmsManager.applyLegacyAttackSpeed(player);
    }
}
