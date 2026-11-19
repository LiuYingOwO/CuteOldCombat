package me.liuyingowo.oldcombat.loader;

import java.lang.instrument.Instrumentation;

public final class Agent {

    private static volatile Instrumentation instrumentation;
    private static volatile boolean isPremain = false;
    private static volatile boolean isAgentMain = false;

    private Agent() { throw new UnsupportedOperationException("This class is not supposed to be instantiated"); }

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        isPremain = true;
    }

    public static void agentmain(String args, Instrumentation inst) {
        instrumentation = inst;
        isAgentMain = true;
    }
}