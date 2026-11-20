# SparkDLC

One mod (`sparkdlc`) that fuses the five former Spark add-ons for Spark-era Wathe. Read `CONTEXT.md` first: it is
the module map and points at each module's own `docs/<module>/CONTEXT.md`.

## Repositories
- https://github.com/XruiDD/TrainMurderMystery (Spark-ver wathe)
- https://github.com/XruiDD/NoellesRoles (Spark-ver NoellesRoles, a role expansion add-on mod for wathe)
- Legacy, kept for history only (their full histories are merged into this repository):
  SparkFactionAPI, SparkStrength, SparkTraits, SparkWitch, SparkAssist under https://github.com/Caecorthus

## Coding

### Architecture
- Modules are packages under `dev.caecorthus.sparkdlc`: `factionapi` (shared faction contract and round services),
  `strength` (role buffs, items, sabotage), `traits` (passive traits), `witch` (witch faction, roles, skills),
  `assist` (client-side helpers: guidebook, achievements, settings, Ponder). `SparkDLC` / `client.SparkDLCClient`
  start them in a fixed order.
- Each module keeps its legacy mod id as its namespace (`sparkfactionapi`, `sparkstrength`, `sparktraits`,
  `sparkwitch`, `sparkassist`) for registry ids, packets, components, assets, lang keys, config files and mixin
  configs. Never rename a namespace or an id: worlds, maps, resource packs and saved settings depend on them.
- Root architecture notes are local-only and must not be committed.

### Cross-Module Check
- Inspect the owning module and every direct consumer of the contract being changed. Do not mechanically read
  every module when the change is local and no cross-module seam is involved.
- Code calls code. There are no `*Bridge` / `*Compat` facades between features and no `bridge` packages any more:
  a feature calls another feature directly, through its contract package when it has one (`faction.api`,
  `trait.api`, `skill.api`, `cooldown.api`, `replay.api`). The shared core (`faction.*` except `faction.witch`, `round.*`,
  `cooldown.*` except `cooldown.provider`, `command.admin`) imports no role feature: a role hooks it by registering
  into the core's listener or rule lists (`PlayerAffectExemption`, `FactionCoWinnerRule`, `AdminCooldownClear`; the
  killer team purse has `KillerTeamEconomyService.WalletRule`). Do not add reflection or an `isModLoaded` check for one of the five
  legacy ids, and do not add a mixin into one of the mod's own classes (the only ones left are three client mixins
  that add the client half of a common class).
- Nothing of the mod's own is looked up by name from Java code any more. The client voice receivers are reached
  through the Fabric entrypoint `sparkdlc_client_voice` (`voice.ClientVoiceReceivers`), because Simple Voice Chat
  gathers plugin events before client initializers run. Classes under `compat.noellesroles` and the facades toward
  Wathe and optional mods are real compatibility code. Reflection on real optional mods (Iris, EMF, ETF, Ponder,
  voicechat) and on Wathe internals stays.
- `./gradlew verifyLayering verifyNoLookupByName` (`gradle/verify/layering.gradle`) checks which packages may name
  which (the shared core, the role features, `trait.api`, the client helpers) and that nothing of the mod is looked
  up by name; every exception is listed there with its reason, and a new one is added deliberately.
- Prove that unrelated roles, factions, and traits remain outside the changed predicate and behavior path.
- Keep gameplay, wire formats and saved data unchanged unless the owner asks for the change.
- Use Java 21 for tests, verify tasks, and builds. Never add production-only test hooks.

### Verification
- `./gradlew build` compiles and packages; it runs neither tests nor verify tasks.
- `./gradlew verifyAll` runs every verify task (`gradle/verify/*.gradle`, one script per kind of check).
- `./gradlew runGameTest` boots a headless server with all modules and audits every common mixin;
  `./gradlew runClientAudit` does the same for client mixins (opens a client window for about half a minute).
- `./gradlew test` runs the local-only unit and contract tests in `src/test` when they are present.

### Subagents
- You're the coordinator/leader between the subagents.
- Please use subagents. You can code with them together at the same time.
- Create multiple subagents for multiple purposes. Create as many as you needed.
	- Do NOT overload one subagent. Create multiple subagents to divide the tasks.
- Use English as the main language between you and your subagents.

### Annotations
- Add concise English and Chinese comments for non-obvious public API, mixin,
  Wathe seam, cross-module, or migration semantics. Do not duplicate self-evident
  code in comments.
- Keep existing comments when moving or rewriting code; update them when the behaviour they describe changes.
