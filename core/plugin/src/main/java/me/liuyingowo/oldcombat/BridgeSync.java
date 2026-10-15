package me.liuyingowo.oldcombat;

import me.liuyingowo.oldcombat.nms.bridge.KnockbackBridge;
import me.liuyingowo.oldcombat.nms.bridge.LegacyCombatBridge;
import me.liuyingowo.oldcombat.nms.bridge.SoundBridge;
import org.bukkit.plugin.Plugin;

public class BridgeSync {

    public static void syncKnockback(Plugin plugin) {
        var config = plugin.getConfig();
        var logger = plugin.getLogger();

        boolean enabled = config.getBoolean("knockback.enabled");
        double horizontal = config.getDouble("knockback.horizontal");
        double vertical = config.getDouble("knockback.vertical");
        double verticalLimit = config.getDouble("knockback.vertical-limit");
        double friction = config.getDouble("knockback.friction");
        double minDirectionLength = config.getDouble("knockback.min-direction-length");
        boolean applyResistance = config.getBoolean("knockback.apply-resistance");

        KnockbackBridge.setEnabled(enabled);
        KnockbackBridge.setHorizontal(horizontal);
        KnockbackBridge.setVertical(vertical);
        KnockbackBridge.setVerticalLimit(verticalLimit);
        KnockbackBridge.setFriction(friction);
        KnockbackBridge.setMinDirectionLength(minDirectionLength);
        KnockbackBridge.setApplyResistance(applyResistance);

        logger.info("Knockback updated: enabled=" + enabled
                + ", horizontal=" + horizontal
                + ", vertical=" + vertical
                + ", verticalLimit=" + verticalLimit
                + ", friction=" + friction
                + ", minDirectionLength=" + minDirectionLength
                + ", applyResistance=" + applyResistance);
    }

    public static void syncLegacyCombat(Plugin plugin) {
        var config = plugin.getConfig();

        boolean enabled = config.getBoolean("legacy_combat.enabled");
        boolean disable_sweep_attack = config.getBoolean("legacy_combat.disable-sweep-attack");
        boolean legacy_damage = config.getBoolean("legacy_combat.enable-legacy-damage");
        boolean critical_while_sprinting = config.getBoolean("legacy_combat.critical-while-sprinting");

        LegacyCombatBridge.setEnabled(enabled);
        LegacyCombatBridge.setDisableSweepAttack(disable_sweep_attack);
        LegacyCombatBridge.setLegacyDamage(legacy_damage);
        LegacyCombatBridge.setCriticalWhileSprinting(critical_while_sprinting);
    }

    public static void syncSound(Plugin plugin) {
        var config = plugin.getConfig();

        boolean disable_sweep = config.getBoolean("sounds.disable-sweep");
        boolean disable_attack = config.getBoolean("sounds.disable-attack");
        boolean disable_knockback = config.getBoolean("sounds.disable-knockback");

        SoundBridge.setDisableSweep(disable_sweep);
        SoundBridge.setDisableAttack(disable_attack);
        SoundBridge.setDisableKnockback(disable_knockback);
    }

}
