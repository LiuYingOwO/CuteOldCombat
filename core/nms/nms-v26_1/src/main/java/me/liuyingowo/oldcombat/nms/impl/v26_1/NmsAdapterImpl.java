package me.liuyingowo.oldcombat.nms.impl.v26_1;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import me.liuyingowo.oldcombat.nms.adapter.NmsAdapter;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Logger;

public final class NmsAdapterImpl implements NmsAdapter {

    private static final NamespacedKey ATTACK_KEY = new NamespacedKey("cuteoldcombat", "attack_speed");
    private static final NamespacedKey INTERACTION_KEY = new NamespacedKey("cuteoldcombat", "interaction_range");

    private static final double DEFAULT_ATTACK_SPEED = 100.0D;

    private static final List<AgentPatch> PATCHES = List.of(
            LegacyAttackAdvice.patch(),
            LegacyDamageAdvice.patch(),
            LegacyKnockbackAdvice.patch(),
            LegacySoundEffectAdvice.patch()
    );

    @Override
    public AgentBuilder apply(AgentBuilder agentBuilder, Logger logger) {
        return AgentPatch.applyAll(agentBuilder, logger, PATCHES);
    }

    @Override
    public void applyLegacyAttackSpeed(org.bukkit.entity.Player player) {
        applyAttribute(player, Attribute.ATTACK_SPEED, ATTACK_KEY, DEFAULT_ATTACK_SPEED);
    }

    @Override
    public void restoreLegacyAttackSpeed(org.bukkit.entity.Player player) {
        removeAttribute(player, Attribute.ATTACK_SPEED, ATTACK_KEY);
    }

    @Override
    public void applyLegacyEntityInteractionRange(org.bukkit.entity.Player player, double range) {
        applyAttribute(player, Attribute.ENTITY_INTERACTION_RANGE, INTERACTION_KEY, range);
    }

    @Override
    public void restoreLegacyEntityInteractionRange(org.bukkit.entity.Player player) {
        removeAttribute(player, Attribute.ENTITY_INTERACTION_RANGE, INTERACTION_KEY);
    }

    private void applyAttribute(Player player, Attribute attribute, NamespacedKey key, double desired) {
        if (player == null) {
            return;
        }
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (modifier.getKey().equals(key)) {
                instance.removeModifier(modifier);
                break;
            }
        }
        double delta = desired - instance.getValue();
        if (delta == 0.0D) {
            return;
        }
        instance.addTransientModifier(new AttributeModifier(key, delta, AttributeModifier.Operation.ADD_NUMBER));
    }


    private void removeAttribute(Player player, Attribute attribute, NamespacedKey key) {
        if (player == null) {
            return;
        }
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            for (AttributeModifier modifier : instance.getModifiers()) {
                if (modifier.getKey().equals(key)) {
                    instance.removeModifier(modifier);
                    return;
                }
            }
        }
    }
}
