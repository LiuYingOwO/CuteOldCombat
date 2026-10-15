package me.liuyingowo.oldcombat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class LegacyCombatListener implements Listener {

    private final CuteOldCombat plugin;
    private final AttributeModifier attributeModifier;

    LegacyCombatListener(CuteOldCombat plugin, AttributeModifier attributeModifier) {
        this.plugin = plugin;
        this.attributeModifier = attributeModifier;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> attributeModifier.applyAttributes(player), null, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> attributeModifier.applyAttributes(player), null, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        var player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> attributeModifier.applyAttributes(player), null, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        var player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> attributeModifier.applyAttributes(player), null, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSweepDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            event.setCancelled(true);
        }
    }
}