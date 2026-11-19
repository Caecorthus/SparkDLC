package dev.caecorthus.sparkdlc.bridge.strength;

import dev.caecorthus.sparkdlc.cooldown.api.ForcedCooldowns;
import dev.caecorthus.sparkdlc.cooldown.api.RoleSkillCooldownStore;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Bridge to SparkFactionAPI's public {@code api.cooldown.ForcedCooldowns}. The modules are fused in SparkDLC, so a
 * compiled {@link RoleSkillCooldownStore} implements the abstract methods with the given functions and inherits the
 * interface's default methods ({@code mayForce}, {@code raiseTo}, {@code extendBy}), and the registry is called
 * directly. Role-mechanic item clears go through {@link #clearItemCooldownKeepingForced}.
 * SparkFactionAPI 公开 {@code api.cooldown.ForcedCooldowns} 的桥接。各模块已融合进 SparkDLC，因此用编译期的
 * {@link RoleSkillCooldownStore} 以给定函数实现抽象方法，并继承接口自身的默认方法（{@code mayForce}、{@code raiseTo}、
 * {@code extendBy}），注册表则被直接调用。职业机制的物品冷却清除经由 {@link #clearItemCooldownKeepingForced}。
 */
public final class SparkFactionCooldownCompat {
    private SparkFactionCooldownCompat() {
    }

    /**
     * Role-mechanic item clear (Serial Killer kill reset, Timekeeper item refresh): SparkFactionAPI's
     * {@code ForcedCooldowns.clearItemKeepingForced} removes the natural cooldown but keeps a penalty another feature
     * forced on the item (Fiend aura, Abyss Shriek, anti-tank shell). Called directly since the SparkDLC fusion, so
     * there is no plain {@code remove} fallback any more. Server thread only.
     * 职业机制的物品冷却清除（连环杀手击杀重置、计时员物品刷新）：SparkFactionAPI 的
     * {@code ForcedCooldowns.clearItemKeepingForced} 清除自然冷却，但保留其他功能强制施加在该物品上的惩罚（魔人光环、
     * 聆渊尖啸、反坦克弹）。融合进 SparkDLC 后直接调用，不再有普通 {@code remove} 回退。仅限服务端线程。
     */
    public static void clearItemCooldownKeepingForced(ServerPlayerEntity player, Item item) {
        ForcedCooldowns.clearItemKeepingForced(player, item);
    }

    /**
     * Registers one role-skill counter; call once during mod initialization (SparkFactionAPI rejects duplicate ids
     * with an {@link IllegalArgumentException}, which now propagates). SparkFactionAPI calls the functions on the
     * server thread only. Always returns true: the registry is called directly since the SparkDLC fusion.
     * 注册一项职业技能计数；只在模组初始化时调用一次（SparkFactionAPI 以 {@link IllegalArgumentException} 拒绝重复 id，
     * 该异常现在会向上抛出）。SparkFactionAPI 只在服务端线程调用这些函数。恒返回 true：融合进 SparkDLC 后直接调用注册表。
     */
    public static boolean registerRoleSkillStore(
            Identifier id,
            Predicate<ServerPlayerEntity> appliesTo,
            ToIntFunction<ServerPlayerEntity> remainingTicks,
            Function<ServerPlayerEntity, OptionalInt> nominalTicks,
            ObjIntConsumer<ServerPlayerEntity> setRemainingTicks
    ) {
        ForcedCooldowns.registerRoleSkillStore(new RoleSkillCooldownStore() {
            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public boolean appliesTo(ServerPlayerEntity player) {
                return appliesTo.test(player);
            }

            @Override
            public int remainingTicks(ServerPlayerEntity player) {
                return remainingTicks.applyAsInt(player);
            }

            @Override
            public OptionalInt nominalTicks(ServerPlayerEntity player) {
                return nominalTicks.apply(player);
            }

            @Override
            public void setRemainingTicks(ServerPlayerEntity player, int ticks) {
                setRemainingTicks.accept(player, ticks);
            }

            @Override
            public String toString() {
                return "SparkStrengthRoleSkillCooldownStore[" + id + "]";
            }
        });
        return true;
    }
}
