package me.liuyingowo.oldcombat.nms;

import me.liuyingowo.oldcombat.nms.adapter.NmsAdapter;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

/**
 * NMS 适配器管理器。
 * <p>参照 {@link Version#CURRENT} 自动检测服务器版本，
 * 通过 {@code Class.forName()} 反射加载对应版本的 {@link NmsAdapter} 实现。
 */
public final class NmsManager {

    private static volatile NmsAdapter adapter;

    private NmsManager() {
    }

    /**
     * 安装当前版本对应的 NMS 适配器。
     *
     * @param logger 日志记录器
     * @return true 表示安装成功，false 表示没有找到匹配的适配器
     */
    public static boolean install(Logger logger) {
        Version version = Version.CURRENT;

        if (version == null) {
            logger.severe("Unsupported Minecraft version! Detected: "
                    + Version.getCurrentMinecraftVersion());
            return false;
        }

        String className = "me.liuyingowo.oldcombat.nms.impl." + version.name() + ".NmsAdapterImpl";
        logger.info("Detected server version: " + version.name());

        try {
            Class<?> implClass = Class.forName(className, true, NmsManager.class.getClassLoader());

            if (!NmsAdapter.class.isAssignableFrom(implClass)) {
                logger.severe(className + " does not implement NmsAdapter.");
                return false;
            }

            Constructor<?> constructor = implClass.getDeclaredConstructor();
            adapter = (NmsAdapter) constructor.newInstance();

            logger.info("NmsAdapter loaded: " + adapter.getClass().getName());
            return true;
        } catch (ClassNotFoundException e) {
            logger.severe("NMS adapter class not found: " + className);
            return false;
        } catch (Exception e) {
            logger.severe("Failed to instantiate NMS adapter: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前已安装的 NMS 适配器。
     *
     * @throws IllegalStateException 如果适配器尚未安装
     */
    public static NmsAdapter getAdapter() {
        if (adapter == null) {
            throw new IllegalStateException(
                    "NmsAdapter has not been initialized. Call NmsManager.install() during onLoad.");
        }
        return adapter;
    }

    public static void applyLegacyAttackSpeed(Player player) {
        NmsAdapter current = adapter;
        if (current == null || player == null) {
            return;
        }

        current.applyLegacyAttackSpeed(player);
    };
    /**
     * 检查适配器是否已安装。
     */
    public static boolean isInstalled() {
        return adapter != null;
    }
}
