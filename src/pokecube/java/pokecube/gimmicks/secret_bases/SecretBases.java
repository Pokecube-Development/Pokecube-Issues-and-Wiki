package pokecube.gimmicks.secret_bases;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.client.gui.watch.SecretBaseRadarPage;
import pokecube.core.init.CoreCreativeTabs;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.gimmicks.secret_bases.blocks.BaseBlock;
import pokecube.gimmicks.secret_bases.blocks.BaseTile;
import pokecube.gimmicks.secret_bases.command.SecretBase;
import pokecube.gimmicks.secret_bases.dimension.SecretBaseDimension;
import thut.api.attachments.Ownable;

import java.util.function.Supplier;

@Mod(value = PokecubeCore.MODID)
@EventBusSubscriber(modid = PokecubeCore.MODID)
public class SecretBases
{
    public static final Supplier<BlockEntityType<?>> BASE_TYPE;
    public static final DeferredBlock<Block> SECRET_BASE;

    static
    {
        SECRET_BASE = PokecubeCore.BLOCKS
                .register("secret_base",
                        () -> new BaseBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                                .requiresCorrectToolForDrops().strength(2000).sound(SoundType.STONE)
                                .instrument(NoteBlockInstrument.BASEDRUM)));
        PokecubeCore.ITEMS.register(SECRET_BASE.getId().getPath(), () -> new BlockItem(SECRET_BASE.get(), new Item.Properties()));

        BASE_TYPE = PokecubeCore.TILES.register("secret_base",
                () -> BlockEntityType.Builder.of(BaseTile::new, SECRET_BASE.get()).build(null));
    }

    public SecretBases(IEventBus bus)
    {
        SecretBaseDimension.onConstruct(bus);
        PokecubeItems.DEFAULT_OWNABLE_TE.add(SecretBase.class);
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event)
    {
        // Register as an ownable block
        Ownable.TILES.add(BASE_TYPE.get());

        PacketPokedex.RADAR_SUPPLIERS.put("_bases_", player -> {
            final ServerLevel level = player.serverLevel().getLevel();
            final BlockPos pos = player.blockPosition();
            final GlobalPos here = GlobalPos.of(level.dimension(), pos);
            return SecretBaseDimension.getNearestBases(here, PokecubeCore.getConfig().baseRadarRange);
        });

        SecretBaseRadarPage.RADAR_MODES.put("_bases_",
                SecretBaseRadarPage.DEFAULT = new SecretBaseRadarPage.RadarMode("base", "_bases_", 1));
    }

    @SubscribeEvent
    public static void onCommandRegister(final RegisterCommandsEvent event)
    {
        SecretBase.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTab().equals(CoreCreativeTabs.BLOCKS_ITEMS_TAB.get()))
        {
            CoreCreativeTabs.addAfter(event, PokecubeItems.DEEPSLATE_FOSSIL_ORE, SECRET_BASE);
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS && PokecubeCore.getConfig().itemsInVanillaTabs)
        {
            CoreCreativeTabs.addAfter(event, Items.LODESTONE, SECRET_BASE);
        }
    }
}
