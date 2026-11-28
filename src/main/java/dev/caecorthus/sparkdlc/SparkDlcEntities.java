package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.entity.AromaOrbEntity;
import dev.caecorthus.sparkdlc.entity.CapsuleEntity;
import dev.caecorthus.sparkdlc.entity.CaptureDeviceEntity;
import dev.caecorthus.sparkdlc.entity.CoolingOilEntity;
import dev.caecorthus.sparkdlc.entity.DroneEntity;
import dev.caecorthus.sparkdlc.entity.M67GrenadeEntity;
import dev.caecorthus.sparkdlc.entity.NinjaGrapplingHookEntity;
import dev.caecorthus.sparkdlc.entity.NinjaShurikenEntity;
import dev.caecorthus.sparkdlc.entity.PartygoerEntity;
import dev.caecorthus.sparkdlc.entity.RealityAnchorEntity;
import dev.caecorthus.sparkdlc.entity.SphinxHaoFistEntity;
import dev.caecorthus.sparkdlc.entity.TaotieHeadEntity;
import dev.caecorthus.sparkdlc.role.jiahao.sphinxhao.SphinxHaoRules;
import dev.caecorthus.sparkdlc.role.killer.magician.MagicianPlaybackEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * The entity types of the mod that have no role registry of their own (a role with several entities keeps its
 * {@code <Role>Entities} class). Each part registers through its own method, at the moment it always did.
 * 本模组中没有独立职业注册类的实体类型（拥有多个实体的职业保留各自的 {@code <Role>Entities} 类）。每个部分通过自己的方法、
 * 在一贯的时刻注册。
 */
public final class SparkDlcEntities {
    private SparkDlcEntities() {
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Role kit entities / 职业套件实体
    // ---------------------------------------------------------------------------------------------------------------

    public static final Identifier CAPSULE_ID = SparkDLC.id("capsule");
    public static final Identifier CAPTURE_DEVICE_ID = SparkDLC.id("capture_device");
    public static final Identifier M67_ID = SparkDLC.id("m67");
    public static final Identifier COOLING_OIL_ID = SparkDLC.id("cooling_oil");
    public static final Identifier AROMA_ORB_ID = SparkDLC.id("aroma_orb");
    public static final Identifier GRENADE_DRONE_ID = SparkDLC.id("grenade_drone");
    public static final Identifier BOMB_DRONE_ID = SparkDLC.id("bomb_drone");
    public static final Identifier TAOTIE_HEAD_ID = SparkDLC.id("taotie_head");
    public static final Identifier PARTYGOER_ID = SparkDLC.id("partygoer");
    public static final Identifier REALITY_ANCHOR_ID = SparkDLC.id("reality_anchor");
    private static EntityType<dev.caecorthus.sparkdlc.entity.BananaPeelEntity> bananaPeel;
    private static EntityType<M67GrenadeEntity> m67;
    private static EntityType<CapsuleEntity> capsule;
    private static EntityType<CaptureDeviceEntity> captureDevice;
    private static EntityType<CoolingOilEntity> coolingOil;
    private static EntityType<AromaOrbEntity> aromaOrb;
    private static EntityType<DroneEntity> grenadeDrone;
    private static EntityType<DroneEntity> bombDrone;
    private static EntityType<TaotieHeadEntity> taotieHead;
    private static EntityType<PartygoerEntity> partygoer;
    private static EntityType<RealityAnchorEntity> realityAnchor;
    private static boolean roleKitEntitiesRegistered;

    public static synchronized void registerRoleKitEntities() {
        if (roleKitEntitiesRegistered) {
            return;
        }
        capsule = Registry.register(
                Registries.ENTITY_TYPE,
                CAPSULE_ID,
                EntityType.Builder.<CapsuleEntity>create(CapsuleEntity::new, SpawnGroup.MISC)
                        .dimensions(0.25F, 0.25F)
                        .maxTrackingRange(4)
                        .trackingTickInterval(10)
                        .build(CAPSULE_ID.toString())
        );
        bananaPeel = Registry.register(
                Registries.ENTITY_TYPE,
                sparkdlc$bananaPeelId(),
                EntityType.Builder.<dev.caecorthus.sparkdlc.entity.BananaPeelEntity>create(
                                dev.caecorthus.sparkdlc.entity.BananaPeelEntity::new, SpawnGroup.MISC)
                        .dimensions(0.6F, 0.06F)
                        .maxTrackingRange(8)
                        .trackingTickInterval(20)
                        .build(sparkdlc$bananaPeelId().toString())
        );
        captureDevice = Registry.register(
                Registries.ENTITY_TYPE,
                CAPTURE_DEVICE_ID,
                EntityType.Builder.<CaptureDeviceEntity>create(CaptureDeviceEntity::new, SpawnGroup.MISC)
                        .dimensions(0.35F, 0.08F)
                        .maxTrackingRange(8)
                        .trackingTickInterval(10)
                        .build(CAPTURE_DEVICE_ID.toString())
        );
        m67 = Registry.register(
                Registries.ENTITY_TYPE,
                M67_ID,
                EntityType.Builder.<M67GrenadeEntity>create(M67GrenadeEntity::new, SpawnGroup.MISC)
                        .dimensions(0.25F, 0.25F)
                        .maxTrackingRange(8)
                        .trackingTickInterval(1)
                        .disableSaving()
                        .disableSummon()
                        .build(M67_ID.toString())
        );
        coolingOil = Registry.register(
                Registries.ENTITY_TYPE,
                COOLING_OIL_ID,
                EntityType.Builder.<CoolingOilEntity>create(CoolingOilEntity::new, SpawnGroup.MISC)
                        .dimensions(0.25F, 0.25F)
                        .maxTrackingRange(4)
                        .trackingTickInterval(10)
                        .disableSaving()
                        .disableSummon()
                        .build(COOLING_OIL_ID.toString())
        );
        aromaOrb = Registry.register(
                Registries.ENTITY_TYPE,
                AROMA_ORB_ID,
                EntityType.Builder.<AromaOrbEntity>create(AromaOrbEntity::new, SpawnGroup.MISC)
                        .dimensions(0.25F, 0.25F)
                        .maxTrackingRange(4)
                        .trackingTickInterval(10)
                        .disableSaving()
                        .disableSummon()
                        .build(AROMA_ORB_ID.toString())
        );
        // Transient round entities; tracked every tick for smooth remote flight. Range is widened further for the
        // pilot by the drone streaming hooks.
        // 临时回合实体；每刻追踪以保证远端飞行平滑。驾驶者的追踪范围由无人机串流钩子进一步放宽。
        grenadeDrone = Registry.register(
                Registries.ENTITY_TYPE,
                GRENADE_DRONE_ID,
                EntityType.Builder.<DroneEntity>create(DroneEntity::new, SpawnGroup.MISC)
                        .dimensions(0.7F, 0.3F)
                        .maxTrackingRange(16)
                        .trackingTickInterval(1)
                        .alwaysUpdateVelocity(true)
                        .disableSaving()
                        .disableSummon()
                        .build(GRENADE_DRONE_ID.toString())
        );
        bombDrone = Registry.register(
                Registries.ENTITY_TYPE,
                BOMB_DRONE_ID,
                EntityType.Builder.<DroneEntity>create(DroneEntity::new, SpawnGroup.MISC)
                        .dimensions(0.6F, 0.3F)
                        .maxTrackingRange(16)
                        .trackingTickInterval(1)
                        .alwaysUpdateVelocity(true)
                        .disableSaving()
                        .disableSummon()
                        .build(BOMB_DRONE_ID.toString())
        );
        // Transient round projectile; tracked every tick with velocity so remote clients can follow its homing.
        // 临时回合弹射物；每刻追踪并同步速度，使远端客户端能跟上其追踪转向。
        taotieHead = Registry.register(
                Registries.ENTITY_TYPE,
                TAOTIE_HEAD_ID,
                EntityType.Builder.<TaotieHeadEntity>create(TaotieHeadEntity::new, SpawnGroup.MISC)
                        .dimensions(0.5F, 0.5F)
                        .maxTrackingRange(8)
                        .trackingTickInterval(1)
                        .alwaysUpdateVelocity(true)
                        .disableSaving()
                        .disableSummon()
                        .build(TAOTIE_HEAD_ID.toString())
        );
        // The Party Animal's summoned zombie: a round-only mob moved by its own brain, tracked every tick so the carry
        // looks smooth. / 派对狂召唤的僵尸：仅存在于本局、由自身大脑驱动的生物；每刻追踪，扛人画面才流畅。
        partygoer = Registry.register(
                Registries.ENTITY_TYPE,
                PARTYGOER_ID,
                EntityType.Builder.<PartygoerEntity>create(PartygoerEntity::new, SpawnGroup.MISC)
                        .dimensions(0.6F, 1.95F)
                        .eyeHeight(1.74F)
                        .maxTrackingRange(10)
                        .trackingTickInterval(1)
                        .makeFireImmune()
                        .disableSaving()
                        .disableSummon()
                        .build(PARTYGOER_ID.toString())
        );
        FabricDefaultAttributeRegistry.register(partygoer, PartygoerEntity.createAttributes());
        // Reality stabilization anchor: a static, saved entity; its NBT carries its own light block and its match.
        // 现实稳定锚：静态、会存盘的实体；其 NBT 记录自己的光源方块与所属对局。
        realityAnchor = Registry.register(
                Registries.ENTITY_TYPE,
                REALITY_ANCHOR_ID,
                EntityType.Builder.<RealityAnchorEntity>create(RealityAnchorEntity::new, SpawnGroup.MISC)
                        .dimensions(0.5F, 0.5F)
                        .maxTrackingRange(16)
                        .trackingTickInterval(20)
                        .build(REALITY_ANCHOR_ID.toString())
        );
        roleKitEntitiesRegistered = true;
    }

    public static EntityType<M67GrenadeEntity> m67() {
        if (m67 == null) {
            throw new IllegalStateException("SparkDLC role kit entities are not registered yet");
        }
        return m67;
    }

    private static Identifier sparkdlc$bananaPeelId() {
        return dev.caecorthus.sparkdlc.SparkDLC.id("banana_peel");
    }

    public static EntityType<dev.caecorthus.sparkdlc.entity.BananaPeelEntity> bananaPeel() {
        if (bananaPeel == null) {
            throw new IllegalStateException("SparkDLC role kit entities are not registered yet");
        }
        return bananaPeel;
    }

    public static EntityType<CapsuleEntity> capsule() {
        if (capsule == null) {
            throw new IllegalStateException("SparkDLC role kit entities are not registered yet");
        }
        return capsule;
    }

    public static EntityType<RealityAnchorEntity> realityAnchor() {
        if (realityAnchor == null) {
            throw new IllegalStateException("SparkDLC role kit entities are not registered yet");
        }
        return realityAnchor;
    }

    public static EntityType<CaptureDeviceEntity> captureDevice() {
        if (captureDevice == null) {
            throw new IllegalStateException("SparkDLC role kit entities are not registered yet");
        }
        return captureDevice;
    }

    public static EntityType<CoolingOilEntity> coolingOil() {
        if (coolingOil == null) {
            throw new IllegalStateException("SparkDLC role kit entities are not registered yet");
        }
        return coolingOil;
    }

    public static EntityType<AromaOrbEntity> aromaOrb() {
        if (aromaOrb == null) {
            throw new IllegalStateException("SparkDLC role kit entities are not registered yet");
        }
        return aromaOrb;
    }

    public static EntityType<DroneEntity> grenadeDrone() {
        if (grenadeDrone == null) {
            throw new IllegalStateException("SparkDLC role kit entities are not registered yet");
        }
        return grenadeDrone;
    }

    public static EntityType<DroneEntity> bombDrone() {
        if (bombDrone == null) {
            throw new IllegalStateException("SparkDLC role kit entities are not registered yet");
        }
        return bombDrone;
    }

    public static EntityType<TaotieHeadEntity> taotieHead() {
        if (taotieHead == null) {
            throw new IllegalStateException("SparkDLC role kit entities are not registered yet");
        }
        return taotieHead;
    }

    public static EntityType<PartygoerEntity> partygoer() {
        if (partygoer == null) {
            throw new IllegalStateException("SparkDLC role kit entities are not registered yet");
        }
        return partygoer;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // Entities of the mod's own roles that have no role registry of their own / 本模组自有职业中没有独立注册类的实体
    // ---------------------------------------------------------------------------------------------------------------

    public static final Identifier NINJA_SHURIKEN_ID = SparkDLC.id("ninja_shuriken");
    public static final Identifier MAGICIAN_PLAYBACK_ID = SparkDLC.id("magician_playback");
    public static final Identifier NINJA_GRAPPLING_HOOK_ID = SparkDLC.id("ninja_grappling_hook");
    public static final Identifier SPHINX_HAO_FIST_ID = SphinxHaoRules.FIST_ENTITY_ID;

    private static EntityType<NinjaShurikenEntity> ninjaShuriken;
    private static EntityType<MagicianPlaybackEntity> magicianPlayback;
    private static EntityType<NinjaGrapplingHookEntity> ninjaGrapplingHook;
    private static EntityType<SphinxHaoFistEntity> sphinxHaoFist;
    private static boolean roleEntitiesRegistered;

    public static synchronized void registerRoleEntities() {
        if (roleEntitiesRegistered) {
            return;
        }
        ninjaShuriken = Registry.register(
                Registries.ENTITY_TYPE,
                NINJA_SHURIKEN_ID,
                EntityType.Builder.<NinjaShurikenEntity>create(NinjaShurikenEntity::new, SpawnGroup.MISC)
                        .dimensions(0.25F, 0.25F)
                        .maxTrackingRange(4)
                        .trackingTickInterval(1)
                        .build(NINJA_SHURIKEN_ID.toString())
        );
        // 播放体是可追踪的非生物实体；服务端只用它承载轨迹，伤害由魔术师管理器显式处理。
        magicianPlayback = Registry.register(
                Registries.ENTITY_TYPE,
                MAGICIAN_PLAYBACK_ID,
                EntityType.Builder.<MagicianPlaybackEntity>create(MagicianPlaybackEntity::new, SpawnGroup.MISC)
                        .dimensions(0.6F, 1.8F)
                        .maxTrackingRange(92)
                        .trackingTickInterval(1)
                        // 皮套是 LivingEntity 子类，不应被存档持久化；播放结束后由管理器负责清理。
                        .disableSaving()
                        // Only the playback manager spawns puppets; /summon would create an ownerless puppet.
                        // 只有回放管理器生成皮套；/summon 会产生无主皮套。
                        .disableSummon()
                        .build(MAGICIAN_PLAYBACK_ID.toString())
        );
        /*
         * 自定义 LivingEntity 必须有默认属性容器。自改版 NoellesRoles 在实体注册后
         * 显式执行了同样的注册；若缺失，开始播放时实体初始化会失败，客户端表现为
         * 点击播放没有任何反应，录制阶段则不受影响。
         */
        FabricDefaultAttributeRegistry.register(
                magicianPlayback,
                MagicianPlaybackEntity.createAttributes()
        );
        // A plain Entity, never a ProjectileEntity, so projectile-only hooks never see it; one hook cycle, never saved.
        // 普通 Entity 而非 ProjectileEntity，只针对投射物的钩子不会处理它；只存活一次钩爪循环，从不保存。
        ninjaGrapplingHook = Registry.register(
                Registries.ENTITY_TYPE,
                NINJA_GRAPPLING_HOOK_ID,
                EntityType.Builder.<NinjaGrapplingHookEntity>create(NinjaGrapplingHookEntity::new, SpawnGroup.MISC)
                        .dimensions(0.25F, 0.25F)
                        .maxTrackingRange(4)
                        .trackingTickInterval(1)
                        .disableSaving()
                        // Only a thrown item owns a hook; /summon would create an ownerless one.
                        // 只有投出的物品拥有钩爪；/summon 会产生无主钩爪。
                        .disableSummon()
                        .build(NINJA_GRAPPLING_HOOK_ID.toString())
        );
        // Sphinx Hao spring fist: presentation only (the shot is a server hitscan), a plain Entity like the grappling
        // hook, one shot long, never saved; appended last.
        // 狮身豪的弹簧拳头：仅用于表现（射击是服务端即时射线），与钩爪一样是普通 Entity，只存在一次射击，从不保存；追加在最后。
        sphinxHaoFist = Registry.register(
                Registries.ENTITY_TYPE,
                SPHINX_HAO_FIST_ID,
                EntityType.Builder.<SphinxHaoFistEntity>create(SphinxHaoFistEntity::new, SpawnGroup.MISC)
                        .dimensions(0.25F, 0.25F)
                        .maxTrackingRange(4)
                        .trackingTickInterval(1)
                        .disableSaving()
                        // Only a fired gun owns a fist; /summon would create an ownerless one.
                        // 只有开火的手枪拥有拳头；/summon 会产生无主拳头。
                        .disableSummon()
                        .makeFireImmune()
                        .build(SPHINX_HAO_FIST_ID.toString())
        );
        roleEntitiesRegistered = true;
    }

    public static EntityType<NinjaShurikenEntity> ninjaShuriken() {
        if (ninjaShuriken == null) {
            throw new IllegalStateException("SparkDLC role entities are not registered yet");
        }
        return ninjaShuriken;
    }

    public static EntityType<MagicianPlaybackEntity> magicianPlayback() {
        if (magicianPlayback == null) throw new IllegalStateException("SparkDLC role entities are not registered yet");
        return magicianPlayback;
    }

    public static EntityType<NinjaGrapplingHookEntity> ninjaGrapplingHook() {
        if (ninjaGrapplingHook == null) {
            throw new IllegalStateException("SparkDLC role entities are not registered yet");
        }
        return ninjaGrapplingHook;
    }

    public static EntityType<SphinxHaoFistEntity> sphinxHaoFist() {
        if (sphinxHaoFist == null) {
            throw new IllegalStateException("SparkDLC role entities are not registered yet");
        }
        return sphinxHaoFist;
    }
}
