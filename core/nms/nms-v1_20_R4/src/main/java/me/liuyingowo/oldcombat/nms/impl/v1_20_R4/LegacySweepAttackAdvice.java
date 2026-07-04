package me.liuyingowo.oldcombat.nms.impl.v1_20_R4;

import net.bytebuddy.asm.Advice;

public final class LegacySweepAttackAdvice {

    private LegacySweepAttackAdvice() {
    }

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onEnter() {
        return true;
    }
}
