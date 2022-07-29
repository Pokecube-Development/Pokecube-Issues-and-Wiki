package pokecube.legends.blocks.properties;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import pokecube.core.PokecubeCore;
import pokecube.legends.handlers.EventsHandler;
import pokecube.legends.init.BlockInit;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Tillables {

    public static void addTillables(final Block block, final Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> use)
    {
        EventsHandler.TILLABLES = Maps.newHashMap(EventsHandler.TILLABLES);
        EventsHandler.TILLABLES.put(block, use);
    }

    public static void tillables(final FMLCommonSetupEvent event) {
        event.enqueueWork(() ->
        {
            PokecubeCore.LOGGER.info("Loading Tillables");
            addTillables(BlockInit.AGED_COARSE_DIRT.get(),
                    Pair.of((item) -> { return false; }, HoeItem.changeIntoState(BlockInit.AGED_DIRT.get().defaultBlockState())));
            addTillables(BlockInit.AZURE_COARSE_DIRT.get(),
                    Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(BlockInit.AZURE_DIRT.get().defaultBlockState())));
            addTillables(BlockInit.CORRUPTED_COARSE_DIRT.get(),
                    Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(BlockInit.CORRUPTED_DIRT.get().defaultBlockState())));
            addTillables(BlockInit.JUNGLE_COARSE_DIRT.get(),
                    Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(BlockInit.JUNGLE_DIRT.get().defaultBlockState())));
            addTillables(BlockInit.MUSHROOM_COARSE_DIRT.get(),
                    Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(BlockInit.MUSHROOM_DIRT.get().defaultBlockState())));
            addTillables(BlockInit.ROOTED_CORRUPTED_DIRT.get(), Pair.of((item) -> { return true; },
                    HoeItem.changeIntoStateAndDropItem(BlockInit.CORRUPTED_DIRT.get().defaultBlockState(), Items.HANGING_ROOTS)));
            addTillables(BlockInit.ROOTED_MUSHROOM_DIRT.get(), Pair.of((item) -> { return true; },
                    HoeItem.changeIntoStateAndDropItem(BlockInit.MUSHROOM_DIRT.get().defaultBlockState(), Items.HANGING_ROOTS)));
        });
    }

    public static boolean anyBlockAbove(UseOnContext context) {
        return context.getClickedFace() != Direction.DOWN && context.getLevel().getBlockState(context.getClickedPos().above()).isAir();
    }
}
