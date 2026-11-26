package me.liuyingowo.oldcombat.nms.bridge;

import me.liuyingowo.oldcombat.nms.Inject;

/**
 * Loaded into Server URLClassLoader by {@code NmsInjector}.
 */
@Inject(type = Inject.InjectType.BRIDGE)
public final class KnockbackBridge {

    public static volatile boolean enabled = true;
    public static volatile double horizontal = 1.0D;
    public static volatile double vertical = 0.4000000059604645D;
    public static volatile double verticalLimit = 0.4000000059604645D;
    public static volatile double friction = 0.5D;
    public static volatile double minDirectionLength = 1.0E-5D;
    public static volatile boolean applyResistance = true;

    private KnockbackBridge() {}
}
