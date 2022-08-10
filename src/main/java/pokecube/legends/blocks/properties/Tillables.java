package pokecube.legends.blocks.properties;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import pokecube.legends.init.BlockInit;

public class Tillables
{
    private static Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES = Maps.newHashMap();

    public static void registerDefaults()
    {
        Tillables.addTillables(BlockInit.AGED_COARSE_DIRT.get(), Pair.of(a -> true,
                HoeItem.changeIntoState(BlockInit.AGED_DIRT.get().defaultBlockState())));
        Tillables.addTillables(BlockInit.AZURE_COARSE_DIRT.get(), Pair.of(a -> true,
                HoeItem.changeIntoState(BlockInit.AZURE_DIRT.get().defaultBlockState())));
        Tillables.addTillables(BlockInit.CORRUPTED_COARSE_DIRT.get(), Pair.of(a -> true,
                HoeItem.changeIntoState(BlockInit.CORRUPTED_DIRT.get().defaultBlockState())));
        Tillables.addTillables(BlockInit.JUNGLE_COARSE_DIRT.get(), Pair.of(a -> true,
                HoeItem.changeIntoState(BlockInit.JUNGLE_DIRT.get().defaultBlockState())));
        Tillables.addTillables(BlockInit.MUSHROOM_COARSE_DIRT.get(), Pair.of(a -> true,
                HoeItem.changeIntoState(BlockInit.MUSHROOM_DIRT.get().defaultBlockState())));
        Tillables.addTillables(BlockInit.ROOTED_CORRUPTED_DIRT.get(), Pair.of((item) -> { return true; },
                HoeItem.changeIntoStateAndDropItem(BlockInit.CORRUPTED_DIRT.get().defaultBlockState(), Items.HANGING_ROOTS)));
        Tillables.addTillables(BlockInit.ROOTED_MUSHROOM_DIRT.get(), Pair.of((item) -> { return true; },
                HoeItem.changeIntoStateAndDropItem(BlockInit.MUSHROOM_DIRT.get().defaultBlockState(), Items.HANGING_ROOTS)));
    }

    public static void addTillables(@NotNull Block block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> of)
    {
        synchronized (TILLABLES)
        {
            TILLABLES.put(block, of);
        }
    }

    public static void tillables(final BlockEvent.BlockToolModificationEvent event)
    {
        final BlockState state = event.getState();
        final ToolAction toolAction = event.getToolAction();
        if (!event.isSimulated() && toolAction == ToolActions.HOE_TILL)
        {
            var pair = TILLABLES.get(state.getBlock());
            if (pair != null && pair.getFirst().test(event.getContext()))
            {
                pair.getSecond().accept(event.getContext());
                event.getWorld().playSound(event.getPlayer(), event.getPos(), SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                event.setResult(Event.Result.ALLOW);
                event.getPlayer().swing(event.getContext().getHand());
            }
        }
    }
}
