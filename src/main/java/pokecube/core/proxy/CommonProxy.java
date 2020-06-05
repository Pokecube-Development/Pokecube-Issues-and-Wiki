package pokecube.core.proxy;

import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.template.AlwaysTrueRuleTest;
import net.minecraft.world.gen.feature.template.RandomBlockMatchRuleTest;
import net.minecraft.world.gen.feature.template.RuleEntry;
import net.minecraft.world.gen.feature.template.RuleStructureProcessor;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.GuardAICapability;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.blocks.healer.HealerTile;
import pokecube.core.database.Database;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.handlers.events.EventsHandler.MeteorAreaSetter;
import pokecube.core.handlers.events.PCEventsHandler;
import pokecube.core.handlers.events.PokemobEventsHandler;
import pokecube.core.handlers.events.SpawnEventsHandler;
import pokecube.core.handlers.events.StatsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemobUseable;
import pokecube.core.interfaces.capabilities.CapabilityAffected;
import pokecube.core.interfaces.capabilities.CapabilityAffected.DefaultAffected;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.items.megastuff.IMegaCapability;
import pokecube.core.items.megastuff.MegaCapability;
import pokecube.core.items.megastuff.WearablesCompat;
import pokecube.core.moves.MoveQueue.MoveQueuer;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.zmoves.CapabilityZMove;
import pokecube.core.moves.zmoves.ZPower;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.world.dimension.SecretBaseDimension;
import pokecube.core.world.gen.jigsaw.JigsawPieces;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import pokecube.nbtedit.NBTEdit;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainSegment;
import thut.core.common.Proxy;

public class CommonProxy implements Proxy
{

    public PlayerEntity getPlayer(final UUID uuid)
    {
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return server.getPlayerList().getPlayerByUUID(uuid);
    }

    public ResourceLocation getPlayerSkin(final String name)
    {
        return null;
    }

    public ResourceLocation getUrlSkin(final String urlSkin)
    {
        return null;
    }

    public ServerWorld getServerWorld()
    {
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return server.getWorld(DimensionType.OVERWORLD);
    }

    public World getWorld()
    {
        final MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        return server.getWorld(DimensionType.OVERWORLD);
    }

    @Override
    public void setup(final FMLCommonSetupEvent event)
    {
        PokecubeCore.LOGGER.info("Hello from Common Proxy setup!");

        // Register some event handlers
        PokecubeCore.POKEMOB_BUS.register(StatsHandler.class);
        PokecubeCore.POKEMOB_BUS.register(SpawnEventsHandler.class);
        PokecubeCore.POKEMOB_BUS.register(PCEventsHandler.class);
        PokecubeCore.POKEMOB_BUS.register(PokemobEventsHandler.class);

        MinecraftForge.EVENT_BUS.register(GeneticsManager.class);
        MinecraftForge.EVENT_BUS.register(MeteorAreaSetter.class);
        MinecraftForge.EVENT_BUS.register(PCEventsHandler.class);
        MinecraftForge.EVENT_BUS.register(PokemobEventsHandler.class);
        MinecraftForge.EVENT_BUS.register(WearablesCompat.class);
        MinecraftForge.EVENT_BUS.register(NBTEdit.class);
        MinecraftForge.EVENT_BUS.register(MoveQueuer.class);
        MinecraftForge.EVENT_BUS.register(SecretBaseDimension.class);

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

        // Register some Village stuff
        if (PokecubeCore.getConfig().villagePokecenters)
        {
            final ImmutableList<StructureProcessor> replacementRules = ImmutableList.of(new RuleStructureProcessor(
                    ImmutableList.of(new RuleEntry(new RandomBlockMatchRuleTest(Blocks.COBBLESTONE, 0.1F),
                            AlwaysTrueRuleTest.INSTANCE, Blocks.MOSSY_COBBLESTONE.getDefaultState()))));

            final SingleJigsawPiece part = new SingleJigsawPiece(new ResourceLocation(PokecubeCore.MODID,
                    "village/common/pokecenter").toString(), replacementRules,
                    JigsawPattern.PlacementBehaviour.TERRAIN_MATCHING);

            JigsawManager.REGISTRY.register(new JigsawPattern(new ResourceLocation(PokecubeCore.MODID,
                    "village/common/pokecenter"), new ResourceLocation("village/plains/terminators"), ImmutableList.of(
                            new Pair<>(part, 100)), JigsawPattern.PlacementBehaviour.TERRAIN_MATCHING));
        }

    }

    public void serverAboutToStart(final FMLServerAboutToStartEvent event)
    {
        // Do nothing here, the client side uses this to clear some things for
        // single player
    }

    @Override
    public void loaded(final FMLLoadCompleteEvent event)
    {
        // Reload this here to initialze anything that needs to be done here.
        PokecubeCore.getConfig().onUpdated();
        Database.postResourcesLoaded();
        JigsawPieces.finializeJigsaws();
    }

    public void pokecenterloop(final HealerTile tileIn, final boolean play)
    {

    }

    public void moveSound(final Vector3 pos, final SoundEvent event, final float volume)
    {

    }

}
