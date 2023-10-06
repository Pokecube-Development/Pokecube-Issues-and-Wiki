package pokecube.legends.legacy;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.MissingMappingsEvent.Mapping;
import pokecube.legends.init.BlockInit;

public class LegendsRegistryChangeFixer
{
    private static final Set<ResourceLocation> one_way_laboratory_glass = Sets.newHashSet();
    
    static { one_way_laboratory_glass.add(new ResourceLocation("pokecube_legends:distortic_one_way_laboratory_glass")); }

    @SubscribeEvent
    public static void onRegistryMissingEvent(MissingMappingsEvent event)
    {
        // Remap the One-Way Glass Blocks.
        if (event.getKey().equals(Keys.BLOCKS))
        {
            List<Mapping<Block>> mappings = event.getAllMappings(Keys.BLOCKS);
            mappings.forEach(m -> {
                if (one_way_laboratory_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_LAB.get());
            });
        }

        // Remap the One-Way Glass Block items.
        if (event.getKey().equals(Keys.ITEMS))
        {
            List<Mapping<Item>> mappings = event.getAllMappings(Keys.ITEMS);
            mappings.forEach(m -> {
                if (one_way_laboratory_glass.contains(m.getKey())) m.remap(BlockInit.ONE_WAY_GLASS_LAB.get().asItem());
            });
        }
    }
}
