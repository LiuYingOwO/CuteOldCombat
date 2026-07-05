# CuteOldCombat

> A Paper plugin that restores 1.8-style combat behavior on modern Minecraft servers through NMS and ByteBuddy hooks.

CuteOldCombat 是一个面向高版本 Paper 服务端的旧版战斗兼容插件。
工作方式: 通过 ByteBuddy 对 NMS 方法做运行时 hook，把攻击冷却、横扫攻击、横扫音效和可配置击退尽量拉回 1.8 风格。

## Features
- 取消攻击冷却带来的低伤害惩罚
- 将玩家攻击强度强制视为满蓄力
- 为玩家应用高攻击速度 Attribute，避免客户端/服务端攻击间隔影响手感
- 禁用横扫攻击判定与横扫伤害
- 阻止横扫攻击音效播放
- 可选替换 Paper 原版击退流程，使用可配置的 legacy-style knockback
- 支持 `/cuteoldcombat reload` 热重载配置与重新安装 NMS transformer

## Warnings
这是一个侵入式插件。它会在运行时重转换 NMS 类，因此请注意：

- 其他修改攻击、击退、伤害流程的插件可能与本插件冲突。
- ByteBuddy 在某些 JVM 参数或其他 JDK 下可能失败。
- reload 会 reset 旧 transformer 再重新安装，但不代表所有运行时状态都能无损恢复。

建议先在测试环境确认攻击、击退、横扫和日志行为。

## Requirements
- Java 21+
- Paper / Paper-fork server
- JVM flags (Optional):
```bash
-Djdk.attach.allowAttachSelf=true
-XX:+EnableDynamicAgentLoading
```
或
```bash
-javaagent:plugins/CuteOldCombat.jar
``` 

这个项目是 mojang-mapped。
它 ***不是*** Bukkit插件, 切勿将他放进 spigot/spigot-fork 服务端中。

## Installation
1. 放入服务端 `plugins/` 目录。
2. *(可选)添加 JVM 参数:
   ```bash
   -Djdk.attach.allowAttachSelf=true
   -XX:+EnableDynamicAgentLoading
   ```
   或使用JavaAgent工作模式来启动它:
   ```bash
   -javaagent:plugins/CuteOldCombat.jar
   ```
4. 启动服务器。

## Supported Versions
当前代码中已经声明的版本：

| Minecraft | Adapter |
| --- | --- |
| 1.20.5 / 1.20.6 | `v1_20_R4` |
| 1.21.11 | `v1_21_R7` |
| 26.1.1 / 26.1.2 | `v26_1` |

如果服务端版本不在 `Version` 枚举中，NMS patches 不会安装。

## Configuration

默认配置：

```yml
knockback:
  enabled: false
  horizontal: 1.0
  vertical: 0.4000000059604645
  vertical-limit: 0.4000000059604645
  friction: 0.5
  min-direction-length: 0.00001
  apply-resistance: true
```

`knockback.enabled` 默认为 `false`。这意味着插件默认只处理攻击冷却、横扫攻击和攻击速度，不会接管原本的击退逻辑。

开启后，插件会 hook `LivingEntity#knockback`，按配置重新计算击退，并继续调用 Paper 的 `EntityKnockbackEvent`。如果事件被其他插件取消，击退也会被取消。

参数含义：

| Key | Meaning |
| --- | --- |
| `enabled` | 是否启用 NMS 击退替换 hook |
| `horizontal` | 传入击退强度的水平倍率 |
| `vertical` | 额外加入的垂直速度 |
| `vertical-limit` | 垂直速度上限 |
| `friction` | 添加击退前，对当前速度应用的摩擦系数 |
| `min-direction-length` | 忽略极小水平方向，避免异常方向向量 |
| `apply-resistance` | 是否应用实体的 knockback resistance 属性 |
