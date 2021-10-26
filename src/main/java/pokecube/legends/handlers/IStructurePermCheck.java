package pokecube.legends.handlers;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public interface IStructurePermCheck
{
    default boolean protectStructure(final ServerPlayerEntity player, final ServerWorld world, final BlockPos pos)
    {
        if(player==null) return true;
        return true;
    }
}
