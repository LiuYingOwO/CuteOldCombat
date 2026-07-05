package me.liuyingowo.oldcombat.nms.impl.v1_20_R4;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import me.liuyingowo.oldcombat.nms.adapter.KnockbackBridge;
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
            logger.info("Knockback hook installed!");
            return agentBuilder
                    .type(ElementMatchers.named(LivingEntity.class.getName()))
                    .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                            builder.visit(Advice.to(LegacyKnockbackAdvice.class)
                                    .on(ElementMatchers.named("knockback")
                                            .and(ElementMatchers.takesArguments(
                                                    double.class,
                                                    double.class,
                                                    double.class,
                                                    Entity.class,
                                                    EntityKnockbackEvent.Cause.class)))));
        };
    }

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onEnter(@Advice.This LivingEntity entity,
                                  @Advice.Argument(0) double strength,
                                  @Advice.Argument(1) double x,
                                  @Advice.Argument(2) double z,
                                  @Advice.Argument(3) Entity attacker,
                                  @Advice.Argument(4) EntityKnockbackEvent.Cause cause) {
        if (!KnockbackBridge.enabled) {
            return false;
        }

        double horizontal = KnockbackBridge.horizontal;
        double vertical = KnockbackBridge.vertical;
        double verticalLimit = KnockbackBridge.verticalLimit;
        double friction = KnockbackBridge.friction;
        double minDirectionLength = KnockbackBridge.minDirectionLength;
        boolean applyResistance = KnockbackBridge.applyResistance;

        double adjustedStrength = strength;
        if (applyResistance) {
            adjustedStrength *= Math.max(0.0D, 1.0D - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        }
        adjustedStrength *= horizontal;

        Vec3 current = entity.getDeltaMovement();
        double nextX = current.x() * friction;
        double nextY = current.y() * friction;
        double nextZ = current.z() * friction;

        double directionLength = Math.sqrt(x * x + z * z);
        if (directionLength >= minDirectionLength) {
            nextX -= x / directionLength * adjustedStrength;
            nextY += vertical;
            nextZ -= z / directionLength * adjustedStrength;

            if (nextY > verticalLimit) {
                nextY = verticalLimit;
            }
        }

        Vec3 legacyMovement = new Vec3(nextX, nextY, nextZ);
        Vec3 delta = legacyMovement.subtract(current);

        EntityKnockbackEvent event = CraftEventFactory.callEntityKnockbackEvent(
                (CraftLivingEntity) entity.getBukkitEntity(),
                attacker,
                cause,
                adjustedStrength,
                delta
        );

        if (event.isCancelled()) {
            return true;
        }

        org.bukkit.util.Vector knockback = event.getKnockback();
        entity.hasImpulse = true;
        entity.setDeltaMovement(current.add(knockback.getX(), knockback.getY(), knockback.getZ()));
        return true;
    }
}
