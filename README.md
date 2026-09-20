# CuteOldCombat

> A Paper plugin that restores 1.8-style combat behavior on modern Minecraft servers through NMS and ByteBuddy hooks.

## Features
- 取消攻击冷却
- 可调战斗相关 Attribute
- 禁用横扫攻击判定与横扫伤害
- 禁用横扫攻击音效
- 可选替换 击退参数, 默认提供1.8战斗机制算法
- 支持nms-patch以及knockback设置热重载

## Warnings
这是一个侵入式插件, 由于直接修改了服务端底层, 请注意：

- 其他修改攻击、击退、伤害流程的插件可能与本插件冲突。
- ByteBuddy 在某些 JVM 参数或其他 JDK 下可能失败。

## Requirements
- Java 21+
- Paper / Paper-fork server
- JVM flags (Optional, choose one of the two):
```bash
-Djdk.attach.allowAttachSelf=true
-XX:+EnableDynamicAgentLoading
```
```bash
-javaagent:plugins/CuteOldCombat.jar
``` 

## Supported Versions
当前代码中已经声明的版本：

| Minecraft | Adapter |
| --- | --- |
| 1.20.5 / 1.20.6 | `v1_20_R4` |
| 1.21.11 | `v1_21_R7` |
| 26.1.1 / 26.1.2 | `v26_1` |
| 26.2 | `v26_2` |

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

`knockback.enabled` 默认为 `false`, 默认只处理攻击冷却、横扫攻击和攻击速度, 而不处理击退.

开启后，插件会更改 `LivingEntity#knockback`，按配置重新计算击退，并继续调用 Paper 的 `EntityKnockbackEvent`。
如果事件被其他插件取消，则击退也会被取消。

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
