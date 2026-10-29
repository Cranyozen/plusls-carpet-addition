# plusls carpet addition

[![Issues](https://img.shields.io/github/issues/Nyan-Work/plusls-carpet-addition?style=flat-square)](https://github.com/Nyan-Work/plusls-carpet-addition/issues)
[![Pull Requests](https://img.shields.io/github/issues-pr/Nyan-Work/plusls-carpet-addition?style=flat-square)](https://github.com/Nyan-Work/plusls-carpet-addition/pulls)
[![CI](https://img.shields.io/github/actions/workflow/status/Nyan-Work/plusls-carpet-addition/build.yml?label=Build&style=flat-square)](https://github.com/Nyan-Work/plusls-carpet-addition/actions/workflows/CI.yml)
[![Release](https://img.shields.io/github/v/release/Nyan-Work/plusls-carpet-addition?include_prereleases&style=flat-square)](https://github.com/Nyan-Work/plusls-carpet-addition/releases)
[![Github Release Downloads](https://img.shields.io/github/downloads/Nyan-Work/plusls-carpet-addition/total?label=Github%20Release%20Downloads&style=flat-square)](https://github.com/Nyan-Work/plusls-carpet-addition/releases)

## 在 plusls 回归前，项目将在此仓库维护。

[English](./README.md) | 中文

❗在报告问题前，请务必尝试最新[测试版](https://github.com/Nyan-Work/plusls-carpet-addition/releases)，检查问题是否依然存在。

📌如果你只想要 PCA同步协议 而对其他功能不感兴趣，我推荐你使用轻量化的 [pca-protocol](https://github.com/Fallen-Breath/pca-protocol).

这是一个 [Carpet mod](https://github.com/gnembon/fabric-carpet) (fabric-carpet) 的扩展 mod，包含了不少~~NotVanilla的~~有意思的功能以及特性。

> **本仓库是 PCA 的 Minecraft 26.3（Fabric）单版本移植。**

## 构建与运行

| 项目 | 版本 |
| --- | --- |
| Minecraft | 26.3 |
| Fabric Loader | 0.20.1 |
| Fabric API | 0.165.0+26.3 |
| Carpet | 26.3 |
| MagicLib | v0.8.882-mc26.3 |
| Java | 25 |
| Gradle | 9.6.0（wrapper） |

MC 26.3 本身是**未混淆**版本，因此移植版使用 `net.fabricmc.fabric-loom` 并且关闭了重映射：没有 `mappings(...)`、没有 intermediary、也没有 `modImplementation` / `remapJar`，依赖一律用 `implementation` / `runtimeOnly`。

```bash
./gradlew build
# -> build/libs/PluslsCarpetAddition-26.3-0.4.0-26.3.jar
```

把 jar 和 Carpet、Fabric API、MagicLib 一起放进 `mods/`。

管理命令: `/pca`

## 依赖

| 依赖         | 链接1                                                                 | 链接2                                                | 链接3                                             |
|------------|---------------------------------------------------------------------|----------------------------------------------------|-------------------------------------------------|
| Carpet     | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/carpet)   | [GitHub](https://github.com/gnembon/fabric-carpet) | [Modrinth](https://modrinth.com/mod/carpet)     |
| Fabric API | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric)   | [GitHub](https://github.com/FabricMC/fabric)       | [Modrinth](https://modrinth.com/mod/fabric-api) |
| MagicLib   | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/magiclib) | [GitHub](https://github.com/Hendrix-Shen/MagicLib) | [Modrinth](https://modrinth.com/mod/magiclib)   |

## 规则列表
## 自动交易 (autoTrade)
使用发射器和村民自动交易。

如果发射器下面是绿宝石块，则交易一次。

如果发射器下面是钻石块，则尽可能的交易。

交易的条目取决于红石信号强度。
- 分类: `发射器`, `特性`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 防止铁砧过于昂贵 (avoidAnvilTooExpensive)
铁砧修复花费可以高于 40 并且不会因为过于昂贵无法使用（如果客户端不安装此 mod 则会显示过于昂贵，但是实际上可以使用）。
- 分类: `特性`, `需要客户端支持`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 创造模式下盔甲架不会被玩家直接杀死 (creativePlayerNoDirectKillArmorStand)
创造模式下盔甲架不会被玩家直接杀死。
- 分类: `创造`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 发射器收集经验 (dispenserCollectXp)
发射器消耗玻璃瓶来收集经验，产出附魔之瓶。
- 分类: `特性`, `发射器`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 发射器修复铁傀儡 (dispenserFixIronGolem)
发射器可以使用铁锭来修复铁傀儡。
- 分类: `特性`, `发射器`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 空潜影盒可堆叠 (emptyShulkerBoxStack)
空潜影盒可堆叠。

空潜影盒可以在玩家的背包进行自动堆叠，同时玩家可以手动堆叠空潜影盒。

但是空潜影盒无法在容器中自动堆叠，比如箱子，漏斗。
- 分类: `特性`, `需要客户端支持`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 不死图腾扳手 (flippingTotemOfUndying)
允许使用不死图腾调整方块朝向。

不会产生方块更新。

主手图腾副手为空则则会翻转方块。

主手图腾副手不为空且为方块则放出的方块会被反转。
- 分类: `特性`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 强制补货 (forceRestock)
使用光灵箭射村民来强制补货。
- 分类: `特性`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 墓碑 (gravestone)
玩家死亡后会在死亡附近的位置生成墓碑，其中保留玩家身上的全部物品以及一半的经验。墓碑不是自定义方块/物品，就是带额外 NBT 的原版 `player_head`。
- 分类: `特性`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)

### 相关规则

| 规则 | 默认值 | 说明 |
| --- | --- | --- |
| `gravestoneProtectionCount` | `3` | 墓碑能扛几次爆炸/活塞。数值写在墓碑里，改规则不会影响已经存在的墓碑。 |
| `gravestoneNotifyPosition` | `true` | 生成墓碑时把坐标发到死亡玩家的聊天栏。 |
| `gravestoneFluidGraceSeconds` | `300` | 被水/岩浆真正灌进来后还能撑多少秒；`0` = 永不因流体消失。 |
| `gravestoneFluidGraceReset` | `false` | `false` 流体离开后冻结倒计时，`true` 清零重来。 |

### 行为

- **玩家自己挖**：全部物品掉出，经验**直接进玩家身上**（不生成经验球实体），墓碑本身不掉落任何东西。
- **爆炸 / 活塞**：先消耗保护次数；次数用尽后由 mod 自己结算并移除方块，内容照掉，但**不会**掉一个 `player_head` 物品。
- **水 / 岩浆**：流体完全进不了墓碑格。只有在流体真的会灌进来时才推进倒计时——侧面是源方块、正上方有流体、或者流动流体的流向正对墓碑；墓碑正下方的流体和最外层 `LEVEL 7` 薄水不算。
- **到期**：倒计时到达 `gravestoneFluidGraceSeconds` 后，内容掉在墓碑位置（同样不掉头颅）。
- **计时前提**：只有区块在 tick 时倒计时才走，没人路过时会冻结。

![player_dead.gif](./docs/player_dead.gif)
![break_gravestone.gif](./docs/break_gravestone.gif)
## PCA调试模式 (pcaDebug)
打印更多调试信息
- 分类: `调试`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## PCA 同步协议可同步玩家数据 (pcaSyncPlayerEntity)
决定哪些玩家的数据将会被同步。

NOBODY：所有玩家数据都无法同步。

BOT：地毯 mod 召唤出的 bot 的数据可以被同步。

OPS：地毯 mod 召唤出的 bot 的数据可以被同步， op 可以同步所有玩家的数据。

OPS_AND_SELF：地毯 mod 召唤出的 bot 和玩家自己的数据可以被同步，op 可以同步所有玩家的数据。

EVERYONE：所有人的数据都可以被同步。
- 分类: `协议`
- 类型: `枚举`
- 默认值: ops
- 参考值: `nobody`, `bot`, `ops`, `ops_and_self`, `everyone`
- 验证器:
    - 枚举(区分大小写)
## PCA 同步协议 (pcaSyncProtocol)
支持同步 BlockEntity 和 Entity 到客户端。
- 分类: `协议`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 玩家操作限制器 (playerOperationLimiter)
每 gt 玩家可以放置 2 个方块，秒破 1 个方块，这两个操作每 gt 只能做一种（用于防人肉盾构机和玩家自动破基岩 mod）。
- 分类: `特性`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 玩家坐下 (playerSit)
在快速潜行 3 次后玩家可以坐下。
- 分类: `特性`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 可回收药水瓶 (potionRecycle)
发射器对着炼药锅发射时，如果发射器中有药水瓶，会将其清空为玻璃瓶。
- 分类: `特性`, `发射器`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 增强骨粉 (powerfulBoneMeal)
骨粉可以催熟甘蔗，仙人掌，紫颂花。
- 分类: `特性`, `发射器`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 快速叶子腐烂 (quickLeafDecay)
在砍树后树叶会快速掉落。
- 分类: `特性`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 可再生下界合金装备 (renewableNetheriteEquip)
只有在下界时，将剩余耐久为 1 钻石装备扔进岩浆流体时将会产生下界合金装备。
- 分类: `特性`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
- 依赖:
    - 与 (需要满足全部条件):
        - minecraft: >1.15.2

## 白天睡觉 (sleepingDuringTheDay)
玩家白天睡觉时时间会切换到晚上。
- 分类: `特性`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 全局刷怪群系 (spawnBiome)
全局刷怪群系，会影响整个游戏，DEFAULT 为默认。
- 分类: `特性`
- 类型: `枚举`
- 默认值: default
- 参考值: `default`, `desert`, `plains`, `the_end`, `nether_wastes`
- 验证器:
    - 枚举(区分大小写)
## 刷怪的最大 Y 值 (spawnYMax)
刷怪的最大 Y 值，会影响刷怪塔效率，114514 为默认。
- 分类: `特性`
- 类型: `整型`
- 默认值: 114514
## 刷怪的最小 Y 值 (spawnYMin)
刷怪的最小 Y 值，会影响刷怪塔效率，114514 为默认。
- 分类: `特性`
- 类型: `整型`
- 默认值: 114514
## 超级拴绳 (superLead)
村民和怪物可以被拴绳拴住（拴怪物需要客户端也安装 PCA）。
- 分类: `特性`, `需要客户端支持`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 追踪被玩家吸附的物品 (trackItemPickupByPlayer)
物品被玩家吸附后会变成屏障方块悬浮在原位。
- 分类: `创造`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 潜影盒使用染料染色 (useDyeOnShulkerBox)
可以使用染料直接对地上的潜影盒染色，用水瓶可以洗掉潜影盒的颜色。
- 分类: `特性`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## 村民被绿宝石块吸引 (villagersAttractedByEmeraldBlock)
村民会被玩家手中的绿宝石块吸引。
- 分类: `特性`
- 类型: `布尔`
- 默认值: false
- 参考值: `true`, `false`
- 验证器:
    - 严格(不区分大小写)
## Xaero 小地图世界名 (xaeroWorldName)
设置 Xaero 世界名来同步世界 ID,"#none" 表示不同步。
- 分类: `协议`
- 类型: `字符串`
- 默认值: #none
- 参考值: `#none`

## 移植状态（26.3）

下列规则都已在真实的 26.3 专用服务端上注册通过、mixin 全部应用成功；

已知限制：

- `pcaSyncProtocol` 能编译、能初始化，但接收端是外部的客户端 mod，未做端到端验证。
- `gravestone`：用 `/setblock`、`/fill`、`/clone` 替换墓碑不会走掉落结算，内容会丢失；玩家挖掘、爆炸、活塞以及其它模组替换方块都已覆盖。
- `gravestone`：故意不掉落头颅物品；流体倒计时只在区块 tick 时推进。
- `emptyShulkerBoxStack` 与 Carpet 26.3 自带的 `stackableShulkers*` 在 `ItemStack#getMaxStackSize` 上有重叠。

## 开发

### 支持

本仓库只针对 **Minecraft 26.3（Fabric）** 维护。上游那套多版本 `预处理` 流程这里不再使用：`versions/`、`common.gradle`、`settings.json` 和 fabricWrapper 脚本仅作参考保留。

- Minecraft 26.3
- Fabric Loader 0.20.1
- Java 25
- Gradle 9.6.0

构建：`./gradlew build` → `build/libs/PluslsCarpetAddition-26.3-0.4.0-26.3.jar`。

### 混淆映射表

MC 26.3 本身不再混淆，因此本移植版**不使用**任何映射表：Loom 关闭了重映射，所有符号都是官方未混淆名。这也是 `modImplementation` / `modCompileOnly` 在这里不可用的原因——请直接用 `implementation` / `compileOnly`。

### 文档

英文文档与中文文档是逐行对应的。

## 许可

此项目在 CC0-1.0许可证 下可用。 从中学习，并将其融入到您自己的项目中。
