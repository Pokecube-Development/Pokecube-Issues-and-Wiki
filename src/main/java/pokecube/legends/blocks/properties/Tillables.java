package pokecube.legends.blocks.properties;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.world.BlockEvent;
import pokecube.legends.init.BlockInit;

public class Tillables
{
    public static void tillables(final BlockEvent.BlockToolModificationEvent event)
    {
        final BlockState state = event.getState();
        final ToolAction toolAction = event.getToolAction();
        if (!event.isSimulated() && toolAction == ToolActions.HOE_TILL)
        {
            if (state.is(BlockInit.AGED_COARSE_DIRT.get()))
            {
                event.setFinalState(BlockInit.AGED_DIRT.get().defaultBlockState());
            } else if (state.is(BlockInit.AZURE_COARSE_DIRT.get()))
            {
                event.setFinalState(BlockInit.AZURE_DIRT.get().defaultBlockState());
            } else if (state.is(BlockInit.CORRUPTED_COARSE_DIRT.get()))
            {
                event.setFinalState(BlockInit.CORRUPTED_DIRT.get().defaultBlockState());
            } else if (state.is(BlockInit.JUNGLE_COARSE_DIRT.get()))
            {
                event.setFinalState(BlockInit.JUNGLE_DIRT.get().defaultBlockState());
            } else if (state.is(BlockInit.MUSHROOM_COARSE_DIRT.get()))
            {
                event.setFinalState(BlockInit.MUSHROOM_DIRT.get().defaultBlockState());
            } else if (state.is(BlockInit.ROOTED_CORRUPTED_DIRT.get()))
            {
                event.setFinalState(BlockInit.CORRUPTED_DIRT.get().defaultBlockState());
                HoeItem.changeIntoStateAndDropItem(BlockInit.CORRUPTED_DIRT.get().defaultBlockState(), Items.HANGING_ROOTS);
            } else if (state.is(BlockInit.ROOTED_MUSHROOM_DIRT.get()))
            {
                event.setFinalState(BlockInit.MUSHROOM_DIRT.get().defaultBlockState());
                HoeItem.changeIntoStateAndDropItem(BlockInit.MUSHROOM_DIRT.get().defaultBlockState(), Items.HANGING_ROOTS);
            }
        }
    }
}
