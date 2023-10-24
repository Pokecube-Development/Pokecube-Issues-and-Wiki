package thut.api.entity.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import thut.core.common.ThutCore;

public class BreakTestEvent extends BreakEvent
{
    public static final boolean DEBUG = false;

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, BreakTestEvent::handleCancel);
        if (DEBUG) MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, BreakTestEvent::testCancelling);
    }

    public static boolean testBreak(Level world, BlockPos pos, BlockState state, Player player)
    {
        try
        {
            var event = new BreakTestEvent(world, pos, state, player);
            MinecraftForge.EVENT_BUS.post(event);
            return !event.wasPreCancelled();
        }
        catch (Exception e)
        {
            ThutCore.LOGGER.error("Error checking if we can break a block!");
            ThutCore.LOGGER.error(e);
            return false;
        }
    }

    private static void testCancelling(BreakEvent test)
    {
        test.setCanceled(true);
    }

    private static void handleCancel(BreakTestEvent event)
    {
        if (event.isCanceled()) event.setPreCancelled();
        event.setCanceled(true);
    }

    private boolean preCancelled = false;

    private BreakTestEvent(Level world, BlockPos pos, BlockState state, Player player)
    {
        super(world, pos, state, player);
    }

    public void setPreCancelled()
    {
        preCancelled = true;
    }

    public boolean wasPreCancelled()
    {
        return preCancelled;
    }
}
