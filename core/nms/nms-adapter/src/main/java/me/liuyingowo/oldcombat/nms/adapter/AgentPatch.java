package me.liuyingowo.oldcombat.nms.adapter;

import net.bytebuddy.agent.builder.AgentBuilder;

import java.util.List;
import java.util.logging.Logger;

@FunctionalInterface
public interface AgentPatch {

    AgentBuilder apply(AgentBuilder agentBuilder, Logger logger);

    static AgentBuilder applyAll(AgentBuilder agentBuilder, Logger logger, List<AgentPatch> patches) {
        AgentBuilder current = agentBuilder;

        for (AgentPatch patch : patches) {
            current = patch.apply(current, logger);
        }

        return current;
    }
}
