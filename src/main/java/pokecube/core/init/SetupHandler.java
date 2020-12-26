package pokecube.core.init;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.GuardAICapability;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.database.Database;
import pokecube.core.database.worldgen.WorldgenHandler;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.capabilities.CapabilityAffected;
import pokecube.core.interfaces.capabilities.CapabilityAffected.DefaultAffected;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.items.megastuff.IMegaCapability;
import pokecube.core.items.megastuff.MegaCapability;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.zmoves.CapabilityZMove;
import pokecube.core.moves.zmoves.ZPower;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import pokecube.nbtedit.NBTEdit;
import thut.api.terrain.TerrainSegment;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = PokecubeCore.MODID)
public class SetupHandler
{
    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        PokecubeCore.LOGGER.info("Hello from Common Proxy setup!");

        // Registers the event listeners.
        EventsHandler.register();

        // Initialize the capabilities.
        CapabilityManager.INSTANCE.register(IGuardAICapability.class, new IGuardAICapability.Storage(),
                GuardAICapability::new);
        CapabilityManager.INSTANCE.register(IPokemob.class, new CapabilityPokemob.Storage(), DefaultPokemob::new);
        CapabilityManager.INSTANCE.register(IOngoingAffected.class, new CapabilityAffected.Storage(),
                DefaultAffected::new);
        CapabilityManager.INSTANCE.register(ZPower.class, new CapabilityZMove.Storage(), CapabilityZMove.Impl::new);
        CapabilityManager.INSTANCE.register(IMegaCapability.class, new Capability.IStorage<IMegaCapability>()
        {
            @Override
            public void readNBT(final Capability<IMegaCapability> capability, final IMegaCapability instance,
                    final Direction side, final INBT nbt)
            {
            }

            @Override
            public INBT writeNBT(final Capability<IMegaCapability> capability, final IMegaCapability instance,
                    final Direction side)
            {
                return null;
            }
        }, MegaCapability.Default::new);
        CapabilityManager.INSTANCE.register(IPokemobUseable.class, new IPokemobUseable.Storage(),
                IPokemobUseable.Default::new);

        // Register terrain effects
        TerrainSegment.terrainEffectClasses.add(PokemobTerrainEffects.class);

        // Registers the packets.
        PokecubePacketHandler.init();

        PokecubeTerrainChecker.init();

        // Forward this to PCEdit mod:
        NBTEdit.setup(event);

        event.enqueueWork(() ->
        {
            WorldgenHandler.setupAll();
        });

        // Register some Village stuff
        // if (PokecubeCore.getConfig().villagePokecenters)
        // {
        // TODO pokecenters in vanilla villages.
        // final ImmutableList<StructureProcessor> replacementRules =
        // ImmutableList.of(new RuleStructureProcessor(
        // ImmutableList.of(new RuleEntry(new
        // RandomBlockMatchRuleTest(Blocks.COBBLESTONE, 0.1F),
        // AlwaysTrueRuleTest.INSTANCE,
        // Blocks.MOSSY_COBBLESTONE.getDefaultState()))));
        //
        // final SingleJigsawPiece part = new SingleJigsawPiece(new
        // ResourceLocation(PokecubeCore.MODID,
        // "village/common/pokecenter").toString(), replacementRules,
        // JigsawPattern.PlacementBehaviour.TERRAIN_MATCHING);
        //
        // JigsawManager.REGISTRY.register(new JigsawPattern(new
        // ResourceLocation(PokecubeCore.MODID,
        // "village/common/pokecenter"), new
        // ResourceLocation("village/plains/terminators"), ImmutableList.of(
        // new Pair<>(part, 100)),
        // JigsawPattern.PlacementBehaviour.TERRAIN_MATCHING));
        // }

    }

    @SubscribeEvent
    public static void loaded(final FMLLoadCompleteEvent event)
    {
        // Reload this here to initialze anything that needs to be done here.
        event.enqueueWork(() ->
        {
            PokecubeCore.getConfig().onUpdated();
            Database.onLoadComplete();
        });
    }

}
