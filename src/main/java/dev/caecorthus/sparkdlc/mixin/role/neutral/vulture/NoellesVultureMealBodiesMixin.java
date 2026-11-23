package dev.caecorthus.sparkdlc.mixin.role.neutral.vulture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.caecorthus.sparkdlc.compat.noellesroles.NoellesVultureDecoyGuard;
import dev.caecorthus.sparkdlc.role.killer.hiro.returning.HiroFakeBodies;
import dev.caecorthus.sparkdlc.role.killer.partyanimal.PartygoerCorpseService;
import dev.doctor4t.wathe.entity.PlayerBodyEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.packet.VultureEatC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Predicate;

/**
 * External seam (pinned NoellesRoles {@code 1.7.6-h1.5.6-spark}): which bodies the Vulture eat receiver
 * {@code lambda$registerPackets$2} finds. It wraps the receiver's single {@code World#getEntitiesByType} body lookup,
 * which runs only after NoellesRoles' role, life, Taotie and cooldown gates, and passes the result through the mod's
 * three body rules. This used to be three {@code @WrapOperation}s on the same call, one mixin per rule; the one handler
 * applies them in the order those wrappers ran (the first-applied wrapper was innermost: Partygoer, then Hiro, then the
 * decoy guard), so every rule sees the list the one before it returned:
 * <ol>
 * <li>owner rule 2026-10-21: a Vulture that eats a Partygoer corpse is poisoned instead of fed
 * ({@link PartygoerCorpseService#vultureMeal}; it looks at the raw lookup result, and an eaten Partygoer corpse
 * leaves an empty list);</li>
 * <li>owner rule 4 of 二阶堂希罗's 死亡回归: the fake corpse of a fake-dead victim is never eaten (it would count toward
 * the Vulture's win and free the victim's camera). {@link HiroFakeBodies#withoutFakes} drops it silently, so
 * NoellesRoles runs its own no-body path (no cooldown, no message; unlike a Magician decoy, the refusal must not name
 * the corpse as fake);</li>
 * <li>owner decision 2026-10-07 D2: a Magician decoy body is never eaten ({@link NoellesVultureDecoyGuard}); a
 * decoy-only result becomes empty and the Vulture is told why on the action bar.</li>
 * </ol>
 * The rules are independent (a decoy is never a Partygoer corpse or a fake corpse), the order only keeps what each one
 * was handed. Additive {@code @WrapOperation} (never {@code @Redirect}); the traits' HEAD silence guard on the same
 * lambda still decides first, and the Fiend's / Charon's post-eat hooks are independent. The full descriptor with
 * {@code require = 1} makes a rebuilt NoellesRoles jar fail at load instead of silently dropping the rules. Class-level
 * {@code remap = false} (NoellesRoles/Fabric members); the vanilla {@code World} target says {@code remap = true}.
 * Server only.
 * 外部接缝（固定 NoellesRoles {@code 1.7.6-h1.5.6-spark}）：秃鹫进食接收器 {@code lambda$registerPackets$2} 能找到哪些尸体。
 * 包装接收器中唯一一次 {@code World#getEntitiesByType} 尸体查找（只在 NoellesRoles 的职业、存活、饕餮与冷却检查之后执行），
 * 并让结果依次经过本 mod 的三条尸体规则。此前这是同一调用上的三个 {@code @WrapOperation}，每条规则一个 mixin；现在这一个
 * 处理器按原先各包装的执行顺序应用它们（先应用的包装在最内层：派对客、希罗、诱饵守卫），每条规则看到的都是前一条返回的列表：
 * <ol>
 * <li>主人 2026-10-21 规则：秃鹫吃掉派对客尸体会中毒而不是进食（{@link PartygoerCorpseService#vultureMeal}；它看到的是
 * 原始查找结果，吃掉派对客尸体后返回空列表）；</li>
 * <li>二阶堂希罗「死亡回归」所有者规则 4：假死受害者的假尸体永远不会被吃掉（否则会计入秃鹫胜利进度并放开受害者的视角）。
 * {@link HiroFakeBodies#withoutFakes} 静默移除它，于是 NoellesRoles 走它自己的“没有尸体”分支（不进冷却、不提示；与魔术师
 * 诱饵不同，拒绝时不能点明尸体是假的）；</li>
 * <li>所有者 2026-10-07 决定 D2：魔术师的诱饵尸体永远不会被吃掉（{@link NoellesVultureDecoyGuard}）；结果只剩诱饵时变为
 * 空列表，并在动作栏告诉秃鹫原因。</li>
 * </ol>
 * 三条规则互不影响（诱饵永远不是派对客尸体，也不是假尸体），顺序只是保持每条规则原先拿到的输入。叠加式
 * {@code @WrapOperation}（绝不用 {@code @Redirect}）；同一 lambda 上特质的 HEAD 沉默守卫仍先行裁决，魔人 / 卡戎的进食后
 * 钩子互不影响。完整描述符加 {@code require = 1}，NoellesRoles jar 重编后会在加载时报错，而不是悄悄失去这些规则。类级
 * {@code remap = false}（NoellesRoles/Fabric 成员）；原版 {@code World} 目标显式声明 {@code remap = true}。仅服务端。
 */
@Mixin(value = Noellesroles.class, remap = false)
public abstract class NoellesVultureMealBodiesMixin {
    @WrapOperation(
            method = "lambda$registerPackets$2(Lorg/agmas/noellesroles/packet/VultureEatC2SPacket;"
                    + "Lnet/fabricmc/fabric/api/networking/v1/ServerPlayNetworking$Context;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getEntitiesByType(Lnet/minecraft/util/TypeFilter;"
                            + "Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;",
                    remap = true
            ),
            require = 1,
            allow = 1
    )
    private static List<PlayerBodyEntity> sparkdlc$vultureMealBodies(
            World world,
            TypeFilter<Entity, PlayerBodyEntity> filter,
            Box box,
            Predicate<? super PlayerBodyEntity> predicate,
            Operation<List<PlayerBodyEntity>> original,
            VultureEatC2SPacket payload,
            ServerPlayNetworking.Context context
    ) {
        // Order is behaviour: innermost first, exactly as the three former wrappers nested.
        // 顺序即行为：由内到外，与原先三层包装的嵌套完全一致。
        List<PlayerBodyEntity> found = original.call(world, filter, box, predicate);
        found = PartygoerCorpseService.vultureMeal(context.player(), found);
        found = HiroFakeBodies.withoutFakes(found);
        return NoellesVultureDecoyGuard.withoutDecoys(context.player(), found);
    }
}
