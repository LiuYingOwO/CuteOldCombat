package me.liuyingowo.oldcombat;

import me.liuyingowo.oldcombat.command.ReloadCommand;
import me.liuyingowo.oldcombat.listener.LegacyAttributeListener;
import me.liuyingowo.oldcombat.loader.Agent;
import me.liuyingowo.oldcombat.loader.NmsBridgeInjector;
import me.liuyingowo.oldcombat.loader.PatchInstaller;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.instrument.Instrumentation;

public final class CuteOldCombat extends JavaPlugin {

    private Instrumentation instrumentation;

    private ReloadCommand oldCombatCommand;
    private AttributeModifier attributeModifier;
    private LegacyAttributeListener legacyCombatListener;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.instrumentation = Agent.getInstrumentation();

        NmsBridgeInjector.injectIfNeeded(instrumentation, getLogger());
        PatchInstaller.install(instrumentation, getLogger(), getConfig());

        BridgeSync.syncKnockback(this);
        BridgeSync.syncLegacyCombat(this);
        BridgeSync.syncSound(this);
    }

    @Override
    public void onEnable() {
        oldCombatCommand = new ReloadCommand(this);
        attributeModifier = new AttributeModifier(this);
        legacyCombatListener = new LegacyAttributeListener(this, attributeModifier);

        attributeModifier.initializeAttributes();

        if (getConfig().getBoolean("enable")) {
            getServer().getPluginManager().registerEvents(legacyCombatListener, this);
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        PatchInstaller.uninstall(instrumentation, getLogger());

        if (attributeModifier != null) {
            for (var player : Bukkit.getOnlinePlayers()) {
                attributeModifier.restoreAttributes(player);
            }
        }
        if (oldCombatCommand != null) {
            oldCombatCommand.unregister(this);
        }
        legacyCombatListener = null;
        attributeModifier = null;
        instrumentation = null;
    }

    public void reload() {
        HandlerList.unregisterAll(this);

        saveDefaultConfig();
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        BridgeSync.syncKnockback(this);
        BridgeSync.syncLegacyCombat(this);
        BridgeSync.syncSound(this);

        if (getConfig().getBoolean("enable")) {
            if (!PatchInstaller.hasTransformer()) {
                PatchInstaller.install(instrumentation, getLogger(), getConfig());
            }
            attributeModifier = new AttributeModifier(this);
            legacyCombatListener = new LegacyAttributeListener(this, attributeModifier);
            attributeModifier.initializeAttributes();
            getServer().getPluginManager().registerEvents(legacyCombatListener, this);
        } else {
            PatchInstaller.uninstall(instrumentation, getLogger());
            attributeModifier = new AttributeModifier(this);
            attributeModifier.initializeAttributes();
            legacyCombatListener = null;
        }
    }
}