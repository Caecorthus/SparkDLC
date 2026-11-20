package dev.caecorthus.sparkdlc.role.special.wraith.conversion;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/** Corpse snapshot contract owned by this mod; Wathe remains unchanged. */
public interface WraithBodyRoleAccess {
    void sparkdlc$setDeathRole(@Nullable Identifier roleId);

    @Nullable Identifier sparkdlc$getDeathRole();
}
