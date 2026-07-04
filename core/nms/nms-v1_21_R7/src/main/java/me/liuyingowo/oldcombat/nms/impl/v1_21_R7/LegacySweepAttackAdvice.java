package me.liuyingowo.oldcombat.nms.impl.v1_21_R7;

import net.bytebuddy.asm.Advice;

public final class LegacySweepAttackAdvice {

    private LegacySweepAttackAdvice() {
    }

    @Advice.OnMethodExit
    public static void onExit(@Advice.Return(readOnly = false) boolean returnValue) {
        returnValue = false;
    }
}
