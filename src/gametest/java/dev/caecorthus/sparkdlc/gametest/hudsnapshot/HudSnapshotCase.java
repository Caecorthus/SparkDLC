package dev.caecorthus.sparkdlc.gametest.hudsnapshot;

import dev.caecorthus.sparkdlc.health.BodyPart;
import dev.doctor4t.wathe.api.Role;
import dev.doctor4t.wathe.api.WatheRoles;
import dev.doctor4t.wathe.cca.PlayerMoodComponent.Task;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One picture of {@code ./gradlew runHudSnapshot}: the state the local player is put into before the screenshot.
 * {@link #ALL} is the whole list; add a line there to get another picture. {@code -PhudSnapshotCases=a,b} picks a
 * subset by {@link #name}.
 * {@code ./gradlew runHudSnapshot} 的一张图片：截图前本地玩家被置入的状态。{@link #ALL} 是完整列表；在那里加一行即可多出一张图片。
 * {@code -PhudSnapshotCases=a,b} 按 {@link #name} 选取其中一部分。
 *
 * @param name       file name of the picture, without extension / 图片文件名（不含扩展名）
 * @param role       the Wathe role of the player; its mood type decides civilian (REAL) or killer (FAKE) drawing /
 *                   玩家的 Wathe 身份；其心情类型决定按平民（REAL）还是杀手（FAKE）绘制
 * @param mood       mood value, {@code -1..1}; only a REAL role has one (Wathe reports 1 for the others) /
 *                   心情值，{@code -1..1}；只有 REAL 身份才有（其余身份 Wathe 一律报告 1）
 * @param tasks      the active mood tasks, at most one per kind, in the order of their rows from top to bottom /
 *                   进行中的心情任务，每种至多一个，按其行自上而下的顺序
 * @param healthMode the server-wide body-health switch / 全服部位血量开关
 * @param health     health per {@link BodyPart}, in declaration order / 各 {@link BodyPart} 的血量，按声明顺序
 * @param psycho     whether Wathe's psycho mode is active on the player / 玩家是否处于 Wathe 的狂暴模式
 */
record HudSnapshotCase(String name, Role role, float mood, List<Task> tasks, boolean healthMode, int[] health,
        boolean psycho) {

    /**
     * Every case, in the order they are shot. Health values are head, chest, abdomen, left arm, right arm, left leg,
     * right leg.
     * 全部用例，按拍摄顺序。血量依次为头、胸、腹、左臂、右臂、左腿、右腿。
     */
    static final List<HudSnapshotCase> ALL = List.of(
            civilian("civilian_mixed", 0.87f, Task.SLEEP, Task.EAT, Task.DRINK).withHealth(35, 85, 22, 0, 60, 65, 40),
            civilian("civilian_full", 1.0f, Task.EAT),
            civilian("civilian_low_mood", 0.15f, Task.OUTSIDE, Task.DRINK).withHealthPercent(100, 84, 68, 52, 36, 20, 5),
            // Below zero Wathe draws its breakdown warning; it kills only at -1. / 低于零时 Wathe 显示崩溃警告；到 -1 才致死。
            civilian("civilian_breakdown", -0.5f, Task.SLEEP, Task.EAT, Task.DRINK).withHealth(35, 85, 22, 0, 60, 65, 40),
            civilian("health_mode_off", 0.41f, Task.SLEEP, Task.DRINK).withHealthMode(false),
            killer("killer", Task.EAT, Task.OUTSIDE),
            killer("psycho").withPsycho(true),
            civilian("no_tasks", 0.7f));

    private static HudSnapshotCase civilian(String name, float mood, Task... tasks) {
        return new HudSnapshotCase(name, WatheRoles.CIVILIAN, mood, List.of(tasks), true, fullHealth(), false);
    }

    /** A killer's tasks are the ones it may pretend to do. / 杀手的任务是它可以假装去做的任务。 */
    private static HudSnapshotCase killer(String name, Task... tasks) {
        return new HudSnapshotCase(name, WatheRoles.KILLER, 1.0f, List.of(tasks), true, fullHealth(), false);
    }

    private HudSnapshotCase withHealth(int... values) {
        if (values.length != BodyPart.values().length) {
            throw new IllegalArgumentException(name + ": one health value per body part, got " + values.length);
        }
        return new HudSnapshotCase(name, role, mood, tasks, healthMode, values.clone(), psycho);
    }

    /** Health as a percentage of each part's own maximum, at least 1. / 以各部位自身上限的百分比给出血量，至少为 1。 */
    private HudSnapshotCase withHealthPercent(int... percents) {
        BodyPart[] parts = BodyPart.values();
        int[] values = new int[percents.length];
        for (int i = 0; i < percents.length && i < parts.length; i++) {
            values[i] = Math.max(1, Math.round(parts[i].maxHealth() * percents[i] / 100.0f));
        }
        return withHealth(values);
    }

    private HudSnapshotCase withHealthMode(boolean enabled) {
        return new HudSnapshotCase(name, role, mood, tasks, enabled, health, psycho);
    }

    private HudSnapshotCase withPsycho(boolean active) {
        return new HudSnapshotCase(name, role, mood, tasks, healthMode, health, active);
    }

    private static int[] fullHealth() {
        BodyPart[] parts = BodyPart.values();
        int[] values = new int[parts.length];
        for (BodyPart part : parts) {
            values[part.ordinal()] = part.maxHealth();
        }
        return values;
    }

    int health(BodyPart part) {
        return health[part.ordinal()];
    }

    boolean realMood() {
        return role.getMoodType() == Role.MoodType.REAL;
    }

    /**
     * The cases named by a comma-separated list, in the list's order; blank selects every case. An unknown name is an
     * error that lists the known ones.
     * 逗号分隔列表所点名的用例，按列表顺序；留空则选择全部用例。未知的名字是错误，错误信息会列出已知的名字。
     */
    static List<HudSnapshotCase> select(String names) {
        if (names == null || names.isBlank()) {
            return ALL;
        }
        Map<String, HudSnapshotCase> byName = new LinkedHashMap<>();
        for (HudSnapshotCase snapshotCase : ALL) {
            byName.put(snapshotCase.name(), snapshotCase);
        }
        List<HudSnapshotCase> selected = new ArrayList<>();
        for (String name : names.split(",")) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            HudSnapshotCase snapshotCase = byName.get(trimmed);
            if (snapshotCase == null) {
                throw new IllegalArgumentException("unknown case '" + trimmed + "'; known cases: " + byName.keySet());
            }
            if (!selected.contains(snapshotCase)) {
                selected.add(snapshotCase);
            }
        }
        if (selected.isEmpty()) {
            throw new IllegalArgumentException("no case selected by '" + names + "'; known cases: " + byName.keySet());
        }
        return selected;
    }
}
