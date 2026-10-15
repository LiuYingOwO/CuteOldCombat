package me.liuyingowo.oldcombat.nms.impl.v1_20_R4;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import me.liuyingowo.oldcombat.nms.adapter.NmsAdapter;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class NmsAdapterImpl implements NmsAdapter {

    private static final String ATTACK_SPEED_NAME = "cuteoldcombat:attack_speed";
    private static final String INTERACTION_RANGE_NAME = "cuteoldcombat:entity_interaction_range";

    private static final UUID ATTACK_SPEED_ID = UUID.nameUUIDFromBytes(ATTACK_SPEED_NAME.getBytes());
    private static final UUID INTERACTION_RANGE_ID = UUID.nameUUIDFromBytes(INTERACTION_RANGE_NAME.getBytes());

    private static final double LEGACY_ATTACK_SPEED = 100.0D;

    private static final List<AgentPatch> PATCHES = List.of(
            LegacyKnockbackAdvice.patch(),
            LegacyDamageAdvice.patch(),
            LegacyAttackAdvice.patch(),
            LegacySoundEffectAdvice.patch()
    );

    @Override
    public AgentBuilder apply(AgentBuilder agentBuilder, Logger logger) {
        return AgentPatch.applyAll(agentBuilder, logger, PATCHES);
    }

    @Override
    public void applyLegacyAttackSpeed(org.bukkit.entity.Player player) {
        applyAttribute(player, Attribute.GENERIC_ATTACK_SPEED, ATTACK_SPEED_ID, ATTACK_SPEED_NAME, LEGACY_ATTACK_SPEED);
    }

    @Override
    public void restoreLegacyAttackSpeed(Player player) {
       removeAttribute(player, Attribute.GENERIC_ATTACK_SPEED, ATTACK_SPEED_ID);
    }

    @Override
    public void applyLegacyEntityInteractionRange(Player player, double range) {
        applyAttribute(player, Attribute.PLAYER_ENTITY_INTERACTION_RANGE, INTERACTION_RANGE_ID, INTERACTION_RANGE_NAME, range);
    }

    @Override
    public void restoreLegacyEntityInteractionRange(org.bukkit.entity.Player player) {
        removeAttribute(player, Attribute.PLAYER_ENTITY_INTERACTION_RANGE, INTERACTION_RANGE_ID);
    }

    private void applyAttribute(Player player, Attribute attribute, UUID id, String name, double desired) {
        if (player == null) {
            return;
        }
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        for (AttributeModifier modifier : instance.getModifiers()) {
            if (id.equals(modifier.getUniqueId())) {
                instance.removeModifier(modifier);
                return;
            }
        }

        double delta = desired - instance.getDefaultValue();
        if (delta == 0.0D) {
            return;
        }

        instance.addTransientModifier(new AttributeModifier(id, name, delta, AttributeModifier.Operation.ADD_NUMBER));
    }


    private void removeAttribute(Player player, Attribute attribute, UUID id) {
        if (player == null) {
            return;
        }
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            for (AttributeModifier modifier : instance.getModifiers()) {
                if (id.equals(modifier.getUniqueId())) {
                    instance.removeModifier(modifier);
                    return;
                }
            }
        }
    }
}
