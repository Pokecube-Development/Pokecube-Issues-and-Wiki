package thut.api.world;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StructureTemplateTools
{
    public static List<ItemStack> getNeededMateials(StructureTemplate template, ServerLevel level, BlockPos location,
            StructurePlaceSettings settings, Predicate<BlockPos> valid)
    {
        List<ItemStack> needed = Lists.newArrayList();
        Map<Item, ItemStack> tmp = Maps.newHashMap();
        List<StructureTemplate.StructureBlockInfo> list = settings.getRandomPalette(template.palettes, location)
                .blocks();
        List<StructureTemplate.StructureBlockInfo> infos = StructureTemplate.processBlockInfos(level, location,
                location, settings, list, template);
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
}
