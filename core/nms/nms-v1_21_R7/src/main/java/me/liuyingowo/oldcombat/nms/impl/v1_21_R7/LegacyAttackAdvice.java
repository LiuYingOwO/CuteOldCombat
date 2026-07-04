package me.liuyingowo.oldcombat.nms.impl.v1_21_R7;

import net.bytebuddy.asm.Advice;
import net.minecraft.world.entity.player.Player;

public final class LegacyAttackAdvice {

    private LegacyAttackAdvice() {
    }

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.This Player attacker) {
        attacker.resetAttackStrengthTicker();
    }
}
