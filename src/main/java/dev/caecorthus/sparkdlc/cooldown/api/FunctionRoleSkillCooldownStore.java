package dev.caecorthus.sparkdlc.cooldown.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * A role-skill counter given as functions: they implement the abstract methods of {@link RoleSkillCooldownStore},
 * and the interface's default methods ({@code mayForce}, {@code raiseTo}, {@code extendBy}) apply unchanged. The
 * functions are called on the server thread only. Register it once during mod initialization with
 * {@link ForcedCooldowns#registerRoleSkillStore} (a duplicate id throws {@link IllegalArgumentException}).
 * 以函数给出的职业技能计数：这些函数实现 {@link RoleSkillCooldownStore} 的抽象方法，接口自身的默认方法（{@code mayForce}、
 * {@code raiseTo}、{@code extendBy}）原样生效。这些函数只在服务端线程被调用。在模组初始化时经
 * {@link ForcedCooldowns#registerRoleSkillStore} 注册一次（重复 id 抛出 {@link IllegalArgumentException}）。
 */
public final class FunctionRoleSkillCooldownStore implements RoleSkillCooldownStore {
    private final Identifier id;
    private final Predicate<ServerPlayerEntity> appliesTo;
    private final ToIntFunction<ServerPlayerEntity> remainingTicks;
    private final Function<ServerPlayerEntity, OptionalInt> nominalTicks;
    private final ObjIntConsumer<ServerPlayerEntity> setRemainingTicks;

    public FunctionRoleSkillCooldownStore(
            Identifier id,
            Predicate<ServerPlayerEntity> appliesTo,
            ToIntFunction<ServerPlayerEntity> remainingTicks,
            Function<ServerPlayerEntity, OptionalInt> nominalTicks,
            ObjIntConsumer<ServerPlayerEntity> setRemainingTicks
    ) {
        this.id = id;
        this.appliesTo = appliesTo;
        this.remainingTicks = remainingTicks;
        this.nominalTicks = nominalTicks;
        this.setRemainingTicks = setRemainingTicks;
    }

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

    /** The text is kept from before the fusion, when only the former SparkStrength roles registered such stores.
     *  文本沿用融合之前的写法，当时只有原 SparkStrength 的职业注册这种存储。 */
    @Override
    public String toString() {
        return "SparkStrengthRoleSkillCooldownStore[" + id + "]";
    }
}
