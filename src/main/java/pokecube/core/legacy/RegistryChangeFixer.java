package pokecube.core.legacy;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.MissingMappingsEvent.Mapping;
import pokecube.core.PokecubeItems;

public class RegistryChangeFixer
{
    private static final Set<ResourceLocation> tmNames = Sets.newHashSet();
    static
    {
        // Put in a few of these incease addons had added many types.
        for (int i = 0; i < 100; i++)
        {
            tmNames.add(new ResourceLocation("pokecube:tm" + i));
        }
    }

    @SubscribeEvent
    public static void onRegistryMissingEvent(MissingMappingsEvent event)
    {

        // Remap the TMs.
        if (event.getKey().equals(Keys.ITEMS))
        {
            List<Mapping<Item>> mappings = event.getAllMappings(Keys.ITEMS);
            mappings.forEach(m -> {
                if (tmNames.contains(m.getKey())) m.remap(PokecubeItems.TM.get());
            });
        }
    }
}
