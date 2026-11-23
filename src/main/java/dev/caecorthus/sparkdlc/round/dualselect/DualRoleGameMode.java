package dev.caecorthus.sparkdlc.round.dualselect;

import dev.doctor4t.wathe.game.gamemode.MurderGameMode;
import net.minecraft.util.Identifier;

/**
 * The dual-role murder mode: identical to the classic murder round, except that every eligible player is offered a
 * second role of the same faction before the round actually starts.
 * <p>
 * 双角色谋杀模式：与经典谋杀一局完全一致，差别只在于开局前会给每名符合条件的玩家再发一个同阵营身份供其二选一。
 */
public class DualRoleGameMode extends MurderGameMode {
    public DualRoleGameMode(Identifier identifier) {
        super(identifier);
    }
}
