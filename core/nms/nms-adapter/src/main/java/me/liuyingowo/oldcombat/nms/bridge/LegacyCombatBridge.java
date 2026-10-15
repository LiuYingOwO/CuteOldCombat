package me.liuyingowo.oldcombat.nms.bridge;

import java.lang.reflect.Field;

public class LegacyCombatBridge {

    public static volatile boolean enabled;
    public static volatile boolean disable_sweep_attack;
    public static volatile boolean enable_legacy_damage;
    public static volatile boolean critical_while_sprinting;

    private LegacyCombatBridge() {}

    private static final Class<?> legacyCombatBridgeClass;

    static {
        try {
            legacyCombatBridgeClass = Class.forName(
                    "me.liuyingowo.oldcombat.nms.bridge.LegacyCombatBridge",
                    false,
                    null
            );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        enabled = true;
        disable_sweep_attack = true;
        enable_legacy_damage = true;
        critical_while_sprinting = true;
    }

    public static void setEnabled(boolean enabled) {
        try {
            Field field = legacyCombatBridgeClass.getDeclaredField("enabled");
            field.set(null, enabled);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setDisableSweepAttack(boolean disable) {
        try {
            Field field = legacyCombatBridgeClass.getDeclaredField("disable_sweep_attack");
            field.set(null, disable);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setLegacyDamage(boolean legacy_damage) {
        try {
            Field field = legacyCombatBridgeClass.getDeclaredField("enable_legacy_damage");
            field.set(null, legacy_damage);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setCriticalWhileSprinting(boolean critical_while_sprinting) {
        try {
            Field field = legacyCombatBridgeClass.getDeclaredField("critical_while_sprinting");
            field.set(null, critical_while_sprinting);
        } catch (ReflectiveOperationException ignored) {}
    }
}
