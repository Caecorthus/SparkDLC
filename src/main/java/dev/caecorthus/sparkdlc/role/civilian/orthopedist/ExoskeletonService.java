package dev.caecorthus.sparkdlc.role.civilian.orthopedist;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.doctor4t.wathe.cca.GameWorldComponent;
import dev.doctor4t.wathe.cca.PlayerStaminaComponent;
import dev.doctor4t.wathe.game.GameFunctions;
import dev.doctor4t.wathe.index.WatheAttributes;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Server side of the Exoskeleton: the fitting itself and the one stamina modifier that mirrors a wearer's layers.
 * The layers live in {@link OrthopedistPlayerComponent}, which every player has, because the wearer can be anyone.
 * 外骨骼的服务端逻辑：装配本身，以及与穿戴者层数保持一致的唯一体力修饰符。层数存放在每名玩家都有的
 * {@link OrthopedistPlayerComponent} 中，因为穿戴者可以是任何人。
 */
public final class ExoskeletonService {
    /**
     * The wearer's modifier on Wathe's {@code max_sprint_time}. {@code ADD_MULTIPLIED_BASE}, amount = layers: Wathe
     * rewrites the attribute base to the role's maximum every tick and reads the attribute value as the cap, so each
     * layer adds the role's base maximum once, and the Excellent Physique trait's total multiplier applies on top.
     * Wathe never reads the attribute for a role with infinite stamina, so such a wearer carries the modifier without
     * any stamina effect.
     * 穿戴者在 Wathe {@code max_sprint_time} 上的修饰符。{@code ADD_MULTIPLIED_BASE}，数值 = 层数：Wathe 每刻把属性基础值
     * 改写为职业上限并以属性值作为体力上限，因此每层增加一份职业基础上限，体质优异词条的总乘区再叠加其上。无限体力的职业
     * 不会读取该属性，这类穿戴者带着修饰符但体力不受影响。
     */
    public static final Identifier STAMINA_MODIFIER_ID = SparkDLC.id("exoskeleton_stamina");

    private ExoskeletonService() {
    }

    /**
     * One use of the item by {@code user} on {@code target} (the same player when fitting oneself). True when a layer
     * was added and the item spent; every refusal leaves the stack untouched and tells the user why.
     * {@code user} 对 {@code target} 使用一次道具（给自己装配时两者相同）。成功加一层并消耗道具时返回 true；任何拒绝都不动
     * 物品，并告知使用者原因。
     */
    public static boolean fit(ServerPlayerEntity user, ServerPlayerEntity target, ItemStack stack) {
        boolean samePlayer = user.getUuid().equals(target.getUuid());
        OrthopedistPlayerComponent wearer = OrthopedistPlayerComponent.KEY.get(target);
        ExoskeletonRules.FitResult result = ExoskeletonRules.fitResult(
                isActivePlayer(user),
                isActivePlayer(target),
                samePlayer,
                user.squaredDistanceTo(target),
                samePlayer || user.canSee(target),
                wearer.getExoskeletonLayers()
        );
        switch (result) {
            case UNAVAILABLE -> {
                user.sendMessage(Text.translatable("message.sparkdlc.exoskeleton.unavailable"), true);
                return false;
            }
            case OUT_OF_RANGE -> {
                user.sendMessage(Text.translatable("message.sparkdlc.exoskeleton.out_of_range"), true);
                return false;
            }
            case AT_MAX_LAYERS -> {
                user.sendMessage(samePlayer
                        ? Text.translatable("message.sparkdlc.exoskeleton.max_layers_self", ExoskeletonRules.MAX_LAYERS)
                        : Text.translatable(
                                "message.sparkdlc.exoskeleton.max_layers_target",
                                target.getDisplayName(),
                                ExoskeletonRules.MAX_LAYERS
                        ), true);
                return false;
            }
            case FIT -> {
            }
        }
        if (!wearer.addExoskeletonLayer()) {
            return false;
        }

        stack.decrementUnlessCreative(1, user);
        int layers = wearer.getExoskeletonLayers();
        if (samePlayer) {
            user.sendMessage(Text.translatable(
                    "message.sparkdlc.exoskeleton.fitted_self",
                    layers,
                    ExoskeletonRules.MAX_LAYERS
            ), true);
        } else {
            user.sendMessage(Text.translatable(
                    "message.sparkdlc.exoskeleton.fitted_target",
                    target.getDisplayName(),
                    layers,
                    ExoskeletonRules.MAX_LAYERS
            ), true);
            target.sendMessage(Text.translatable(
                    "message.sparkdlc.exoskeleton.received",
                    layers,
                    ExoskeletonRules.MAX_LAYERS
            ), true);
        }
        return true;
    }

    /** Whether the wearer's layers still belong to a running round. / 穿戴者的层数是否仍属于进行中的对局。 */
    static boolean keepsLayers(PlayerEntity player) {
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        return ExoskeletonRules.keepsLayers(game.isRunning(), game.hasAnyRole(player));
    }

    /**
     * Server only: makes the stamina modifier match {@code layers}. Removing the last layer also lowers a finite
     * maximum and clamps the live stamina to it, since Wathe only does that for a living player in a running round.
     * 仅服务端：让体力修饰符与 {@code layers} 一致。移除最后一层时同时降低有限体力的上限并截断当前体力，因为 Wathe 只对
     * 进行中对局里的存活玩家这样做。
     */
    static void writeStaminaModifier(PlayerEntity player, int layers) {
        EntityAttributeInstance stamina = player.getAttributeInstance(WatheAttributes.MAX_SPRINT_TIME);
        if (stamina == null) {
            return;
        }
        boolean hadModifier = stamina.hasModifier(STAMINA_MODIFIER_ID);
        if (hadModifier) {
            stamina.removeModifier(STAMINA_MODIFIER_ID);
        }
        double bonus = ExoskeletonRules.staminaBonus(layers);
        if (bonus > 0.0D) {
            // Temporary: the layer count in the component is what is saved, and it rewrites this after a load.
            // 临时修饰符：存档的是组件里的层数，读档后由它重新写入。
            stamina.addTemporaryModifier(new EntityAttributeModifier(
                    STAMINA_MODIFIER_ID,
                    bonus,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        } else if (!hadModifier) {
            return;
        }

        PlayerStaminaComponent staminaComponent = PlayerStaminaComponent.KEY.get(player);
        if (bonus <= 0.0D && !staminaComponent.isInfiniteStamina()) {
            int maxSprintTime = (int) stamina.getValue();
            staminaComponent.setMaxSprintTime(maxSprintTime);
            staminaComponent.setSprintingTicks(Math.min(staminaComponent.getSprintingTicks(), maxSprintTime));
        }
        staminaComponent.sync();
    }

    private static boolean isActivePlayer(PlayerEntity player) {
        return GameFunctions.isPlayerPlayingAndAlive(player)
                && !GameFunctions.isPlayerSpectatingOrCreative(player);
    }
}
