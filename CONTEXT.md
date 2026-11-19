# SparkDLC context map

SparkDLC is one Fabric mod for Minecraft 1.21.1 (`sparkdlc`, version shared by all modules) built on Spark-era
Wathe and NoellesRoles. It was created on 2026-11-19 by fusing five mods; their histories are merged in, so
`git log --follow` and `git blame` reach back into each legacy repository.

## Modules

| Module | Package | Namespace (legacy mod id) | What it owns | Module notes |
| --- | --- | --- | --- | --- |
| factionapi | `dev.caecorthus.sparkdlc.factionapi` | `sparkfactionapi` | Faction registration, native Wathe factions, round end and win services, payload policy, replay, anti-fast-round, lobby map reset, version handshake | `docs/factionapi/CONTEXT.md` |
| strength | `dev.caecorthus.sparkdlc.strength` | `sparkstrength` | Buffs and reworks for Wathe/NoellesRoles roles, items (M67, tablet, drones...), sabotage, Partygoer | — |
| traits | `dev.caecorthus.sparkdlc.traits` | `sparktraits` | Passive trait mechanics and their planning, sync and client presentation | — |
| witch | `dev.caecorthus.sparkdlc.witch` | `sparkwitch` | Witch and Jiahao factions, their roles, the active skill framework, most new roles and items | `docs/witch/CONTEXT.md` |
| assist | `dev.caecorthus.sparkdlc.assist` | `sparkassist` | Client-side helpers: guidebook, achievements, unified settings screen, Ponder scenes, HUD aids | `docs/assist/CONTEXT.md` |

Common code is in `src/main/java`, client code in `src/client/java` (each module's client classes sit in its
`client` subpackage). `assist` is client-only in behaviour: its common-side classes are pure data and rules and are
only reached from client code.

## Stable contracts of the fusion

- **Namespaces never change.** Registry ids, packet ids, component ids, asset paths, lang keys, config file names,
  commands and mixin config names keep the legacy mod ids. `fabric.mod.json` lists the five ids under `provides`,
  so `FabricLoader#isModLoaded("sparkwitch")` and `#getModContainer("sparkwitch")` resolve to the SparkDLC container.
  Code that needs a module's display name uses `SparkDlcModule`, not the container name.
- **Start order.** `SparkDLC#onInitialize` runs factionapi, strength, traits, witch; `SparkDLCClient` runs assist,
  factionapi, strength, traits, witch. This is the alphabetical mod-id order Fabric used for the separate mods.
  The nine mixin configs are listed in `fabric.mod.json` in the same order.
- **Shared resource files.** Five files that several mods used to ship are single merged files now:
  `assets/minecraft/font/default.json`, `data/sparktraits/tags/item/{bloodthirsty_weapons,throwables,thrust_weapons}.json`
  and `data/wathe/tags/item/guns.json`. A module adds its entries to the shared file.
- **Dependencies.** Wathe and NoellesRoles jars are in `libs/`, pinned by sha256 in `gradle.properties`.

## Verification entry points

See `AGENTS.md` (Verification). Module-specific verification notes stay in the module `CONTEXT.md` files; where they
name legacy task names or a legacy `build.gradle`, the task now carries the module infix
(`verifyWitchClientMixinSelectors`, ...) and lives in `gradle/verify/<module>.gradle`.

## Bringing in new legacy commits

Until every contributor works here, the legacy repositories may still receive commits. `tools/fusion/README.md`
explains how to rewrite and merge them (the rewrite is deterministic, so it is an ordinary `git merge`).
