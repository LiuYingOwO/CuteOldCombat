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

        KnockbackBridge.enabled = enabled;
        KnockbackBridge.horizontal = horizontal;
        KnockbackBridge.vertical = vertical;
        KnockbackBridge.verticalLimit = verticalLimit;
        KnockbackBridge.friction = friction;
        KnockbackBridge.minDirectionLength = minDirectionLength;
        KnockbackBridge.applyResistance = applyResistance;

//        logger.info("Knockback updated: enabled=" + enabled
//                + ", horizontal=" + horizontal
//                + ", vertical=" + vertical
//                + ", verticalLimit=" + verticalLimit
//                + ", friction=" + friction
//                + ", minDirectionLength=" + minDirectionLength
//                + ", applyResistance=" + applyResistance);
    }

    public static void syncLegacyCombat(Plugin plugin) {
        var config = plugin.getConfig();

        boolean enabled = config.getBoolean("legacy_combat.enabled");
        boolean disable_sweep_attack = config.getBoolean("legacy_combat.disable-sweep-attack");
        boolean legacy_damage = config.getBoolean("legacy_combat.enable-legacy-damage");
        boolean critical_while_sprinting = config.getBoolean("legacy_combat.critical-while-sprinting");

        LegacyCombatBridge.enabled = (enabled);
        LegacyCombatBridge.disable_sweep_attack = (disable_sweep_attack);
        LegacyCombatBridge.enable_legacy_damage = (legacy_damage);
        LegacyCombatBridge.critical_while_sprinting = (critical_while_sprinting);
    }

    public static void syncSound(Plugin plugin) {
        var config = plugin.getConfig();

        boolean disable_sweep = config.getBoolean("sounds.disable-sweep");
        boolean disable_attack = config.getBoolean("sounds.disable-attack");
        boolean disable_knockback = config.getBoolean("sounds.disable-knockback");
        boolean disable_nodamage = config.getBoolean("sounds.disable-nodamage");

        SoundBridge.disable_sweep = (disable_sweep);
        SoundBridge.disable_attack = (disable_attack);
        SoundBridge.disable_knockback = (disable_knockback);
        SoundBridge.disable_nodamage = (disable_nodamage);
    }

}
