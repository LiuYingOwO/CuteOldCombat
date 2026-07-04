package me.liuyingowo.oldcombat.nms.impl.v1_20_R4;

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
