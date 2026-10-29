# plusls carpet addition

[![Issues](https://img.shields.io/github/issues/Nyan-Work/plusls-carpet-addition?style=flat-square)](https://github.com/Nyan-Work/plusls-carpet-addition/issues)
[![Pull Requests](https://img.shields.io/github/issues-pr/Nyan-Work/plusls-carpet-addition?style=flat-square)](https://github.com/Nyan-Work/plusls-carpet-addition/pulls)
[![CI](https://img.shields.io/github/actions/workflow/status/Nyan-Work/plusls-carpet-addition/build.yml?label=Build&style=flat-square)](https://github.com/Nyan-Work/plusls-carpet-addition/actions/workflows/CI.yml)
[![Release](https://img.shields.io/github/v/release/Nyan-Work/plusls-carpet-addition?include_prereleases&style=flat-square)](https://github.com/Nyan-Work/plusls-carpet-addition/releases)
[![Github Release Downloads](https://img.shields.io/github/downloads/Nyan-Work/plusls-carpet-addition/total?label=Github%20Release%20Downloads&style=flat-square)](https://github.com/Nyan-Work/plusls-carpet-addition/releases)

## The project will be maintained in this repository until plusls back.

English | [中文](./README_ZH_CN.md)

❗Before reporting a problem, be sure to try the latest [beta version](https://github.com/Nyan-Work/plusls-carpet-addition/releases) to check if the problem still exists.

📌If you only want the PCA sync protocol and are not interested in other features, I recommend the lightweight [pca-protocol](https://github.com/Fallen-Breath/pca-protocol).

This is a [Carpet mod](https://github.com/gnembon/fabric-carpet) extension mod, a collection of carpet mod style useful tools and interesting features.

Operation command: `/pca`

> **Minecraft 26.3 (Fabric) port.** This checkout is a single-version port of PCA to **Minecraft 26.3**.
> Migration report, mixin pitfalls and verification records: [docs/PORT-26.3-方案.md](./docs/PORT-26.3-方案.md).

## Build & run

| Item | Value |
| --- | --- |
| Minecraft | 26.3 |
| Fabric Loader | 0.20.1 |
| Fabric API | 0.165.0+26.3 |
| Carpet | 26.3 |
| MagicLib | v0.8.882-mc26.3 |
| Java | 25 |
| Gradle | 9.6.0 (wrapper) |

MC 26.3 ships **unobfuscated**, so the port uses `net.fabricmc.fabric-loom` with remapping disabled:
no `mappings(...)`, no intermediary, no `modImplementation` / `remapJar` — dependencies are plain `implementation` / `runtimeOnly`.

```bash
./gradlew build
# -> build/libs/PluslsCarpetAddition-26.3-0.4.0-26.3.jar
```

Drop the jar into `mods/` next to Carpet, Fabric API and MagicLib.

## Dependencies

| Dependency | Link1                                                               | Link2                                              | Link3                                           |
|------------|---------------------------------------------------------------------|----------------------------------------------------|-------------------------------------------------|
| Carpet     | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/carpet)   | [GitHub](https://github.com/gnembon/fabric-carpet) | [Modrinth](https://modrinth.com/mod/carpet)     |
| Fabric API | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric)   | [GitHub](https://github.com/FabricMC/fabric)       | [Modrinth](https://modrinth.com/mod/fabric-api) |
| MagicLib   | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/magiclib) | [GitHub](https://github.com/Hendrix-Shen/MagicLib) | [Modrinth](https://modrinth.com/mod/magiclib)   |

## Rule List
## autoTrade
Use dispenser to auto trade with villager.

If EMERALD_BLOCK under the dispenser, it will trade once.

If DIAMOND_BLOCK under the dispenser, it will trade all.

Trade offer depend on redStone power.
- Categories: `Dispenser`, `Feature`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## avoidAnvilTooExpensive
Allow anvil level cost above 40 (If the client is not installed mod, it will be too expensive but can be used in practice).
- Categories: `Feature`, `Need Client`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## creativePlayerNoDirectKillArmorStand
Creative Player No Direct Kill ArmorStand.
- Categories: `Creative`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## dispenserCollectXp
Dispenser use bottle to collect xp.
- Categories: `Feature`, `Dispenser`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## dispenserFixIronGolem
Dispenser can fix iron golem.
- Categories: `Feature`, `Dispenser`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## emptyShulkerBoxStack
Empty shulker boxes stack

Empty shulker boxes can stack in a player's inventory or hand.

But empty shulker boxes will not stack in other inventories, such as chests or hoppers.
- Categories: `Feature`, `Need Client`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## flippingTotemOfUndying
Players can flip and rotate blocks when holding Totem Of Undying.

Doesn't cause block updates when rotated/flipped.

When Totem Of Undying in main hand,  offhand is empty will flip block.

When Totem Of Undying in main hand,  offhand is not empty, will place flipped block.
- Categories: `Feature`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## forceRestock
Use spectral arrow to shoot villager to force restock.
- Categories: `Feature`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## gravestone
A player head is placed where the player died; it keeps the whole inventory plus half of the total experience.
No custom block or item is involved — the gravestone is a vanilla `player_head` carrying extra NBT data.
- Categories: `Feature`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)

### Related rules

| Rule | Default | Meaning |
| --- | --- | --- |
| `gravestoneProtectionCount` | `3` | How many explosion / piston hits the gravestone survives. The value is written into the gravestone, so changing the rule does not retro-fit existing ones. |
| `gravestoneNotifyPosition` | `true` | Send the gravestone coordinates to the dead player in chat. |
| `gravestoneFluidGraceSeconds` | `300` | How many seconds a gravestone survives while water / lava actually flows into it. `0` = never destroyed by fluids. |
| `gravestoneFluidGraceReset` | `false` | `false` freezes the countdown once the fluid is gone, `true` resets it to zero. |

### Behaviour

- **Breaking it yourself** — the whole inventory drops, the experience is granted **directly to the player** (no experience-orb entities), and the gravestone itself drops nothing.
- **Explosions / pistons** — the protection count is consumed first; once exhausted the mod destroys the gravestone itself, dropping the contents but **never** a `player_head` item.
- **Water / lava** — fluids are kept out of the gravestone block completely. The countdown only advances when a fluid would really flow into it: a source next to it, fluid above it, or a flowing block whose flow vector points at it. Fluid below it and thin `LEVEL 7` edge water do not count.
- **Expiry** — when the countdown reaches `gravestoneFluidGraceSeconds`, the contents are dropped at the gravestone position (again without a skull item).
- **Ticking** — the countdown only advances while the chunk is ticking, so it freezes when nobody is around.

![player_dead.gif](./docs/player_dead.gif)
![break_gravestone.gif](./docs/break_gravestone.gif)
## pcaDebug
pcaDebug mode.
- Categories: `Debug`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## pcaSyncPlayerEntity
Which player entity can be sync.

NOBODY: nobody will be sync.

BOT: carpet bot will be sync.

OPS: carpet bot will be sync, and op can sync everyone's player entity data.

OPS_AND_SELF: carpet bot and self data will be sync, and op can sync everyone's player entity data.

EVERYONE: everyone's player entity will be sync.
- Categories: `Protocol`
- Type: `Enum`
- Default value: ops
- Options: `nobody`, `bot`, `ops`, `ops_and_self`, `everyone`
- Validators:
    - Enum(Case-sensitive)
## pcaSyncProtocol
Support sync entity and blockEntity from server.
- Categories: `Protocol`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## playerOperationLimiter
One tick player can place 2 block, insta break 1 block, can't do it at the same tick.
- Categories: `Feature`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## playerSit
Player can sit down when fast sneak 3 times.
- Categories: `Feature`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## potionRecycle
Dispenser can clear potion to cauldron.
- Categories: `Feature`, `Dispenser`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## powerfulBoneMeal
Allow use bone meal in cactus, sugar cane, chorus flower.
- Categories: `Feature`, `Dispenser`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## quickLeafDecay
quick leaf decay.
- Categories: `Feature`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## renewableNetheriteEquip
Only in nether, throw the diamond equipment with 1 durability to lava fluid to get netherite equipment.
- Categories: `Feature`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
- Dependencies:
    - And (All conditions need to be satisfied):
        - minecraft: >1.15.2

## sleepingDuringTheDay
World will switch to night when player sleep during the day.
- Categories: `Feature`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## spawnBiome
spawn biome.
- Categories: `Feature`
- Type: `Enum`
- Default value: default
- Options: `default`, `desert`, `plains`, `the_end`, `nether_wastes`
- Validators:
    - Enum(Case-sensitive)
## spawnYMax
spawn Y Max, 114514 to close.
- Categories: `Feature`
- Type: `Integer`
- Default value: 114514
## spawnYMin
spawn Y Min, 114514 to close.
- Categories: `Feature`
- Type: `Integer`
- Default value: 114514
## superLead
Leash villagers and mobs by lead.
- Categories: `Feature`, `Need Client`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## trackItemPickupByPlayer
When item pick up by player, item will freeze.
- Categories: `Creative`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## useDyeOnShulkerBox
Dyes can be used on shulker boxes, empty potion will clean color.
- Categories: `Feature`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## villagersAttractedByEmeraldBlock
Villagers are attracted by emerald block.
- Categories: `Feature`
- Type: `Boolean`
- Default value: false
- Options: `true`, `false`
- Validators:
    - Strict(Case-insensitive)
## xaeroWorldName
set xaero world name to sync word id to xaerominimap, "#none" is disable.
- Categories: `Protocol`
- Type: `String`
- Default value: #none
- Options: `#none`

## Port status (26.3)

All rules below are registered on a real 26.3 dedicated server and their mixins apply cleanly; the per-feature verification log lives in [docs/PORT-26.3-方案.md](./docs/PORT-26.3-方案.md).

Known limitations:

- `pcaSyncProtocol` compiles and initialises, but the receiving side is an external client mod — not verified end to end.
- `gravestone`: replacing a gravestone with `/setblock`, `/fill` or `/clone` does not go through the loot hook, so its contents are lost. Player mining, explosions, pistons and other mods replacing the block are handled.
- `gravestone`: the skull item is intentionally never dropped, and the fluid countdown only advances while the chunk is ticking.
- `emptyShulkerBoxStack` overlaps with Carpet 26.3's own `stackableShulkers*` mixin on `ItemStack#getMaxStackSize`.

## Development

### Support

This checkout targets **Minecraft 26.3 (Fabric) only**. The upstream multi-version `preprocess` layout is intentionally not used here — `versions/`, `common.gradle`, `settings.json` and the fabricWrapper scripts are kept for reference only.

- Minecraft 26.3
- Fabric Loader 0.20.1
- Java 25
- Gradle 9.6.0

Build: `./gradlew build` → `build/libs/PluslsCarpetAddition-26.3-0.4.0-26.3.jar`.
Porting notes, mixin pitfalls and verification records: [docs/PORT-26.3-方案.md](./docs/PORT-26.3-方案.md).

### Mappings

MC 26.3 no longer ships obfuscated, so this port does **not** use any mappings: Loom runs with remapping disabled and every symbol is the official unobfuscated name. That is also why `modImplementation` / `modCompileOnly` are unavailable — use plain `implementation` / `compileOnly`.

### Document

The English doc and the Chinese doc are aligned line by line.

## License

This project is available under the CC0-1.0 license. Feel free to learn from it and incorporate it in your own projects.
