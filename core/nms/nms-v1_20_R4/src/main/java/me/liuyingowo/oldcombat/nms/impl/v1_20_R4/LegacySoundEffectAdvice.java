package me.liuyingowo.oldcombat.nms.impl.v1_20_R4;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public final class LegacySoundEffectAdvice {

    private LegacySoundEffectAdvice() {
    }

    public static AgentPatch patch() {
        return (agentBuilder, logger) -> agentBuilder
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

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onEnter(@Advice.Argument(4) SoundEvent soundEvent) {
        return soundEvent == SoundEvents.PLAYER_ATTACK_SWEEP;
    }
}
