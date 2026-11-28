package dev.caecorthus.sparkdlc.item.drink;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * The five house drinks (owner 2026-11-28): modelled on Wathe's martini and mojito, each completing the drinking task.
 * 五种自调饮料（所有者 2026-11-28）：模仿 Wathe 的马提尼与莫吉托，都能完成饮酒任务。
 */
public final class SparkDrinks {

    public static final Identifier ABYSS_CHAMPAGNE_ID = SparkDLC.id("abyss_champagne");
    public static final Identifier CRIMSON_MARTINI_ID = SparkDLC.id("crimson_martini");
    public static final Identifier SUNSET_WHISKEY_ID = SparkDLC.id("sunset_whiskey");
    public static final Identifier POLAR_MINT_ID = SparkDLC.id("polar_mint");
    public static final Identifier BLACKBERRY_DREAM_ID = SparkDLC.id("blackberry_dream");

    private static Item abyssChampagne;
    private static Item crimsonMartini;
    private static Item sunsetWhiskey;
    private static Item polarMint;
    private static Item blackberryDream;
    private static boolean registered;

    private SparkDrinks() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        abyssChampagne = register(ABYSS_CHAMPAGNE_ID);
        crimsonMartini = register(CRIMSON_MARTINI_ID);
        sunsetWhiskey = register(SUNSET_WHISKEY_ID);
        polarMint = register(POLAR_MINT_ID);
        blackberryDream = register(BLACKBERRY_DREAM_ID);
    }

    private static Item register(Identifier id) {
        return Registry.register(Registries.ITEM, id, new SparkDrinkItem(new Item.Settings().maxCount(1)));
    }

    public static Item abyssChampagne() {
        return abyssChampagne;
    }

    public static Item crimsonMartini() {
        return crimsonMartini;
    }

    public static Item sunsetWhiskey() {
        return sunsetWhiskey;
    }

    public static Item polarMint() {
        return polarMint;
    }

    public static Item blackberryDream() {
        return blackberryDream;
    }
}
