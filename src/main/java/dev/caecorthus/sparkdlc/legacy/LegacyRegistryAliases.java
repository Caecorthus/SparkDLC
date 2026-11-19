package dev.caecorthus.sparkdlc.legacy;

import dev.caecorthus.sparkdlc.SparkDLC;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Makes every id the five former mods registered in a vanilla registry an alias of the id it has now, so a world
 * saved before the fusion still loads: item stacks, block states, entities, status effects, data component types
 * inside item NBT, particles and sound events stored under {@code sparkstrength:} / {@code sparktraits:} /
 * {@code sparkwitch:} resolve to the {@code sparkdlc:} entry, and are written back under the new id on the next save.
 *
 * <p>The rows come from {@link LegacyIds}: a kind of the table that is named after a vanilla registry
 * ({@code item}, {@code block}, {@code entity_type}, {@code sound_event}, {@code data_component_type},
 * {@code mob_effect}, {@code particle_type}, {@code armor_material}, {@code creative_mode_tab}; any registry kind
 * added to the table later is picked up without a change here). Several old ids may lead to one new id: both old
 * creative tabs lead to the single {@code sparkdlc:items}.</p>
 *
 * <p>Fabric's alias table only exists on registries that are not frozen yet, and static registries freeze right
 * after mod initialisation (registry-sync {@code MainMixin} on a dedicated server, {@code MinecraftClientMixin} on a
 * client). {@link #register()} therefore runs at the end of {@code SparkDLC#onInitialize}, on both sides, after
 * every module has registered its entries. Aliases are local: they are not part of registry sync, so both sides of
 * a connection still need the fused jar.</p>
 *
 * <p>Lookups by {@code RegistryKey} (commands, structure files) need
 * {@code mixin.legacy.SimpleRegistryKeyAliasMixin} on top of this, see there.</p>
 *
 * 把五个旧模组在原版注册表里注册过的每个 id 设为其现有 id 的别名，使融合前保存的世界仍能加载：以 {@code sparkstrength:} /
 * {@code sparktraits:} / {@code sparkwitch:} 保存的物品堆、方块状态、实体、状态效果、物品 NBT 中的数据组件类型、粒子和声音事件
 * 都会解析为 {@code sparkdlc:} 的条目，并在下次保存时以新 id 写回。
 *
 * <p>数据行来自 {@link LegacyIds}：对照表中以原版注册表命名的种类（{@code item}、{@code block}、{@code entity_type}、
 * {@code sound_event}、{@code data_component_type}、{@code mob_effect}、{@code particle_type}、{@code armor_material}、
 * {@code creative_mode_tab}；之后表里新增的注册表种类无需改动此处即可生效）。多个旧 id 可以指向同一个新 id：两个旧创造物品栏
 * 都指向唯一的 {@code sparkdlc:items}。</p>
 *
 * <p>Fabric 的别名表只能在注册表冻结前写入，而静态注册表在模组初始化结束后立即冻结（专用服务器上由 registry-sync 的
 * {@code MainMixin}、客户端由 {@code MinecraftClientMixin} 完成）。因此 {@link #register()} 在两端的
 * {@code SparkDLC#onInitialize} 末尾、所有模块注册完条目之后执行。别名只在本地有效，不参与注册表同步，连接双方仍都需要融合后的 jar。</p>
 *
 * <p>通过 {@code RegistryKey} 的查找（命令、结构文件）还需要 {@code mixin.legacy.SimpleRegistryKeyAliasMixin}，详见该类。</p>
 */
public final class LegacyRegistryAliases {
    private static boolean registered;

    private LegacyRegistryAliases() {
    }

    /**
     * Adds the aliases once. Fails start-up when a table row cannot be honoured: its new id is not registered (the
     * entry was renamed or removed without updating {@code legacy-ids.tsv}; Fabric itself would only log a warning
     * and old saves would silently lose that entry), or its old id is registered again or already aliased elsewhere.
     * 只添加一次别名。对照表中有无法兑现的行时让启动失败：新 id 未注册（条目被改名或删除却没有更新 {@code legacy-ids.tsv}；
     * Fabric 自己只会打印警告，旧存档会悄悄丢失该条目），或旧 id 又被注册、已在别处被设为别名。
     */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        Map<String, Integer> added = new LinkedHashMap<>();
        List<String> broken = new ArrayList<>();
        for (Registry<?> registry : Registries.REGISTRIES) {
            Identifier registryId = registry.getKey().getValue();
            if (!Identifier.DEFAULT_NAMESPACE.equals(registryId.getNamespace())) {
                continue;
            }
            String kind = registryId.getPath();
            for (Map.Entry<String, String> row : LegacyIds.renamed(kind).entrySet()) {
                Identifier oldId = Identifier.of(row.getKey());
                Identifier newId = Identifier.of(row.getValue());
                if (!registry.containsId(newId)) {
                    broken.add(kind + " " + oldId + " -> " + newId + " (new id is not registered)");
                    continue;
                }
                try {
                    registry.addAlias(oldId, newId);
                    added.merge(kind, 1, Integer::sum);
                } catch (RuntimeException exception) {
                    broken.add(kind + " " + oldId + " -> " + newId + " (" + exception.getMessage() + ")");
                }
            }
        }
        if (!broken.isEmpty()) {
            throw new IllegalStateException("SparkDLC cannot alias " + broken.size()
                    + " legacy registry ids of legacy-ids.tsv; old worlds would lose them: " + broken);
        }
        registered = true;
        SparkDLC.LOGGER.debug("Registered legacy registry aliases: {}", added);
    }
}
