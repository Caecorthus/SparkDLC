package dev.caecorthus.sparkdlc.client.ponder;

import dev.caecorthus.sparkdlc.SparkDLC;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderStoryBoardEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * The demo catalog: demo id -> scenes. Item demos use the item id, are also registered with Ponder (so holding W
 * over the item in an inventory plays them) and show the item as the player's icon; role demos use
 * {@code sparkdlc:roles/<namespace>/<role>}, are opened only from the guide and show a head icon.
 * 演示目录：演示 id 对应若干场景。物品演示使用物品 id，同时注册到 Ponder（在背包里悬停该物品按住 W 即可播放），
 * 播放界面的图标就是该物品；职业演示使用 sparkdlc:roles/<命名空间>/<职业>，只从指南书打开，图标为头颅。
 */
final class SparkPonderDemos {
    private static final Identifier ROLE_ICON = Identifier.ofVanilla("player_head");
    private static final Map<String, Demo> DEMOS = new LinkedHashMap<>();

    static {
        WatheItemScenes.register();
        WatheShopScenes.register();
        WatheRoleScenes.register();
        NoellesCivilianScenes.register();
        NoellesKillerScenes.register();
        PartyAnimalScenes.register();
        SparkCivilianScenes.register();
        SparkKillerScenes.register();
        WraithScenes.register();
        RoleKitCivilianItemScenes.register();
        RoleKitKillerItemScenes.register();
        SparkRoleItemScenes.register();
    }

    private SparkPonderDemos() {
    }

    /** One scene: its stage structure under assets/sparkdlc/ponder/ and its script. 一个场景：舞台结构与脚本。 */
    record Scene(String stage, PonderStoryBoard board) {
    }

    /**
     * Every demo exists unconditionally: the mods its scenes show (this mod, NoellesRoles, Wathe) are always loaded,
     * the last two being required dependencies in {@code fabric.mod.json}.
     * 每个演示都无条件存在：场景展示的模组（本模组、NoellesRoles、Wathe）总是已加载，后两者是 fabric.mod.json 中的必需依赖。
     */
    private record Demo(Identifier icon, List<Scene> scenes, boolean indexed) {
    }

    static void item(String itemId, Scene... scenes) {
        DEMOS.put(itemId, new Demo(Identifier.of(itemId), List.of(scenes), true));
    }

    static void role(String roleDemoId, Scene... scenes) {
        DEMOS.put(roleDemoId, new Demo(ROLE_ICON, List.of(scenes), false));
    }

    static Scene scene(String stage, PonderStoryBoard board) {
        return new Scene(stage, board);
    }

    static boolean has(String id) {
        return DEMOS.containsKey(id);
    }

    static void open(String id, @Nullable Screen returnTo) {
        if (!has(id)) {
            return;
        }
        Demo demo = DEMOS.get(id);
        List<StoryBoardEntry> entries = new ArrayList<>(demo.scenes().size());
        for (Scene scene : demo.scenes()) {
            entries.add(new PonderStoryBoardEntry(scene.board(), SparkDLC.MOD_ID, scene.stage(), demo.icon()));
        }
        List<PonderScene> compiled = PonderIndex.getSceneAccess().compile(entries);
        if (!compiled.isEmpty()) {
            ScreenOpener.transitionTo(new DemoPonderUI(compiled, returnTo));
        }
    }

    /** Item demos join Ponder's own index and the hold-W hint. 物品演示加入 Ponder 自己的索引与按住 W 提示。 */
    static void registerIndexed(PonderSceneRegistrationHelper<Identifier> helper) {
        DEMOS.values().stream().filter(Demo::indexed).forEach(demo -> {
            for (Scene scene : demo.scenes()) {
                helper.addStoryBoard(demo.icon(), scene.stage(), scene.board());
            }
        });
    }
}
