package me.liuyingowo.oldcombat.nms.adapter;

import net.bytebuddy.agent.builder.AgentBuilder;
import org.bukkit.entity.Player;

public interface NmsAdapter {

    AgentBuilder apply(AgentBuilder agentBuilder, java.util.logging.Logger logger);

    void applyLegacyAttackSpeed(Player player, double speed);

    void restoreLegacyAttackSpeed(Player player);

    void applyLegacyEntityInteractionRange(Player player, double range);

    void restoreLegacyEntityInteractionRange(Player player);
}
