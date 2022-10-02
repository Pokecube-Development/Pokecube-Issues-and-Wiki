package pokecube.core.legacy;

import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
    public static void onRegistryMissingEvent(RegistryEvent.MissingMappings<Item> event)
    {

        // Remap the TMs.
        if (event.getName().toString().equals("minecraft:item"))
        {
            ImmutableList<Mapping<Item>> mappings = event.getAllMappings();
            mappings.forEach(m -> {
                if (tmNames.contains(m.key)) m.remap(PokecubeItems.TM.get());
            });
        }
    }
}
