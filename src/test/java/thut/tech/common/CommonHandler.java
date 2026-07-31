package thut.tech.common;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.core.init.CommonInit;
import thut.tech.Reference;
import thut.tech.common.entity.LiftStickApplier;
import thut.tech.common.network.PacketLift;

@EventBusSubscriber(modid = Reference.MOD_ID)
public class CommonHandler
{
    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        TechCore.packets.registerToServerMessage(PacketLift.class);
        CommonInit.HANDLERS.add(new LiftStickApplier());
    }
}
