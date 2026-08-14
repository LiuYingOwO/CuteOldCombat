package me.liuyingowo.oldcombat.nms.impl.v1_21_R7;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class LegacyAttackAdvice {

    private LegacyAttackAdvice() {
    }

    public static AgentPatch patch() {
        return (agentBuilder, logger) -> agentBuilder
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder
                                .visit(Advice.to(AttackSubAdvice.class)
                                        .on(ElementMatchers.named("attack")
                                                .and(ElementMatchers.takesArguments(Entity.class))))
                                .visit(Advice.to(CriticalSubAdvice.class)
                                        .on(ElementMatchers.named("canCriticalAttack")
                                                .and(ElementMatchers.isPrivate())
                                                .and(ElementMatchers.takesArguments(Entity.class)))));
    }

    public static class AttackSubAdvice {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.This Player attacker) {
            attacker.resetAttackStrengthTicker();
        }
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
}
