package dev.caecorthus.sparkdlc.client.scope;

import dev.caecorthus.sparkdlc.client.SparkDLCClient;
import dev.caecorthus.sparkdlc.client.config.SparkDlcConfigManager;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * The live {@link ScopeSettings}, kept in the one client settings file {@code config/sparkdlc.json}
 * ({@link SparkDlcConfigManager}, which holds the three scope keys beside its own): loaded once from
 * {@link ScopeClient#register()} (or by the first {@code ScopeSettingsEditor} call, whichever comes first). The
 * {@code apply*} setters behind {@link ScopeSettingsEditor} (the SparkDLC settings screen, the only place that edits
 * them) change the live settings and the settings file's keys in memory without writing; the screen's save of that
 * file writes them. Until stage 3 they had a file of their own, {@code config/sparkdlc-client.json}, which is folded
 * into the one file on the first start ({@code client.legacy.LegacyClientSettingsFold}).
 * 实时的 {@link ScopeSettings}，保存在唯一的客户端设置文件 {@code config/sparkdlc.json} 中（{@link SparkDlcConfigManager}，三个开镜键
 * 与其自身的键放在一起）：由 {@link ScopeClient#register()}（或第一次 {@code ScopeSettingsEditor} 调用，以先到者为准）加载一次。
 * {@link ScopeSettingsEditor}（SparkDLC 设置界面，唯一编辑这些设置的地方）背后的 {@code apply*} 方法修改实时设置以及设置文件在内存中的键，
 * 但不写文件；由设置界面保存该文件时写入。第三阶段之前它们有自己的文件 {@code config/sparkdlc-client.json}，首次启动时会并入这一个
 * 文件（{@code client.legacy.LegacyClientSettingsFold}）。
 */
public final class ScopeSettingsStore {
    private static volatile ScopeSettings current = ScopeSettings.DEFAULTS;
    private static boolean loaded;
    /** Test seam ({@link #useStore}); null means the client's settings file. / 测试接缝；null 表示客户端的设置文件。 */
    @Nullable
    private static SparkDlcConfigManager store;

    private ScopeSettingsStore() {
    }

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        current = ScopeSettings.fromJson(store().otherKeys());
    }

    public static ScopeSettings current() {
        return current;
    }

    /**
     * Live-only: the new mode takes effect at once and is written by the next save of the settings file.
     * 仅实时生效：新模式立即生效，由下一次保存设置文件时写入。
     */
    public static void applyMode(ScopeMode mode) {
        apply(settings -> settings.withMode(mode));
    }

    /** Live-only, clamped and snapped like the file; see {@link #applyMode}. / 仅实时生效，钳制与取整同文件。 */
    public static void applySensitivityPercent(int percent) {
        apply(settings -> settings.withSensitivityPercent(percent));
    }

    /** Live-only; see {@link #applyMode}. / 仅实时生效。 */
    public static void applyLensResolutionPercent(int percent) {
        apply(settings -> settings.withLensResolutionPercent(percent));
    }

    /** Sets all three keys in the settings file's memory, so any change goes with the next save. / 三个键一并写入内存。 */
    private static synchronized void apply(UnaryOperator<ScopeSettings> change) {
        ScopeSettings next = change.apply(current);
        if (next.equals(current)) {
            return;
        }
        current = next;
        store().editOtherKeys(next::writeTo);
    }

    /**
     * Test seam: reads and writes {@code testStore} instead (null: the client's settings file again) and forgets the
     * loaded state, so the next {@link #load()} starts afresh.
     * 测试接缝：改为读写 {@code testStore}（null：恢复客户端的设置文件），并清除已加载状态，下一次 {@link #load()} 重新开始。
     */
    static synchronized void useStore(@Nullable SparkDlcConfigManager testStore) {
        store = testStore;
        loaded = false;
        current = ScopeSettings.DEFAULTS;
    }

    private static SparkDlcConfigManager store() {
        SparkDlcConfigManager override = store;
        return override != null ? override : SparkDLCClient.configManager();
    }
}
