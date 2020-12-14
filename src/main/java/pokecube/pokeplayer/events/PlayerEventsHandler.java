package pokecube.pokeplayer.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.SaveToFile;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import pokecube.core.network.packets.PacketDataSync;
import pokecube.pokeplayer.Reference;
import pokecube.pokeplayer.data.DataSyncWrapper;
import thut.core.common.handlers.PlayerDataHandler;

@EventBusSubscriber
public class PlayerEventsHandler
{
    public static final ResourceLocation DATACAP = new ResourceLocation(Reference.ID, "data");

    @SubscribeEvent
    public static void startTracking(final StartTracking event)
    {
        if (event.getTarget() instanceof ServerPlayerEntity && event.getPlayer() instanceof ServerPlayerEntity)
            PacketDataSync.sendUpdatePacket((ServerPlayerEntity) event.getTarget(), (ServerPlayerEntity) event
                    .getPlayer(), "pokeplayer-data");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onEntityCapabilityAttach(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof PlayerEntity && !event.getCapabilities().containsKey(
                PlayerEventsHandler.DATACAP)) event.addCapability(PlayerEventsHandler.DATACAP, new DataSyncWrapper());
    }

    @SubscribeEvent
    public static void savePlayer(final SaveToFile event)
    {
        final PlayerEntity player = (PlayerEntity) event.getEntity();
        PlayerDataHandler.getInstance().save(player.getCachedUniqueIdString(), "pokeplayer-data");
    }
}
