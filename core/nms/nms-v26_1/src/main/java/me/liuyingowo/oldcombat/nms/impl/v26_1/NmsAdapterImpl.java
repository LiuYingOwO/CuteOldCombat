package me.liuyingowo.oldcombat.nms.impl.v26_1;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import me.liuyingowo.oldcombat.nms.adapter.NmsAdapter;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.List;

public final class NmsAdapterImpl implements NmsAdapter {

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
        applyAttackSpeed(player, Attribute.ATTACK_SPEED);
    }

    private static void applyAttackSpeed(org.bukkit.entity.Player player, Attribute attribute) {
        AttributeInstance attackSpeed = player.getAttribute(attribute);
        if (attackSpeed != null && attackSpeed.getBaseValue() < 100.0D) {
            attackSpeed.setBaseValue(100.0D);
        }
    }
}
