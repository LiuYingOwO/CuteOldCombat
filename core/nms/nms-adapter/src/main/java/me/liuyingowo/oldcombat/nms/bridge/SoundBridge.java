package me.liuyingowo.oldcombat.nms.bridge;

import me.liuyingowo.oldcombat.nms.Inject;

@Inject(type = Inject.InjectType.BRIDGE)
public final class SoundBridge {

    public static volatile boolean disable_sweep = true;
    public static volatile boolean disable_attack = true;
    public static volatile boolean disable_knockback = true;
    public static volatile boolean disable_nodamage = true;

    private SoundBridge() {}
}
