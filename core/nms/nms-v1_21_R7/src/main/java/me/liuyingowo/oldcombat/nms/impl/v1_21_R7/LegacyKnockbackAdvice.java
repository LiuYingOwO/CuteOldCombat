package me.liuyingowo.oldcombat.nms.impl.v1_21_R7;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import me.liuyingowo.oldcombat.nms.adapter.KnockbackSettings;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;

public final class LegacyKnockbackAdvice {

    private LegacyKnockbackAdvice() {
    }

    public static AgentPatch patch() {
        return (agentBuilder, logger) -> {
            if (!KnockbackSettings.current().isEnabled()) {
                logger.info("Legacy knockback hook disabled by config.");
                return agentBuilder;
            }

            logger.info("Legacy knockback hook enabled.");
            return agentBuilder
                    .type(ElementMatchers.named(LivingEntity.class.getName()))
                    .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                            builder.visit(Advice.to(LegacyKnockbackAdvice.class)
                                    .on(ElementMatchers.named("knockback")
                                            .and(ElementMatchers.takesArgument(0, double.class))
                                            .and(ElementMatchers.takesArgument(1, double.class))
                                            .and(ElementMatchers.takesArgument(2, double.class)))));
        };
    }

    /**
     * 使用 {@code @AllArguments} 动态适配不同 Paper/Leaf fork 的参数数量差异。
     * <p>参数顺序始终为: {@code (double strength, double x, double z, ...)}
     * 后续可选参数: {@code Entity attacker}, {@code Entity damager}, {@code Cause cause}
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onEnter(@Advice.This LivingEntity entity,
                                  @Advice.AllArguments Object[] args) {
        KnockbackSettings.Snapshot settings = KnockbackSettings.current();
        if (!settings.isEnabled()) {
            return false;
        }

        double strength = (double) args[0];
        double x = (double) args[1];
        double z = (double) args[2];

        Entity attacker = null;
        Entity damager = null;
        EntityKnockbackEvent.Cause cause = EntityKnockbackEvent.Cause.UNKNOWN;

        int len = args.length;
        if (len > 3 && args[3] instanceof Entity) {
            attacker = (Entity) args[3];
        }
        if (len > 4 && args[4] instanceof Entity) {
            damager = (Entity) args[4];
        }
        if (len > 5 && args[5] instanceof EntityKnockbackEvent.Cause) {
            cause = (EntityKnockbackEvent.Cause) args[5];
        } else if (len > 4 && args[4] instanceof EntityKnockbackEvent.Cause) {
            cause = (EntityKnockbackEvent.Cause) args[4];
        } else if (len > 3 && args[3] instanceof EntityKnockbackEvent.Cause) {
            cause = (EntityKnockbackEvent.Cause) args[3];
        }

        if (damager == null) {
            damager = attacker;
        }

        double adjustedStrength = strength;
        if (settings.isApplyResistance()) {
            adjustedStrength *= Math.max(0.0D, 1.0D - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        }
        adjustedStrength *= settings.getHorizontal();

        Vec3 current = entity.getDeltaMovement();
        double nextX = current.x() * settings.getFriction();
        double nextY = current.y() * settings.getFriction();
        double nextZ = current.z() * settings.getFriction();

        double directionLength = Math.sqrt(x * x + z * z);
        if (directionLength >= settings.getMinDirectionLength()) {
            nextX -= x / directionLength * adjustedStrength;
            nextY += settings.getVertical();
            nextZ -= z / directionLength * adjustedStrength;

            if (nextY > settings.getVerticalLimit()) {
                nextY = settings.getVerticalLimit();
            }
        }

        Vec3 legacyMovement = new Vec3(nextX, nextY, nextZ);
        Vec3 delta = legacyMovement.subtract(current);

        EntityKnockbackEvent event = CraftEventFactory.callEntityKnockbackEvent(
                (CraftLivingEntity) entity.getBukkitEntity(),
                attacker,
                damager,
                cause,
                adjustedStrength,
                delta
        );

        if (event.isCancelled()) {
            return true;
        }

        org.bukkit.util.Vector knockback = event.getKnockback();
        entity.needsSync = true;
        entity.setDeltaMovement(current.add(knockback.getX(), knockback.getY(), knockback.getZ()));
        return true;
    }
}
