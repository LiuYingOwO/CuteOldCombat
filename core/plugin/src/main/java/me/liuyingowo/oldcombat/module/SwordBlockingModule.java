package me.liuyingowo.oldcombat.module;

import me.liuyingowo.oldcombat.nms.Version;
import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public class SwordBlockingModule extends Module {

    public SwordBlockingModule(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onLoad() {
        warnIfBlockingUnavailable();
        initializeBlocking();
    }

    @Override
    public void onUnload() {
        restoreAllBlocking();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> applyBlocking(player), null, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        var player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> applyBlocking(player), null, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        var player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> applyBlocking(player), null, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        var player = event.getPlayer();
        var previousSlot = event.getPreviousSlot();
        player.getScheduler().runDelayed(plugin, scheduledTask -> {
            restoreSlot(player, previousSlot);
            applyBlocking(player);
        }, null, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        var player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> {
            restoreHands(player);
            applyBlocking(player);
        }, null, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        var dropped = event.getItemDrop().getItemStack();
        if (SwordBlockingComponents.clear(dropped)) {
            event.getItemDrop().setItemStack(dropped);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        for (ItemStack drop : event.getDrops()) {
            SwordBlockingComponents.clear(drop);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        sweepBlocking(event.getPlayer());
    }

    public void initializeBlocking() {
        if (blockingEnabled()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getScheduler().run(plugin, scheduledTask -> applyBlocking(player), null);
            }
        } else {
            restoreAllBlocking();
        }
    }

    public void applyBlocking(Player player) {
        if (player == null || !blockingEnabled()) {
            return;
        }
        var inventory = player.getInventory();
        var mainHand = inventory.getItemInMainHand();
        if (SwordBlockingComponents.apply(mainHand)) {
            inventory.setItemInMainHand(mainHand);
        }
    }

    // 清主手 + 副手 + 整个背包
    public void sweepBlocking(Player player) {
        if (player == null) {
            return;
        }
        var inventory = player.getInventory();

        var mainHand = inventory.getItemInMainHand();
        if (SwordBlockingComponents.clear(mainHand)) {
            inventory.setItemInMainHand(mainHand);
        }
        var offHand = inventory.getItemInOffHand();
        if (SwordBlockingComponents.clear(offHand)) {
            inventory.setItemInOffHand(offHand);
        }

        var storage = inventory.getStorageContents();
        for (int slot = 0; slot < storage.length; slot++) {
            if (SwordBlockingComponents.clear(storage[slot])) {
                inventory.setItem(slot, storage[slot]);
            }
        }
    }

    public void restoreHands(Player player) {
        if (player == null) {
            return;
        }
        var inventory = player.getInventory();
        var mainHand = inventory.getItemInMainHand();
        if (SwordBlockingComponents.clear(mainHand)) {
            inventory.setItemInMainHand(mainHand);
        }
        var offHand = inventory.getItemInOffHand();
        if (SwordBlockingComponents.clear(offHand)) {
            inventory.setItemInOffHand(offHand);
        }
    }

    public void restoreAllBlocking() {
        boolean schedulerAvailable = plugin.isEnabled();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (schedulerAvailable) {
                player.getScheduler().run(plugin, scheduledTask -> sweepBlocking(player), null);
            } else {
                sweepBlocking(player);
            }
        }
    }

    private void restoreSlot(Player player, int slot) {
        var inventory = player.getInventory();
        var item = inventory.getItem(slot);
        if (SwordBlockingComponents.clear(item)) {
            inventory.setItem(slot, item);
        }
    }

    private boolean blockingEnabled() {
        return SwordBlockingComponents.available()
                && plugin.getConfig().getBoolean("enable")
                && plugin.getConfig().getBoolean("sword_blocking.enabled", true);
    }

    private void warnIfBlockingUnavailable() {
        if (!plugin.getConfig().getBoolean("enable")
                || !plugin.getConfig().getBoolean("sword-blocking.enabled", true)
                || SwordBlockingComponents.available()) {
            return;
        }

        String version = Version.getCurrentMinecraftVersion();
        if (Version.CURRENT == null || Version.CURRENT == Version.v1_20_R4) {
            plugin.getLogger().info("Sword blocking is unavailable on " + version
                    + "; the feature stays disabled.");
            return;
        }

        plugin.getLogger().warning("Sword blocking is DISABLED on " + version
                + ": Paper data-component API could not be initialised ("
                + SwordBlockingComponents.failureReason() + "). "
        );
    }


    public static class SwordBlockingComponents {
        private static final float CONSUME_SECONDS = 2_592_000.0F;

        private static final boolean AVAILABLE;
        private static final Method SET_DATA;
        private static final Method HAS_DATA;
        private static final Method UNSET_DATA;
        private static final Object CONSUMABLE_TYPE;
        private static final Object CONSUMABLE_VALUE;
        private static final String FAILURE_REASON;

        static {
            boolean available = false;
            Method setData = null;
            Method hasData = null;
            Method unsetData = null;
            Object type = null;
            Object value = null;
            String failure = null;

            try {
                Class<?> componentTypes = Class.forName("io.papermc.paper.datacomponent.DataComponentTypes");
                Class<?> componentType = Class.forName("io.papermc.paper.datacomponent.DataComponentType");
                Class<?> valuedType = Class.forName("io.papermc.paper.datacomponent.DataComponentType$Valued");
                Class<?> consumable = Class.forName("io.papermc.paper.datacomponent.item.Consumable");
                Class<?> consumableBuilder = Class.forName("io.papermc.paper.datacomponent.item.Consumable$Builder");
                Class<?> animation = Class.forName("io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation");

                type = componentTypes.getField("CONSUMABLE").get(null);

                Object builder = consumable.getMethod("consumable").invoke(null);
                builder = consumableBuilder.getMethod("consumeSeconds", float.class).invoke(builder, CONSUME_SECONDS);
                builder = consumableBuilder.getMethod("animation", animation).invoke(builder, animation.getField("BLOCK").get(null));
                builder = consumableBuilder.getMethod("hasConsumeParticles", boolean.class).invoke(builder, false);
                value = consumableBuilder.getMethod("build").invoke(builder);

                setData = ItemStack.class.getMethod("setData", valuedType, Object.class);
                hasData = ItemStack.class.getMethod("hasData", componentType);
                unsetData = ItemStack.class.getMethod("unsetData", componentType);
                available = true;
            } catch (Throwable t) {
                // 1.20.6 / 1.21.x 早期版本的 paper-api：没有 data component API，功能自动停用
                available = false;
                failure = t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage());
            }

            AVAILABLE = available;
            SET_DATA = setData;
            HAS_DATA = hasData;
            UNSET_DATA = unsetData;
            CONSUMABLE_TYPE = type;
            CONSUMABLE_VALUE = value;
            FAILURE_REASON = failure;
        }

        private SwordBlockingComponents() {
        }

        public static boolean available() {
            return AVAILABLE;
        }

        public static String failureReason() {
            return FAILURE_REASON;
        }

        public static boolean isSword(ItemStack stack) {
            return stack != null && !stack.getType().isAir() && Tag.ITEMS_SWORDS.isTagged(stack.getType());
        }

        public static boolean apply(ItemStack stack) {
            if (!AVAILABLE || !isSword(stack)) {
                return false;
            }
            try {
                if ((Boolean) HAS_DATA.invoke(stack, CONSUMABLE_TYPE)) {
                    return false;
                }
                SET_DATA.invoke(stack, CONSUMABLE_TYPE, CONSUMABLE_VALUE);
                return true;
            } catch (Throwable ignored) {
                return false;
            }
        }

        public static boolean clear(ItemStack stack) {
            if (!AVAILABLE || !isSword(stack)) {
                return false;
            }
            try {
                if (!(Boolean) HAS_DATA.invoke(stack, CONSUMABLE_TYPE)) {
                    return false;
                }
                UNSET_DATA.invoke(stack, CONSUMABLE_TYPE);
                return true;
            } catch (Throwable ignored) {
                return false;
            }
        }
    }
}
