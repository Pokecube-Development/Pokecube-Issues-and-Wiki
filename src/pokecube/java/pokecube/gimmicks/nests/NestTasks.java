package pokecube.gimmicks.nests;

import com.google.common.collect.Sets;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
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
import org.jetbrains.annotations.Nullable;
import pokecube.api.ai.IInhabitor;
import pokecube.api.entity.CapabilityInhabitor;
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
import thut.api.item.ItemList;

import java.util.function.Predicate;
import java.util.function.Supplier;

@Mod(value = PokecubeCore.MODID)
@EventBusSubscriber(modid = PokecubeCore.MODID)
public class NestTasks
{
    public static final DeferredBlock<Block> NEST;
    public static final Supplier<BlockEntityType<NestTile>> NEST_TYPE;
    public static final DeferredHolder<PoiType, PoiType> NEST_POI_TYPE;
    public static final Predicate<Holder<PoiType>> NEST_POI;

    static
    {
        NEST = PokecubeCore.BLOCKS.register("nest",
                () -> new NestBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).ignitedByLava()
                        .strength(0.5F).isValidSpawn(PokecubeItems::ocelotOrParrot).sound(SoundType.MANGROVE_ROOTS)
                        .instrument(NoteBlockInstrument.HARP).pushReaction(PushReaction.NORMAL)));
        NEST_TYPE = PokecubeCore.TILES.register("nest",
                () -> BlockEntityType.Builder.of(NestTile::new, NEST.get()).build(null));
        NEST_POI_TYPE = PointsOfInterest.REG.register("pokemob_nest",
                () -> new PoiType(Sets.newHashSet(NEST.get().getStateDefinition().getPossibleStates()), 1,
                        2));
        NEST_POI =holder -> holder.is(NEST_POI_TYPE.getKey());
        init();
    }

    public NestTasks()
    {
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
