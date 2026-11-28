package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorRules;
import dev.caecorthus.sparkdlc.role.civilian.usec.UsecRules;
import dev.caecorthus.sparkdlc.role.jiahao.sphinxhao.SphinxHaoRules;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterRules;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.wither.MiniWitherSoundSlot;
import dev.doctor4t.ratatouille.util.registrar.SoundEventRegistrar;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * The sound events of the mod (the Seeker and Blind kits keep their own classes). Each part registers through its own
 * method, at the moment it always did; the role kit and trait parts each keep their own registrar.
 * 本模组的声音事件（搜寻者与盲人套件保留各自的类）。每个部分通过自己的方法、在一贯的时刻注册；职业套件与词条部分各自保留自己的
 * registrar。
 */
public final class SparkDlcSounds {
    private SparkDlcSounds() {
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Role kit sounds / 职业套件声音
    // ---------------------------------------------------------------------------------------------------------------
    /*
     * Registers the role kit sound events through Wathe's ambience registrar path.
     * 通过 Wathe 环境音使用的 registrar 链路注册职业套件的声音事件。
     */

    private static final SoundEventRegistrar ROLE_KIT_REGISTRAR = new SoundEventRegistrar(SparkDLC.MOD_ID);

    public static final Identifier MUSIC_TAKEDISKRUSH_ID = SparkDLC.id("music.takediskrush");
    public static final SoundEvent MUSIC_TAKEDISKRUSH = ROLE_KIT_REGISTRAR.create("music.takediskrush");
    public static final SoundEvent M67_EQUIP = ROLE_KIT_REGISTRAR.create("item.m67.equip");
    public static final SoundEvent M67_PULL = ROLE_KIT_REGISTRAR.create("item.m67.pull");
    public static final SoundEvent M67_THROW = ROLE_KIT_REGISTRAR.create("item.m67.throw");
    public static final SoundEvent M67_LAND = ROLE_KIT_REGISTRAR.create("item.m67.land");
    public static final SoundEvent M67_EXPLODE = ROLE_KIT_REGISTRAR.create("item.m67.explode");
    /** Looping rotor noise; each drone kind sounds different. / 循环旋翼噪音，两种无人机音色不同。 */
    public static final SoundEvent GRENADE_DRONE_LOOP = ROLE_KIT_REGISTRAR.create("entity.grenade_drone.loop");
    public static final SoundEvent BOMB_DRONE_LOOP = ROLE_KIT_REGISTRAR.create("entity.bomb_drone.loop");
    public static final SoundEvent DRONE_PLACE = ROLE_KIT_REGISTRAR.create("entity.drone.place");
    public static final SoundEvent DRONE_BREAK = ROLE_KIT_REGISTRAR.create("entity.drone.break");
    public static final SoundEvent DRONE_RELEASE = ROLE_KIT_REGISTRAR.create("entity.drone.release");
    public static final SoundEvent DRONE_BIND = ROLE_KIT_REGISTRAR.create("item.drone.bind");
    /** Taotie head hitting a player. / 饕餮头颅砸中玩家。 */
    public static final SoundEvent TAOTIE_HEAD_BONK = ROLE_KIT_REGISTRAR.create("entity.taotie_head.bonk");
    public static final Identifier SHADOW_JESTER_ID = SparkDLC.id("ambient.shadow_jester");
    public static final SoundEvent SHADOW_JESTER = ROLE_KIT_REGISTRAR.create("ambient.shadow_jester");
    /**
     * Vulture Super Curse (~30 s, streamed) and its Childish helium take. The ids double as StopSound targets when the
     * Vulture dies or the round ends, so they must stay in step with the events below.
     * 秃鹫超级骂（约 30 秒，流式）及其幼稚氦气版。死亡或回合结束时这些 ID 也用作停止音效的目标，必须与下方事件一致。
     */
    public static final Identifier VULTURE_SUPER_CURSE_ID = SparkDLC.id("vulture.super_curse");
    public static final SoundEvent VULTURE_SUPER_CURSE = ROLE_KIT_REGISTRAR.create("vulture.super_curse");
    public static final Identifier VULTURE_SUPER_CURSE_HELIUM_ID = SparkDLC.id("vulture.super_curse_helium");
    public static final SoundEvent VULTURE_SUPER_CURSE_HELIUM = ROLE_KIT_REGISTRAR.create("vulture.super_curse_helium");
    /**
     * Vulture death scream (10 s). The helium event has two clips; the server plays it with one seed so every client
     * picks the same one.
     * 秃鹫死亡惨叫（10 秒）。氦气版含两段音频；服务端用同一个种子播放，所有客户端选中同一段。
     */
    public static final SoundEvent VULTURE_DEATH_SCREAM = ROLE_KIT_REGISTRAR.create("vulture.death_scream");
    public static final SoundEvent VULTURE_DEATH_SCREAM_HELIUM = ROLE_KIT_REGISTRAR.create("vulture.death_scream_helium");

    /** Sabotage system: junction boxes, circuit breakers and the tablet ready chime. / 破坏系统：接线箱、电闸与平板就绪提示音。 */
    public static final SoundEvent JUNCTION_BOX_PRY = ROLE_KIT_REGISTRAR.create("block.junction_box.pry");
    public static final SoundEvent JUNCTION_BOX_BREAK = ROLE_KIT_REGISTRAR.create("block.junction_box.break");
    public static final SoundEvent JUNCTION_BOX_SPARK = ROLE_KIT_REGISTRAR.create("block.junction_box.spark");
    public static final SoundEvent JUNCTION_BOX_HACK = ROLE_KIT_REGISTRAR.create("block.junction_box.hack");
    public static final SoundEvent JUNCTION_BOX_HACKED = ROLE_KIT_REGISTRAR.create("block.junction_box.hacked");
    public static final SoundEvent JUNCTION_BOX_REPAIR = ROLE_KIT_REGISTRAR.create("block.junction_box.repair");
    public static final SoundEvent CIRCUIT_BREAKER_TRIP = ROLE_KIT_REGISTRAR.create("block.circuit_breaker.trip");
    public static final SoundEvent CIRCUIT_BREAKER_SWITCH = ROLE_KIT_REGISTRAR.create("block.circuit_breaker.switch");
    public static final SoundEvent CIRCUIT_BREAKER_RESTORE = ROLE_KIT_REGISTRAR.create("block.circuit_breaker.restore");
    public static final SoundEvent SABOTAGE_READY = ROLE_KIT_REGISTRAR.create("ui.sabotage.ready");

    /**
     * Party Animal's Partygoer, clips picked by the owner (2026-10-21): spawn, grab, killed, gave up (vanish) and rage.
     * The ids double as StopSound targets so a new clip cuts the previous one.
     * 派对狂的派对客，音频由主人指定（2026-10-21）：出生、抓到人、被打死、放弃（消失）与狂怒。ID 也用作停止音效的目标，让新音频切断上一段。
     */
    public static final Identifier PARTYGOER_SPAWN_ID = SparkDLC.id("entity.partygoer.spawn");
    public static final SoundEvent PARTYGOER_SPAWN = ROLE_KIT_REGISTRAR.create("entity.partygoer.spawn");
    public static final Identifier PARTYGOER_GRAB_ID = SparkDLC.id("entity.partygoer.grab");
    public static final SoundEvent PARTYGOER_GRAB = ROLE_KIT_REGISTRAR.create("entity.partygoer.grab");
    public static final Identifier PARTYGOER_DEATH_ID = SparkDLC.id("entity.partygoer.death");
    public static final SoundEvent PARTYGOER_DEATH = ROLE_KIT_REGISTRAR.create("entity.partygoer.death");
    public static final Identifier PARTYGOER_VANISH_ID = SparkDLC.id("entity.partygoer.vanish");
    public static final SoundEvent PARTYGOER_VANISH = ROLE_KIT_REGISTRAR.create("entity.partygoer.vanish");
    public static final Identifier PARTYGOER_RAGE_ID = SparkDLC.id("entity.partygoer.rage");
    public static final SoundEvent PARTYGOER_RAGE = ROLE_KIT_REGISTRAR.create("entity.partygoer.rage");

    public static void registerRoleKitSounds() {
        ROLE_KIT_REGISTRAR.registerEntries();
        if (!Registries.SOUND_EVENT.containsId(MUSIC_TAKEDISKRUSH_ID)) {
            SparkDLC.LOGGER.warn("SparkDLC sound event {} was not registered.", MUSIC_TAKEDISKRUSH_ID);
        }
        if (!Registries.SOUND_EVENT.containsId(SHADOW_JESTER_ID)) {
            SparkDLC.LOGGER.warn("SparkDLC sound event {} was not registered.", SHADOW_JESTER_ID);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Trait sounds / 词条声音
    // ---------------------------------------------------------------------------------------------------------------
    /*
     * Registers the trait sound events through the same registrar path used by Wathe ambience.
     * 通过 Wathe 环境音同款 registrar 链路注册词条的自定义声音事件。
     */

    private static final SoundEventRegistrar TRAIT_REGISTRAR = new SoundEventRegistrar(SparkDLC.MOD_ID);

    public static final Identifier DEPRESSION_DOCILE_TO_RAGE_ID = SparkDLC.id("depression.docile_to_rage");
    public static final SoundEvent DEPRESSION_DOCILE_TO_RAGE = TRAIT_REGISTRAR.create("depression.docile_to_rage");
    public static final Identifier DEPRESSION_RAGE_LOOP_ID = SparkDLC.id("depression.rage_loop");
    public static final SoundEvent DEPRESSION_RAGE_LOOP = TRAIT_REGISTRAR.create("depression.rage_loop");
    public static final Identifier DEPRESSION_BLIND_RAGE_ENRAGE_ID = SparkDLC.id("depression.blind_rage_enrage");
    public static final SoundEvent DEPRESSION_BLIND_RAGE_ENRAGE = TRAIT_REGISTRAR.create("depression.blind_rage_enrage");
    public static final Identifier DEPRESSION_BLIND_RAGE_CHASE_ID = SparkDLC.id("depression.blind_rage_chase");
    public static final SoundEvent DEPRESSION_BLIND_RAGE_CHASE = TRAIT_REGISTRAR.create("depression.blind_rage_chase");
    public static final Identifier DEPRESSION_RAGE_TO_DOCILE_ID = SparkDLC.id("depression.rage_to_docile");
    public static final SoundEvent DEPRESSION_RAGE_TO_DOCILE = TRAIT_REGISTRAR.create("depression.rage_to_docile");
    public static final Identifier DEPRESSION_PLAYER_WAS_SEEN_ID = SparkDLC.id("depression.player_was_seen");
    public static final SoundEvent DEPRESSION_PLAYER_WAS_SEEN = TRAIT_REGISTRAR.create("depression.player_was_seen");
    public static final Identifier DEPRESSION_MELEE_KILL_1_ID = SparkDLC.id("depression.melee_kill_1");
    public static final SoundEvent DEPRESSION_MELEE_KILL_1 = TRAIT_REGISTRAR.create("depression.melee_kill_1");
    public static final Identifier DEPRESSION_MELEE_KILL_2_ID = SparkDLC.id("depression.melee_kill_2");
    public static final SoundEvent DEPRESSION_MELEE_KILL_2 = TRAIT_REGISTRAR.create("depression.melee_kill_2");
    public static final Identifier DEPRESSION_SHYGUY_KILLED_ID = SparkDLC.id("depression.shyguy_killed");
    public static final SoundEvent DEPRESSION_SHYGUY_KILLED = TRAIT_REGISTRAR.create("depression.shyguy_killed");
    /** Last Stand final moment BGM. / 背水一战终局时刻背景音乐。 */
    public static final Identifier FINAL_MOMENT_MUSIC_ID = SparkDLC.id("music.final_moment");
    public static final SoundEvent FINAL_MOMENT_MUSIC = TRAIT_REGISTRAR.create("music.final_moment");

    public static void registerTraitSounds() {
        TRAIT_REGISTRAR.registerEntries();
        // English: Keep this explicit so /playsound visibility failures leave a clear startup clue.
        // 中文：显式校验注册结果，让 /playsound 不可见时启动日志能给出清晰线索。
        for (Identifier id : new Identifier[]{
                DEPRESSION_DOCILE_TO_RAGE_ID,
                DEPRESSION_RAGE_LOOP_ID,
                DEPRESSION_BLIND_RAGE_ENRAGE_ID,
                DEPRESSION_BLIND_RAGE_CHASE_ID,
                DEPRESSION_RAGE_TO_DOCILE_ID,
                DEPRESSION_PLAYER_WAS_SEEN_ID,
                DEPRESSION_MELEE_KILL_1_ID,
                DEPRESSION_MELEE_KILL_2_ID,
                DEPRESSION_SHYGUY_KILLED_ID,
                FINAL_MOMENT_MUSIC_ID
        }) {
            if (!Registries.SOUND_EVENT.containsId(id)) {
                SparkDLC.LOGGER.warn("SparkDLC sound event {} was not registered.", id);
                return;
            }
        }
        SparkDLC.LOGGER.info("Registered SparkDLC trait sound events for /playsound.");
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Sounds of the mod's own roles / 本模组自有职业的声音
    // ---------------------------------------------------------------------------------------------------------------

    public static final Identifier PIG_CHASE_ID = SparkDLC.id("skill.pig_chase");
    public static final Identifier GRAND_WITCH_CEREMONIAL_SWORD_BGM_ID =
            SparkDLC.id("ambient.grand_witch_ceremonial_sword_bgm");
    /** Otherworld BGM: three bundled variants, one chosen per window and synced to everyone. / 里世界 BGM：三首，窗口开启时随机选一同步给所有人。 */
    public static final Identifier[] SPECTER_OTHERWORLD_BGM_IDS = {
            SparkDLC.id("ambient.specter_otherworld_bgm_0"),
            SparkDLC.id("ambient.specter_otherworld_bgm_1"),
            SparkDLC.id("ambient.specter_otherworld_bgm_2")
    };
    public static final Identifier SAINT_BELL_ID = SparkDLC.id("ambient.saint_bell");
    /** Reuses vanilla bell audio; no new asset. / 复用原版钟声音频，不新增资源。 */
    public static final Identifier BELL_RINGER_TOLL_ID = SparkDLC.id("ambient.bell_ringer_toll");
    /**
     * Victim-private Time Stealer Pocket Watch stage chime and final stop; both use bundled pocket-watch audio (never
     * bell audio) and keep ids distinct from the Bell Ringer so players and client volume groups can tell them apart.
     * 仅受害者可闻的窃时者怀表阶段鸣响与停摆；均使用自带的怀表音频（不用钟声），id 与敲钟人区分，便于玩家辨认与客户端音量分组。
     */
    public static final Identifier TIME_STEALER_CHIME_ID = SparkDLC.id("ambient.time_stealer_chime");
    public static final Identifier TIME_STEALER_FINAL_ID = SparkDLC.id("ambient.time_stealer_final");
    /**
     * Holy Flash tinnitus: a seamless 3 s loop played only on the flashed player's own client (relative, no
     * attenuation, non-streamed so OpenAL loops the buffer without a gap). The client excludes this id from the flash
     * muffle so the ring stays on top.
     * 圣光弹耳鸣：只在被闪玩家自己的客户端播放的 3 秒无缝循环（相对玩家、无衰减、非流式，OpenAL 直接循环缓冲区无间隙）。
     * 客户端在闪光压音中排除此 id，保证耳鸣始终最响。
     */
    public static final Identifier HOLY_FLASH_TINNITUS_ID = SparkDLC.id("skill.holy_flash_tinnitus");
    /**
     * USEC rifle sounds (ids live in UsecRules). Variable range: the shot carries by its volume (12 loud, 2.5
     * suppressed), and the server plays it publicly so the Blind can perceive it.
     * USEC 步枪音效（id 定义在 UsecRules）。可变范围：枪声按音量传播（未消音 12，消音 2.5），由服务端公开播放，盲人才能感知。
     */
    public static final Identifier USEC_RIFLE_SHOOT_ID = UsecRules.SHOOT_SOUND_ID;
    public static final Identifier USEC_RIFLE_SHOOT_SUPPRESSED_ID = UsecRules.SHOOT_SUPPRESSED_SOUND_ID;
    public static final Identifier USEC_RIFLE_BOLT_ID = UsecRules.BOLT_SOUND_ID;
    public static final Identifier USEC_RIFLE_MAGAZINE_ID = UsecRules.MAGAZINE_SOUND_ID;
    public static final Identifier USEC_RIFLE_LOAD_ROUND_ID = UsecRules.LOAD_ROUND_SOUND_ID;
    public static final Identifier USEC_RIFLE_SCOPE_ID = UsecRules.SCOPE_SOUND_ID;
    /**
     * Hunter double-barrel shotgun sounds (ids live in HunterRules): the shot carries 16 blocks and the reload 8.
     * 猎人双管猎枪音效（id 定义在 HunterRules）：枪声传播 16 格，装填 8 格。
     */
    public static final Identifier DOUBLE_BARREL_SHOT_ID = HunterRules.SHOT_SOUND_ID;
    public static final Identifier DOUBLE_BARREL_LOAD_ID = HunterRules.LOAD_SOUND_ID;
    /**
     * Sniper M1903A4 sounds: shot (16 blocks), bolt, bolt end, open bolt and round seat (8 each). No subtitles.
     * 狙击手 M1903A4 音效：枪声传播 16 格，拉栓、关栓、开栓与压弹各 8 格。无字幕。
     */
    public static final Identifier M1903A4_SHOOT_ID = SparkDLC.id("item.m1903a4.shoot");
    public static final Identifier M1903A4_BOLT_ID = SparkDLC.id("item.m1903a4.bolt");
    public static final Identifier M1903A4_BOLT_1_ID = SparkDLC.id("item.m1903a4.bolt_1");
    public static final Identifier M1903A4_OPENBOLT_ID = SparkDLC.id("item.m1903a4.openbolt");
    public static final Identifier M1903A4_LOAD_ID = SparkDLC.id("item.m1903a4.load");
    /** Kill confirmation played at the shooter when the rifle kills. / 击杀时在射手位置播放的确认音。 */
    public static final Identifier M1903A4_KILL_ID = SparkDLC.id("item.m1903a4.kill");
    /**
     * M16A4 sounds: shot (16 blocks) and the two reloads, tactical (rounds left) and empty. No subtitles.
     * M16A4 音效：枪声（16 格）与两种换弹——战术换弹（还有子弹）和空仓换弹。无字幕。
     */
    public static final Identifier M16A4_SHOOT_ID = SparkDLC.id("item.m16a4.shoot");
    public static final Identifier M16A4_RELOAD_TACTICAL_ID = SparkDLC.id("item.m16a4.reload_tactical");
    public static final Identifier M16A4_RELOAD_EMPTY_ID = SparkDLC.id("item.m16a4.reload_empty");
    /**
     * Savior pistol sounds (ids live in SaviorRules): the shot carries at Wathe's revolver volume 5.0 and the click at
     * 0.5. The bundled audio lives under {@code sparkdlc:sounds/item/} so it can be replaced.
     * 救世主手枪音效（id 定义在 SaviorRules）：枪声沿用 Wathe 左轮的 5.0 音量，点击声 0.5。音频位于
     * {@code sparkdlc:sounds/item/}，可直接替换。
     */
    public static final Identifier SAVIOR_PISTOL_SHOOT_ID = SaviorRules.PISTOL_SHOOT_SOUND_ID;
    public static final Identifier SAVIOR_PISTOL_CLICK_ID = SaviorRules.PISTOL_CLICK_SOUND_ID;
    /** Savior lightsaber impact (single CC0 variant); no subtitle. / 救世主光剑命中（单个 CC0 变体）；无字幕。 */
    public static final Identifier SAVIOR_LIGHTSABER_HIT_ID = SaviorRules.LIGHTSABER_HIT_SOUND_ID;
    /** Savior lightsaber ignition, requested by the client at the 0.95 s mark; no subtitle. / 救世主光剑点火音，客户端在 0.95 秒处请求；无字幕。 */
    public static final Identifier SAVIOR_LIGHTSABER_IGNITE_ID = SaviorRules.LIGHTSABER_IGNITE_SOUND_ID;
    /** Looping idle hum while the blade is lit; non-streamed so OpenAL loops it seamlessly. / 刀刃点亮时的循环嗡鸣；非流式以便 OpenAL 无缝循环。 */
    public static final Identifier SAVIOR_LIGHTSABER_HUM_ID = SaviorRules.LIGHTSABER_HUM_SOUND_ID;
    /** Lightsaber swing, requested on left-click only while the blade is lit. / 光剑挥动音，仅在刀刃点亮时左键请求。 */
    public static final Identifier SAVIOR_LIGHTSABER_SWING_ID = SaviorRules.LIGHTSABER_SWING_SOUND_ID;
    public static SoundEvent PIG_CHASE;
    public static SoundEvent GRAND_WITCH_CEREMONIAL_SWORD_BGM;
    public static final SoundEvent[] SPECTER_OTHERWORLD_BGM = new SoundEvent[3];
    public static SoundEvent SAINT_BELL;
    public static SoundEvent BELL_RINGER_TOLL;
    public static SoundEvent TIME_STEALER_CHIME;
    public static SoundEvent TIME_STEALER_FINAL;
    public static SoundEvent HOLY_FLASH_TINNITUS;
    public static SoundEvent USEC_RIFLE_SHOOT;
    public static SoundEvent USEC_RIFLE_SHOOT_SUPPRESSED;
    public static SoundEvent USEC_RIFLE_BOLT;
    public static SoundEvent USEC_RIFLE_MAGAZINE;
    public static SoundEvent USEC_RIFLE_LOAD_ROUND;
    public static SoundEvent USEC_RIFLE_SCOPE;
    public static SoundEvent DOUBLE_BARREL_SHOT;
    public static SoundEvent DOUBLE_BARREL_LOAD;
    public static SoundEvent M1903A4_SHOOT;
    public static SoundEvent M1903A4_BOLT;
    public static SoundEvent M1903A4_BOLT_1;
    public static SoundEvent M1903A4_OPENBOLT;
    public static SoundEvent M1903A4_LOAD;
    public static SoundEvent M1903A4_KILL;
    public static SoundEvent M16A4_SHOOT;
    public static SoundEvent M16A4_RELOAD_TACTICAL;
    public static SoundEvent M16A4_RELOAD_EMPTY;
    public static SoundEvent SAVIOR_PISTOL_SHOOT;
    public static SoundEvent SAVIOR_PISTOL_CLICK;
    public static SoundEvent SAVIOR_LIGHTSABER_HIT;
    public static SoundEvent SAVIOR_LIGHTSABER_IGNITE;
    public static SoundEvent SAVIOR_LIGHTSABER_HUM;
    public static SoundEvent SAVIOR_LIGHTSABER_SWING;
    /** Knight duel audio; no subtitles. / 骑士决斗音频；无字幕。 */
    public static final Identifier DUEL_MANBA_ID = dev.caecorthus.sparkdlc.role.civilian.knight.duel.KnightDuelRules.MANBA_SOUND_ID;
    public static final Identifier DUEL_SF_ID = dev.caecorthus.sparkdlc.role.civilian.knight.duel.KnightDuelRules.SF_SOUND_ID;
    public static SoundEvent DUEL_MANBA;
    public static SoundEvent DUEL_SF;
    /**
     * Frozen contract (BEAR WP0): the eleven BEAR sound events, all variable range (the playing code picks the volume).
     * The ricochet, visor hit and tinnitus files are the owner's picks of 2026-11-05 (helmet A, visor B, tinnitus B);
     * the other eight are silent placeholders until the sound package delivers. The twelfth, the M2A1 sight click, left
     * with the range adjustment (owner, 2026-11-11).
     * 冻结契约（BEAR WP0）：十一个 BEAR 音效事件，均为可变范围（由播放代码决定音量）。跳弹、面罩命中与耳鸣音频为所有者
     * 2026-11-05 的选择（头盔 A、面罩 B、耳鸣 B）；其余八个在音效工作包交付前为静音占位。第十二个（M2A1 瞄具咔嗒声）已随
     * 射程调节一并移除（所有者 2026-11-11）。
     */
    public static final Identifier BEAR_M32_FIRE_ID = SparkDLC.id("bear.m32.fire");
    public static final Identifier BEAR_M32_LOAD_ID = SparkDLC.id("bear.m32.load");
    public static final Identifier BEAR_M32_UNLOAD_ID = SparkDLC.id("bear.m32.unload");
    public static final Identifier BEAR_M32_EMPTY_ID = SparkDLC.id("bear.m32.empty");
    public static final Identifier BEAR_SMOKE_BURST_ID = SparkDLC.id("bear.smoke.burst");
    public static final Identifier BEAR_RYS_T_VISOR_DOWN_ID = SparkDLC.id("bear.rys_t.visor_down");
    public static final Identifier BEAR_RYS_T_VISOR_UP_ID = SparkDLC.id("bear.rys_t.visor_up");
    public static final Identifier BEAR_RYS_T_RICOCHET_ID = SparkDLC.id("bear.rys_t.ricochet");
    public static final Identifier BEAR_RYS_T_VISOR_HIT_ID = SparkDLC.id("bear.rys_t.visor_hit");
    public static final Identifier BEAR_RYS_T_TINNITUS_ID = SparkDLC.id("bear.rys_t.tinnitus");
    public static final Identifier BEAR_EARPLUGS_INSERT_ID = SparkDLC.id("bear.earplugs.insert");
    public static SoundEvent BEAR_M32_FIRE;
    public static SoundEvent BEAR_M32_LOAD;
    public static SoundEvent BEAR_M32_UNLOAD;
    public static SoundEvent BEAR_M32_EMPTY;
    public static SoundEvent BEAR_SMOKE_BURST;
    public static SoundEvent BEAR_RYS_T_VISOR_DOWN;
    public static SoundEvent BEAR_RYS_T_VISOR_UP;
    public static SoundEvent BEAR_RYS_T_RICOCHET;
    public static SoundEvent BEAR_RYS_T_VISOR_HIT;
    public static SoundEvent BEAR_RYS_T_TINNITUS;
    public static SoundEvent BEAR_EARPLUGS_INSERT;
    /**
     * 狮身豪's 《Fade》 (id in SphinxHaoRules): a 10 s streamed clip every client plays during a 豪气冲天 window, driven by
     * the synced world flag (non-attenuated, AMBIENT category: Wathe locks MUSIC).
     * 狮身豪的《Fade》（id 定义在 SphinxHaoRules）：豪气冲天窗口期间每个客户端播放的 10 秒流式片段，由同步的世界标记驱动
     * （无衰减，AMBIENT 类别：Wathe 锁定了 MUSIC）。
     */
    public static final Identifier SPHINX_HAO_FADE_ID = SphinxHaoRules.FADE_SOUND_ID;
    public static SoundEvent SPHINX_HAO_FADE;
    /**
     * The Mini Wither's own events, one per {@link MiniWitherSoundSlot}: variable range like the vanilla events they
     * replaced, and sounds.json defaults each to the same vanilla files and subtitle. Read with {@link #miniWither}.
     * 小凋灵自有事件，每个 {@link MiniWitherSoundSlot} 一个：与被替换的原版事件一样为可变范围，sounds.json 默认指向相同的
     * 原版音频与字幕。通过 {@link #miniWither} 读取。
     */
    private static final Map<MiniWitherSoundSlot, SoundEvent> MINI_WITHER = new EnumMap<>(MiniWitherSoundSlot.class);
    private static boolean roleSoundsRegistered;

    public static synchronized void registerRoleSounds() {
        if (roleSoundsRegistered) {
            return;
        }
        roleSoundsRegistered = true;
        PIG_CHASE = Registry.register(Registries.SOUND_EVENT, PIG_CHASE_ID, SoundEvent.of(PIG_CHASE_ID));
        GRAND_WITCH_CEREMONIAL_SWORD_BGM = Registry.register(
                Registries.SOUND_EVENT,
                GRAND_WITCH_CEREMONIAL_SWORD_BGM_ID,
                SoundEvent.of(GRAND_WITCH_CEREMONIAL_SWORD_BGM_ID)
        );
        for (int index = 0; index < SPECTER_OTHERWORLD_BGM_IDS.length; index++) {
            Identifier id = SPECTER_OTHERWORLD_BGM_IDS[index];
            SPECTER_OTHERWORLD_BGM[index] = Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
        }
        SAINT_BELL = Registry.register(Registries.SOUND_EVENT, SAINT_BELL_ID, SoundEvent.of(SAINT_BELL_ID));
        BELL_RINGER_TOLL = Registry.register(
                Registries.SOUND_EVENT,
                BELL_RINGER_TOLL_ID,
                SoundEvent.of(BELL_RINGER_TOLL_ID)
        );
        TIME_STEALER_CHIME = Registry.register(
                Registries.SOUND_EVENT,
                TIME_STEALER_CHIME_ID,
                SoundEvent.of(TIME_STEALER_CHIME_ID)
        );
        TIME_STEALER_FINAL = Registry.register(
                Registries.SOUND_EVENT,
                TIME_STEALER_FINAL_ID,
                SoundEvent.of(TIME_STEALER_FINAL_ID)
        );
        HOLY_FLASH_TINNITUS = Registry.register(
                Registries.SOUND_EVENT,
                HOLY_FLASH_TINNITUS_ID,
                SoundEvent.of(HOLY_FLASH_TINNITUS_ID)
        );
        USEC_RIFLE_SHOOT = Registry.register(Registries.SOUND_EVENT, USEC_RIFLE_SHOOT_ID,
                SoundEvent.of(USEC_RIFLE_SHOOT_ID));
        USEC_RIFLE_SHOOT_SUPPRESSED = Registry.register(Registries.SOUND_EVENT, USEC_RIFLE_SHOOT_SUPPRESSED_ID,
                SoundEvent.of(USEC_RIFLE_SHOOT_SUPPRESSED_ID));
        USEC_RIFLE_BOLT = Registry.register(Registries.SOUND_EVENT, USEC_RIFLE_BOLT_ID,
                SoundEvent.of(USEC_RIFLE_BOLT_ID));
        USEC_RIFLE_MAGAZINE = Registry.register(Registries.SOUND_EVENT, USEC_RIFLE_MAGAZINE_ID,
                SoundEvent.of(USEC_RIFLE_MAGAZINE_ID));
        USEC_RIFLE_LOAD_ROUND = Registry.register(Registries.SOUND_EVENT, USEC_RIFLE_LOAD_ROUND_ID,
                SoundEvent.of(USEC_RIFLE_LOAD_ROUND_ID));
        // Fixed range: only players within SCOPE_SOUND_RANGE receive it. / 固定范围：仅 SCOPE_SOUND_RANGE 格内的玩家收到。
        USEC_RIFLE_SCOPE = Registry.register(Registries.SOUND_EVENT, USEC_RIFLE_SCOPE_ID,
                SoundEvent.of(USEC_RIFLE_SCOPE_ID, UsecRules.SCOPE_SOUND_RANGE));
        DOUBLE_BARREL_SHOT = Registry.register(Registries.SOUND_EVENT, DOUBLE_BARREL_SHOT_ID,
                SoundEvent.of(DOUBLE_BARREL_SHOT_ID));
        DOUBLE_BARREL_LOAD = Registry.register(Registries.SOUND_EVENT, DOUBLE_BARREL_LOAD_ID,
                SoundEvent.of(DOUBLE_BARREL_LOAD_ID));
        M1903A4_SHOOT = Registry.register(Registries.SOUND_EVENT, M1903A4_SHOOT_ID,
                SoundEvent.of(M1903A4_SHOOT_ID));
        M1903A4_BOLT = Registry.register(Registries.SOUND_EVENT, M1903A4_BOLT_ID,
                SoundEvent.of(M1903A4_BOLT_ID));
        M1903A4_BOLT_1 = Registry.register(Registries.SOUND_EVENT, M1903A4_BOLT_1_ID,
                SoundEvent.of(M1903A4_BOLT_1_ID));
        M1903A4_OPENBOLT = Registry.register(Registries.SOUND_EVENT, M1903A4_OPENBOLT_ID,
                SoundEvent.of(M1903A4_OPENBOLT_ID));
        M1903A4_LOAD = Registry.register(Registries.SOUND_EVENT, M1903A4_LOAD_ID,
                SoundEvent.of(M1903A4_LOAD_ID));
        M1903A4_KILL = Registry.register(Registries.SOUND_EVENT, M1903A4_KILL_ID,
                SoundEvent.of(M1903A4_KILL_ID));
        M16A4_SHOOT = Registry.register(Registries.SOUND_EVENT, M16A4_SHOOT_ID,
                SoundEvent.of(M16A4_SHOOT_ID));
        M16A4_RELOAD_TACTICAL = Registry.register(Registries.SOUND_EVENT, M16A4_RELOAD_TACTICAL_ID,
                SoundEvent.of(M16A4_RELOAD_TACTICAL_ID));
        M16A4_RELOAD_EMPTY = Registry.register(Registries.SOUND_EVENT, M16A4_RELOAD_EMPTY_ID,
                SoundEvent.of(M16A4_RELOAD_EMPTY_ID));
        SAVIOR_PISTOL_SHOOT = Registry.register(Registries.SOUND_EVENT, SAVIOR_PISTOL_SHOOT_ID,
                SoundEvent.of(SAVIOR_PISTOL_SHOOT_ID));
        SAVIOR_PISTOL_CLICK = Registry.register(Registries.SOUND_EVENT, SAVIOR_PISTOL_CLICK_ID,
                SoundEvent.of(SAVIOR_PISTOL_CLICK_ID));
        SAVIOR_LIGHTSABER_HIT = Registry.register(Registries.SOUND_EVENT, SAVIOR_LIGHTSABER_HIT_ID,
                SoundEvent.of(SAVIOR_LIGHTSABER_HIT_ID));
        SAVIOR_LIGHTSABER_IGNITE = Registry.register(Registries.SOUND_EVENT, SAVIOR_LIGHTSABER_IGNITE_ID,
                SoundEvent.of(SAVIOR_LIGHTSABER_IGNITE_ID));
        SAVIOR_LIGHTSABER_HUM = Registry.register(Registries.SOUND_EVENT, SAVIOR_LIGHTSABER_HUM_ID,
                SoundEvent.of(SAVIOR_LIGHTSABER_HUM_ID));
        SAVIOR_LIGHTSABER_SWING = Registry.register(Registries.SOUND_EVENT, SAVIOR_LIGHTSABER_SWING_ID,
                SoundEvent.of(SAVIOR_LIGHTSABER_SWING_ID));
        DUEL_MANBA = Registry.register(Registries.SOUND_EVENT, DUEL_MANBA_ID, SoundEvent.of(DUEL_MANBA_ID));
        DUEL_SF = Registry.register(Registries.SOUND_EVENT, DUEL_SF_ID, SoundEvent.of(DUEL_SF_ID));
        BEAR_M32_FIRE = Registry.register(Registries.SOUND_EVENT, BEAR_M32_FIRE_ID, SoundEvent.of(BEAR_M32_FIRE_ID));
        BEAR_M32_LOAD = Registry.register(Registries.SOUND_EVENT, BEAR_M32_LOAD_ID, SoundEvent.of(BEAR_M32_LOAD_ID));
        BEAR_M32_UNLOAD = Registry.register(Registries.SOUND_EVENT, BEAR_M32_UNLOAD_ID, SoundEvent.of(BEAR_M32_UNLOAD_ID));
        BEAR_M32_EMPTY = Registry.register(Registries.SOUND_EVENT, BEAR_M32_EMPTY_ID, SoundEvent.of(BEAR_M32_EMPTY_ID));
        BEAR_SMOKE_BURST = Registry.register(Registries.SOUND_EVENT, BEAR_SMOKE_BURST_ID, SoundEvent.of(BEAR_SMOKE_BURST_ID));
        BEAR_RYS_T_VISOR_DOWN = Registry.register(Registries.SOUND_EVENT, BEAR_RYS_T_VISOR_DOWN_ID, SoundEvent.of(BEAR_RYS_T_VISOR_DOWN_ID));
        BEAR_RYS_T_VISOR_UP = Registry.register(Registries.SOUND_EVENT, BEAR_RYS_T_VISOR_UP_ID, SoundEvent.of(BEAR_RYS_T_VISOR_UP_ID));
        BEAR_RYS_T_RICOCHET = Registry.register(Registries.SOUND_EVENT, BEAR_RYS_T_RICOCHET_ID, SoundEvent.of(BEAR_RYS_T_RICOCHET_ID));
        BEAR_RYS_T_VISOR_HIT = Registry.register(Registries.SOUND_EVENT, BEAR_RYS_T_VISOR_HIT_ID, SoundEvent.of(BEAR_RYS_T_VISOR_HIT_ID));
        BEAR_RYS_T_TINNITUS = Registry.register(Registries.SOUND_EVENT, BEAR_RYS_T_TINNITUS_ID, SoundEvent.of(BEAR_RYS_T_TINNITUS_ID));
        BEAR_EARPLUGS_INSERT = Registry.register(Registries.SOUND_EVENT, BEAR_EARPLUGS_INSERT_ID, SoundEvent.of(BEAR_EARPLUGS_INSERT_ID));
        for (MiniWitherSoundSlot slot : MiniWitherSoundSlot.values()) {
            MINI_WITHER.put(slot, Registry.register(Registries.SOUND_EVENT, slot.eventId(),
                    SoundEvent.of(slot.eventId())));
        }
        SPHINX_HAO_FADE = Registry.register(Registries.SOUND_EVENT, SPHINX_HAO_FADE_ID,
                SoundEvent.of(SPHINX_HAO_FADE_ID));
    }

    /** The registered event of one Mini Wither slot. / 某个小凋灵槽位已注册的事件。 */
    public static SoundEvent miniWither(MiniWitherSoundSlot slot) {
        return Objects.requireNonNull(MINI_WITHER.get(slot), "SparkDlcSounds.registerRoleSounds() has not run");
    }
}
