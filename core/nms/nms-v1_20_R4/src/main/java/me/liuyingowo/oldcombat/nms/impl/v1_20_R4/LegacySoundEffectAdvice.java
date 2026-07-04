package me.liuyingowo.oldcombat.nms.impl.v1_20_R4;

import net.bytebuddy.asm.Advice;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class LegacySoundEffectAdvice {

    private LegacySoundEffectAdvice() {
    }

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onEnter(@Advice.Argument(4) SoundEvent soundEvent) {
        return soundEvent == SoundEvents.PLAYER_ATTACK_SWEEP;
    }
}
