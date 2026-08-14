package me.liuyingowo.oldcombat.nms.impl.v26_1;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import me.liuyingowo.oldcombat.nms.adapter.NmsAdapter;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.List;

public final class NmsAdapterImpl implements NmsAdapter {

    private static final double DEFAULT_ENTITY_INTERACTION_RANGE = 3.0D;
    private static final double DEFAULT_ATTACK_SPEED = 4.0D;

    private static final List<AgentPatch> PATCHES = List.of(
            LegacyAttackAdvice.patch(),
            LegacyDamageAdvice.patch(),
            LegacyKnockbackAdvice.patch(),
            LegacySoundEffectAdvice.patch(),
            LegacySweepAttackAdvice.patch()
    );

    @Override
    public AgentBuilder apply(AgentBuilder builder, java.util.logging.Logger logger) {
        return AgentPatch.applyAll(builder, logger, PATCHES);
    }

    @Override
    public void applyLegacyAttackSpeed(Player player) {
        applyAttribute(player, Attribute.ATTACK_SPEED, 100.0D);
    }

    @Override
    public void restoreLegacyAttackSpeed(Player player) {
        applyAttribute(player, Attribute.ATTACK_SPEED, DEFAULT_ATTACK_SPEED);
    }

    @Override
    public void applyLegacyEntityInteractionRange(Player player, double range) {
        applyAttribute(player, Attribute.ENTITY_INTERACTION_RANGE, range);
    }

    @Override
    public void restoreLegacyEntityInteractionRange(Player player) {
        applyAttribute(player, Attribute.ENTITY_INTERACTION_RANGE, DEFAULT_ENTITY_INTERACTION_RANGE);
    }

    private static void applyAttribute(org.bukkit.entity.Player player, Attribute attribute, double value) {
        if (player == null) return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null && instance.getBaseValue() != value) {
            instance.setBaseValue(value);
        }
    }
}
