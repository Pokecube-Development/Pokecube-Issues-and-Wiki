package thut.api.entity.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import thut.api.ThutAPI;

public class BreakTestEvent extends BreakEvent
{
    public static final boolean DEBUG = false;

    public static void init()
    {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, BreakTestEvent::handleCancel);
        if (DEBUG) NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, BreakTestEvent::testCancelling);
    }

    public static boolean testBreak(Level world, BlockPos pos, BlockState state, Player player)
    {
        try
        {
            if (player == null && world instanceof ServerLevel level) player = FakePlayerFactory.getMinecraft(level);
            var event = new BreakTestEvent(world, pos, state, player);
            NeoForge.EVENT_BUS.post(event);
            return !event.wasPreCancelled() && !event.isCanceled();
        }
        catch (Exception e)
        {
            ThutAPI.LOGGER.error("Error checking if we can break a block!");
            ThutAPI.LOGGER.error(e);
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
