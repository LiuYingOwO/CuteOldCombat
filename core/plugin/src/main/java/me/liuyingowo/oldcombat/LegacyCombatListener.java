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

/**
 * 还原 1.8 战斗体验的监听器。
 * <p>监听器的注册与否严格跟随 {@code enable} 配置：
 * 仅在 {@code enable=true} 时由插件注册；因此监听器生命周期由插件
 * {@code registerEvents / unregisterAll} 控制，事件处理内不再自行判断开关。
 * </p>
 */
public final class LegacyCombatListener implements Listener {

    private final CuteOldCombat plugin;
    private final AttributeModifier attributeModifier;

    LegacyCombatListener(CuteOldCombat plugin, AttributeModifier attributeModifier) {
        this.plugin = plugin;
        this.attributeModifier = attributeModifier;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        attributeModifier.applyAttributes(event.getPlayer());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> attributeModifier.applyAttributes(event.getPlayer()), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> attributeModifier.applyAttributes(event.getPlayer()), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        attributeModifier.applyAttributes(event.getPlayer());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> attributeModifier.applyAttributes(event.getPlayer()), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> attributeModifier.applyAttributes(event.getPlayer()), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSweepDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            event.setCancelled(true);
        }
    }
}