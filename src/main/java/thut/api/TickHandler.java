package thut.api;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class TickHandler
{
    public static Map<UUID, Integer> playerTickTracker = Maps.newHashMap();

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    /**
     * This is used to re-set view bobbing for when a player walks off a block
     * entity.
     */
    public static void PlayerTick(final PlayerTickEvent event)
    {
        if (event.phase == Phase.END && TickHandler.playerTickTracker.containsKey(event.player.getUniqueID()))
        {
            final Integer time = TickHandler.playerTickTracker.get(event.player.getUniqueID());
            if (time < (int) (System.currentTimeMillis() % 2000) - 100) Minecraft
                    .getInstance().gameSettings.viewBobbing = true;
        }
        /**
         * This deals with the massive hunger reduction for standing on the
         * block entities.
         */
        if (event.phase == Phase.END && event.side == LogicalSide.CLIENT) if (event.player.ticksExisted == event.player
                .getPersistentData().getInt("lastStandTick") + 1) event.player.onGround = true;
    }
}
