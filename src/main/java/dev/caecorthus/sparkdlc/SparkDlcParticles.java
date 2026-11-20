package dev.caecorthus.sparkdlc;

import dev.caecorthus.sparkdlc.SparkDLC;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

/** Registers SparkTraits custom particle types.
 * 注册 SparkTraits 自定义粒子类型。 */
public final class SparkDlcParticles {
    public static final SimpleParticleType BLUE_POISON = Registry.register(
            Registries.PARTICLE_TYPE,
            SparkDLC.id("blue_poison"),
            FabricParticleTypes.simple(true)
    );

    private SparkDlcParticles() {
    }

    public static void register() {
        // Static initializer performs the registration.
        // 静态初始化器会完成注册。
    }
}
