package me.liuyingowo.oldcombat.nms.impl.v26_1;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class LegacyBlockRecoilAdvice {

    private LegacyBlockRecoilAdvice() {
    }

    public static AgentPatch patch() {
        return (agentBuilder, logger) -> {
            logger.info("Patched into Player#blockedByItem !");
            return agentBuilder
                    .type(ElementMatchers.named(LivingEntity.class.getName()))
                    .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                            builder.visit(Advice.to(LegacyBlockRecoilAdvice.class)
                                    .on(ElementMatchers.named("blockedByItem"))));
        };
    }

    /**
     * @return true = 跳过原方法体（ByteBuddy 的 {@code skipOn = OnNonDefaultValue} 对 void 方法
     *         就是"返回非默认值即跳过"，和现有 {@code LegacySoundEffectAdvice} 同一套写法）
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onEnter(@Advice.This LivingEntity self) {
//        if (!LegacyCombatBridge.disable_block_recoil) {
//            return false;
//        }
        final ItemStack blockingWith = self.getItemBlockingWith();
        return blockingWith != null && blockingWith.is(ItemTags.SWORDS);
    }
}