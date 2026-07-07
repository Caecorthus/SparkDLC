package dev.caecorthus.sparkdlc.factionapi.impl;

import dev.caecorthus.sparkdlc.factionapi.api.EffectiveFactionResolver;
import dev.caecorthus.sparkdlc.factionapi.api.FactionBlackoutCooldownPolicy;
import dev.caecorthus.sparkdlc.factionapi.api.FactionCapabilities;
import dev.caecorthus.sparkdlc.factionapi.api.FactionDefinition;
import dev.caecorthus.sparkdlc.factionapi.api.FactionEconomyPolicy;
import dev.caecorthus.sparkdlc.factionapi.api.FactionGunPunishmentPolicy;
import dev.caecorthus.sparkdlc.factionapi.api.FactionInstinctPolicy;
import dev.caecorthus.sparkdlc.factionapi.api.FactionRoleDefinition;
import dev.caecorthus.sparkdlc.factionapi.api.FactionTargetEligibility;
import dev.caecorthus.sparkdlc.factionapi.impl.blackout.FactionBlackoutCooldownPolicies;
import dev.caecorthus.sparkdlc.factionapi.impl.economy.FactionEconomyPolicies;
import dev.caecorthus.sparkdlc.factionapi.impl.gun.FactionGunPunishmentPolicies;
import dev.caecorthus.sparkdlc.factionapi.impl.registry.EffectiveFactionResolvers;
import dev.caecorthus.sparkdlc.factionapi.impl.registry.FactionCatalog;
import dev.caecorthus.sparkdlc.factionapi.impl.registry.FactionRegistryBootstrap;
import dev.caecorthus.sparkdlc.factionapi.impl.registry.FactionRoleCatalog;
import dev.caecorthus.sparkdlc.factionapi.impl.target.FactionTargetPolicies;
import dev.caecorthus.sparkdlc.factionapi.impl.target.FactionTargetRules;
import dev.caecorthus.sparkdlc.factionapi.impl.vision.FactionInstinctPolicies;
import dev.doctor4t.wathe.api.Faction;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class FactionRegistryImpl {
    private FactionRegistryImpl() {
    }

    public static void bootstrap() {
        FactionRegistryBootstrap.bootstrap();
    }

    public static FactionDefinition registerFaction(FactionDefinition definition) {
        return FactionCatalog.registerFaction(definition);
    }

    public static Role registerRole(FactionRoleDefinition definition) {
        return FactionRoleCatalog.registerRole(definition);
    }

    public static boolean isSparkFactionRole(Role role) {
        return FactionRoleCatalog.isSparkFactionRole(role);
    }

    public static Optional<Faction> nativeFactionOverride(Role role) {
        return FactionRoleCatalog.nativeFactionOverride(role);
    }

    public static Optional<Boolean> nativeNeutralOverride(Role role) {
        return FactionRoleCatalog.nativeNeutralOverride(role);
    }

    public static Identifier resolveBaseFaction(Role role) {
        return FactionRoleCatalog.resolveBaseFaction(role);
    }

    public static Identifier resolveEffectiveFaction(PlayerEntity player, GameWorldComponent gameComponent) {
        return EffectiveFactionResolvers.resolve(player, gameComponent);
    }

    public static boolean canTarget(
            PlayerEntity viewer,
            PlayerEntity target,
            Identifier targetTag,
            GameWorldComponent gameComponent
    ) {
        bootstrap();
        return FactionTargetRules.canTarget(viewer, target, targetTag, gameComponent);
    }

    public static FactionCapabilities capabilities(Identifier factionId) {
        return FactionCatalog.capabilities(factionId);
    }

    public static Optional<FactionDefinition> getFaction(Identifier factionId) {
        return FactionCatalog.getFaction(factionId);
    }

    public static Collection<FactionDefinition> getCustomFactions() {
        return FactionCatalog.getCustomFactions();
    }

    public static boolean isCustomFaction(Identifier factionId) {
        return FactionCatalog.isCustomFaction(factionId);
    }

    public static Collection<Role> getRolesForFaction(Identifier factionId) {
        return FactionRoleCatalog.getRolesForFaction(factionId);
    }

    public static List<FactionTargetEligibility> targetEligibility() {
        return FactionTargetPolicies.targetEligibility();
    }

    public static List<FactionEconomyPolicy> economyPolicies() {
        return FactionEconomyPolicies.economyPolicies();
    }

    public static List<FactionGunPunishmentPolicy> gunPunishmentPolicies() {
        return FactionGunPunishmentPolicies.gunPunishmentPolicies();
    }

    public static List<FactionBlackoutCooldownPolicy> blackoutCooldownPolicies() {
        return FactionBlackoutCooldownPolicies.blackoutCooldownPolicies();
    }

    public static List<FactionInstinctPolicy> instinctPolicies() {
        return FactionInstinctPolicies.instinctPolicies();
    }

    public static void registerEffectiveFactionResolver(EffectiveFactionResolver resolver) {
        EffectiveFactionResolvers.register(resolver);
    }

    public static void registerTargetEligibility(FactionTargetEligibility eligibility) {
        FactionTargetPolicies.register(eligibility);
    }

    public static void registerEconomyPolicy(FactionEconomyPolicy policy) {
        FactionEconomyPolicies.register(policy);
    }

    public static void registerGunPunishmentPolicy(FactionGunPunishmentPolicy policy) {
        FactionGunPunishmentPolicies.register(policy);
    }

    public static void registerBlackoutCooldownPolicy(FactionBlackoutCooldownPolicy policy) {
        FactionBlackoutCooldownPolicies.register(policy);
    }

    public static void registerInstinctPolicy(FactionInstinctPolicy policy) {
        FactionInstinctPolicies.register(policy);
    }

}
