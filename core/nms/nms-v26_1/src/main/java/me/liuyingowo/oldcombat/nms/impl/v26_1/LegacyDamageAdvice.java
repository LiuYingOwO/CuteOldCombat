package me.liuyingowo.oldcombat.nms.impl.v26_1;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.world.entity.player.Player;

public final class LegacyDamageAdvice {

    private LegacyDamageAdvice() {
    }

    public static AgentPatch patch() {
        return (agentBuilder, logger) -> agentBuilder
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder.visit(Advice.to(LegacyDamageAdvice.class)
                                .on(ElementMatchers.named("getAttackStrengthScale")
                                        .and(ElementMatchers.takesArguments(float.class)))));
    }

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.This Player player) {
        player.resetAttackStrengthTicker();
    }

    @Advice.OnMethodExit
    public static void onExit(@Advice.Return(readOnly = false) float returnValue) {
        returnValue = 1.0F;
    }
}
