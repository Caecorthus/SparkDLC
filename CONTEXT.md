# SparkDLC context map

SparkDLC is one Fabric mod for Minecraft 1.21.1 (mod id and only namespace `sparkdlc`) built on Spark-era Wathe and
NoellesRoles. It was created on 2026-11-19 by fusing five add-ons (SparkFactionAPI, SparkStrength, SparkTraits,
SparkWitch, SparkAssist) and then unified into one mod inside; their histories are merged in, so `git log --follow`
and `git blame` reach back into each legacy repository. `tools/unify/README.md` records every step of the
unification and the scripts that did it.

## Where things are

Common code is in `src/main/java`, client code in `src/client/java` (every client class sits under `client`), game
tests and the start-up / client audits in `src/gametest`. Packages under `dev.caecorthus.sparkdlc`:

| Area | Packages | Notes |
| --- | --- | --- |
| Start-up and registries | root package: `SparkDLC`, `SparkDlcItems`, `SparkDlcBlocks`, `SparkDlcEntities`, `SparkDlcSounds`, `SparkDlcDataComponents`, `SparkDlcParticles`, `SparkDlcRoles`, `SparkDlcFactions`, `SparkDlcDeathReasons`, `SparkDlcItemGroups`, `SparkDlcComponents`; `client.SparkDLCClient` | One class per entrypoint kind, one registry class per kind. |
| Shared core | `faction` (except `faction.witch`), `round`, `cooldown` (except `cooldown.provider`), `command.admin`, with `mixin.faction`, `mixin.round`, `mixin.cooldown`, `client.round`, `client.mixin.faction` | Factions and native Wathe factions, round end and win rules, settlement, anti-fast-round, lobby map reset, cooldown services. Contract: `faction.api`, `cooldown.api`, `round.settlement.SettlementExclusions`, `round.end.RoundEndRows`. Notes: `docs/core/CONTEXT.md`. |
| Roles | `role.<category>.<role>` (categories `civilian`, `killer`, `neutral`, `jiahao`, `witch`, `special`), `faction.witch`, `skill` (contract `skill.api`), `cooldown.provider`, `item`, `entity`, `block`, `sabotage`, `tablet`, `headset`, `economy`, `viewpoint`, and their `client.*` / `mixin.*` halves | Every role, new or reworked from Wathe / NoellesRoles, with its items, components, payloads, services and client presentation; the active skill framework; sabotage; the tablet. Notes: `docs/roles/CONTEXT.md`. |
| Traits | `trait` (contract `trait.api`), `client.trait`, `mixin.trait`, `client.mixin.trait` | Passive traits: planning, assignment, sync, effects, presentation. |
| Client helpers | `guidebook`, `achievement`, `record`, `ponder`, `config`, `input`, `sound`, `client.settings`, `client.guidebook`, `client.achievement`, `client.ponder` | Guidebook, achievements and match records, the one settings screen (`config/sparkdlc.json`), Ponder scenes, event sound volumes. Their common-side classes are pure data and rules, reached from client code only. Notes: `docs/client-helpers/CONTEXT.md`. |
| Replay | `replay` (contract `replay.api`) | Round replay and its formatters. |
| Networking | `net` (`SparkDlcPackets` + feature registrars), `net.version` (handshake), `net.policy` (`GeneratedPayloadPolicy`), `client.net` | One packet hub, one version handshake; the payload policy is generated from `config/payload-policy/actions.tsv` by `tools/payload_policy.py`. |
| Commands | `command` (`SparkDlcCommands`, `SparkDlcPermissions`) | Every command is registered from `SparkDlcCommands`. |
| Glue to other mods | `compat.noellesroles`, `client.compat.iris`, `mixin.wathe`, `mixin.minecraft`, `mixin.compat`, `voice` | Real compatibility code toward Wathe, NoellesRoles and optional mods. |
| Old names | `legacy`, `client.legacy`, `mixin.legacy`, `client.mixin.legacy`; `tools/unify/legacy-ids.tsv` | The only place that knows the names of the five former mods (see below). |

## Contracts of the one mod

- **One namespace, `sparkdlc`.** Every registry id, packet id, component key, role / faction / trait / skill id,
  shop-entry id, command, permission node, lang key, asset and data path and config file name of the mod is under
  `sparkdlc`. The lang files and `sounds.json` are single files under `assets/sparkdlc`. The five former mod ids are
  no namespaces; they stay in `fabric.mod.json` under `provides` (listed in `SparkDlcModule`), so a third-party mod
  asking `isModLoaded("sparkwitch")` still finds SparkDLC. No text shown to a player names a former mod.
- **Old names keep working, in one place.** The compatibility layer maps every name the five mods published to its
  `sparkdlc` name: registry ids (items, blocks, entities, sounds, effects, data components; also through
  `RegistryKey` look-ups), saved component data and private data names, Wathe role lists, attribute modifier ids,
  commands, typed ids and permission nodes (old command roots still run but are hidden from Tab completion), config
  files, achievement statistics, key bindings, debug system properties, and the client settings, which now live in
  one file `config/sparkdlc.json` (older files are carried over once and kept as backups). Each old name is a row of
  `tools/unify/legacy-ids.tsv`. Resource packs and data packs written for the old namespaces are not translated.
  The private names inside saved worlds (NBT keys, item custom-data markers, scoreboard teams, an entity tag) are
  `sparkdlc` names since 2026-11-20; their old names are read through `legacy.LegacyDataNames`. Names that still
  carry an old mod name on purpose (the HTTP `User-Agent` of the team sync, the public classes `SparkFactionApi` and
  `SparkTraitsApi`) are listed in `tools/unify/README.md`.
- **One entry, and its order is behaviour.** `SparkDLC#onInitialize` and `SparkDLCClient#onInitializeClient` are the
  whole start-up, written as an ordered list of named steps. Common side: faction and round services, role-kit
  content, the packet hub, role-kit gameplay, the version handshake, traits, the mod's own role content, roles and
  skills and their gameplay, commands, the creative tab, the registry aliases for old ids. Client side: carry-over
  of old client names, settings / guidebook / achievements, the faction and replay client, packets, then role kits,
  traits and the mod's own roles in the same order as on the common side. This is the order the five mods used to
  load in, and it must not be reordered casually: listeners on one event run in registration order and many are
  first-wins, the faction policy lists are evaluated in registration order, the Role Skill 2 key keeps the first
  handler per role, and voice chat handlers of equal priority run in registration order. `runGameTest
  -PstartupOrderDump=<dir>` (and `runClientAudit` likewise) writes what the start-up registered, in order, so two
  builds can be diffed; the game test `SparkDlcStartupTest` pins the component ids and listener orders.
- **Mixin order is behaviour.** The two configs (`sparkdlc.mixins.json`, `sparkdlc.client.mixins.json`) still list
  their entries in the order the former nine configs had; at equal priority that order is the application order
  (for `@WrapOperation` / `@WrapMethod` the first applied is innermost). Renaming or moving a mixin keeps its entry
  where it is; merging two mixins needs a proof that nothing between their former positions observes the change
  (`tools/unify/README.md`, "One of each" and "No twins").
- **Code calls code, and nothing inside is optional.** Features call each other with compiled calls, directly or
  through a contract package (`faction.api`, `trait.api`, `skill.api`, `cooldown.api`, `replay.api`); there is no
  fallback for a part of the mod being absent. The shared core names no role feature; roles hook it through its
  listener and rule lists. `gradle/verify/layering.gradle` enforces this (`AGENTS.md`, Cross-feature check).
- **Shared resource files.** `assets/minecraft/font/default.json`,
  `data/sparkdlc/tags/item/{bloodthirsty_weapons,throwables,thrust_weapons}.json` and
  `data/wathe/tags/item/guns.json` are single files that several features add entries to.
- **Dependencies.** Wathe and NoellesRoles jars are in `libs/`, pinned by sha256 in `gradle.properties` and checked
  by `verifyUpstreamJars`.

## Verification entry points

See `AGENTS.md` (Verification). The checks are tasks of `gradle/verify/*.gradle`, one script per kind of check and
each task named for what it checks (`verifyMixinConfigs`, `verifyUpstreamMixinSelectors`, `verifyLayering`,
`verifyBlackRavenSeams`, …); `./gradlew verifyAll` runs them all. Notes in the area `CONTEXT.md` files that name a
check refer to these tasks.

## Bringing in new legacy commits

Should a legacy repository still receive commits, `tools/fusion/README.md` explains how to rewrite and merge them
(the history rewrite is deterministic, so it is an ordinary `git merge`). Code from before the unification also has
to be translated to the current layout and names: `tools/unify/` holds the tables (`file-moves.tsv`,
`legacy-ids.tsv`, the rename tables) and `rename_classes.py translate`.
