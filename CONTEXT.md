# SparkDLC context map

SparkDLC is one Fabric mod for Minecraft 1.21.1 (mod id and only namespace `sparkdlc`, version shared by all modules) built on Spark-era
Wathe and NoellesRoles. It was created on 2026-11-19 by fusing five mods; their histories are merged in, so
`git log --follow` and `git blame` reach back into each legacy repository.

## Modules

| Legacy module | Main packages now (under `dev.caecorthus.sparkdlc`) | Legacy mod id (a `provides` alias, no longer a namespace) | What it owns | Module notes |
| --- | --- | --- | --- | --- |
| factionapi | `faction`, `round`, `cooldown`, `replay`, `record`, `team`, `net.version` | `sparkfactionapi` | Faction registration, native Wathe factions, round end and win services, payload policy, replay, anti-fast-round, lobby map reset, version handshake | `docs/factionapi/CONTEXT.md` |
| strength | `role.<category>.<role>`, `item`, `entity`, `sabotage`, `tablet`, `economy`, `viewpoint` | `sparkstrength` | Buffs and reworks for Wathe/NoellesRoles roles, items (M67, tablet, drones...), sabotage, Partygoer | — |
| traits | `trait` | `sparktraits` | Passive trait mechanics and their planning, sync and client presentation | — |
| witch | `role.<category>.<role>`, `skill`, `faction.witch`, `item`, `net` | `sparkwitch` | Witch and Jiahao factions, their roles, the active skill framework, most new roles and items | `docs/witch/CONTEXT.md` |
| assist | `guidebook`, `achievement`, `ponder`, `config`, `sound`, `client.settings` | `sparkassist` | Client-side helpers: guidebook, achievements, unified settings screen, Ponder scenes, HUD aids | `docs/assist/CONTEXT.md` |

Since the stage-1 unification move the code is organised by function, not by legacy module: a role's items,
components, payloads and services live in `role.<category>.<role>` (categories `civilian`, `killer`, `neutral`,
`jiahao`, `witch`, `special`), shared systems in packages such as `trait`, `faction`, `round`, `cooldown`, `skill`,
`item`, `net`, and every mixin under `mixin.*` / `client.mixin.*`. The legacy registries and facades still exist
as classes (root package, `bridge.*`); the five former initializer classes (`SparkFactionApiMod`, `SparkStrength`,
`SparkTraits`, `SparkWitch`, `SparkAssist` and three client counterparts) start nothing any more and only hold
`MOD_ID`, `LOGGER`, `id(...)` and a few forwarders until a scripted pass renames their call sites.
`tools/unify/README.md` describes the move, and `tools/unify/file-moves.tsv` lists where every file came from.

Common code is in `src/main/java`, client code in `src/client/java` (every client class sits under the
`client` package). `assist` is client-only in behaviour: its common-side classes are pure data and rules and are
only reached from client code.

## Stable contracts of the fusion

- **One namespace, `sparkdlc`.** Since the stage-2a rename (`tools/unify/README.md`) every registry id, packet id,
  component key, role / faction / trait / skill id, shop-entry id, command, permission node, lang key, asset and
  data path and config file name of the mod is under `sparkdlc`; the lang files and `sounds.json` are single merged
  files under `assets/sparkdlc` in `src/main/resources`. `tools/unify/legacy-ids.tsv` lists every renamed id with its
  kind (old id, new id). The five legacy mod ids are no namespaces any more. They stay in `fabric.mod.json` under
  `provides`, so `FabricLoader#isModLoaded("sparkwitch")` and `#getModContainer("sparkwitch")` still resolve to the
  SparkDLC container, and `SparkDlcModule` lists them (ids only: no text shown to a player names a former mod). What deliberately keeps a legacy
  name: the retired-trait filter (`RetiredTraitIds`), private NBT / custom-data field names, `sparkwitch$`-style mixin member prefixes and Java class names
  (`tools/unify/namespace-keep.tsv`, `namespace-overrides.tsv`). Reading worlds, configs and packs that still use
  the old names is the job of the compatibility layer (stage 2b), not of the code that owns an id.
- **One entry, and its order is behaviour.** The mod has one class per entrypoint kind: `SparkDLC` (common),
  `client.SparkDLCClient` (client), `SparkDlcComponents` (Cardinal Components; the ids are declared once under
  `custom.cardinal-components` in `fabric.mod.json`), `voice.SparkDlcVoiceChatPlugin` (Simple Voice Chat) and
  `client.SparkDlcClientMixinPlugin` (the plugin of the client mixin config). `SparkDLC#onInitialize` and
  `SparkDLCClient#onInitializeClient` are the whole start-up, written as an ordered list of named steps. The
  sequence is the one the five separate mods had in Fabric's alphabetical mod-id order: common side faction and
  round services, kits of the Wathe / NoellesRoles roles, traits, the mod's own roles, then commands, the creative
  tab and the legacy registry aliases; client side settings, guidebook and achievements first, then the same four.
  It must not be reordered casually: listeners on one event run in registration order and many are first-wins,
  the faction policy lists are evaluated in registration order, the Role Skill 2 key keeps the first handler per
  role, and voice chat handlers of equal priority run in registration order. `./gradlew runGameTest
  -PstartupOrderDump=<dir>` (and `runClientAudit` likewise) writes what the start-up registered, in order, so that
  two builds can be diffed; the game test `SparkDlcStartupTest` pins the component ids and two listener orders.
  The two mixin configs (`sparkdlc.mixins.json`, `sparkdlc.client.mixins.json`) list the mixins of the former
  nine configs in the same module order on each side; the common config is listed first.
- **Shared resource files.** Five files that several mods used to ship are single merged files now:
  `assets/minecraft/font/default.json`, `data/sparkdlc/tags/item/{bloodthirsty_weapons,throwables,thrust_weapons}.json`
  and `data/wathe/tags/item/guns.json`. A module adds its entries to the shared file.
- **Module calls are direct.** The modules were separate jars that reached each other through reflection behind
  `isModLoaded` gates. Since the fusion they call each other with compiled calls (the `*Bridge` / `*Compat`
  classes are thin facades over them), a failure inside the called module propagates instead of being turned
  into a default, and there is no absent-module fallback. `AGENTS.md` (Cross-Module Check) lists the few seams
  that are still resolved by name.
- **Dependencies.** Wathe and NoellesRoles jars are in `libs/`, pinned by sha256 in `gradle.properties`.

## Verification entry points

See `AGENTS.md` (Verification). Module-specific verification notes stay in the module `CONTEXT.md` files; where they
name legacy task names or a legacy `build.gradle`, the task now carries the module infix
(`verifyWitchClientMixinSelectors`, ...) and lives in `gradle/verify/<module>.gradle`.

## Bringing in new legacy commits

Until every contributor works here, the legacy repositories may still receive commits. `tools/fusion/README.md`
explains how to rewrite and merge them (the rewrite is deterministic, so it is an ordinary `git merge`).
