package pokecube.core.init;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.nbtedit.NBTEdit;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.level.terrain.TerrainSegment;

@EventBusSubscriber(modid = PokecubeCore.MODID)
public class SetupHandler
{
    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        if (PokecubeCore.getConfig().debug_misc) PokecubeAPI.logInfo("Hello from Common Proxy setup!");

        // Registers the event listeners.
        EventsHandler.register();

        // Register terrain effects
        TerrainSegment.registerTerrainEffect(PokemobTerrainEffects.class);

        // Registers the packets.
        PokecubePacketHandler.init();

        PokecubeTerrainChecker.init();

        // Forward this to PCEdit mod:
        NBTEdit.setup(event);
    }

    @SubscribeEvent
    public static void loaded(final FMLLoadCompleteEvent event)
    {
        // Reload this here to initialze anything that needs to be done here.
        event.enqueueWork(() -> {
            PokecubeCore.getConfig().onUpdated();
            Database.onLoadComplete();
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityAttributes(final EntityAttributeModificationEvent event)
    {
        event.getTypes().forEach((type) -> {
            // We use this for attack cooldown scaling.
            if (!event.has(type, Attributes.ATTACK_SPEED)) event.add(type, Attributes.ATTACK_SPEED);
            // We use this for attack damage scaling.
            if (!event.has(type, Attributes.ATTACK_DAMAGE)) event.add(type, Attributes.ATTACK_DAMAGE);
        });
    }

    @SubscribeEvent
    public static void onEntityAttributes(final EntityAttributeCreationEvent event)
    {
        if (PokecubeCore.getConfig().debug_misc) PokecubeAPI.logInfo("Registering Pokecube Attributes");

        var attribs = LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.FLYING_SPEED, 0.6);

        event.put(EntityTypes.getPokecube(), attribs.build());
        event.put(EntityTypes.getEgg(), attribs.build());
        event.put(EntityTypes.getNpc(), attribs.build());

        // Now add the pokemob attributes
        for (var a : PokecubeAttributes.ATTRIBUTES) attribs.add(a);

        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            if (entry.dummy) continue;
            if (!entry.stock) continue;
            if (entry.generated) continue;
            try
            {
                event.put(entry.getEntityType(), attribs.build());
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityAttributesModify(final EntityAttributeModificationEvent event)
    {
    }

}
