package pokecube.core.legacy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.MissingMappingsEvent.Mapping;
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

        // Remap the sounds.
        if (event.getKey().equals(Keys.SOUND_EVENTS))
        {
            List<Mapping<SoundEvent>> mappings = event.getAllMappings(Keys.SOUND_EVENTS);
            mappings.forEach(m -> {
                if (m.getKey().getPath().startsWith("mobs.")) m.remap(SoundEvents.PIG_AMBIENT);
            });
        }

        // Remap the entity types
        if (event.getKey().equals(Keys.ENTITY_TYPES))
        {
            List<Mapping<EntityType<?>>> mappings = event.getAllMappings(Keys.ENTITY_TYPES);
            mappings.forEach(m -> {
                if (ENTRY_RENAMES.containsKey(m.getKey()))
                {
                    PokecubeAPI.LOGGER.warn("Remapping {} to {}", m.getKey(),
                            Database.getEntry(ENTRY_RENAMES.get(m.getKey()).getPath()));
                    m.remap(Database.getEntry(ENTRY_RENAMES.get(m.getKey()).getPath()).getEntityType());
                }
                // Otherwise check if maybe it changed to a form? if so, return
                // type
                // of new root.
                else if (Database.formeToEntry.containsKey(m.getKey()))
                {
                    PokecubeAPI.LOGGER.warn("Remapping {} to {}", m.getKey(), Database.formeToEntry.get(m.getKey()));
                    m.remap(Database.formeToEntry.get(m.getKey()).getEntityType());
                }
                else
                {
                    PokecubeAPI.LOGGER.warn("Remapping {} to {}", m.getKey(), Database.missingno);
                    m.remap(Database.missingno.getEntityType());
                }
            });
        }
    }
}
