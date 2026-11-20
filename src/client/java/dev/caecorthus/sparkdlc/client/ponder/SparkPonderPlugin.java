package dev.caecorthus.sparkdlc.client.ponder;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.util.Identifier;

/**
 * Registers SparkAssist's item demos with Ponder. 向 Ponder 注册 SparkAssist 的物品演示。
 */
public final class SparkPonderPlugin implements PonderPlugin {
    static void register() {
        PonderIndex.addPlugin(new SparkPonderPlugin());
    }

    @Override
    public String getModId() {
        return SparkDLC.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<Identifier> helper) {
        SparkPonderDemos.registerIndexed(helper);
    }
}
