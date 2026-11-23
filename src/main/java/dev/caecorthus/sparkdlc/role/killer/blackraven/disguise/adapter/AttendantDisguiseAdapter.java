package dev.caecorthus.sparkdlc.role.killer.blackraven.disguise.adapter;

import dev.caecorthus.sparkdlc.role.civilian.attendant.AttendantFlashlightService;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Disguise parity surface of the Attendant: kit only (the NoellesRoles room book plus the Attendant's starter
 * flashlight, {@code sparkdlc:flashlight}). The Attendant's round-start Spare Key
 * ({@code item/sparekey/SpareKeyAttendantKit}) is deliberately not part of the disguise kit.
 * 乘务员的伪装对等面：仅开局物品（诺艾尔职业房间手册，以及乘务员的开局手电筒 {@code sparkdlc:flashlight}）。
 * 乘务员开局的备用钥匙（{@code item/sparekey/SpareKeyAttendantKit}）有意不计入伪装物品。
 */
public final class AttendantDisguiseAdapter implements BlackRavenDisguiseAdapter {
    public static final Identifier ROLE_ID = Identifier.of("noellesroles", "attendant");

    @Override
    public Identifier roleId() {
        return ROLE_ID;
    }

    @Override
    public List<ItemStack> firstKit(ServerPlayerEntity player) {
        List<ItemStack> kit = new ArrayList<>(2);
        kit.add(AttendantBookFactory.create(player));
        // The Attendant's own duplicate guard: skip when a flashlight is already carried.
        // 乘务员自身的防重复检查：已携带手电筒时不再发放。
        if (!AttendantFlashlightService.hasFlashlight(player)) {
            kit.add(AttendantFlashlightService.starterFlashlight());
        }
        return List.copyOf(kit);
    }
}
