package thut.api;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class TickHandler
{
    public static Map<UUID, Integer> playerTickTracker = Maps.newHashMap();

    /**
     * This is used to re-set view bobbing for when a player walks off a block
     * entity.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void PlayerTick(final PlayerTickEvent.Post event)
    {
        if (TickHandler.playerTickTracker.containsKey(event.getEntity().getUUID()))
        {
            final Integer time = TickHandler.playerTickTracker.get(event.getEntity().getUUID());
            if (time < (int) (System.currentTimeMillis() % 2000) - 100)
                Minecraft.getInstance().options.bobView().set(true);
        }
        /*
         * This deals with the massive hunger reduction for standing on the
         * block entities.
         */
        if (event.getEntity().level().isClientSide())
            if (event.getEntity().tickCount == event.getEntity().getPersistentData().getInt("lastStandTick") + 1)
                event.getEntity().setOnGround(true);
    }
}
