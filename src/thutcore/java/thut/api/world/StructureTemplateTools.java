package thut.api.world;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class StructureTemplateTools
{
    public static interface BlockPlacer
    {
        default ItemStack getForBlock(BlockState state)
        {
            if (state == null) return ItemStack.EMPTY;
            if (state.hasProperty(BedBlock.PART) && state.getValue(BedBlock.PART) == BedPart.HEAD)
            {
                // We handle this by placing foot, and ignoring head.
                return ItemStack.EMPTY;
            }
            if (state.hasProperty(DoorBlock.HALF) && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
            {
                // We handle this by placing lower, and ignoring upper.
                return ItemStack.EMPTY;
            }
            if (state.getBlock() == Blocks.PISTON_HEAD)
            {
                // We handle placing the base, not the head.
                return ItemStack.EMPTY;
            }
            Item item = state.getBlock().asItem();
            if (item != null)
            {
                return new ItemStack(item);
            }
            return ItemStack.EMPTY;
        }

        default void placeBlock(BlockState state, BlockPos pos, ServerLevel level)
        {
            level.setBlockAndUpdate(pos, state);
            if (state.hasProperty(BedBlock.PART) && state.getValue(BedBlock.PART) == BedPart.FOOT)
            {
                level.setBlockAndUpdate(pos.relative(state.getValue(BedBlock.FACING)),
                        state.setValue(BedBlock.PART, BedPart.HEAD));
            }
            if (state.hasProperty(DoorBlock.HALF) && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER)
            {
                level.setBlockAndUpdate(pos.above(), state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            }
            if (state.hasProperty(PistonBaseBlock.EXTENDED) && state.getValue(PistonBaseBlock.EXTENDED))
            {
                var dir = state.getValue(PistonBaseBlock.FACING);
                var head = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, dir);
                if (state.getBlock() == Blocks.STICKY_PISTON)
                    head = head.setValue(PistonHeadBlock.TYPE, PistonType.STICKY);
                level.setBlockAndUpdate(pos.relative(dir), head);
            }
        }
    }

    private static final BlockPlacer DEFAULT = new BlockPlacer()
    {
    };

    public static Map<Block, BlockPlacer> placers = Maps.newHashMap();

    public static BlockPlacer getPlacer(BlockState toPlace)
    {
        if (toPlace == null) return DEFAULT;
        return placers.getOrDefault(toPlace.getBlock(), DEFAULT);
    }

    public static ItemStack getForInfo(StructureBlockInfo info)
    {
        return getPlacer(info.state).getForBlock(info.state);
    }

    public static Map<BlockPos, ItemStack> getNeededMaterials(ServerLevel level, List<StructureBlockInfo> infos,
            int startIndex, int endIndex)
    {
        endIndex = Math.min(endIndex, infos.size() - 1);
        Map<Item, ItemStack> tmp = Maps.newHashMap();
        Map<BlockPos, ItemStack> neededItems = Maps.newHashMap();
        for (int i = startIndex; i <= endIndex; i++)
        {
            var info = infos.get(i);
            if (info.state != null && !info.state.isAir())
            {
                BlockPlacer placer = getPlacer(info.state);
                BlockState old = level.getBlockState(info.pos);
                if (old.getBlock() == info.state.getBlock()) continue;
                ItemStack newStack = placer.getForBlock(info.state);
                if (!newStack.isEmpty())
                {
                    Item item = newStack.getItem();
                    ItemStack stack = tmp.get(item);
                    if (stack == null) stack = newStack;
                    else stack.setCount(stack.getCount() + 1);
                    tmp.put(item, stack);
                    neededItems.put(info.pos, stack);
                }
            }
        }
        return neededItems;
    }

    public static Map<BlockPos, ItemStack> getNeededMaterials(ServerLevel level, List<StructureBlockInfo> infos)
    {
        return getNeededMaterials(level, infos, 0, infos.size() - 1);
    }

    public static List<BlockPos> getNeedsRemoval(ServerLevel level, StructurePlaceSettings settings,
            List<StructureBlockInfo> infos)
    {
        List<BlockPos> remove = Lists.newArrayList();
        for (var info : infos)
        {
            if (info.state != null)
            {
                BlockState old = level.getBlockState(info.pos);
                if (old.isAir()) continue;
                if (old.getBlock() != info.state.getBlock()) remove.add(info.pos);
//                else
//                {
//                    BlockState placeState = info.state.mirror(settings.getMirror()).rotate(level, info.pos,
//                            settings.getRotation());
//                    if (old.getProperties().stream().anyMatch(p -> !old.getValue(p).equals(placeState.getValue(p))))
//                        remove.add(info.pos);
//                }
            }
        }
        return remove;
    }

}
