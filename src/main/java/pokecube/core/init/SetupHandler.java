package pokecube.core.init;

import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.ai.IInhabitor;
import pokecube.api.blocks.IInhabitable;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.SharedAttributes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.items.IPokemobUseable;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.commands.arguments.PokemobArgument;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.nbtedit.NBTEdit;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.level.terrain.TerrainSegment;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
public class SetupHandler
{
    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event)
    {
        // Initialize the capabilities.
        event.register(IGuardAICapability.class);
        event.register(IPokemob.class);
        event.register(IOngoingAffected.class);
        event.register(IPokemobUseable.class);
        event.register(IInhabitable.class);
        event.register(IInhabitor.class);
    }

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

        // Register command argument type serializers
        ArgumentTypes.register("pokecube:pokemob", PokemobArgument.class,
                new EmptyArgumentSerializer<>(PokemobArgument::pokemob));
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

    @SubscribeEvent
    public static void onEntityAttributes(final EntityAttributeCreationEvent event)
    {
        if (PokecubeCore.getConfig().debug_misc) PokecubeAPI.logInfo("Registering Pokecube Attributes");

        final AttributeSupplier.Builder attribs = LivingEntity.createLivingAttributes()
                .add(SharedAttributes.MOB_SIZE_SCALE.get()).add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ATTACK_KNOCKBACK).add(Attributes.MAX_HEALTH, 10.0D);
        event.put(EntityTypes.getPokecube(), attribs.build());
        event.put(EntityTypes.getEgg(), attribs.build());
        event.put(EntityTypes.getNpc(), attribs.build());

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
        event.getTypes().forEach(e -> {
            if (!event.has(e, SharedAttributes.MOB_SIZE_SCALE.get()))
                event.add(e, SharedAttributes.MOB_SIZE_SCALE.get());
        });
    }

}
