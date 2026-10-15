package me.liuyingowo.oldcombat.nms.bridge;

import java.lang.reflect.Field;

public class LegacyCombatBridge {

    public static volatile boolean enabled;
    public static volatile double attack_speed;
    public static volatile boolean force_legacy;

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
        attack_speed = 100D;
        force_legacy = false;
    }

    public static void setEnabled(boolean enabled) {
        try {
            Field field = legacyCombatBridgeClass.getDeclaredField("enabled");
            field.set(null, enabled);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setAttackSpeed(double speed) {
        try {
            Field field = legacyCombatBridgeClass.getDeclaredField("attack_speed");
            field.set(null, speed);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setForceLegacy(boolean legacy) {
        try {
            Field field = legacyCombatBridgeClass.getDeclaredField("force_legacy");
            field.set(null, legacy);
        } catch (ReflectiveOperationException ignored) {}
    }
}
