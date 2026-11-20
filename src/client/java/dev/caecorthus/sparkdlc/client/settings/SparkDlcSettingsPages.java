package dev.caecorthus.sparkdlc.client.settings;

import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.client.config.SparkDlcClientSettings;
import dev.caecorthus.sparkdlc.client.scope.ScopeSettingsEditor;
import dev.caecorthus.sparkdlc.config.BloodAppearance;
import dev.caecorthus.sparkdlc.config.ChargeIndicatorStyle;
import dev.caecorthus.sparkdlc.config.SparkDlcClientConfig.GuidebookDecor;
import dev.caecorthus.sparkdlc.config.SparkDlcClientConfig.InstinctKeyMode;
import dev.caecorthus.sparkdlc.sound.EventSoundGroup;
import dev.doctor4t.wathe.WatheConfig.SnowModeConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the pages of the SparkDLC settings screen from the settings the mod's features, Wathe and NoellesRoles
 * expose. Pages and groups with nothing in them are left out.
 * 根据本模组各功能以及 Wathe、NoellesRoles 提供的设置构建 SparkDLC 设置界面的页面。没有内容的页面与分组会被省略。
 */
public final class SparkDlcSettingsPages {
    public static final String GENERAL = "general";
    public static final String AUDIO = "audio";
    public static final String SCOPE = "scope";
    public static final String DISPLAY = "display";

    private static final Logger LOGGER = LoggerFactory.getLogger("SparkDLC/Settings");
    /**
     * The source of a row that is SparkDLC's own setting: none, so the description panel shows no "From ..." line.
     * Only a row whose value or sounds belong to another mod (Wathe, NoellesRoles) names that mod.
     * SparkDLC 自有设置行的来源：无，说明面板不显示「来自……」一行。只有取值或声音属于其他模组（Wathe、NoellesRoles）的行
     * 才标出该模组。
     */
    @Nullable
    private static final Text OWN = null;
    private static final Text WATHE = Text.literal("Wathe");
    /** Serialized name of the Picture-in-Picture scope mode. 画中画开镜模式的序列化名称。 */
    private static final String PICTURE_IN_PICTURE = "picture_in_picture";
    private static final SettingsStore SCOPE_STORE = new SettingsStore("sparkdlc-client", ScopeSettingsEditor::save);

    private SparkDlcSettingsPages() {
    }

    public static List<SettingsPage> build() {
        List<SettingsPage> pages = new ArrayList<>();
        pages.add(general());
        pages.add(audio());
        try {
            pages.add(scope());
        } catch (IllegalArgumentException exception) {
            // The scope settings answered with values their own defaults are not among; leave the page out.
            // 开镜设置返回的取值不包含其默认值时，不显示此页。
            LOGGER.warn("SparkDLC scope settings are inconsistent; hiding the scope page", exception);
        }
        pages.add(display());
        return pages.stream().filter(page -> !page.isEmpty()).toList();
    }

    private static SettingsPage general() {
        ChoiceOption<InstinctKeyMode> instinct = new ChoiceOption<>(
                "instinct_key_mode",
                Text.translatable("option.sparkdlc.instinct_key_mode"),
                Text.translatable("option.sparkdlc.instinct_key_mode.description"),
                OWN,
                SparkDlcClientSettings.STORE,
                List.of(InstinctKeyMode.values()),
                SparkDlcClientSettings::instinctKeyMode,
                SparkDlcClientSettings::setInstinctKeyMode,
                InstinctKeyMode.HOLD,
                mode -> Text.translatable(mode.getTranslationKey()));
        ChoiceOption<GuidebookDecor> decor = new ChoiceOption<>(
                "guidebook_decor",
                Text.translatable("option.sparkdlc.guidebook_decor"),
                Text.translatable("option.sparkdlc.guidebook_decor.description"),
                OWN,
                SparkDlcClientSettings.STORE,
                List.of(GuidebookDecor.values()),
                SparkDlcClientSettings::guidebookDecor,
                SparkDlcClientSettings::setGuidebookDecor,
                GuidebookDecor.MEDIUM,
                decorLevel -> Text.translatable(decorLevel.getTranslationKey()));
        ToggleOption foliage = new ToggleOption(
                "guidebook_foliage",
                Text.translatable("option.sparkdlc.guidebook_foliage"),
                Text.translatable("option.sparkdlc.guidebook_foliage.description"),
                OWN,
                SparkDlcClientSettings.STORE,
                SparkDlcClientSettings::guidebookFoliage,
                SparkDlcClientSettings::setGuidebookFoliage,
                true);
        foliage.enabledWhen(() -> SparkDlcClientSettings.guidebookDecor() != GuidebookDecor.OFF,
                () -> Text.translatable("option.sparkdlc.guidebook_foliage.needs_decor"));
        return new SettingsPage(GENERAL, Text.translatable("screen.sparkdlc.settings.page.general"), List.of(
                new SettingsPage.Group("controls", Text.translatable("screen.sparkdlc.settings.group.controls"),
                        List.of(instinct)),
                new SettingsPage.Group("guidebook", Text.translatable("screen.sparkdlc.settings.group.guidebook"),
                        List.of(decor, foliage))));
    }

    private static SettingsPage audio() {
        List<SettingsPage.Group> groups = new ArrayList<>();
        SliderOption master = new SliderOption(
                "event_volume.master",
                Text.translatable("option.sparkdlc.event_sound_volume.master"),
                Text.translatable("option.sparkdlc.event_sound_volume.master.description"),
                OWN,
                SparkDlcClientSettings.STORE,
                0, 100, 1,
                () -> (int) Math.round(SparkDlcClientSettings.eventSoundMasterVolume() * 100.0D),
                percent -> SparkDlcClientSettings.setEventSoundMasterVolume(percent / 100.0D),
                100,
                SparkDlcSettingsPages::percent);
        groups.add(new SettingsPage.Group("master",
                Text.translatable("screen.sparkdlc.settings.group.event_master"), List.of(master)));

        List<List<EventVolumeRows.Row>> sections = EventVolumeRows.visibleBySection(row ->
                FabricLoader.getInstance().isModLoaded(row.mod()) && preview(row).available());
        EventVolumeRows.Section[] order = EventVolumeRows.Section.values();
        for (int i = 0; i < order.length; i++) {
            List<SettingsOption> volumes = new ArrayList<>();
            for (EventVolumeRows.Row row : sections.get(i)) {
                volumes.add(eventVolume(row));
            }
            groups.add(new SettingsPage.Group(order[i].id(), Text.translatable(order[i].titleKey()), volumes));
        }
        return new SettingsPage(AUDIO, Text.translatable("screen.sparkdlc.settings.page.audio"), groups);
    }

    static SliderOption eventVolume(EventVolumeRows.Row row) {
        EventSoundGroup group = row.group();
        Text sound = Text.translatable(group.translationKey());
        Text label = row.whoKey() == null ? sound : Text.translatableWithFallback(row.whoKey(), row.whoFallback());
        SliderOption option = new SliderOption(
                "event_volume." + group.serializedName(),
                label,
                Text.translatable(group.translationKey() + ".description"),
                modName(row.mod()),
                SparkDlcClientSettings.STORE,
                0, 100, 1,
                () -> (int) Math.round(SparkDlcClientSettings.eventSoundVolume(group) * 100.0D),
                percent -> SparkDlcClientSettings.setEventSoundVolume(group, percent / 100.0D),
                100,
                SparkDlcSettingsPages::percent);
        if (row.whoKey() != null) {
            option.withDetail(sound, row.colour());
        }
        option.withPreview(preview(row));
        return option;
    }

    private static SoundPreview preview(EventVolumeRows.Row row) {
        return new SoundPreview(row.preview().id(), row.preview().category());
    }

    private static SettingsPage scope() {
        ChoiceOption<String> mode = new ChoiceOption<>(
                "sparkdlc.scope_mode",
                Text.translatable("option.sparkdlc.scope_mode"),
                Text.translatable("option.sparkdlc.scope_mode.description"),
                OWN,
                SCOPE_STORE,
                ScopeSettingsEditor.scopeModes(),
                ScopeSettingsEditor::scopeMode,
                ScopeSettingsEditor::setScopeMode,
                ScopeSettingsEditor.defaultScopeMode(),
                name -> Text.translatable("option.sparkdlc.scope_mode." + name));
        mode.describeValues(name -> {
            Text tooltip = Text.translatable("option.sparkdlc.scope_mode." + name + ".tooltip");
            if (PICTURE_IN_PICTURE.equals(name)
                    && ScopeSettingsEditor.pictureInPictureSuppressed()) {
                return Text.empty().append(tooltip).append("\n\n")
                        .append(Text.translatable("option.sparkdlc.scope_mode.suppressed"));
            }
            return tooltip;
        });
        ChoiceOption<Integer> lens = new ChoiceOption<>(
                "sparkdlc.scope_lens_resolution",
                Text.translatable("option.sparkdlc.scope_lens_resolution"),
                Text.translatable("option.sparkdlc.scope_lens_resolution.tooltip"),
                OWN,
                SCOPE_STORE,
                Arrays.stream(ScopeSettingsEditor.lensResolutionChoices()).boxed().toList(),
                ScopeSettingsEditor::lensResolutionPercent,
                ScopeSettingsEditor::setLensResolutionPercent,
                ScopeSettingsEditor.defaultLensResolutionPercent(),
                SparkDlcSettingsPages::percent);
        lens.enabledWhen(() -> PICTURE_IN_PICTURE.equals(ScopeSettingsEditor.scopeMode()),
                () -> Text.translatable("option.sparkdlc.scope_lens_resolution.needs_pip"));
        SliderOption sensitivity = new SliderOption(
                "sparkdlc.scope_sensitivity",
                Text.translatable("option.sparkdlc.scope_sensitivity"),
                Text.translatable("option.sparkdlc.scope_sensitivity.tooltip"),
                OWN,
                SCOPE_STORE,
                ScopeSettingsEditor.scopedSensitivityMin(),
                ScopeSettingsEditor.scopedSensitivityMax(),
                ScopeSettingsEditor.scopedSensitivityStep(),
                ScopeSettingsEditor::scopedSensitivityPercent,
                ScopeSettingsEditor::setScopedSensitivityPercent,
                ScopeSettingsEditor.defaultScopedSensitivityPercent(),
                SparkDlcSettingsPages::percent);
        return new SettingsPage(SCOPE, Text.translatable("screen.sparkdlc.settings.page.scope"), List.of(
                new SettingsPage.Group("scope", Text.translatable("screen.sparkdlc.settings.group.scope"),
                        List.of(mode, lens, sensitivity))));
    }

    private static SettingsPage display() {
        ChoiceOption<SnowModeConfig> snowMode = new ChoiceOption<>(
                "wathe.snow_mode",
                Text.translatable("option.sparkdlc.wathe.snow_mode"),
                Text.translatable("wathe.midnightconfig.snowOptLevel.tooltip"),
                WATHE,
                WatheClientSettings.STORE,
                WatheClientSettings.snowModes(),
                WatheClientSettings::snowMode,
                WatheClientSettings::setSnowMode,
                WatheClientSettings.DEFAULT_SNOW_MODE,
                mode -> Text.translatable("wathe.midnightconfig.enum.SnowModeConfig." + mode.name()));
        SliderOption snowflakes = new SliderOption(
                "wathe.snowflake_chance",
                Text.translatable("option.sparkdlc.wathe.snowflake_amount"),
                Text.translatable("wathe.midnightconfig.snowflakeChance.tooltip"),
                WATHE,
                WatheClientSettings.STORE,
                WatheClientSettings.SNOWFLAKE_CHANCE_MIN, WatheClientSettings.SNOWFLAKE_CHANCE_MAX, 1,
                WatheClientSettings::snowflakeChance,
                WatheClientSettings::setSnowflakeChance,
                WatheClientSettings.DEFAULT_SNOWFLAKE_CHANCE,
                SparkDlcSettingsPages::percent);
        snowflakes.enabledWhen(() -> WatheClientSettings.snowMode() != SnowModeConfig.TURN_OFF,
                () -> Text.translatable("option.sparkdlc.wathe.snowflake_amount.needs_particles"));
        ChoiceOption<ChargeIndicatorStyle> charge = new ChoiceOption<>(
                "charge_indicator",
                Text.translatable("option.sparkdlc.charge_indicator"),
                Text.translatable("option.sparkdlc.charge_indicator.description"),
                OWN,
                SparkDlcClientSettings.STORE,
                List.of(ChargeIndicatorStyle.values()),
                SparkDlcClientSettings::chargeIndicatorStyle,
                SparkDlcClientSettings::setChargeIndicatorStyle,
                ChargeIndicatorStyle.RING_OPEN,
                style -> Text.translatable(style.translationKey()))
                .describeValues(style -> Text.translatable(style.translationKey() + ".description"));
        ToggleOption creative = new ToggleOption(
                "noelles_creative_entries",
                Text.translatable("option.sparkdlc.noelles_creative_entries"),
                Text.translatable("option.sparkdlc.noelles_creative_entries.description"),
                OWN,
                SparkDlcClientSettings.STORE,
                SparkDlcClientSettings::noellesCreativeEntries,
                SparkDlcClientSettings::setNoellesCreativeEntries,
                true);
        ChoiceOption<BloodAppearance> blood = new ChoiceOption<>(
                "blood_appearance",
                Text.translatable("option.sparkdlc.blood_appearance"),
                Text.translatable("option.sparkdlc.blood_appearance.description"),
                OWN,
                SparkDlcClientSettings.STORE,
                List.of(BloodAppearance.values()),
                SparkDlcClientSettings::bloodAppearance,
                SparkDlcClientSettings::setBloodAppearance,
                BloodAppearance.DEFAULT,
                v -> Text.translatable(v.translationKey()));
        ToggleOption scavenger = new ToggleOption(
                "suppress_scavenger_blood",
                Text.translatable("option.sparkdlc.suppress_scavenger_blood"),
                Text.translatable("option.sparkdlc.suppress_scavenger_blood.description"),
                OWN,
                SparkDlcClientSettings.STORE,
                SparkDlcClientSettings::suppressScavengerBlood,
                SparkDlcClientSettings::setSuppressScavengerBlood,
                true);
        List<SettingsPage.Group> groups = new ArrayList<>();
        groups.add(new SettingsPage.Group("snow", Text.translatable("screen.sparkdlc.settings.group.train_effects"),
                List.of(snowMode, snowflakes)));
        groups.add(new SettingsPage.Group("combat", Text.translatable("screen.sparkdlc.settings.group.combat"),
                List.of(charge)));
        // The row files NoellesRoles' items into the creative tabs; NoellesRoles is a hard dependency, so it is always
        // shown. 这一行把 NoellesRoles 的物品收进创造模式标签页；NoellesRoles 是硬依赖，因此始终显示。
        groups.add(new SettingsPage.Group("creative",
                Text.translatable("screen.sparkdlc.settings.group.creative"), List.of(creative)));
        groups.add(new SettingsPage.Group("blood", Text.translatable("screen.sparkdlc.settings.group.blood"),
                List.of(blood, scavenger)));
        return new SettingsPage(DISPLAY, Text.translatable("screen.sparkdlc.settings.page.display"), groups);
    }

    /**
     * The mod named under a volume row's description, or null for a row of SparkDLC itself. {@code modId} may be one
     * of the mod ids SparkDLC provides: the loader answers each of them with the SparkDLC container, and such a row
     * gets no "From ..." line.
     * 音量行说明下方标出的模组；SparkDLC 自己的行为 null。{@code modId} 可以是 SparkDLC 通过 provides 提供的 mod id：
     * 加载器对它们都返回 SparkDLC 容器，这样的行不显示「来自……」。
     */
    @Nullable
    private static Text modName(String modId) {
        Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(modId);
        String name = sourceName(modId,
                container.map(found -> found.getMetadata().getId()).orElse(null),
                container.map(found -> found.getMetadata().getName()).orElse(null));
        return name == null ? null : Text.literal(name);
    }

    /**
     * The words after "From" for a row of mod {@code modId}, given the container the loader resolved it to: nothing
     * for SparkDLC's own container, otherwise the container's display name, or the bare id when no container answers.
     * 模组 {@code modId} 的行在「来自」之后显示的文字：解析到 SparkDLC 自己的容器时不显示，否则为容器显示名；没有容器
     * 应答时退回 id 本身。
     */
    @Nullable
    static String sourceName(String modId, @Nullable String containerId, @Nullable String containerName) {
        if (SparkDLC.MOD_ID.equals(containerId)) {
            return null;
        }
        return containerName != null ? containerName : modId;
    }

    static Text percent(int value) {
        return Text.translatable("screen.sparkdlc.settings.percent", String.format(Locale.ROOT, "%d", value));
    }
}
