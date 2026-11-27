package me.liuyingowo.oldcombat.nms.event;

import me.liuyingowo.oldcombat.nms.Inject;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import java.util.*;

@Inject(type = Inject.InjectType.EVENT)
public abstract class OldCombatEvent extends Event {

    static {
        System.out.println("[<clinit>] OldCombatEvent injected into " + OldCombatEvent.class.getClassLoader());
        System.out.println("[<clinit>] try to get Bukkit Server: " + Bukkit.getServer());
    }
}
