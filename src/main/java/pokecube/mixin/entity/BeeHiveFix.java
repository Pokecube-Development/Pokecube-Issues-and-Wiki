package pokecube.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.eventhandlers.PokemobEventsHandler;

@Mixin(BeehiveBlockEntity.class)
public class BeeHiveFix
{
    @Inject(method = "serverTick", at = @At(value = "HEAD"))
    private static void preServerTickFix(Level level, BlockPos pos, BlockState state, BeehiveBlockEntity beehive,
            CallbackInfo ci)
    {
        // At the start of the call, set this true, so any additions this tick are bees
        PokemobEventsHandler.BEE_RELEASE_TICK.add(level.dimension());
    }

    @Inject(method = "serverTick", at = @At(value = "RETURN"))
    private static void postServerTickFix(Level level, BlockPos pos, BlockState state, BeehiveBlockEntity beehive,
            CallbackInfo ci)
    {
        // Now set it false again.
        PokemobEventsHandler.BEE_RELEASE_TICK.remove(level.dimension());
    }
}
