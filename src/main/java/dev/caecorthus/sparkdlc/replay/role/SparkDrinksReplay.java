package dev.caecorthus.sparkdlc.replay.role;

import dev.caecorthus.sparkdlc.item.drink.SparkDrinks;
import dev.doctor4t.wathe.record.replay.ReplayGenerator;
import dev.doctor4t.wathe.record.replay.ReplayRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

/**
 * Replay lines for the five house drinks. Without a registered formatter Wathe drops the item-use event, so a drink
 * would leave no trace in the replay at all. 五种自调饮料的回放文本。没有注册格式化器时 Wathe 会丢弃该物品使用事件，
 * 于是回放里完全看不到有人喝过饮料。
 */
public final class SparkDrinksReplay {
    private SparkDrinksReplay() {
    }

    public static void register() {
        for (Identifier id : List.of(
                SparkDrinks.ABYSS_CHAMPAGNE_ID,
                SparkDrinks.CRIMSON_MARTINI_ID,
                SparkDrinks.SUNSET_WHISKEY_ID,
                SparkDrinks.POLAR_MINT_ID,
                SparkDrinks.BLACKBERRY_DREAM_ID)) {
            ReplayRegistry.registerItemUseFormatter(id, (event, match, world) -> {
                NbtCompound data = event.data();
                UUID actorUuid = data.containsUuid("actor") ? data.getUuid("actor") : null;
                if (actorUuid == null) {
                    return null;
                }
                Text actorText = ReplayGenerator.formatPlayerName(
                        actorUuid, ReplayGenerator.getPlayerInfoCache(match));
                return Text.translatable(
                        "replay.item_use.sparkdlc.drink",
                        actorText,
                        ReplayGenerator.formatItemName(data, world)
                );
            });
        }
    }
}
