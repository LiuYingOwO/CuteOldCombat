package me.liuyingowo.oldcombat.nms.adapter;

/**
 * Loaded into bootstrap by {@code KnockbackBridgeInstaller}.
 *
 * <p>NMS classes cannot see the Bukkit plugin classloader, so injected advice
 * must only touch classes that are visible from the NMS side. Keep this class
 * dependency-free: primitives only, no Bukkit/Paper/plugin types.</p>
 */
public final class KnockbackBridge {

    public static volatile boolean enabled = false;
    public static volatile double horizontal = 1.0D;
    public static volatile double vertical = 0.4000000059604645D;
    public static volatile double verticalLimit = 0.4000000059604645D;
    public static volatile double friction = 0.5D;
    public static volatile double minDirectionLength = 1.0E-5D;
    public static volatile boolean applyResistance = true;

    private KnockbackBridge() {}

    public static void update(boolean enabled,
                              double horizontal,
                              double vertical,
                              double verticalLimit,
                              double friction,
                              double minDirectionLength,
                              boolean applyResistance) {
        KnockbackBridge.enabled = enabled;
        KnockbackBridge.horizontal = horizontal;
        KnockbackBridge.vertical = vertical;
        KnockbackBridge.verticalLimit = verticalLimit;
        KnockbackBridge.friction = friction;
        KnockbackBridge.minDirectionLength = minDirectionLength;
        KnockbackBridge.applyResistance = applyResistance;
    }
}
