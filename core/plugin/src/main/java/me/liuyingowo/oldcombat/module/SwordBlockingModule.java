package me.liuyingowo.oldcombat.module;

import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import me.liuyingowo.oldcombat.CuteOldCombat;
import me.liuyingowo.oldcombat.nms.NmsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class SwordBlockingModule extends Module {

    public SwordBlockingModule(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onUnload() {

    }

    /** 总开关 + 配置开关 + 适配器能力位，三者都满足才启用。 */
    static boolean isSupported(JavaPlugin plugin) {
        return plugin.getConfig().getBoolean("enable")
                && plugin.getConfig().getBoolean("sword-blocking.enabled", true)
                && NmsManager.isInstalled()
                && NmsManager.getAdapter().supportsLegacySwordBlocking();
    }

    /* ------------------------------ 打上组件 ------------------------------ */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        scheduleApply(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        scheduleApply(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        scheduleApply(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        final Player player = event.getPlayer();
        final int previousSlot = event.getPreviousSlot();
        // 1 tick 后再做，理由同 LegacyCombatListener：让原版先把这个 tick 的背包/属性结算完
        player.getScheduler().runDelayed(plugin, scheduledTask -> {
            clearSlot(player, previousSlot);
            applyMainHand(player);
        }, null, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        final Player player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, scheduledTask -> {
            // 交换后主副手内容对调：两边都要重新看一遍（副手那个要摘掉）
            clearHeld(player);
            applyMainHand(player);
        }, null, 1L);
    }

    /* ------------------------------ 验收用观测点 ------------------------------ */

    /**
     * 松开右键时打一行：{@code ticksHeldFor} 就是服务端认定"你在使用这件物品"的 tick 数。
     *
     * <p>这是验证整条链路最省事的办法——按住右键 1 秒再松开，日志里应该出现 ~20。
     * 如果是 0 或者这个事件根本不出现，说明服务端没进"使用中"状态（组件没打上/没同步）。</p>
     *
     * <p>注意取的是 {@code getTicksHeldFor()}：1.20.6 和 1.21.11 的 paper-api 都是这个名字
     * （插件模块编译在 paper-api 1.20.6 上，所以只能用两代都有的访问器）。</p>
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerStopUsingItem(PlayerStopUsingItemEvent event) {
        if (!plugin.getConfig().getBoolean("sword-blocking.debug", false)) {
            return;
        }
        plugin.getLogger().info("[sword-blocking] stop-using"
                + " player=" + event.getPlayer().getName()
                + " item=" + event.getItem().getType()
                + " ticksHeldFor=" + event.getTicksHeldFor());
    }

    /* ------------------------------ 摘掉组件 ------------------------------ */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 组件跟着物品进 playerdata，退出时必须立刻清，否则下次上线还带着
        clearHeld(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // 掉落物会落到杀人者手里；keepInventory 时物品还在背包（那个由下次切槽兜住）
        for (ItemStack drop : event.getDrops()) {
            clearStack(drop);
        }
    }

    /* ------------------------------ 实现 ------------------------------ */

    /** 供插件启用/重载时对在线玩家补打一遍。 */
    public void applyNow(Player player) {
        if (!isSupported(plugin)) {
            return;
        }
        applyMainHand(player);
    }

    private void scheduleApply(Player player) {
        player.getScheduler().runDelayed(plugin, scheduledTask -> applyMainHand(player), null, 1L);
    }

    private void applyMainHand(Player player) {
        if (!isSupported(plugin)) {
            return;
        }
        final PlayerInventory inventory = player.getInventory();
        final ItemStack mainHand = inventory.getItemInMainHand();
        if (applyStack(mainHand)) {
            // 必须写回：组件是打在没有视图的物品对象上的，不写回不会触发槽位同步，
            // 客户端收不到组件，姿势就不会出现
            inventory.setItemInMainHand(mainHand);
            debug(player, "applied");
        }
    }

    private boolean applyStack(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return false;
        }
        return NmsManager.getAdapter().applyLegacySwordBlocking(stack);
    }

    private static void clearHeld(Player player) {
        final PlayerInventory inventory = player.getInventory();
        final ItemStack mainHand = inventory.getItemInMainHand();
        if (clearStack(mainHand)) {
            inventory.setItemInMainHand(mainHand);
        }
        final ItemStack offHand = inventory.getItemInOffHand();
        if (clearStack(offHand)) {
            inventory.setItemInOffHand(offHand);
        }
    }

    private void clearSlot(Player player, int slot) {
        final PlayerInventory inventory = player.getInventory();
        final ItemStack item = inventory.getItem(slot);
        if (clearStack(item)) {
            inventory.setItem(slot, item);
        }
    }

    private static boolean clearStack(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || !NmsManager.isInstalled()) {
            return false;
        }
        return NmsManager.getAdapter().clearLegacySwordBlocking(stack);
    }

    /**
     * 卸下/关闭时的兜底清理，把在线玩家手里的剑还原干净。
     *
     * <p>两条路径的差别是必须的：正常重载时走玩家自己的 region 调度器；
     * 而 {@code onDisable} 期间插件调度器已经全部不可用（{@code isEnabled()} 已是 false，
     * 任何 runTask 都会抛 IllegalPluginAccessException），只能就地执行。</p>
     */
    static void clearAll(CuteOldCombat plugin) {
        final boolean schedulerAvailable = plugin.isEnabled();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (schedulerAvailable) {
                player.getScheduler().run(plugin, scheduledTask -> clearHeld(player), null);
            } else {
                clearHeld(player);
            }
        }
    }

    private void debug(Player player, String what) {
        if (!plugin.getConfig().getBoolean("sword-blocking.debug", false)) {
            return;
        }
        plugin.getLogger().info("[sword-blocking] " + what
                + " player=" + player.getName()
                + " main=" + player.getInventory().getItemInMainHand().getType()
                + " serverIsBlocking=" + NmsManager.getAdapter().isLegacySwordBlocking(player));
    }

}
