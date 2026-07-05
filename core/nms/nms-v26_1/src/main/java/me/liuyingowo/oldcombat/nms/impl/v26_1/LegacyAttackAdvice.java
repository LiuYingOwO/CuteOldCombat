package me.liuyingowo.oldcombat.nms.impl.v26_1;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class LegacyAttackAdvice {

    private LegacyAttackAdvice() {
    }

    public static AgentPatch patch() {
        return (agentBuilder, logger) -> agentBuilder
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder.visit(Advice.to(LegacyAttackAdvice.class)
                                .on(ElementMatchers.named("attack")
                                        .and(ElementMatchers.takesArguments(Entity.class)))));
    }

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.This Player attacker) {
        attacker.resetAttackStrengthTicker();
    }
}
