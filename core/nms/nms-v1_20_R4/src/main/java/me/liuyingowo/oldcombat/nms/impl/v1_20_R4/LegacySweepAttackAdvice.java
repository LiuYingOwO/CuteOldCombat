package me.liuyingowo.oldcombat.nms.impl.v1_20_R4;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.world.entity.player.Player;

public final class LegacySweepAttackAdvice {

    private LegacySweepAttackAdvice() {
    }

    public static AgentPatch patch() {
        return (agentBuilder, logger) -> agentBuilder
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder.visit(Advice.to(LegacySweepAttackAdvice.class)
                                .on(ElementMatchers.named("sweepAttack")
                                        .and(ElementMatchers.takesArguments(0)))));
    }

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onEnter() {
        return true;
    }
}
