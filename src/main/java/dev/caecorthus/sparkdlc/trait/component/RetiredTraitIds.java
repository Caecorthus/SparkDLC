package dev.caecorthus.sparkdlc.trait.component;

import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;

/** Filters exact legacy trait ids at every component-state ingress.
 *  在组件状态入口过滤精确匹配的旧天赋 id。 */
final class RetiredTraitIds {
    // The ids as old saves hold them: the literal legacy namespace on purpose, not the mod's id helper.
    // 旧存档中保存的 id：有意写死旧命名空间，而不使用本模组的 id 辅助方法。
    static final Identifier ARROGANT_ASF = Identifier.of("sparktraits", "arrogant_asf");
    static final Identifier WRAITH = Identifier.of("sparktraits", "wraith");
    static final Identifier CHAMELEON = Identifier.of("sparktraits", "chameleon");

    private RetiredTraitIds() {
    }

    static boolean isRetired(Identifier id) {
        return ARROGANT_ASF.equals(id) || WRAITH.equals(id) || CHAMELEON.equals(id);
    }

    static List<Identifier> filter(Collection<Identifier> ids) {
        return List.copyOf(ids.stream().filter(id -> !isRetired(id)).toList());
    }
}
