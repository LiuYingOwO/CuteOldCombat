package me.liuyingowo.oldcombat.nms.bridge;

import java.lang.reflect.Field;

public class SoundBridge {

    public static volatile boolean disable_sweep;
    public static volatile boolean disable_attack;
    public static volatile boolean disable_knockback;
    public static volatile boolean disable_nodamage;

    private SoundBridge() {}

    private static final Class<?> soundBridgeClass;

    static {
        try {
            soundBridgeClass = Class.forName(
                    "me.liuyingowo.oldcombat.nms.bridge.SoundBridge",
                    false,
                    null
            );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        disable_attack = true;
        disable_sweep = true;
        disable_knockback = true;
        disable_nodamage = true;
    }

    public static void setDisableSweep(boolean disable_sweep) {
        try {
            Field field = soundBridgeClass.getDeclaredField("disable_sweep");
            field.set(null, disable_sweep);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setDisableAttack(boolean disable_attack) {
        try {
            Field field = soundBridgeClass.getDeclaredField("disable_attack");
            field.set(null, disable_attack);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setDisableKnockback(boolean disable_knockback) {
        try {
            Field field = soundBridgeClass.getDeclaredField("disable_knockback");
            field.set(null, disable_knockback);
        } catch (ReflectiveOperationException ignored) {}
    }

    public static void setDisableNodamage(boolean disable_nodamage) {
        try {
            Field field = soundBridgeClass.getDeclaredField("disable_nodamage");
            field.set(null, disable_nodamage);
        } catch (ReflectiveOperationException ignored) {}
    }
}
