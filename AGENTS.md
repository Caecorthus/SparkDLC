# SparkDLC

One Fabric mod (`sparkdlc`) for Spark-era Wathe and NoellesRoles, made by fusing five former add-ons. Read
`CONTEXT.md` first: it is the map of the code, and points at `docs/core/CONTEXT.md`, `docs/roles/CONTEXT.md` and
`docs/client-helpers/CONTEXT.md`.

## Repositories
- https://github.com/XruiDD/TrainMurderMystery (Spark-ver wathe)
- https://github.com/XruiDD/NoellesRoles (Spark-ver NoellesRoles, a role expansion add-on mod for wathe)
- Legacy, kept for history only (their full histories are merged into this repository):
  SparkFactionAPI, SparkStrength, SparkTraits, SparkWitch, SparkAssist under https://github.com/Caecorthus

## Coding

### Architecture
- One mod, organised by function under `dev.caecorthus.sparkdlc` (map in `CONTEXT.md`): roles in
  `role.<category>.<role>`, traits in `trait`, the shared core in `faction` / `round` / `cooldown` / `command.admin`,
  role skills in `skill`, items, entities, sabotage, networking and so on in their own packages, the client in
  `client.*`, every mixin in `mixin.*` / `client.mixin.*`. There are no per-former-mod packages, classes, registries
  or mixin configs; do not reintroduce any (no `SparkWitch*`-style names, no "kit of mod X" buckets).
- One namespace, `sparkdlc`, for every id, key, asset, command, permission node and config file. Old names from the
  five former mods are understood only by the compatibility layer in the `legacy` packages (`legacy`,
  `client.legacy`, `mixin.legacy`, `client.mixin.legacy`) and `tools/unify/legacy-ids.tsv`. Never rename an id that
  is already published without adding its old name there: worlds, maps, saved settings and packs depend on them.
  Never scatter old names through feature code.
- One entry per entrypoint kind (`SparkDLC`, `client.SparkDLCClient`, `SparkDlcComponents`,
  `voice.SparkDlcVoiceChatPlugin`, `client.SparkDlcClientMixinPlugin`) and one registry class per kind in the root
  package (`SparkDlcItems`, `SparkDlcBlocks`, `SparkDlcEntities`, …). The start-up steps and the entries of the two
  mixin configs are in an order that is behaviour; see `CONTEXT.md` before moving any of them.
- Root architecture notes are local-only and must not be committed.

### Cross-feature check
- Inspect the owning feature and every direct consumer of the contract being changed. Do not mechanically read the
  whole tree when the change is local and no seam is involved.
- Code calls code. A feature calls another feature directly, through its contract package when it has one
  (`faction.api`, `trait.api`, `skill.api`, `cooldown.api`, `replay.api`). The shared core (`faction.*` except
  `faction.witch`, `round.*`, `cooldown.*` except `cooldown.provider`, `command.admin`) names no role feature: a role
  hooks it by registering into the core's listener or rule lists (`PlayerAffectExemption`, `FactionCoWinnerRule`,
  `AdminCooldownClear`, `SettlementExclusions`; the killer team purse has `KillerTeamEconomyService.WalletRule`).
  Outside the trait feature, traits are reached only through `trait.api`.
- Nothing inside the mod is optional. Do not add a fallback for "another part of the mod is absent or older", an
  `isModLoaded` check on `sparkdlc` or one of the five former ids, a `LinkageError` catch around the mod's own code,
  a by-name lookup of the mod's own classes, items, entities or components, or a mixin into one of the mod's own
  classes (the only ones are three client mixins that add the client half of a common class). Wathe and
  NoellesRoles are required dependencies; Iris, EMF, ETF, Sodium, Ponder and Simple Voice Chat are real optional
  mods, and reflection on them and on Wathe internals stays.
- The client voice receivers are reached through the Fabric entrypoint `sparkdlc_client_voice`
  (`voice.ClientVoiceReceivers`), because Simple Voice Chat gathers plugin events before client initializers run.
- `./gradlew verifyLayering verifyNoLookupByName` (`gradle/verify/layering.gradle`) enforces the layering above on
  the built jar; every exception is listed there with its reason, and a new one is added deliberately.
- Prove that unrelated roles, factions, and traits remain outside the changed predicate and behavior path.
- Keep gameplay, wire formats and saved data unchanged unless the owner asks for the change.
- Use Java 21 for tests, verify tasks, and builds. Never add production-only test hooks.

### Verification
- `./gradlew build` compiles and packages; it runs neither tests nor verify tasks.
- `./gradlew verifyAll` runs every verify task (`gradle/verify/*.gradle`, one script per kind of check: the jar and
  its metadata, the pinned Wathe / NoellesRoles jars and selectors into them, layering, source contracts, the
  payload policy, a few feature seams). A `verify*` task that `verifyAll` does not reach fails the build.
- `./gradlew runGameTest` boots a headless server with the whole mod, audits every common mixin and runs the
  compatibility game tests; `./gradlew runClientAudit` does the same for client mixins and the client-side
  carry-over of old settings (opens a client window for about half a minute). Both accept
  `-PstartupOrderDump=<dir>` to write what the start-up registered, in order, for diffing two builds.
- `./gradlew test` runs the local-only unit and contract tests in `src/test` when they are present. The plain-Java
  fixture harness and its Python suite are in `src/test/harness/{java,python}`
  (`cd src/test/harness/python && python3 -m unittest discover -p 'test_*.py'`, after a build, `JAVA_HOME` set).

### Subagents
- You're the coordinator/leader between the subagents.
- Please use subagents. You can code with them together at the same time.
- Create multiple subagents for multiple purposes. Create as many as you needed.
	- Do NOT overload one subagent. Create multiple subagents to divide the tasks.
- Use English as the main language between you and your subagents.

### Annotations
- Add concise English and Chinese comments for non-obvious public API, mixin,
  Wathe seam, cross-feature, or migration semantics. Do not duplicate self-evident
  code in comments.
- Keep existing comments when moving or rewriting code; update them when the behaviour they describe changes.
- Comments describe the one mod as it is. Mention a former mod only in the past tense, where it explains why
  something still looks the way it does.
