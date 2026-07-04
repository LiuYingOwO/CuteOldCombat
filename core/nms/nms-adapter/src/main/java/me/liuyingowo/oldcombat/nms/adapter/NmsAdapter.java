package me.liuyingowo.oldcombat.nms.adapter;

import net.bytebuddy.agent.builder.AgentBuilder;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * 版本适配器接口，每个 Minecraft 版本提供各自的实现，
 * 负责向 AgentBuilder 注册 ByteBuddy Transformer。
 */
public interface NmsAdapter {

    /**
     * 向给定的 AgentBuilder 应用当前版本的 NMS 战斗补丁。
     *
     * @param agentBuilder AgentBuilder 实例（通常是已配置了 .with() .ignore() 等的 builder）
     * @param logger       日志记录器
     * @return 应用补丁后的 AgentBuilder（用于继续链式调用 .installOn()）
     */
    AgentBuilder apply(AgentBuilder agentBuilder, Logger logger);

    void applyLegacyAttackSpeed(Player player);
}
