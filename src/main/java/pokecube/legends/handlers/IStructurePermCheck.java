package pokecube.legends.handlers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface IStructurePermCheck
{
    default boolean protectStructure(final ServerPlayer player, final ServerLevel world, final BlockPos pos)
    {
        if(player==null) return true;
        return true;
    }
}
