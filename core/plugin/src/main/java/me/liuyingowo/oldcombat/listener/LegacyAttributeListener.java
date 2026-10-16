package me.liuyingowo.oldcombat.listener;

import me.liuyingowo.oldcombat.AttributeModifier;
import me.liuyingowo.oldcombat.CuteOldCombat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class LegacyAttributeListener implements Listener {

    private final CuteOldCombat plugin;
    private final AttributeModifier attributeModifier;

    public LegacyAttributeListener(CuteOldCombat plugin, AttributeModifier attributeModifier) {
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
}