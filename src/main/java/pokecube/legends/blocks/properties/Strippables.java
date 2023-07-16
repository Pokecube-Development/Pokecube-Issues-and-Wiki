package pokecube.legends.blocks.properties;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import pokecube.legends.init.BlockInit;

public class Strippables
{
    private static Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> STRIPPABLES = Maps.newHashMap();

    public static void registerDefaults()
    {
        Strippables.addStrippables(BlockInit.AGED_LOG.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_AGED_LOG.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.AGED_WOOD.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_AGED_WOOD.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.CONCRETE_LOG.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_CONCRETE_LOG.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.CONCRETE_WOOD.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_CONCRETE_WOOD.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.CORRUPTED_LOG.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_CORRUPTED_LOG.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.CORRUPTED_WOOD.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_CORRUPTED_WOOD.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.DISTORTIC_LOG.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_DISTORTIC_LOG.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.DISTORTIC_WOOD.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_DISTORTIC_WOOD.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.INVERTED_LOG.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_INVERTED_LOG.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.INVERTED_WOOD.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_INVERTED_WOOD.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.MIRAGE_LOG.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_MIRAGE_LOG.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.MIRAGE_WOOD.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_MIRAGE_WOOD.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.TEMPORAL_LOG.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_TEMPORAL_LOG.get().defaultBlockState())));
        Strippables.addStrippables(BlockInit.TEMPORAL_WOOD.get(), Pair.of(a -> true,
                Strippables.changeIntoState(BlockInit.STRIP_TEMPORAL_WOOD.get().defaultBlockState())));
    }

    public static void addStrippables(@NotNull Block log, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> strippedLog)
    {
        synchronized (STRIPPABLES)
        {
            STRIPPABLES.put(log, strippedLog);
        }
    }

    public static void strippables(final BlockEvent.BlockToolModificationEvent event)
    {
        final BlockState state = event.getState();
//        final BlockPlaceContext context = event.getState();
        final ToolAction toolAction = event.getToolAction();
        if (!event.isSimulated() && toolAction == ToolActions.AXE_STRIP)
        {
            var pair = STRIPPABLES.get(state.getBlock());
            if (pair != null && pair.getFirst().test(event.getContext()))
            {
                pair.getSecond().accept(event.getContext());
                event.getWorld().playSound(event.getPlayer(), event.getPos(), SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                event.setResult(Event.Result.ALLOW);
                event.getPlayer().swing(event.getContext().getHand());
            }
        }
    }

    public static Consumer<UseOnContext> changeIntoState(BlockState state) {
        return (useOnContext) -> {
            useOnContext.getLevel().setBlock(useOnContext.getClickedPos(), state.setValue(RotatedPillarBlock.AXIS,
                    useOnContext.getLevel().getBlockState(useOnContext.getClickedPos()).getValue(RotatedPillarBlock.AXIS)), 11);
        };
    }
}
