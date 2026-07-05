package me.liuyingowo.oldcombat.nms.adapter;

import net.bytebuddy.agent.builder.AgentBuilder;

public interface NmsAdapter {

    /**
     * 向给定的 AgentBuilder 应用当前版本的 NMS 战斗补丁。
     *
     * @param agentBuilder AgentBuilder 实例（通常是已配置了 .with() .ignore() 等的 builder）
     * @param logger       日志记录器
     * @return 应用补丁后的 AgentBuilder（用于继续链式调用 .installOn()）
     */
    AgentBuilder apply(AgentBuilder agentBuilder, java.util.logging.Logger logger);

    void applyLegacyAttackSpeed(org.bukkit.entity.Player player);
}
