package me.liuyingowo.oldcombat.nms.impl.v1_21_R7;

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
        return agentBuilder
                .type(ElementMatchers.named(LivingEntity.class.getName()))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder.visit(Advice.to(LegacyKnockbackAdvice.class)
                                .on(ElementMatchers.named("knockback"))))
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder.visit(Advice.to(LegacyDamageAdvice.class)
                                .on(ElementMatchers.named("getAttackStrengthScale")
                                        .and(ElementMatchers.takesArguments(float.class)))))
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder.visit(Advice.to(LegacyAttackAdvice.class)
                                .on(ElementMatchers.named("attack")
                                        .and(ElementMatchers.takesArguments(Entity.class)))))
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder.visit(Advice.to(LegacySweepAttackAdvice.class)
                                .on(ElementMatchers.named("isSweepAttack")
                                        .and(ElementMatchers.takesArguments(
                                                boolean.class,
                                                boolean.class,
                                                boolean.class)))))
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder.visit(Advice.to(LegacySoundEffectAdvice.class)
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
        applyAttackSpeed(player, Attribute.ATTACK_SPEED);
    }

    private static void applyAttackSpeed(org.bukkit.entity.Player player, Attribute attribute) {
        AttributeInstance attackSpeed = player.getAttribute(attribute);
        if (attackSpeed != null && attackSpeed.getBaseValue() < 100.0D) {
            attackSpeed.setBaseValue(100.0D);
        }
    }
}
