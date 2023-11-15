package pokecube.gimmicks.builders.builders;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IBlocksBuilder
{
    public static enum CanPlace
    {
        YES, NO, NEED_ITEM;
    }

    public static record PlaceInfo(CanPlace valid, StructureBlockInfo info, int itemSlot)
    {
    }

    public static record BoMRecord(Supplier<ItemStack> BoMProvider, Consumer<ItemStack> BoMConsumer)
    {
    }
    
    boolean isCreative();
    
    void setCreative(boolean creative);

    boolean validBuilder();

    void clearBoMRecords();

    void addBoMRecord(BoMRecord BoM);

    void provideBoM(BoMRecord record);

    void checkBoM(BoMRecord record);

    ServerLevel getLevel();

    void update(ServerLevel level);

    boolean tryPlace(PlaceInfo placement, ServerLevel level, IItemHandlerModifiable itemSource);

    PlaceInfo getNextPlacement(ServerLevel level, IItemHandlerModifiable itemSource);

    PlaceInfo canPlace(StructureBlockInfo info, int index, IItemHandlerModifiable itemSource);

    void markPendingBuild(BlockPos pos);

    void markBuilt(BlockPos pos);

    void getNextNeeded(List<ItemStack> requested, int number);
}
