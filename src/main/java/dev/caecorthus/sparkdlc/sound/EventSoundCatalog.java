package dev.caecorthus.sparkdlc.sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Identifier;

/**
 * Catalog Implementation for the event sounds of Wathe, NoellesRoles and the mod itself.
 * Wathe、NoellesRoles 与本模组自身事件声音的目录实现。
 *
 * Dotted event ids and slash resource ids stay separate here, while
 * EventSoundVolumeRules remains the stable caller-facing lookup Interface.
 * 点号事件 id 和斜杠资源 id 在这里分列维护，EventSoundVolumeRules 保持稳定的调用方查询接口。
 */
final class EventSoundCatalog {
    private final Map<Identifier, EventSoundGroup> eventIdGroups;
    private final Map<Identifier, EventSoundGroup> resourceIdGroups;

    private EventSoundCatalog(List<CatalogGroup> groups) {
        Map<Identifier, EventSoundGroup> eventIdGroups = new HashMap<>();
        Map<Identifier, EventSoundGroup> resourceIdGroups = new HashMap<>();
        for (CatalogGroup group : groups) {
            group.indexInto(eventIdGroups, resourceIdGroups);
        }
        validateCrossFamilyOwnership(eventIdGroups, resourceIdGroups);
        this.eventIdGroups = Map.copyOf(eventIdGroups);
        this.resourceIdGroups = Map.copyOf(resourceIdGroups);
    }

    static EventSoundCatalog createDefault() {
        return new EventSoundCatalog(List.of(
                group(EventSoundGroup.PSYCHO_MODE,
                        eventIds("wathe",
                                "ambient.psycho_drone"
                        ),
                        resourceIds("wathe",
                                "ambient/psycho_drone"
                        )
                ),
                group(EventSoundGroup.CORRUPT_COP_MOMENT,
                        eventIds("noellesroles",
                                "ambient.corrupt_cop_execution",
                                "music.corrupt_cop_moment_1",
                                "music.corrupt_cop_moment_2"
                        ),
                        resourceIds("noellesroles",
                                "ambient/manba_out",
                                "ambient/kemiao",
                                "ambient/boyi",
                                "ambient/donk"
                        )
                ),
                group(EventSoundGroup.JESTER_MOMENT,
                        eventIds("noellesroles",
                                "ambient.jester_laugh",
                                "music.jester_moment"
                        ),
                        resourceIds("noellesroles",
                                "ambient/jester_laugh",
                                "ambient/dbd_jester"
                        )
                ),
                group(EventSoundGroup.TRAIN_INSIDE,
                        eventIds("wathe",
                                "ambient.train.inside"
                        ),
                        resourceIds("wathe",
                                "ambient/train_inside"
                        )
                ),
                // The ship map's outside loop is the same kind of sound as the train's, not a horn.
                // 船图的车外循环音与列车车外音是同一类声音，不是汽笛。
                group(EventSoundGroup.TRAIN_OUTSIDE,
                        eventIds("wathe",
                                "ambient.train.outside",
                                "ambient.ship.outside"
                        ),
                        resourceIds("wathe",
                                "ambient/train_outside",
                                "ambient/ship_outside"
                        )
                ),
                group(EventSoundGroup.TRAIN_HORN,
                        eventIds("wathe",
                                "ambient.train.horn"
                        ),
                        resourceIds("wathe",
                                "ambient/train_horn"
                        )
                ),
                group(EventSoundGroup.BLACKOUT,
                        eventIds("wathe",
                                "ambient.blackout"
                        ),
                        resourceIds("wathe",
                                "ambient/blackout"
                        )
                ),
                group(EventSoundGroup.ROUND_MUSIC,
                        eventIds("wathe",
                                "ui.riser",
                                "ui.piano",
                                "ui.piano_stinger",
                                "ui.piano_win",
                                "ui.piano_lose"
                        ),
                        resourceIds("wathe",
                                "ui/riser",
                                "ui/piano",
                                "ui/piano_stinger",
                                "ui/piano_win",
                                "ui/piano_lose"
                        )
                ),
                group(EventSoundGroup.SHADOW_JESTER_FINALE,
                        eventIds("sparkdlc",
                                "ambient.shadow_jester"
                        ),
                        resourceIds("sparkdlc",
                                "ambient/shadow_jester"
                        )
                ),
                group(EventSoundGroup.VULTURE_CURSE,
                        eventIds("sparkdlc",
                                "vulture.super_curse",
                                "vulture.super_curse_helium",
                                "vulture.death_scream",
                                "vulture.death_scream_helium"
                        ),
                        resourceIds("sparkdlc",
                                "vulture/super_curse",
                                "vulture/super_curse_helium",
                                "vulture/death_scream",
                                "vulture/death_scream_helium_1",
                                "vulture/death_scream_helium_2"
                        )
                ),
                group(EventSoundGroup.TIME_STEALER_CHIME,
                        eventIds("sparkdlc",
                                "ambient.time_stealer_chime",
                                "ambient.time_stealer_final"
                        ),
                        resourceIds("sparkdlc",
                                "ambient/time_stealer_chime",
                                "ambient/time_stealer_final"
                        )
                ),
                group(EventSoundGroup.HOLY_FLASH_TINNITUS,
                        eventIds("sparkdlc",
                                "skill.holy_flash_tinnitus"
                        ),
                        resourceIds("sparkdlc",
                                "skill/holy_flash_tinnitus"
                        )
                ),
                group(EventSoundGroup.SHIELD_CLANG,
                        eventIds("wathe",
                                "item.psycho.armour"
                        ),
                        resourceIds("wathe",
                                "item/psycho_armour"
                        )
                ),
                group(EventSoundGroup.SAINT_BELL,
                        eventIds("sparkdlc",
                                "ambient.saint_bell"
                        ),
                        resourceIds("sparkdlc",
                                "ambient/saint_bell"
                        )
                ),
                group(EventSoundGroup.PIG_CHASE,
                        eventIds("sparkdlc",
                                "skill.pig_chase"
                        ),
                        resourceIds("sparkdlc",
                                "skill/pig_chase"
                        )
                ),
                group(EventSoundGroup.ARROGANT_ASF_MUSIC,
                        eventIds("sparkdlc",
                                "music.takediskrush"
                        ),
                        resourceIds("sparkdlc",
                                "music/takediskrush"
                        )
                ),
                group(EventSoundGroup.GRAND_WITCH_CEREMONIAL_SWORD_BGM,
                        eventIds("sparkdlc",
                                "ambient.grand_witch_ceremonial_sword_bgm"
                        ),
                        resourceIds("sparkdlc",
                                "ambient/grand_witch_ceremonial_sword_bgm"
                        )
                ),
                // The toll plays vanilla bell audio; only the Bell Ringer's event id is cataloged so ordinary bells
                // keep their volume.
                // 丧钟复用原版钟声音频；这里只登记敲钟人的事件 id，普通钟声的音量保持不变。
                group(EventSoundGroup.BELL_RINGER_TOLL,
                        eventIds("sparkdlc",
                                "ambient.bell_ringer_toll"
                        ),
                        List.of()
                ),
                group(EventSoundGroup.DEPRESSION_PSYCHO_RANGE,
                        eventIds("sparkdlc",
                                "depression.docile_to_rage",
                                "depression.rage_loop",
                                "depression.melee_kill_1",
                                "depression.melee_kill_2",
                                "depression.rage_to_docile",
                                "depression.shyguy_killed"
                        ),
                        resourceIds("sparkdlc",
                                "depression/docile_to_rage",
                                "depression/rage_loop",
                                "depression/melee_kill_1",
                                "depression/melee_kill_2",
                                "depression/rage_to_docile",
                                "depression/shyguy_killed"
                        )
                ),
                group(EventSoundGroup.DEPRESSION_PSYCHO_MUSIC,
                        eventIds("sparkdlc",
                                "depression.blind_rage_enrage",
                                "depression.blind_rage_chase"
                        ),
                        resourceIds("sparkdlc",
                                "depression/blind_rage_enrage",
                                "depression/blind_rage_chase"
                        )
                ),
                group(EventSoundGroup.DEPRESSION_PSYCHO_ALERT,
                        eventIds("sparkdlc",
                                "depression.player_was_seen"
                        ),
                        resourceIds("sparkdlc",
                                "depression/player_was_seen"
                        )
                ),
                group(EventSoundGroup.LAST_STAND_FINAL_MOMENT,
                        eventIds("sparkdlc",
                                "music.final_moment"
                        ),
                        resourceIds("sparkdlc",
                                "music/final_moment"
                        )
                ),
                // The server plays this 10 s clip for every client during the Sphinx Hao's 豪气冲天 (AMBIENT, not
                // attenuated); only this id is cataloged, so the mod's other music keeps its volume.
                // 服务端在狮身豪「豪气冲天」期间为所有客户端播放这段 10 秒片段（AMBIENT、无距离衰减）；只登记这个 id，
                // 本模组其他音乐的音量不受影响。
                group(EventSoundGroup.SPHINX_HAO_FADE,
                        eventIds("sparkdlc",
                                "music.sphinx_hao_fade"
                        ),
                        resourceIds("sparkdlc",
                                "music/sphinx_hao_fade"
                        )
                )
        ));
    }

    EventSoundGroup groupFor(Identifier id) {
        if (id == null) {
            return null;
        }
        EventSoundGroup eventGroup = eventIdGroups.get(id);
        if (eventGroup != null) {
            return eventGroup;
        }
        return resourceIdGroups.get(id);
    }

    private static CatalogGroup group(
            EventSoundGroup group,
            List<Identifier> eventIds,
            List<Identifier> resourceIds
    ) {
        return new CatalogGroup(group, eventIds, resourceIds);
    }

    private static List<Identifier> eventIds(String namespace, String... paths) {
        return identifiers(namespace, paths);
    }

    private static List<Identifier> resourceIds(String namespace, String... paths) {
        return identifiers(namespace, paths);
    }

    private static List<Identifier> identifiers(String namespace, String... paths) {
        List<Identifier> ids = new ArrayList<>(paths.length);
        for (String path : paths) {
            ids.add(Identifier.of(namespace, path));
        }
        return List.copyOf(ids);
    }

    /**
     * Keeps every catalog identifier owned by exactly one event group.
     * 保证目录中的每个标识符只归属于一个事件声音分组。
     */
    static void putUnique(
            Map<Identifier, EventSoundGroup> index,
            Identifier id,
            EventSoundGroup group,
            String identifierKind
    ) {
        EventSoundGroup existing = index.putIfAbsent(id, group);
        if (existing != null) {
            throw new IllegalArgumentException(
                    "Duplicate " + identifierKind + " sound identifier " + id
                            + " is owned by both " + existing + " and " + group
            );
        }
    }

    static void validateCrossFamilyOwnership(
            Map<Identifier, EventSoundGroup> eventIdGroups,
            Map<Identifier, EventSoundGroup> resourceIdGroups
    ) {
        eventIdGroups.forEach((id, eventGroup) -> {
            EventSoundGroup resourceGroup = resourceIdGroups.get(id);
            if (resourceGroup != null && resourceGroup != eventGroup) {
                throw new IllegalArgumentException(
                        "Sound identifier " + id + " is owned by both " + eventGroup + " and " + resourceGroup
                );
            }
        });
    }

    private record CatalogGroup(
            EventSoundGroup group,
            List<Identifier> eventIds,
            List<Identifier> resourceIds
    ) {
        private CatalogGroup {
            eventIds = List.copyOf(eventIds);
            resourceIds = List.copyOf(resourceIds);
        }

        private void indexInto(
                Map<Identifier, EventSoundGroup> eventIdGroups,
                Map<Identifier, EventSoundGroup> resourceIdGroups
        ) {
            for (Identifier eventId : eventIds) {
                putUnique(eventIdGroups, eventId, group, "event");
            }
            for (Identifier resourceId : resourceIds) {
                putUnique(resourceIdGroups, resourceId, group, "resource");
            }
        }
    }
}
