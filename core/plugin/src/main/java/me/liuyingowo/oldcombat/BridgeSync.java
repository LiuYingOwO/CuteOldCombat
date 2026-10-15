package me.liuyingowo.oldcombat;

import me.liuyingowo.oldcombat.nms.bridge.KnockbackBridge;
import me.liuyingowo.oldcombat.nms.bridge.LegacyCombatBridge;
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

        logger.info("Knockback bridge updated: enabled=" + enabled
                + ", horizontal=" + horizontal
                + ", vertical=" + vertical
                + ", verticalLimit=" + verticalLimit
                + ", friction=" + friction
                + ", minDirectionLength=" + minDirectionLength
                + ", applyResistance=" + applyResistance);
    }

    public static void syncLegacyCombat(Plugin plugin) {
        var config = plugin.getConfig();
        var logger = plugin.getLogger();

        boolean enabled = config.getBoolean("legacy_combat.enabled");
        double attack_speed = config.getDouble("legacy_combat.attack-speed");
        boolean force_legacy = config.getBoolean("legacy_combat.force-legacy");

        LegacyCombatBridge.setEnabled(enabled);
        LegacyCombatBridge.setAttackSpeed(attack_speed);
        LegacyCombatBridge.setForceLegacy(force_legacy);

        logger.info("LegacyCombat bridge updated: enabled=" + enabled
                + ", attack_speed=" + attack_speed
                + ", force_legacy=" + force_legacy
        );
    }

}
