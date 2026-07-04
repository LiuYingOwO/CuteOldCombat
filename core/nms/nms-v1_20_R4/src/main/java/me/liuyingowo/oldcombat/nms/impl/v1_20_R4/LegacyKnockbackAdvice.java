package me.liuyingowo.oldcombat.nms.impl.v1_20_R4;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import me.liuyingowo.oldcombat.nms.adapter.KnockbackSettings;
import net.bytebuddy.asm.Advice;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;

public final class LegacyKnockbackAdvice {

    private LegacyKnockbackAdvice() {
    }

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onEnter(@Advice.This LivingEntity entity,
                                  @Advice.Argument(0) double strength,
                                  @Advice.Argument(1) double x,
                                  @Advice.Argument(2) double z,
                                  @Advice.Argument(3) Entity attacker,
                                  @Advice.Argument(4) EntityKnockbackEvent.Cause cause) {
        KnockbackSettings.Snapshot settings = KnockbackSettings.current();
        if (!settings.isEnabled()) {
            return false;
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
