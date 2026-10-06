package me.liuyingowo.oldcombat.nms.impl.v1_21_R7;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 负责1.8的暴击判定/取消挥砍伤害
 */
public final class LegacyAttackAdvice {

    private LegacyAttackAdvice() {
    }

    public static AgentPatch patch() {
        return (agentBuilder, logger) -> agentBuilder
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder
                                .visit(Advice.to(SweepSubAdvice.class)
                                        .on(ElementMatchers.named("isSweepAttack")
                                                .and(
                                                        ElementMatchers.takesArguments(
                                                                double.class,
                                                                double.class,
                                                                double.class)
                                                )))
                                .visit(Advice.to(CriticalSubAdvice.class)
                                        .on(ElementMatchers.named("canCriticalAttack")
                                                .and(ElementMatchers.isPrivate())
                                                .and(ElementMatchers.takesArguments(Entity.class)))));
    }

    public static class CriticalSubAdvice {
        @Advice.OnMethodExit
        public static void onExit(@Advice.This Player attacker,
                                  @Advice.Argument(0) Entity target,
                                  @Advice.Return(readOnly = false) boolean returnValue) {
            if (!returnValue) {
                if (attacker.fallDistance > 0.0f
                        && !attacker.onGround()
                        && !attacker.onClimbable()
                        && !attacker.isInWater()
                        && !attacker.isMobilityRestricted()
                        && !attacker.isPassenger()
                        && target instanceof LivingEntity) {
                    returnValue = true;
                }
            }
        }
    }

    public static class SweepSubAdvice {
        @Advice.OnMethodExit
        public static void onExit(@Advice.Return(readOnly = false) boolean returnValue) {
            returnValue = false;
        }
    }
}
