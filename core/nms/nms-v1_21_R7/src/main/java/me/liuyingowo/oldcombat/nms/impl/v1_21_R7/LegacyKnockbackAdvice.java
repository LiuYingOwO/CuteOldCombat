package me.liuyingowo.oldcombat.nms.impl.v1_21_R7;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.bytebuddy.asm.Advice;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.event.CraftEventFactory;

public final class LegacyKnockbackAdvice {

    private static final double LEGACY_VERTICAL = 0.4000000059604645D;
    private static final double MIN_DIRECTION_LENGTH = 1.0E-5D;

    private LegacyKnockbackAdvice() {
    }

    /**
     * 使用 {@code @AllArguments} 动态适配不同 Paper/Leaf fork 的参数数量差异。
     * <p>参数顺序始终为: {@code (double strength, double x, double z, ...)}
     * 后续可选参数: {@code Entity attacker}, {@code Entity damager}, {@code Cause cause}
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onEnter(@Advice.This LivingEntity entity,
                                  @Advice.AllArguments Object[] args) {
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

        // damager 缺失时，使用 attacker 作为兜底
        if (damager == null) {
            damager = attacker;
        }

        double adjustedStrength = strength * (1.0D - entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));

        Vec3 current = entity.getDeltaMovement();
        double nextX = current.x() / 2.0D;
        double nextY = current.y() / 2.0D;
        double nextZ = current.z() / 2.0D;

        double directionLength = Math.sqrt(x * x + z * z);
        if (directionLength >= MIN_DIRECTION_LENGTH) {
            nextX -= x / directionLength * adjustedStrength;
            nextY += LEGACY_VERTICAL;
            nextZ -= z / directionLength * adjustedStrength;

            if (nextY > LEGACY_VERTICAL) {
                nextY = LEGACY_VERTICAL;
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
        entity.setDeltaMovement(current.add(knockback.getX(), knockback.getY(), knockback.getZ()));
        return true;
    }
}
