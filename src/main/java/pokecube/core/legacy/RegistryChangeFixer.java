package pokecube.core.legacy;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
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

    private static final Map<ResourceLocation, ResourceLocation> ENTRY_RENAMES = Maps.newHashMap();

    public static void registerRename(String oldName, String newName)
    {
        ENTRY_RENAMES.put(new ResourceLocation("pokecube", oldName), new ResourceLocation("pokecube", newName));
    }

    @SubscribeEvent
    public static void onRegistryMissingItemEvent(RegistryEvent.MissingMappings<Item> event)
    {
        // Remap the TMs.
        ImmutableList<Mapping<Item>> mappings = event.getAllMappings();
        mappings.forEach(m -> {
            if (tmNames.contains(m.key)) m.remap(PokecubeItems.TM.get());
        });
    }

    @SubscribeEvent
    public static void onRegistryMissingEntityTypeEvent(RegistryEvent.MissingMappings<EntityType<?>> event)
    {
        ImmutableList<Mapping<EntityType<?>>> mappings = event.getAllMappings();
        mappings.forEach(m -> {
            if (ENTRY_RENAMES.containsKey(m.key)) m.remap(ForgeRegistries.ENTITIES.getValue(ENTRY_RENAMES.get(m.key)));
        });
    }
}
