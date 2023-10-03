package pokecube.core.legacy;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.MissingMappings.Mapping;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.api.PokecubeAPI;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;

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
        if (oldName.contains(","))
        {
            String[] args = oldName.split(",");
            for (var s : args) registerRename(s.strip(), newName);
        }
        else ENTRY_RENAMES.put(new ResourceLocation("pokecube", oldName), new ResourceLocation("pokecube", newName));
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
    public static void onRegistryMissingSoundEventsEvent(RegistryEvent.MissingMappings<SoundEvent> event)
    {
        ImmutableList<Mapping<SoundEvent>> mappings = event.getAllMappings();
        mappings.forEach(m -> {
            if (m.key.getPath().startsWith("mobs.")) m.remap(SoundEvents.PIG_AMBIENT);
        });
    }

    @SubscribeEvent
    public static void onRegistryMissingEntityTypeEvent(RegistryEvent.MissingMappings<EntityType<?>> event)
    {
        ImmutableList<Mapping<EntityType<?>>> mappings = event.getAllMappings();
        mappings.forEach(m -> {
            if (ENTRY_RENAMES.containsKey(m.key))
            {
                PokecubeAPI.LOGGER.warn("Remapping {} to {}", m.key,
                        Database.getEntry(ENTRY_RENAMES.get(m.key).getPath()));
                m.remap(Database.getEntry(ENTRY_RENAMES.get(m.key).getPath()).getEntityType());
            }
            // Otherwise check if maybe it changed to a form? if so, return type
            // of new root.
            else if (Database.formeToEntry.containsKey(m.key))
            {
                PokecubeAPI.LOGGER.warn("Remapping {} to {}", m.key, Database.formeToEntry.get(m.key));
                m.remap(Database.formeToEntry.get(m.key).getEntityType());
            }
            else if(m.key.getNamespace().contains("pokecube"))
            {
                PokecubeAPI.LOGGER.warn("Remapping {} to {}", m.key, Database.missingno);
                m.remap(Database.missingno.getEntityType());
            }
        });
    }
}
