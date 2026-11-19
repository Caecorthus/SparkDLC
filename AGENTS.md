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
- `factionapi` is the shared contract; `traits` and `witch` call it directly. Many other seams between modules are
  still reflection bridges from the time the modules were separate jars (packages named `compat` / `bridge`);
  search for the string form of a class or method name before renaming or moving it.
- Prove that unrelated roles, factions, and traits remain outside the changed predicate and behavior path.
- Keep gameplay, wire formats and saved data unchanged unless the owner asks for the change.
- Use Java 21 for tests, verify tasks, and builds. Never add production-only test hooks.

### Verification
- `./gradlew build` compiles and packages; it runs neither tests nor verify tasks.
- `./gradlew verifyAll` runs every module's ported verify tasks (`gradle/verify/<module>.gradle`).
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
