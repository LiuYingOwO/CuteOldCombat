package me.liuyingowo.oldcombat.nms.adapter;

import net.bytebuddy.agent.builder.AgentBuilder;
import org.bukkit.entity.Player;

public interface NmsAdapter {

    AgentBuilder apply(AgentBuilder agentBuilder, java.util.logging.Logger logger);

    void applyLegacyAttackSpeed(Player player, double speed);

    void restoreLegacyAttackSpeed(Player player);

    void applyLegacyEntityInteractionRange(Player player, double range);

    void restoreLegacyEntityInteractionRange(Player player);

    default boolean supportsLegacySwordBlocking() {
        return false;
    }

    default boolean applyLegacySwordBlocking(org.bukkit.inventory.ItemStack stack) {
        return false;
    }

    default boolean clearLegacySwordBlocking(org.bukkit.inventory.ItemStack stack) {
        return false;
    }

    // for debug
    default boolean isLegacySwordBlocking(Player player) {
        return false;
    }
}
