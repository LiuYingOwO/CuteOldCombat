package me.liuyingowo.oldcombat.nms;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;

/**
 * Minecraft 版本枚举，自动检测当前服务器版本并映射到对应的 NMS 适配器实现。
 * 命名约定：{@link #name()} 的小写形式对应 nms 子包名，
 * 例如 {@code v1_21_R7} → {@code me.liuyingowo.oldcombat.nms.v1_21_r7.NmsAdapterImpl}。
 */
public enum Version {

    v1_20_R4("1.20.5", "1.20.6"),
    v1_21_R7("1.21.11"),
    ;

    /** 当前运行环境的版本常量 */
    public static final Version CURRENT;

    static {
        CURRENT = fromMinecraftVersion(getCurrentMinecraftVersion());
    }

    /**
     * 从 Minecraft 版本字符串（如 "1.21.11"）查找匹配的 Version 枚举。
     */
    @Nullable
    public static Version fromMinecraftVersion(String minecraftVersion) {
        for (Version version : values()) {
            for (String candidate : version.minecraftVersions) {
                if (candidate.equals(minecraftVersion)) {
                    return version;
                }
            }
        }
        return null;
    }

    /**
     * 获取当前服务器的 Minecraft 版本字符串（如 "1.21.11"）。
     */
    public static String getCurrentMinecraftVersion() {
        // Bukkit.getBukkitVersion() 返回 "1.21.11-R0.1-SNAPSHOT"，取第一部分
        return Bukkit.getServer().getBukkitVersion().split("-", 2)[0];
    }

    private final String[] minecraftVersions;

    Version(String... minecraftVersions) {
        this.minecraftVersions = minecraftVersions;
    }

    public String[] getMinecraftVersions() {
        return minecraftVersions;
    }

    public String getAdapterClassName() {
        return "me.liuyingowo.oldcombat.nms.impl." + name() + ".NmsAdapterImpl";
    }
}
