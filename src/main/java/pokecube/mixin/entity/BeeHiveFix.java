package pokecube.mixin.entity;

import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.eventhandlers.PokemobEventsHandler;

@Mixin(BeehiveBlockEntity.class)
public class BeeHiveFix
{
    @Inject(method = "releaseOccupant", at = @At(value = "HEAD"))
    private static void preServerTickFix(Level level, BlockPos pos, BlockState state, BeehiveBlockEntity.BeeData data,
            @Nullable List<Entity> list, BeehiveBlockEntity.BeeReleaseStatus status, @Nullable BlockPos pos2,
            CallbackInfoReturnable<Boolean> ci)
    {
        // At the start of the call, set this true, so any additions this tick
        // are bees
        PokemobEventsHandler.BEE_RELEASE_TICK.add(level.dimension());
    }

    @Inject(method = "releaseOccupant", at = @At(value = "RETURN"))
    private static void postServerTickFix(Level level, BlockPos pos, BlockState state, BeehiveBlockEntity.BeeData data,
            @Nullable List<Entity> list, BeehiveBlockEntity.BeeReleaseStatus status, @Nullable BlockPos pos2,
            CallbackInfoReturnable<Boolean> ci)
    {
        // Now set it false if we had returned false.
        if (!ci.getReturnValueZ()) PokemobEventsHandler.BEE_RELEASE_TICK.remove(level.dimension());
    }
}
