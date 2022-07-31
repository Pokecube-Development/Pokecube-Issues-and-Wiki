package pokecube.legends.blocks.properties;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.world.BlockEvent;
import pokecube.legends.init.BlockInit;

public class Strippables
{
    public static void strippables(final BlockEvent.BlockToolModificationEvent event)
    {
        final ToolAction toolAction = event.getToolAction();
        final BlockState state = event.getState();
        if (!event.isSimulated() && toolAction == ToolActions.AXE_STRIP)
        {
            if (state.is(BlockInit.AGED_LOG.get()))
            {
                event.setFinalState(BlockInit.STRIP_AGED_LOG.get().defaultBlockState());
            } else if (state.is(BlockInit.AGED_WOOD.get()))
            {
                event.setFinalState(BlockInit.STRIP_AGED_WOOD.get().defaultBlockState());
            } else if (state.is(BlockInit.CONCRETE_LOG.get()))
            {
                event.setFinalState(BlockInit.STRIP_CONCRETE_LOG.get().defaultBlockState());
            } else if (state.is(BlockInit.CONCRETE_WOOD.get()))
            {
                event.setFinalState(BlockInit.STRIP_CONCRETE_WOOD.get().defaultBlockState());
            } else if (state.is(BlockInit.CORRUPTED_LOG.get()))
            {
                event.setFinalState(BlockInit.STRIP_CORRUPTED_LOG.get().defaultBlockState());
            } else if (state.is(BlockInit.CORRUPTED_WOOD.get()))
            {
                event.setFinalState(BlockInit.STRIP_CORRUPTED_WOOD.get().defaultBlockState());
            } else if (state.is(BlockInit.DISTORTIC_LOG.get()))
            {
                event.setFinalState(BlockInit.STRIP_DISTORTIC_LOG.get().defaultBlockState());
            } else if (state.is(BlockInit.DISTORTIC_WOOD.get()))
            {
                event.setFinalState(BlockInit.STRIP_DISTORTIC_WOOD.get().defaultBlockState());
            } else if (state.is(BlockInit.INVERTED_LOG.get()))
            {
                event.setFinalState(BlockInit.STRIP_INVERTED_LOG.get().defaultBlockState());
            } else if (state.is(BlockInit.INVERTED_WOOD.get()))
            {
                event.setFinalState(BlockInit.STRIP_INVERTED_WOOD.get().defaultBlockState());
            } else if (state.is(BlockInit.MIRAGE_LOG.get()))
            {
                event.setFinalState(BlockInit.STRIP_MIRAGE_LOG.get().defaultBlockState());
            } else if (state.is(BlockInit.MIRAGE_WOOD.get()))
            {
                event.setFinalState(BlockInit.STRIP_MIRAGE_WOOD.get().defaultBlockState());
            } else if (state.is(BlockInit.TEMPORAL_LOG.get()))
            {
                event.setFinalState(BlockInit.STRIP_TEMPORAL_LOG.get().defaultBlockState());
            } else if (state.is(BlockInit.TEMPORAL_WOOD.get()))
            {
                event.setFinalState(BlockInit.STRIP_TEMPORAL_WOOD.get().defaultBlockState());
            }
        }
    }
}
