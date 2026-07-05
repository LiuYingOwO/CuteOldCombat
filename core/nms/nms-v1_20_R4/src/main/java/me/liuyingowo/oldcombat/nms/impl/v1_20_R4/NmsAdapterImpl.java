package me.liuyingowo.oldcombat.nms.impl.v1_20_R4;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import me.liuyingowo.oldcombat.nms.adapter.NmsAdapter;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

import java.util.List;
import java.util.logging.Logger;

public final class NmsAdapterImpl implements NmsAdapter {

    private static final List<AgentPatch> PATCHES = List.of(
            LegacyKnockbackAdvice.patch(),
            LegacyDamageAdvice.patch(),
            LegacyAttackAdvice.patch(),
            LegacySweepAttackAdvice.patch(),
            LegacySoundEffectAdvice.patch()
    );

    @Override
    public AgentBuilder apply(AgentBuilder agentBuilder, Logger logger) {
        return AgentPatch.applyAll(agentBuilder, logger, PATCHES);
    }

    @Override
    public void applyLegacyAttackSpeed(org.bukkit.entity.Player player) {
        applyAttackSpeed(player, Attribute.GENERIC_ATTACK_SPEED);
    }

    private static void applyAttackSpeed(org.bukkit.entity.Player player, Attribute attribute) {
        AttributeInstance attackSpeed = player.getAttribute(attribute);
        if (attackSpeed != null && attackSpeed.getBaseValue() < 100.0D) {
            attackSpeed.setBaseValue(100.0D);
        }
    }
}
