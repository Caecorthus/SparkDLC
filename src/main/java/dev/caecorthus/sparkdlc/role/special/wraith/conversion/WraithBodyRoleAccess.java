package dev.caecorthus.sparkdlc.role.special.wraith.conversion;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/** SparkWitch-owned corpse snapshot contract; Wathe remains unchanged. */
public interface WraithBodyRoleAccess {
    void sparkdlc$setDeathRole(@Nullable Identifier roleId);

    @Nullable Identifier sparkdlc$getDeathRole();
}
