package me.liuyingowo.oldcombat.nms.impl.v26_1;


import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.world.entity.player.Player;

public final class LegacySweepAttackAdvice {

    private LegacySweepAttackAdvice() {};

    public static AgentPatch patch() {
        return (agentBuilder, logger) -> agentBuilder
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder.visit(Advice.to(LegacySweepAttackAdvice.class)
                                .on(ElementMatchers.named("isSweepAttack")
                                        .and(ElementMatchers.takesArguments(
                                                boolean.class,
                                                boolean.class,
                                                boolean.class
                                        ))
                        ))
        );
    }

    @Advice.OnMethodExit
    public static void onExit(@Advice.Return(readOnly = false) boolean returnValue) {
        returnValue = false;
    }
}
