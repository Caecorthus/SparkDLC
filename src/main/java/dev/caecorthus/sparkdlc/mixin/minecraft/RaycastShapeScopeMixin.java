package dev.caecorthus.sparkdlc.mixin.minecraft;

import dev.caecorthus.sparkdlc.util.RaycastShapeScope;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;

/**
 * The mod's single wrapper around ray block-shape queries, on both logical sides (it was three nested wrappers, one per
 * former mod, each entering its own copy of the scope). It keeps the ray's own entity shape context, so other mods'
 * entity-sensitive ray geometry is unchanged; door-passing mixins read the scope instead. {@code @WrapMethod} encloses
 * every other injector on the method, so everything that runs inside {@code getBlockShape} sees the scope.
 * 本 mod 唯一的射线方块形状查询包装，双端生效（此前是三层嵌套包装，每个旧 mod 一层，各自进入自己的作用域副本）。保留射线自身
 * 的实体形状上下文，其他模组按实体区分的射线几何不变；穿门 mixin 改为读取该作用域。{@code @WrapMethod} 包在该方法的所有其他
 * 注入之外，因此 {@code getBlockShape} 内执行的一切都能看到作用域。
 */
@Mixin(RaycastContext.class)
public abstract class RaycastShapeScopeMixin {
    @WrapMethod(method = "getBlockShape")
    private VoxelShape sparkdlc$scopeRayBlockShape(BlockState state, BlockView world, BlockPos pos,
                                                        Operation<VoxelShape> original) {
        // Runs at every block step of every ray: the scope is entered without a lambda and left in finally.
        // 每条射线的每个方块步进都会执行：作用域不经 lambda 进入，并在 finally 中退出。
        int[] scope = RaycastShapeScope.enter();
        try {
            return original.call(state, world, pos);
        } finally {
            RaycastShapeScope.exit(scope);
        }
    }
}
