package pokecube.gimmicks.nests;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import pokecube.api.PokecubeAPI;
import pokecube.api.ai.IInhabitor;
import pokecube.api.entity.CapabilityInhabitor;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.events.EggEvent;
import pokecube.api.items.EggInfo;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.ai.tasks.utility.UtilBehaviour;
import pokecube.core.init.CoreCreativeTabs;
import pokecube.gimmicks.nests.blocks.NestBlock;
import pokecube.gimmicks.nests.blocks.NestTile;
import pokecube.gimmicks.nests.tasks.ants.AntTasks;
import pokecube.gimmicks.nests.tasks.ants.AntTasks.AntInhabitor;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks.BeeInhabitor;
import pokecube.gimmicks.nests.tasks.burrows.BurrowTasks;
import thut.api.attachments.Inventory;
import thut.api.data.HolderProvider;
import thut.api.inventory.InvHelper;
import thut.api.item.ItemList;
import thut.lib.RegHelper;

import java.util.function.Predicate;
import java.util.function.Supplier;

@Mod(value = PokecubeCore.MODID)
@EventBusSubscriber(modid = PokecubeCore.MODID)
public class NestTasks
{
    public static final DeferredRegister.Blocks BLOCKS;
    public static final DeferredRegister.Items ITEMS;
    public static final DeferredRegister<BlockEntityType<?>> TILES;

    public static final DeferredBlock<Block> NEST;
    public static final Supplier<BlockEntityType<NestTile>> NEST_TYPE;
    public static final DeferredHolder<PoiType, PoiType> NEST_POI_TYPE;
    public static final Predicate<Holder<PoiType>> NEST_POI;

    static
    {
        // Setup the DeferredRegisters
        BLOCKS = DeferredRegister.createBlocks(PokecubeCore.MODID);
        ITEMS = DeferredRegister.createItems(PokecubeCore.MODID);
        TILES = DeferredRegister.create(RegHelper.BLOCK_ENTITY_TYPE_REGISTRY, PokecubeCore.MODID);

        NEST = BLOCKS.register("nest",
                () -> new NestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).ignitedByLava()
                        .strength(0.5F).isValidSpawn(PokecubeItems::ocelotOrParrot).sound(SoundType.MANGROVE_ROOTS)
                        .instrument(NoteBlockInstrument.HARP).pushReaction(PushReaction.NORMAL)));
        ITEMS.register(NEST.getId().getPath(), () -> new BlockItem(NEST.get(), new Item.Properties()));


        NEST_TYPE = TILES.register("nest",
                () -> BlockEntityType.Builder.of(NestTile::new, NEST.get()).build(null));
        NEST_POI_TYPE = PointsOfInterest.REG.register("pokemob_nest",
                () -> new PoiType(Sets.newHashSet(NEST.get().getStateDefinition().getPossibleStates()), 1,
                        2));
        NEST_POI =holder -> holder.is(NEST_POI_TYPE.getKey());
        init();
    }

    public NestTasks(IEventBus bus)
    {
        // Register the DeferredRegisters
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TILES.register(bus);

        // Register custom listeners for the POKEMOB_BUS
        PokecubeAPI.POKEMOB_BUS.addListener(EventPriority.LOW, NestTasks::onHatch);
    }

    public static void onHatch(EggEvent.Hatch event)
    {
        var stack = event.egg.getMainHandItem();
        var world = event.egg.level();
        var mob = event.mob;
        EggInfo contents = PokemobCaps.getEggContents(stack);
        var nest = contents.getNest();
        nests: // TODO an event or such for setting the nest tile in NestTasks?
        if (nest.isPresent())
        {
            final BlockPos pos = nest.get();
            if (!world.isLoaded(pos)) break nests;
            final BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof NestTile _nest) _nest.addResident(mob);
            mob.setGeneralState(GeneralStates.EXITINGCUBE, false);
        }
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event)
    {
        // Prevent mobs digging nests.
        UtilBehaviour.diggable = UtilBehaviour.diggable.and(state->state.getBlock() != NestTasks.NEST.get());
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTab().equals(CoreCreativeTabs.BLOCKS_ITEMS_TAB.get()))
        {
            CoreCreativeTabs.addBefore(event, PokecubeItems.FOSSIL_ORE, NEST);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            CoreCreativeTabs.addBefore(event, Items.BEE_NEST, NEST);
        }
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            CoreCreativeTabs.addBefore(event, Items.BEE_NEST, NEST);
        }
    }

    static class NestProvider implements ICapabilityProvider<NestTile, Direction, IItemHandler>
    {
        @Override
        public @Nullable IItemHandler getCapability(NestTile object, Direction context)
        {
            // Only 1 inventory, so mark it as down here.
            return Inventory.get(object);
        }
    }

    @SubscribeEvent
    public static void AttachCaps(final RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, NEST_TYPE.get(), new NestProvider());

        Inventory.REGISTRY.register(new HolderProvider.Provider<>()
        {
            final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("pokecube", "nest");

            @Override
            public InvHelper.ItemCap apply(IAttachmentHolder t)
            {
                if (t instanceof NestTile) return new InvHelper.ItemCap(54);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });
    }

    public static void init()
    {
        BeeTasks.init();
        AntTasks.init();
        BurrowTasks.init();
        ResourceLocation ANT = ResourceLocation.parse("pokecube:ant");
        CapabilityInhabitor._REGISTRY.register(new HolderProvider.Provider<IInhabitor>()
        {

            @Override
            public IInhabitor apply(IAttachmentHolder t)
            {
                if (!(t instanceof Mob mob)) return null;
                if (!(ItemList.is(AntTasks.ANTS, mob))) return null;
                return new AntInhabitor(mob);
            }

            @Override
            protected ResourceLocation key()
            {
                return ANT;
            }
        });
        ResourceLocation BEE = ResourceLocation.parse("pokecube:bee");
        CapabilityInhabitor._REGISTRY.register(new HolderProvider.Provider<IInhabitor>()
        {

            @Override
            public IInhabitor apply(IAttachmentHolder t)
            {
                if (!(t instanceof Mob mob)) return null;
                if (!mob.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) return null;
                return new BeeInhabitor(mob);
            }

            @Override
            protected ResourceLocation key()
            {
                return BEE;
            }
        });
    }
}
