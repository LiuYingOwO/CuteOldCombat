package me.liuyingowo.oldcombat.nms.impl.v1_20_R4;

import net.bytebuddy.asm.Advice;
import net.minecraft.world.entity.player.Player;

public final class LegacyDamageAdvice {

    private LegacyDamageAdvice() {
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
