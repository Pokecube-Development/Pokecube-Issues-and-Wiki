package thut.mixin.entity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thut.api.entity.multipart.IMultpart;

import java.util.Set;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMultiPartMobs extends NodeEvaluator
{
    @Unique
    private boolean thutcore$needProcessing;

    @Inject(method = "prepare", at = @At(value = "RETURN"))
    public void thutcore$prepare(PathNavigationRegion level, Mob mob, CallbackInfo ci)
    {
        if (this.entityWidth * this.entityDepth > 4)
        {
            this.entityWidth = this.entityDepth = 2;
            thutcore$needProcessing = true;
        }
    }

    @Inject(method = "getPathTypeWithinMobBB", at = @At(value = "HEAD"))
    public void thutcore$getPathTypeWithinMobBB(PathfindingContext context, int x, int y, int z,
            CallbackInfoReturnable<Set<PathType>> cir)
    {
        // If this is the case, it is better to compute this for each one individually.
        if (this.mob instanceof IMultpart<?, ?> multpart && thutcore$needProcessing)
        {

        }
    }
}
