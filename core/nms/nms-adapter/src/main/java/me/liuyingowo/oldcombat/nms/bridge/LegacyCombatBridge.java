package me.liuyingowo.oldcombat.nms.bridge;

import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.util.logging.Logger;

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

    public static void sync(FileConfiguration config, Logger logger) {
        boolean enabled = config.getBoolean("legacy_combat.enabled");
        double attack_speed = config.getDouble("legacy_combat.attack-speed");
        boolean force_legacy = config.getBoolean("legacy_combat.force-legacy");

        setEnabled(enabled);
        setAttackSpeed(attack_speed);
        setForceLegacy(force_legacy);

        logger.info("LegacyCombat bridge updated: enabled=" + enabled
                + ", attack_speed=" + attack_speed
                + ", force_legacy=" + force_legacy
        );
    }
}
