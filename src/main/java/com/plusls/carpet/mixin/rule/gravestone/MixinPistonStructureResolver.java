package com.plusls.carpet.mixin.rule.gravestone;

import com.plusls.carpet.util.rule.gravestone.GravesStoneSkullBlockEntity;
import com.plusls.carpet.util.rule.gravestone.GravestoneUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * 玩家头骨在活塞眼里是「推一下就碎」（{@code PushReaction.POPPED}），
 * 所以在 {@code PistonBaseBlock.isPushable} 上返回 false 反而会让活塞把它直接摧毁。
 * 这里改成在解算完成时拦截：只要推/毁列表里出现墓碑，且保护次数还有，
 * 就把列表清空并让 {@code resolve()} 失败，活塞这一下就完全不动。
 */
@Mixin(PistonStructureResolver.class)
public abstract class MixinPistonStructureResolver {
    @Shadow
    @Final
    private Level level;

    @Shadow
    private List<BlockPos> toPush;

    @Shadow
    private List<BlockPos> toDestroy;

    @Inject(
            method = "resolve",
            at = @At(value = "RETURN"),
            cancellable = true
    )
    private void pca$protectGraves(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }

        List<BlockPos> affected = new ArrayList<>(this.toPush);
        affected.addAll(this.toDestroy);

        for (BlockPos pos : affected) {
            if (this.level.getBlockEntity(pos) instanceof GravesStoneSkullBlockEntity gravestone &&
                    gravestone.pca$getDeathInfo() != null) {
                // 保护还有次数 → 活塞完全不动；次数耗尽 → 我们自己结算并移除方块
                // （不走原版摧毁流程，所以不会掉一个玩家头物品）
                if (!GravestoneUtil.protectGrave(this.level, pos, gravestone) &&
                        this.level instanceof ServerLevel serverLevel) {
                    GravestoneUtil.destroyGrave(serverLevel, pos, gravestone);
                }

                this.toPush.clear();
                this.toDestroy.clear();
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
