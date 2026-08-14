package me.liuyingowo.oldcombat.nms.impl.v1_21_R7;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import me.liuyingowo.oldcombat.nms.adapter.NmsAdapter;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

import java.util.List;
import java.util.logging.Logger;

public final class NmsAdapterImpl implements NmsAdapter {

    private static final double DEFAULT_ENTITY_INTERACTION_RANGE = 3.0D;
    private static final double DEFAULT_ATTACK_SPEED = 4.0D;

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
        applyAttribute(player, Attribute.ATTACK_SPEED, 100.0D);
    }

    @Override
    public void restoreLegacyAttackSpeed(org.bukkit.entity.Player player) {
        applyAttribute(player, Attribute.ATTACK_SPEED, DEFAULT_ATTACK_SPEED);
    }

    @Override
    public void applyLegacyEntityInteractionRange(org.bukkit.entity.Player player, double range) {
        applyAttribute(player, Attribute.ENTITY_INTERACTION_RANGE, range);
    }

    @Override
    public void restoreLegacyEntityInteractionRange(org.bukkit.entity.Player player) {
        applyAttribute(player, Attribute.ENTITY_INTERACTION_RANGE, DEFAULT_ENTITY_INTERACTION_RANGE);
    }

    private static void applyAttribute(org.bukkit.entity.Player player, Attribute attribute, double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null && instance.getBaseValue() != value) {
            instance.setBaseValue(value);
        }
    }
}
