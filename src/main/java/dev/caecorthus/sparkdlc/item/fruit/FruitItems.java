package dev.caecorthus.sparkdlc.item.fruit;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * The Fruit Vendor's four foods and the peel her banana leaves behind (owner 2026-11-28). Nutrition is ours — the
 * owner left the hunger and saturation values to me: they sit between a carrot (3/0.6) and a golden carrot (6/1.2)
 * for the paying items, and at the vanilla-original level for the crate fruits.
 * 水果贩的四种食物，以及她吃香蕉后留下的果皮（所有者 2026-11-28）。营养值由我们定——所有者把饥饿度与饱食度交给我：
 * 付费商品落在胡萝卜（3/0.6）与金胡萝卜（6/1.2）之间，箱子里的水果则与原版一致。
 */
public final class FruitItems {

    public static final Identifier BANANA_ID = SparkDLC.id("banana");
    public static final Identifier BANANA_PEEL_ID = SparkDLC.id("banana_peel");
    public static final Identifier ORANGE_ID = SparkDLC.id("orange");
    public static final Identifier ENLIGHTENMENT_BERRY_ID = SparkDLC.id("enlightenment_berry");

    private static Item banana;
    private static Item bananaPeel;
    private static Item orange;
    private static Item enlightenmentBerry;

    private FruitItems() {
    }

    public static void register() {
        // Banana: 4 hunger / 0.4 saturation, and it always leaves a peel behind. / 香蕉：4 饥饿 / 0.4 饱食，且必留果皮。
        banana = Registry.register(Registries.ITEM, BANANA_ID,
                new BananaItem(new Item.Settings().food(new FoodComponent.Builder()
                        .nutrition(4).saturationModifier(0.4F).alwaysEdible().build())));
        // The peel is an entity (like a note), so no block is registered at all.
        // 果皮是实体（像便签），因此不再注册方块。
        bananaPeel = Registry.register(Registries.ITEM, BANANA_PEEL_ID, new BananaPeelItem(new Item.Settings()));
        orange = Registry.register(Registries.ITEM, ORANGE_ID,
                new EffectFruitItem(new Item.Settings().food(new FoodComponent.Builder()
                        .nutrition(3).saturationModifier(0.3F).alwaysEdible().build()), EffectFruitItem.Kind.ORANGE));
        enlightenmentBerry = Registry.register(Registries.ITEM, ENLIGHTENMENT_BERRY_ID,
                new EffectFruitItem(new Item.Settings().food(new FoodComponent.Builder()
                        .nutrition(2).saturationModifier(0.2F).alwaysEdible().build()),
                        EffectFruitItem.Kind.ENLIGHTENMENT_BERRY));
    }

    public static Item banana() {
        return banana;
    }

    public static Item bananaPeel() {
        return bananaPeel;
    }

    public static Item orange() {
        return orange;
    }

    public static Item enlightenmentBerry() {
        return enlightenmentBerry;
    }
}
