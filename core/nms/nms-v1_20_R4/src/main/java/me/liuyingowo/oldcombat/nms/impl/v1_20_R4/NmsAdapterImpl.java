package me.liuyingowo.oldcombat.nms.impl.v1_20_R4;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import me.liuyingowo.oldcombat.nms.adapter.KnockbackSettings;
import me.liuyingowo.oldcombat.nms.adapter.NmsAdapter;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

import java.util.logging.Logger;

public final class NmsAdapterImpl implements NmsAdapter {

    @Override
    public AgentBuilder apply(AgentBuilder agentBuilder, Logger logger) {
        AgentBuilder patchedBuilder = agentBuilder;
        if (KnockbackSettings.current().isEnabled()) {
            logger.info("[OldCombat] Legacy knockback hook enabled.");
            patchedBuilder = patchedBuilder
                    .type(ElementMatchers.named(LivingEntity.class.getName()))
                    .transform((typeBuilder, typeDescription, classLoader, javaModule, protectionDomain) ->
                            typeBuilder.visit(Advice.to(LegacyKnockbackAdvice.class)
                                    .on(ElementMatchers.named("knockback")
                                            .and(ElementMatchers.takesArguments(
                                                    double.class,
                                                    double.class,
                                                    double.class,
                                                    Entity.class,
                                                    EntityKnockbackEvent.Cause.class)))));
        } else {
            logger.info("[OldCombat] Legacy knockback hook disabled by config.");
        }

        return patchedBuilder
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((typeBuilder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        typeBuilder.visit(Advice.to(LegacyDamageAdvice.class)
                                .on(ElementMatchers.named("getAttackStrengthScale")
                                        .and(ElementMatchers.takesArguments(float.class)))))
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((typeBuilder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        typeBuilder.visit(Advice.to(LegacyAttackAdvice.class)
                                .on(ElementMatchers.named("attack")
                                        .and(ElementMatchers.takesArguments(Entity.class)))))
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((typeBuilder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        typeBuilder.visit(Advice.to(LegacySweepAttackAdvice.class)
                                .on(ElementMatchers.named("sweepAttack")
                                        .and(ElementMatchers.takesArguments(0)))))
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((typeBuilder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        typeBuilder.visit(Advice.to(LegacySoundEffectAdvice.class)
                                .on(ElementMatchers.named("sendSoundEffect")
                                        .and(ElementMatchers.isStatic())
                                        .and(ElementMatchers.takesArguments(
                                                Player.class,
                                                double.class,
                                                double.class,
                                                double.class,
                                                SoundEvent.class,
                                                SoundSource.class,
                                                float.class,
                                                float.class)))));
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
