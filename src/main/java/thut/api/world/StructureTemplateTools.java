package thut.api.world;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

public class StructureTemplateTools
{
    public static List<ItemStack> getNeededMateials(StructureTemplate template, ServerLevel level, BlockPos location,
            StructurePlaceSettings settings, Predicate<BlockPos> valid)
    {
        List<ItemStack> needed = Lists.newArrayList();
        Map<Item, ItemStack> tmp = Maps.newHashMap();
        List<StructureBlockInfo> list = settings.getRandomPalette(template.palettes, location).blocks();
        List<StructureBlockInfo> infos = StructureTemplate.processBlockInfos(level, location, location, settings, list,
                template);
        for (var info : infos)
        {
            if (info.state != null && !info.state.isAir())
            {
                Item item = info.state.getBlock().asItem();
                if (item != null && valid.test(info.pos))
                {
                    ItemStack stack = tmp.get(item);
                    if (stack == null) stack = new ItemStack(item);
                    else stack.setCount(stack.getCount() + 1);
                    tmp.put(item, stack);
                }
            }
        }
        needed.addAll(tmp.values());
        return needed;
    }

    public static List<ItemStack> getNeededMaterials(ServerLevel level, List<StructureBlockInfo> infos)
    {
        Map<Item, ItemStack> tmp = Maps.newHashMap();
        List<ItemStack> needed = Lists.newArrayList();
        for (var info : infos)
        {
            if (info.state != null && !info.state.isAir())
            {
                BlockState old = level.getBlockState(info.pos);
                if (old.getBlock() == info.state.getBlock()) continue;
                Item item = info.state.getBlock().asItem();
                if (item != null)
                {
                    ItemStack stack = tmp.get(item);
                    if (stack == null) stack = new ItemStack(item);
                    else stack.setCount(stack.getCount() + 1);
                    tmp.put(item, stack);
                }
            }
        }
        needed.addAll(tmp.values());
        return needed;
    }

    public static List<BlockPos> getNeedsRemoval(ServerLevel level, StructurePlaceSettings settings,
            List<StructureBlockInfo> infos)
    {
        List<BlockPos> remove = Lists.newArrayList();
        for (var info : infos)
        {
            if (info.state != null && !info.state.isAir())
            {
                BlockState old = level.getBlockState(info.pos);
                if (old.isAir()) continue;
                if (old.getBlock() != info.state.getBlock()) remove.add(info.pos);
                else
                {
                    BlockState placeState = info.state.mirror(settings.getMirror()).rotate(level, info.pos,
                            settings.getRotation());
                    if (old.getProperties().stream().anyMatch(p -> !old.getValue(p).equals(placeState.getValue(p))))
                        remove.add(info.pos);
                }
            }
        }
        return remove;
    }

}
