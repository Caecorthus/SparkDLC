package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.SparkFactionApiMod;
import dev.caecorthus.sparkdlc.SparkStrength;
import dev.caecorthus.sparkdlc.SparkTraits;
import dev.caecorthus.sparkdlc.SparkWitch;
import dev.caecorthus.sparkdlc.command.SparkDlcCommands;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entrypoint of SparkDLC. It starts the modules in the order Fabric used when they were separate mods
 * (mod ids sorted alphabetically: sparkfactionapi, sparkstrength, sparktraits, sparkwitch), so registration and event
 * listener order is unchanged. SparkAssist has no common-side initializer.
 * SparkDLC 的通用入口。按它们还是独立 mod 时 Fabric 采用的顺序（mod id 字母序：sparkfactionapi、sparkstrength、
 * sparktraits、sparkwitch）启动各模块，注册顺序和事件监听顺序因此保持不变。SparkAssist 没有通用端初始化。
 */
public final class SparkDLC implements ModInitializer {
    public static final String MOD_ID = "sparkdlc";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        new SparkFactionApiMod().onInitialize();
        new SparkStrength().onInitialize();
        new SparkTraits().onInitialize();
        new SparkWitch().onInitialize();
        // Every command of the mod, and the old command names, in one place.
        // 本模组的全部命令以及旧命令名，统一在一处注册。
        SparkDlcCommands.register();
        // One creative tab for the whole mod, once every module has registered its items.
        // 所有模块注册完物品后，注册全模组唯一的创造模式物品栏。
        SparkDlcItemGroups.register();
    }
}
