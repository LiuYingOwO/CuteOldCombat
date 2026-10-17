package me.liuyingowo.oldcombat.nms.impl.v26_1;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class LegacySwordBlockAdvice {

    private LegacySwordBlockAdvice() {}

    public static AgentPatch patch() {
        return (agentBuilder, logger) -> {
            return agentBuilder
                    .type(ElementMatchers.named(LivingEntity.class.getName()))
                    .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) -> builder
                            .visit(Advice.to(CanBlockAdvice.class)
                                    .on(ElementMatchers.named("canBlockAttack")
                                            .and(ElementMatchers.takesArguments(DamageSource.class, float.class))))
                            .visit(Advice.to(ResolveBlocked.class)
                                    .on(ElementMatchers.named("resolveBlockedDamage")
                                            .and(ElementMatchers.takesArguments(DamageSource.class, float.class)))));
        };
    }

    public static class CanBlockAdvice {

        @Advice.OnMethodExit
        public static void onExit(@Advice.This LivingEntity self,
                                  @Advice.Argument(0) DamageSource source,
                                  @Advice.Argument(1) float damage,
                                  @Advice.Return(readOnly = false) boolean result) {
            if (result || damage <= 0.0F) {
                return;
            }
            if (source.is(DamageTypeTags.BYPASSES_SHIELD)) {
                return;
            }
            final ItemStack useItem = self.getUseItem();
            if (!useItem.isEmpty() && useItem.is(ItemTags.SWORDS) && useItem.has(DataComponents.CONSUMABLE)) {
                result = true;
            }
        }
    }

    /** 用 1.8 公式替换原生（依赖组件、会 NPE 的）结算。 */
    public static class ResolveBlocked {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter(@Advice.This LivingEntity self) {
            final ItemStack useItem = self.getUseItem();
            return !useItem.isEmpty() && useItem.is(ItemTags.SWORDS) && useItem.has(DataComponents.CONSUMABLE);
        }

        @Advice.OnMethodExit
        public static void onExit(@Advice.Enter boolean skipped,
                                  @Advice.Argument(1) float damage,
                                  @Advice.Return(readOnly = false) float result) {
            if (skipped) {
                result = damage > 0.0F ? (damage - 1.0F) * 0.5F : 0.0F;
            }
        }
    }
}
