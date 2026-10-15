package me.liuyingowo.oldcombat.nms.bridge;

import java.lang.reflect.Field;

/**
 * Loaded into bootstrap by {@code NmsBridgeInstaller}.
 *
 * <p>NMS classes cannot see the Bukkit plugin classloader, so injected advice
 * must only touch classes that are visible from the NMS side. Keep this class
 * dependency-free: primitives only, no Bukkit/Paper/plugin types.</p>
 */
public final class KnockbackBridge {

    public static volatile boolean enabled;
    public static volatile double horizontal;
    public static volatile double vertical;
    public static volatile double verticalLimit;
    public static volatile double friction;
    public static volatile double minDirectionLength;
    public static volatile boolean applyResistance;

    private KnockbackBridge() {}

    private static final Class<?> knockbackBridgeClass;

    static {
        try {
            knockbackBridgeClass = Class.forName(
                    "me.liuyingowo.oldcombat.nms.bridge.KnockbackBridge",
                    false,
                    null
            );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        enabled = true;
        horizontal = 1.0D;
        vertical = 0.4000000059604645D;
        verticalLimit = 0.4000000059604645D;
        friction = 0.5D;
        minDirectionLength = 1.0E-5D;
        applyResistance = true;
    }

    public static void setEnabled(boolean enabled) {
        try {
            Field field = knockbackBridgeClass.getDeclaredField("enabled");
            field.set(null, enabled);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setHorizontal(double horizontal) {
        try {
            Field field = knockbackBridgeClass.getDeclaredField("horizontal");
            field.set(null, horizontal);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setVertical(double vertical) {
        try {
            Field field = knockbackBridgeClass.getDeclaredField("vertical");
            field.set(null, vertical);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setVerticalLimit(double verticalLimit) {
        try {
            Field field = knockbackBridgeClass.getDeclaredField("verticalLimit");
            field.set(null, verticalLimit);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setFriction(double friction) {
        try {
            Field field = knockbackBridgeClass.getDeclaredField("friction");
            field.set(null, friction);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setMinDirectionLength(double minDirectionLength) {
        try {
            Field field = knockbackBridgeClass.getDeclaredField("minDirectionLength");
            field.set(null, minDirectionLength);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setApplyResistance(boolean applyResistance) {
        try {
            Field field = knockbackBridgeClass.getDeclaredField("applyResistance");
            field.set(null, applyResistance);
        } catch (ReflectiveOperationException ignored) {}
    }
}
