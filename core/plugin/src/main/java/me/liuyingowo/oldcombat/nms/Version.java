package me.liuyingowo.oldcombat.nms;

import org.bukkit.Bukkit;

import javax.annotation.Nullable;

public enum Version {

    v1_20_R4("1.20.5", "1.20.6"),
    v1_21_R7("1.21.11"),
    v26_1("26.1", "26.1.1", "26.1.2")
    ;

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
        return Bukkit.getServer().getVersion().split("-", 2)[0];
    }

    private final String[] minecraftVersions;

    Version(String... minecraftVersions) {
        this.minecraftVersions = minecraftVersions;
    }

    public String[] getMinecraftVersions() {
        return minecraftVersions;
    }
}
