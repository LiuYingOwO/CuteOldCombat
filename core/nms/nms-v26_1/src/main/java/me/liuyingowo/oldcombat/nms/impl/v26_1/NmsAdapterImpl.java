package me.liuyingowo.oldcombat.nms.impl.v26_1;

import me.liuyingowo.oldcombat.nms.adapter.AgentPatch;
import me.liuyingowo.oldcombat.nms.adapter.NmsAdapter;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.component.BlocksAttacks;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public final class NmsAdapterImpl implements NmsAdapter {

    private static final NamespacedKey ATTACK_KEY = new NamespacedKey("cuteoldcombat", "attack_speed");
    private static final NamespacedKey INTERACTION_KEY = new NamespacedKey("cuteoldcombat", "interaction_range");

    private static final BlocksAttacks LEGACY_SWORD_BLOCK = new BlocksAttacks(
            0.0F,
            0.0F,
            List.of(new BlocksAttacks.DamageReduction(180.0F, Optional.empty(), -0.5F, 0.5F)),
            new BlocksAttacks.ItemDamageFunction(0.0F, 0.0F, 0.0F),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );

    private static final List<AgentPatch> PATCHES = List.of(
            LegacyAttackAdvice.patch(),
            LegacyDamageAdvice.patch(),
            LegacyKnockbackAdvice.patch(),
            LegacySoundEffectAdvice.patch(),
            LegacyBlockRecoilAdvice.patch()
    );

    @Override
    public AgentBuilder apply(AgentBuilder agentBuilder, Logger logger) {
        return AgentPatch.applyAll(agentBuilder, logger, PATCHES);
    }

    @Override
    public void applyLegacyAttackSpeed(org.bukkit.entity.Player player, double speed) {
        applyAttribute(player, Attribute.ATTACK_SPEED, ATTACK_KEY, speed);
    }

    @Override
    public void restoreLegacyAttackSpeed(org.bukkit.entity.Player player) {
        removeAttribute(player, Attribute.ATTACK_SPEED, ATTACK_KEY);
    }

    @Override
    public void applyLegacyEntityInteractionRange(org.bukkit.entity.Player player, double range) {
        applyAttribute(player, Attribute.ENTITY_INTERACTION_RANGE, INTERACTION_KEY, range);
    }

    @Override
    public void restoreLegacyEntityInteractionRange(org.bukkit.entity.Player player) {
        removeAttribute(player, Attribute.ENTITY_INTERACTION_RANGE, INTERACTION_KEY);
    }

    @Override
    public boolean supportsLegacySwordBlocking() {
        return true;
    }

    @Override
    public boolean applyLegacySwordBlocking(ItemStack stack) {
        final net.minecraft.world.item.ItemStack handle = swordHandle(stack);
        if (handle == null || handle.has(DataComponents.BLOCKS_ATTACKS)) {
            return false;
        }
        handle.set(DataComponents.BLOCKS_ATTACKS, LEGACY_SWORD_BLOCK);
        return true;
    }

    @Override
    public boolean clearLegacySwordBlocking(ItemStack stack) {
        final net.minecraft.world.item.ItemStack handle = swordHandle(stack);
        if (handle == null || !handle.has(DataComponents.BLOCKS_ATTACKS)) {
            return false;
        }
        handle.remove(DataComponents.BLOCKS_ATTACKS);
        return true;
    }

    @Override
    public boolean isLegacySwordBlocking(Player player) {
        if (!(player instanceof CraftPlayer)) {
            return false;
        }
        final net.minecraft.world.item.ItemStack useItem = ((CraftPlayer) player).getHandle().getUseItem();
        return !useItem.isEmpty() && useItem.is(ItemTags.SWORDS);
    }

    /**
     * 取背包里那一把剑的<strong>活体</strong> NMS 句柄，改它就是改背包。
     *
     * <p>关键区别：{@code CraftItemStack.unwrap(stack)} 返回的是 {@code craftItemStack.handle}
     * 本身；而 {@code CraftItemStack.asNMSCopy(stack)} 返回的是 {@code handle.copy()} ——
     * 改后者不生效。别把 {@code asBukkitCopy} 造出来的副本传进来（那种 ItemStack 没有底层句柄，
     * unwrap 得到 EMPTY，这里会返回 null）。</p>
     *
     * @return null 表示"不是剑 / 不是活体物品 / 空物品"，调用方不需要做任何事
     */
    private static net.minecraft.world.item.ItemStack swordHandle(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return null;
        }
        final net.minecraft.world.item.ItemStack handle = CraftItemStack.unwrap(stack);
        if (handle.isEmpty() || !handle.is(ItemTags.SWORDS)) {
            return null;
        }
        return handle;
    }

    private void applyAttribute(Player player, Attribute attribute, NamespacedKey key, double desired) {
        if (player == null) {
            return;
        }
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (modifier.getKey().equals(key)) {
                instance.removeModifier(modifier);
                break;
            }
        }
        double delta = desired - instance.getValue();
        if (delta == 0.0D) {
            return;
        }
        instance.addTransientModifier(new AttributeModifier(key, delta, AttributeModifier.Operation.ADD_NUMBER));
    }


    private void removeAttribute(Player player, Attribute attribute, NamespacedKey key) {
        if (player == null) {
            return;
        }
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            for (AttributeModifier modifier : instance.getModifiers()) {
                if (modifier.getKey().equals(key)) {
                    instance.removeModifier(modifier);
                    return;
                }
            }
        }
    }
}
