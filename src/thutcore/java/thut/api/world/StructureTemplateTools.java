package thut.api.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class StructureTemplateTools
{
    public static record PlaceContext(StructurePlaceSettings settings, ServerLevel level,
            Predicate<StructureBlockInfo> applyTag)
    {
    }

    public static interface BlockPlacer
    {
        default ItemStack getForBlock(StructureBlockInfo info)
        {
            var state = info.state;

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
                ItemStack stack = new ItemStack(item);
                if (info.nbt != null)
                {
                    CompoundTag tag = new CompoundTag();
                    tag.put("BlockEntityTag", info.nbt);
                    stack.setTag(tag);
                }
                return stack;
            }
            return ItemStack.EMPTY;
        }

        default void placeBlock(StructureBlockInfo info, PlaceContext context)
        {
            var pos = info.pos;
            var tag = info.nbt;
            var settings = context.settings();
            var level = context.level();

            // We do not use the "recommended" rotate function, as that is
            // for blocks already in world. Using it prevents pistons from
            // rotating properly!
            @SuppressWarnings("deprecation")
            var state = info.state.mirror(settings.getMirror()).rotate(settings.getRotation());

            level.setBlockAndUpdate(pos, state);

            if (tag != null && context.applyTag().test(info))
            {
                var blockentity = level.getBlockEntity(pos);
                Clearable.tryClear(blockentity);
                // Load custom sign things.
                if (blockentity != null) blockentity.load(tag);
            }

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

    public static void placeBlock(StructureBlockInfo info, PlaceContext context)
    {
        getPlacer(info.state).placeBlock(info, context);
    }

    public static ItemStack getForInfo(StructureBlockInfo info)
    {
        return getPlacer(info.state).getForBlock(info);
    }

    public static Map<BlockPos, ItemStack> getNeededMaterials(ServerLevel level, List<StructureBlockInfo> infos,
            int startIndex, int endIndex, Predicate<ItemStack> applyTag)
    {
        endIndex = Math.min(endIndex, infos.size());
        Map<Item, List<ItemStack>> stacks = Maps.newHashMap();
        Map<BlockPos, ItemStack> byCoordinate = Maps.newHashMap();
        outer:
        for (int i = startIndex; i < endIndex; i++)
        {
            var info = infos.get(i);
            if (info.state != null && !info.state.isAir())
            {
                BlockPlacer placer = getPlacer(info.state);
                BlockState old = level.getBlockState(info.pos);
                if (old.getBlock() == info.state.getBlock()) continue;
                ItemStack stack = placer.getForBlock(info);
                if (stack.hasTag() && !applyTag.test(stack)) stack.setTag(null);
                if (!stack.isEmpty())
                {
                    var list = stacks.getOrDefault(stack.getItem(), new ArrayList<>());
                    stacks.put(stack.getItem(), list);
                    if (list.isEmpty())
                    {
                        byCoordinate.put(info.pos, stack);
                        list.add(stack);
                        continue outer;
                    }
                    else
                    {
                        if (!stack.hasTag())
                        {
                            for (ItemStack held : list)
                            {
                                if (!held.hasTag())
                                {
                                    held.grow(stack.getCount());
                                    byCoordinate.put(info.pos, held);
                                    continue outer;
                                }
                            }
                            byCoordinate.put(info.pos, stack);
                            list.add(stack);
                            continue outer;
                        }
                        else
                        {
                            for (ItemStack held : list)
                            {
                                if (held.hasTag() && held.getTag().equals(stack.getTag()))
                                {
                                    held.grow(stack.getCount());
                                    byCoordinate.put(info.pos, held);
                                    continue outer;
                                }
                            }
                            byCoordinate.put(info.pos, stack);
                            list.add(stack);
                            continue outer;
                        }
                    }
                }
            }
        }
        return byCoordinate;
    }

    public static Map<BlockPos, ItemStack> getNeededMaterials(ServerLevel level, List<StructureBlockInfo> infos,
            Predicate<ItemStack> applyTag)
    {
        return getNeededMaterials(level, infos, 0, infos.size(), applyTag);
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
                BlockHitResult result = new BlockHitResult(new Vec3(info.pos.getX(), info.pos.getY(), info.pos.getZ()),
                        Direction.UP, info.pos, false);
                ItemStack stack = getForInfo(info);
                BlockPlaceContext context = new BlockPlaceContext(level, null, InteractionHand.MAIN_HAND, stack,
                        result);
                // Just directly replace blocks that allow it
                if (old.canBeReplaced(context)) continue;
                if (old.getBlock() != info.state.getBlock()) remove.add(info.pos);
            }
        }
        return remove;
    }

}
