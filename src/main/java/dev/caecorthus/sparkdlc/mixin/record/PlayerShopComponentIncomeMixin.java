package dev.caecorthus.sparkdlc.mixin.record;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.caecorthus.sparkdlc.record.IncomeRecorder;
import dev.caecorthus.sparkdlc.record.PlayerScopeStack;
import dev.doctor4t.wathe.cca.PlayerShopComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Measures the actual personal balance change of every {@code addToBalance} call for {@code sparkdlc:income}.
 * The killer-team purse also wraps this method and only reads the balance around its own original call,
 * so either wrapper order sees the same change; the traits' {@code @ModifyArg} on the inner {@code setBalance} is
 * included. The amount is passed through unchanged.
 * 为 {@code sparkdlc:income} 测量每次 {@code addToBalance} 调用对个人余额的实际变化。杀手团队资金也包裹此
 * 方法，且只在自身 original 调用前后读取余额，因此无论包裹顺序如何看到的变化都相同；天赋对内部
 * {@code setBalance} 的 {@code @ModifyArg} 会被计入。金额原样传递。
 */
@Mixin(value = PlayerShopComponent.class, remap = false)
public abstract class PlayerShopComponentIncomeMixin {
    @Shadow
    @Final
    private PlayerEntity player;

    @Shadow
    public int balance;

    @WrapMethod(method = "addToBalance(I)V")
    private void sparkdlc$countIncome(int amount, Operation<Void> original) {
        PlayerScopeStack.Frame<Integer> frame = IncomeRecorder.beginCredit(player, balance);
        try {
            original.call(amount);
        } finally {
            IncomeRecorder.endCredit(frame, balance);
        }
    }
}
