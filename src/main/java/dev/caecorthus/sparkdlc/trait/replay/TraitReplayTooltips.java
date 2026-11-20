package dev.caecorthus.sparkdlc.trait.replay;

import dev.caecorthus.sparkdlc.replay.api.ReplayBadge;
import dev.caecorthus.sparkdlc.replay.api.ReplayPlayerView;
import dev.caecorthus.sparkdlc.replay.api.SparkReplayApi;
import dev.caecorthus.sparkdlc.SparkDLC;
import dev.caecorthus.sparkdlc.trait.api.SparkTraitsApi;
import dev.caecorthus.sparkdlc.trait.api.Trait;
import dev.caecorthus.sparkdlc.trait.api.TraitRegistry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Adds each participant's round-end traits to the faction core's replay hover tooltip and, as coloured badges, to
 * their card in the replay screen.
 * Read-only: runs during replay generation before Wathe resets players, and never throws into the replay.
 * Hidden-at-start traits are shown on purpose, because the round is over.
 * 将每名参与者的本局最终词条追加到阵营核心的回放悬停提示，并以彩色标签显示在回放界面的玩家卡片上。只读：在 Wathe 重置玩家前的回放生成期间运行，
 * 不向回放抛出异常。开局隐藏的词条在局末同样展示（对局已结束）。
 */
public final class TraitReplayTooltips {
    static final Identifier CONTRIBUTOR_ID = SparkDLC.id("traits");
    static final String HEADER_KEY = "replay.sparkdlc.tooltip.traits";
    static final String NONE_KEY = "replay.sparkdlc.tooltip.none";
    static final String HIDDEN_AT_START_KEY = "replay.sparkdlc.tooltip.hidden_at_start";

    private TraitReplayTooltips() {
    }

    public static void register() {
        SparkReplayApi.registerPlayerTooltipContributor(CONTRIBUTOR_ID, TraitReplayTooltips::contribute);
        SparkReplayApi.registerPlayerBadgeContributor(CONTRIBUTOR_ID, TraitReplayTooltips::contributeBadges);
    }

    static void contributeBadges(ReplayPlayerView player, Consumer<ReplayBadge> badges) {
        try {
            List<Identifier> traitIds = SparkTraitsApi.getRoundEndTraitIds(player.world(), player.uuid());
            badges(traitIds, TraitRegistry::get).forEach(badges);
        } catch (RuntimeException exception) {
            SparkDLC.LOGGER.warn("Unable to build trait replay badges", exception);
        }
    }

    /**
     * One badge per known trait in resolver order: trait name in the trait colour, description as the badge tooltip.
     * 按解析顺序每个已知词条一个标签：词条名使用词条颜色，描述作为标签悬停提示。
     */
    static List<ReplayBadge> badges(List<Identifier> traitIds, Function<Identifier, Trait> lookup) {
        List<ReplayBadge> badges = new ArrayList<>(traitIds.size());
        for (Identifier traitId : traitIds) {
            try {
                Trait trait = lookup.apply(traitId);
                if (trait == null) {
                    continue;
                }
                MutableText tooltip = trait.description().copy().formatted(Formatting.GRAY);
                if (trait.hiddenFromOwnerAtStart()) {
                    tooltip.append(Text.translatable(HIDDEN_AT_START_KEY).formatted(Formatting.DARK_GRAY));
                }
                badges.add(ReplayBadge.of(trait.name(), trait.color(), tooltip));
            } catch (RuntimeException exception) {
                SparkDLC.LOGGER.debug("Skipping trait {} that failed to render as a replay badge", traitId, exception);
            }
        }
        return List.copyOf(badges);
    }

    static void contribute(ReplayPlayerView player, Consumer<Text> lines) {
        try {
            List<Identifier> traitIds = SparkTraitsApi.getRoundEndTraitIds(player.world(), player.uuid());
            tooltipLines(traitIds, TraitRegistry::get).forEach(lines);
        } catch (RuntimeException exception) {
            SparkDLC.LOGGER.warn("Unable to build trait replay tooltip", exception);
        }
    }

    static List<Text> tooltipLines(List<Identifier> traitIds, Function<Identifier, Trait> lookup) {
        List<Text> traitLines = new ArrayList<>();
        for (Identifier traitId : traitIds) {
            Text line = traitLine(traitId, lookup);
            if (line != null) {
                traitLines.add(line);
            }
        }

        MutableText header = Text.translatable(HEADER_KEY).formatted(Formatting.GRAY);
        if (traitLines.isEmpty()) {
            return List.of(header.append(Text.translatable(NONE_KEY)));
        }
        List<Text> result = new ArrayList<>(traitLines.size() + 1);
        result.add(header);
        result.addAll(traitLines);
        return List.copyOf(result);
    }

    private static @Nullable Text traitLine(Identifier traitId, Function<Identifier, Trait> lookup) {
        try {
            Trait trait = lookup.apply(traitId);
            if (trait == null) {
                SparkDLC.LOGGER.debug("Skipping unknown trait {} in replay tooltip", traitId);
                return null;
            }
            MutableText line = Text.literal("  ")
                    .append(Text.literal("[").append(trait.name()).append("]").withColor(trait.color() & 0xFFFFFF))
                    .append(" ")
                    .append(trait.description().copy().formatted(Formatting.GRAY));
            if (trait.hiddenFromOwnerAtStart()) {
                line.append(Text.translatable(HIDDEN_AT_START_KEY).formatted(Formatting.DARK_GRAY));
            }
            return line;
        } catch (RuntimeException exception) {
            SparkDLC.LOGGER.debug("Skipping trait {} that failed to render in replay tooltip", traitId, exception);
            return null;
        }
    }
}
