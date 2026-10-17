package me.liuyingowo.oldcombat;

import me.liuyingowo.oldcombat.command.ReloadCommand;
import me.liuyingowo.oldcombat.loader.Agent;
import me.liuyingowo.oldcombat.loader.NmsBridgeInjector;
import me.liuyingowo.oldcombat.loader.PatchInstaller;
import me.liuyingowo.oldcombat.module.LegacyAttributeModule;
import me.liuyingowo.oldcombat.module.ModuleLoader;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.instrument.Instrumentation;

public final class CuteOldCombat extends JavaPlugin {

    private Instrumentation instrumentation;
    private ModuleLoader loader;
    private ReloadCommand oldCombatCommand;

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

        this.loader = new ModuleLoader();
        loader.register(new LegacyAttributeModule(this));

        if (getConfig().getBoolean("enable")) {
            loader.enable(LegacyAttributeModule.class);
        }

        getLogger().info("Loaded Complete.");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        PatchInstaller.uninstall(instrumentation, getLogger());

        if (oldCombatCommand != null) {
            oldCombatCommand.unregister(this);
        }
        loader.shutdown();
        loader = null;
        instrumentation = null;
    }

    public void reload() {
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

            loader.reload(LegacyAttributeModule.class);
        } else {
            PatchInstaller.uninstall(instrumentation, getLogger());

            loader.disable(LegacyAttributeModule.class);
        }
    }
}