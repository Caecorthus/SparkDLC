package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.role.killer.witchmaiden.wither.MiniWitherEntities;
import dev.caecorthus.sparkdlc.role.killer.blackraven.crow.BlackRavenCrowEntities;
import dev.caecorthus.sparkdlc.role.killer.blackraven.crow.BlackRavenFeatherRegistry;
import dev.caecorthus.sparkdlc.role.witch.potiongunner.shell.PotionGunnerEntities;
import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesHiddenEquipment;
import dev.caecorthus.sparkdlc.command.ForceAbilityCommand;
import dev.caecorthus.sparkdlc.command.ForceAccompliceRoleCommand;
import dev.caecorthus.sparkdlc.command.ForcePromotionCommand;
import dev.caecorthus.sparkdlc.command.GhostSettingsCommand;
import dev.caecorthus.sparkdlc.command.SetManaCommand;
import dev.caecorthus.sparkdlc.command.ThirdFactionSettingsCommand;
import dev.caecorthus.sparkdlc.command.WatheGhostDividendCommand;
import dev.caecorthus.sparkdlc.command.WitchFactorCommand;
import dev.caecorthus.sparkdlc.skill.SparkWitchBuiltInSkills;
import dev.caecorthus.sparkdlc.event.SparkWitchEvents;
import dev.caecorthus.sparkdlc.net.SparkWitchPackets;
import dev.caecorthus.sparkdlc.SparkWitchDataComponents;
import dev.caecorthus.sparkdlc.net.version.SparkWitchVersionHandshake;
import dev.caecorthus.sparkdlc.role.civilian.controlexpert.ControlExpertEntities;
import dev.caecorthus.sparkdlc.role.civilian.savior.SaviorParticles;
import dev.caecorthus.sparkdlc.role.civilian.saint.flash.HolyFlashEntities;
import dev.caecorthus.sparkdlc.role.witch.abysslistener.zone.AbyssListenerEntities;
import dev.caecorthus.sparkdlc.role.civilian.seeker.SeekerSounds;
import dev.caecorthus.sparkdlc.role.civilian.seeker.device.SeekerEntities;
import dev.caecorthus.sparkdlc.role.witch.riftwalker.gate.RiftGateEntities;
import dev.caecorthus.sparkdlc.role.civilian.orthopedist.OrthopedistEffects;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelEffects;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterEffects;
import dev.caecorthus.sparkdlc.role.killer.hunter.HunterEntities;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterEntities;
import dev.caecorthus.sparkdlc.role.killer.specter.SpecterParticles;
import dev.caecorthus.sparkdlc.role.killer.witchmaiden.FocusedFootstepsEffects;
import dev.caecorthus.sparkdlc.role.witch.curser.CurserFeatureService;
import dev.caecorthus.sparkdlc.role.civilian.guardianangel.GuardianAngelRules;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithParticipationRules;
import dev.caecorthus.sparkdlc.role.special.wraith.WraithStateService;
import dev.caecorthus.sparkdlc.util.hitscan.PlayerHitboxHistory;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SparkWitch implements ModInitializer {
    public static final String MOD_ID = "sparkdlc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SparkWitchSounds.register();
        SaviorParticles.register();
        SeekerSounds.register();
        HunterEffects.register();
        OrthopedistEffects.register();
        GuardianAngelEffects.register();
        FocusedFootstepsEffects.register();
        HunterEntities.register();
        SpecterEntities.register();
        SpecterParticles.register();
        ControlExpertEntities.register();
        AbyssListenerEntities.register();
        RiftGateEntities.register();
        SeekerEntities.register();
        HolyFlashEntities.register();
        PotionGunnerEntities.register();
        MiniWitherEntities.register();
        BlackRavenCrowEntities.register();
        BlackRavenFeatherRegistry.register();
        // BEAR smoke round entity type. / BEAR 烟雾榴弹实体类型。
        dev.caecorthus.sparkdlc.role.civilian.bear.launcher.BearEntities.register();
        SparkWitchDataComponents.register();
        SparkWitchItems.register();
        // BEAR items (launcher, smoke round, Rys-T, earplugs), after the data components they carry and before the tab.
        // BEAR 物品（发射器、烟雾榴弹、Rys-T、耳塞），位于其携带的数据组件之后、物品栏之前。
        dev.caecorthus.sparkdlc.role.civilian.bear.BearItems.register();
        SparkWitchItemGroups.register();
        SparkWitchEntities.register();
        // NoellesRoles remains the packet-filter owner; FactionAPI only extends its hidden-item predicate.
        // NoellesRoles 仍负责装备包过滤，FactionAPI 这里只扩展其隐藏物品判定。
        NoellesHiddenEquipment.register(SparkWitchItems.perfumeEssence());
        NoellesHiddenEquipment.register(SparkWitchItems.cologne());
        NoellesHiddenEquipment.register(SparkWitchItems.blackRavenLedger());
        NoellesHiddenEquipment.register(SparkWitchItems.timeStealerClock());
        NoellesHiddenEquipment.register(SparkWitchItems.timeStealerGiftWatch());
        NoellesHiddenEquipment.register(SparkWitchItems.timeStamp());
        NoellesHiddenEquipment.register(SparkWitchItems.blackRavenMask());
        NoellesHiddenEquipment.register(SparkWitchItems.prophetNecrology());
        NoellesHiddenEquipment.register(SparkWitchItems.tollBell());
        NoellesHiddenEquipment.register(SparkWitchItems.seekerCar());
        NoellesHiddenEquipment.register(SparkWitchItems.seekerCamera());
        // Angler: the rod, bait and every non-weapon fish; the Key Fish and Swordfish stay visible on purpose.
        // 钓鱼佬：鱼竿、鱼饵与所有非武器类的鱼；钥匙鱼与剑鱼刻意保持可见。
        NoellesHiddenEquipment.register(SparkWitchItems.fishingRod());
        NoellesHiddenEquipment.register(SparkWitchItems.fishBait());
        NoellesHiddenEquipment.register(SparkWitchItems.salmon());
        NoellesHiddenEquipment.register(SparkWitchItems.cod());
        NoellesHiddenEquipment.register(SparkWitchItems.clownfish());
        NoellesHiddenEquipment.register(SparkWitchItems.goldfish());
        NoellesHiddenEquipment.register(SparkWitchItems.glimmerfish());
        // Blind: the cane is hidden in round (D9); a held ComTac would expose the role the same way.
        // 盲人：盲杖局内隐藏（D9）；手持的 ComTac 同样会暴露身份。
        NoellesHiddenEquipment.register(SparkWitchItems.whiteCane());
        NoellesHiddenEquipment.register(SparkWitchItems.comTac());
        // Abyss Listener: the bound Shriek Gun is hidden in hand (owner default N11); the flask stays visible.
        // 聆渊者：绑定的啸音铳手持时对他人隐藏（所有者默认 N11）；孢瓶保持可见。
        NoellesHiddenEquipment.register(SparkWitchItems.shriekGun());
        // Riftwalker: the Rift Gate stack is hidden in hand, like the Seeker devices.
        // 隙行者：裂隙门手持时对他人隐藏，与搜寻者设备相同。
        NoellesHiddenEquipment.register(SparkWitchItems.riftGate());
        SparkWitchRoles.register();
        SparkWitchBuiltInSkills.register();
        SparkWitchPackets.register();
        PlayerHitboxHistory.register();
        CurserFeatureService.register();
        SparkWitchVersionHandshake.registerServer();
        SparkWitchEvents.register();
        dev.caecorthus.sparkdlc.cooldown.provider.SparkWitchForcedCooldowns.register();
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(
                (message, sender, params) -> mayUseTextChat(sender)
        );
        ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) -> {
            ServerPlayerEntity sender = source.getPlayer();
            return sender == null || mayUseTextChat(sender);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> SparkWitchRoles.refreshAssassinGuessRoleOrder());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SetManaCommand.register(dispatcher);
            ForceAbilityCommand.register(dispatcher);
            ForcePromotionCommand.register(dispatcher);
            ForceAccompliceRoleCommand.register(dispatcher);
            GhostSettingsCommand.register(dispatcher);
            WatheGhostDividendCommand.register(dispatcher);
            ThirdFactionSettingsCommand.register(dispatcher);
            WitchFactorCommand.register(dispatcher);
        });
    }

    private static boolean mayUseTextChat(ServerPlayerEntity sender) {
        if (!WraithStateService.isActive(sender)) {
            return true;
        }
        var role = GameWorldComponent.KEY.get(sender.getWorld()).getRole(sender);
        return WraithParticipationRules.mayUseTextChat(
                true,
                GuardianAngelRules.isGuardianAngel(role),
                sender.isCreative()
        );
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
