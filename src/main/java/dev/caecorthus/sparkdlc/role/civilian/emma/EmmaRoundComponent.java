package dev.caecorthus.sparkdlc.role.civilian.emma;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

/** Server-only ledger: no role claims or pending punishments are synchronized to clients.
 * 仅服务端账本：职业声明与待结算惩罚不向客户端同步。 */
public final class EmmaRoundComponent extends EmmaRoundState implements Component {
    public static final ComponentKey<EmmaRoundComponent> KEY = ComponentRegistry.getOrCreate(
            SparkDLC.id("emma_round"), EmmaRoundComponent.class);
    public EmmaRoundComponent(World world) { }
}
