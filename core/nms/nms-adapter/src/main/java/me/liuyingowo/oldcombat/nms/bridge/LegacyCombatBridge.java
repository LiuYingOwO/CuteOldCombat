package me.liuyingowo.oldcombat.nms.bridge;

import me.liuyingowo.oldcombat.nms.Inject;

@Inject(type = Inject.InjectType.BRIDGE)
public final class LegacyCombatBridge {

    public static volatile boolean enabled = true;
    public static volatile boolean disable_sweep_attack = true;
    public static volatile boolean enable_legacy_damage = true;
    public static volatile boolean critical_while_sprinting = true;

    private LegacyCombatBridge() {}
}
