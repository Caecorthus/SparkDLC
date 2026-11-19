package dev.caecorthus.sparkdlc.legacy;

import net.minecraft.util.Identifier;

/**
 * Ids an admin types as a command argument. An id in one of the former namespaces
 * ({@code /sparkdlc:addTrait sparktraits:anxiety @p}, also from a command block or a function written before
 * the rename) resolves to the current id through the legacy table, so it gives the same result as typing the
 * current id. Anything else is returned as typed; an old id the table does not know is not guessed.
 *
 * 管理员作为命令参数输入的 id。位于旧命名空间的 id（{@code /sparkdlc:addTrait sparktraits:anxiety @p}，也可能来自改名
 * 之前写好的命令方块或函数）通过对照表解析为当前 id，结果与直接输入当前 id 相同。其他输入原样返回；对照表里没有的旧 id 不做猜测。
 */
public final class LegacyCommandIds {
    private LegacyCommandIds() {
    }

    /** A typed trait id. / 输入的词条 id。 */
    public static Identifier trait(Identifier typed) {
        return LegacyIds.canonical(typed, "trait");
    }

    /** A typed witch skill id. / 输入的魔女技能 id。 */
    public static Identifier skill(Identifier typed) {
        return LegacyIds.canonical(typed, "skill");
    }

    /** A typed role id. / 输入的职业 id。 */
    public static Identifier role(Identifier typed) {
        return LegacyIds.canonical(typed, "role");
    }
}
