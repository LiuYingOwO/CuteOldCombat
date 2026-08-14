package me.liuyingowo.oldcombat.nms.impl.v1_20_R4;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.CraftVector;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import java.util.List;

public final class LegacyAttackAdvice {

    private LegacyAttackAdvice() {
    }

    public static AgentPatch patch() {
        return (agentBuilder, logger) -> agentBuilder
                .type(ElementMatchers.named(Player.class.getName()))
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder.visit(Advice.to(LegacyAttackAdvice.class)
                                .on(ElementMatchers.named("attack")
                                        .and(ElementMatchers.takesArguments(Entity.class)))));
    }

    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onEnter(@Advice.This Player self, @Advice.Argument(0) Entity target) {
        self.resetAttackStrengthTicker();

        boolean willAttack = target.isAttackable() && !target.skipAttackInteraction(self);
        PrePlayerAttackEntityEvent playerAttackEntityEvent = new PrePlayerAttackEntityEvent(
                (org.bukkit.entity.Player) self.getBukkitEntity(), target.getBukkitEntity(), willAttack);
        if (!playerAttackEntityEvent.callEvent() || !willAttack) {
            return true;
        }

        float f = (float) self.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float f1 = EnchantmentHelper.getDamageBonus(self.getMainHandItem(), target.getType());
        float f2 = self.getAttackStrengthScale(0.5f);
        f *= 0.2f + f2 * f2 * 0.8f;
        f1 *= f2;

        if (target.getType().is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && target instanceof Projectile iprojectile) {
            DamageSource damagesource = self.damageSources().playerAttack(self);
            if (CraftEventFactory.handleNonLivingEntityDamageEvent(target, damagesource, f1, false)) {
                return true;
            }
            iprojectile.deflect(ProjectileDeflection.AIM_DEFLECT, self, self, true);
            return true;
        }

        if (!(f > 0.0f || f1 > 0.0f)) {
            return true;
        }

        boolean flag = f2 > 0.9f;
        boolean flag1 = false;
        int i = 0;
        i += EnchantmentHelper.getKnockbackBonus(self);
        if (self.isSprinting() && flag) {
            self.level().playSound(self, self.getX(), self.getY(), self.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, self.getSoundSource(), 1.0f, 1.0f);
            if (self instanceof ServerPlayer) {
                ((ServerPlayer) self).connection.send(new ClientboundSoundPacket(
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_ATTACK_KNOCKBACK),
                        self.getSoundSource(), self.getX(), self.getY(), self.getZ(), 1.0f, 1.0f, self.random.nextLong()));
            }
            ++i;
            flag1 = true;
        }

        f += self.getItemInHand(InteractionHand.MAIN_HAND).getItem().getAttackDamageBonus(self, f);

        // 1.8 语义：暴击判定去掉 !isSprinting()，允许疾跑下落攻击暴击
        boolean flag2 = flag
                && self.fallDistance > 0.0f
                && !self.onGround()
                && !self.onClimbable()
                && !self.isInWater()
                && !self.hasEffect(MobEffects.BLINDNESS)
                && !self.isPassenger()
                && target instanceof LivingEntity;
        flag2 = flag2 && !self.level().paperConfig().entities.behavior.disablePlayerCrits;
        if (flag2) {
            f *= 1.5f;
        }

        f += f1;

        boolean flag3 = false;
        double d0 = self.walkDist - self.walkDistO;
        ItemStack itemstack;
        if (flag && !flag2 && !flag1 && self.onGround() && d0 < self.getSpeed()
                && (itemstack = self.getItemInHand(InteractionHand.MAIN_HAND)).getItem() instanceof SwordItem) {
            flag3 = true;
        }

        float f3 = 0.0f;
        boolean flag4 = false;
        int j = EnchantmentHelper.getFireAspect(self);
        if (target instanceof LivingEntity) {
            f3 = ((LivingEntity) target).getHealth();
            if (j > 0 && !target.isOnFire()) {
                EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(self.getBukkitEntity(), target.getBukkitEntity(), 1);
                Bukkit.getPluginManager().callEvent(combustEvent);
                if (!combustEvent.isCancelled()) {
                    flag4 = true;
                    target.igniteForSeconds(combustEvent.getDuration(), false);
                }
            }
        }

        Vec3 vec3d = target.getDeltaMovement();
        boolean flag5 = target.hurt(self.damageSources().playerAttack(self).critical(flag2), f);

        if (flag5) {
            if (i > 0) {
                if (target instanceof LivingEntity) {
                    ((LivingEntity) target).knockback(
                            i * 0.5f,
                            Mth.sin(self.getYRot() * (float) (Math.PI / 180.0)),
                            -Mth.cos(self.getYRot() * (float) (Math.PI / 180.0)),
                            self,
                            EntityKnockbackEvent.Cause.ENTITY_ATTACK);
                } else {
                    target.push(
                            -Mth.sin(self.getYRot() * (float) (Math.PI / 180.0)) * i * 0.5f,
                            0.1,
                            Mth.cos(self.getYRot() * (float) (Math.PI / 180.0)) * i * 0.5f,
                            self);
                }
                self.setDeltaMovement(self.getDeltaMovement().multiply(0.6, 1.0, 0.6));
                if (!self.level().paperConfig().misc.disableSprintInterruptionOnAttack) {
                    self.setSprinting(false);
                }
            }

            if (flag3) {
                float f4 = 1.0f + EnchantmentHelper.getSweepingDamageRatio(self) * f;
                List<LivingEntity> list = self.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(1.0, 0.25, 1.0));
                for (LivingEntity entityliving : list) {
                    if (entityliving == self
                            || entityliving == target
                            || self.isAlliedTo(entityliving)
                            || (entityliving instanceof ArmorStand && ((ArmorStand) entityliving).isMarker())
                            || !(self.distanceToSqr(entityliving) < 9.0)
                            || !entityliving.hurt(self.damageSources().playerAttack(self).sweep().critical(flag2), f4)) {
                        continue;
                    }
                    entityliving.knockback(
                            0.4f,
                            Mth.sin(self.getYRot() * (float) (Math.PI / 180.0)),
                            -Mth.cos(self.getYRot() * (float) (Math.PI / 180.0)),
                            self,
                            EntityKnockbackEvent.Cause.SWEEP_ATTACK);
                }
                self.level().playSound(self, self.getX(), self.getY(), self.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, self.getSoundSource(), 1.0f, 1.0f);
                if (self instanceof ServerPlayer) {
                    ((ServerPlayer) self).connection.send(new ClientboundSoundPacket(
                            BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_ATTACK_SWEEP),
                            self.getSoundSource(), self.getX(), self.getY(), self.getZ(), 1.0f, 1.0f, self.random.nextLong()));
                }
                self.sweepAttack();
            }

            if (target instanceof ServerPlayer && target.hurtMarked) {
                boolean cancelled = false;
                org.bukkit.entity.Player player = (org.bukkit.entity.Player) target.getBukkitEntity();
                Vector velocity = CraftVector.toBukkit(vec3d);
                PlayerVelocityEvent event = new PlayerVelocityEvent(player, velocity.clone());
                self.level().getCraftServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    cancelled = true;
                } else if (!velocity.equals(event.getVelocity())) {
                    player.setVelocity(event.getVelocity());
                }
                if (!cancelled) {
                    ((ServerPlayer) target).connection.send(new ClientboundSetEntityMotionPacket(target));
                    target.hurtMarked = false;
                    target.setDeltaMovement(vec3d);
                }
            }

            if (flag2) {
                self.level().playSound(self, self.getX(), self.getY(), self.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, self.getSoundSource(), 1.0f, 1.0f);
                if (self instanceof ServerPlayer) {
                    ((ServerPlayer) self).connection.send(new ClientboundSoundPacket(
                            BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_ATTACK_CRIT),
                            self.getSoundSource(), self.getX(), self.getY(), self.getZ(), 1.0f, 1.0f, self.random.nextLong()));
                }
                self.crit(target);
            }

            if (!flag2 && !flag3) {
                if (flag) {
                    self.level().playSound(self, self.getX(), self.getY(), self.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, self.getSoundSource(), 1.0f, 1.0f);
                    if (self instanceof ServerPlayer) {
                        ((ServerPlayer) self).connection.send(new ClientboundSoundPacket(
                                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_ATTACK_STRONG),
                                self.getSoundSource(), self.getX(), self.getY(), self.getZ(), 1.0f, 1.0f, self.random.nextLong()));
                    }
                } else {
                    self.level().playSound(self, self.getX(), self.getY(), self.getZ(), SoundEvents.PLAYER_ATTACK_WEAK, self.getSoundSource(), 1.0f, 1.0f);
                    if (self instanceof ServerPlayer) {
                        ((ServerPlayer) self).connection.send(new ClientboundSoundPacket(
                                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_ATTACK_WEAK),
                                self.getSoundSource(), self.getX(), self.getY(), self.getZ(), 1.0f, 1.0f, self.random.nextLong()));
                    }
                }
            }

            if (f1 > 0.0f) {
                self.magicCrit(target);
            }

            self.setLastHurtMob(target);
            if (target instanceof LivingEntity) {
                EnchantmentHelper.doPostHurtEffects((LivingEntity) target, self);
            }
            EnchantmentHelper.doPostDamageEffects(self, target);

            ItemStack itemstack1 = self.getMainHandItem();
            Object object = target;
            if (target instanceof EnderDragonPart) {
                object = ((EnderDragonPart) target).parentMob;
            }
            if (!self.level().isClientSide && !itemstack1.isEmpty() && object instanceof LivingEntity) {
                itemstack1.hurtEnemy((LivingEntity) object, self);
                if (itemstack1.isEmpty()) {
                    self.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                }
            }

            if (target instanceof LivingEntity) {
                float f5 = f3 - ((LivingEntity) target).getHealth();
                self.awardStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                if (j > 0) {
                    EntityCombustByEntityEvent combustEvent = new EntityCombustByEntityEvent(self.getBukkitEntity(), target.getBukkitEntity(), j * 4);
                    Bukkit.getPluginManager().callEvent(combustEvent);
                    if (!combustEvent.isCancelled()) {
                        target.igniteForSeconds(combustEvent.getDuration(), false);
                    }
                }
                if (self.level() instanceof ServerLevel && f5 > 2.0F) {
                    int k = (int) (f5 * 0.5);
                    ((ServerLevel) self.level()).sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5), target.getZ(), k, 0.1, 0.0, 0.1, 0.2);
                }
            }

            self.causeFoodExhaustion(self.level().spigotConfig.combatExhaustion, ExhaustionReason.ATTACK);
        } else {
            self.level().playSound(self, self.getX(), self.getY(), self.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, self.getSoundSource(), 1.0f, 1.0f);
            if (self instanceof ServerPlayer) {
                ((ServerPlayer) self).connection.send(new ClientboundSoundPacket(
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_ATTACK_NODAMAGE),
                        self.getSoundSource(), self.getX(), self.getY(), self.getZ(), 1.0f, 1.0f, self.random.nextLong()));
            }
            if (flag4) {
                target.clearFire();
            }
            if (self instanceof ServerPlayer) {
                ((ServerPlayer) self).getBukkitEntity().updateInventory();
            }
        }

        return true;
    }
}
