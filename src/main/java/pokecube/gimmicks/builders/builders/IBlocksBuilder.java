package pokecube.gimmicks.builders.builders;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public interface IBlocksBuilder
{
    public static enum CanPlace
    {
        YES, NO, NEED_ITEM;
    }

    public static record PlaceInfo(CanPlace valid, StructureBlockInfo info, int itemSlot)
    {
    }

    boolean tryPlace(PlaceInfo placement, ServerLevel level);

    PlaceInfo getNextPlacement(ServerLevel level);

    PlaceInfo canPlace(StructureBlockInfo info, int index);
    
    void markPendingBuild(BlockPos pos);
    
    void markBuilt(BlockPos pos);
}
