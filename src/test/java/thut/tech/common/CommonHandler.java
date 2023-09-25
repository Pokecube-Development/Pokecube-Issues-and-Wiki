package thut.tech.common;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import thut.core.init.CommonInit;
import thut.tech.Reference;
import thut.tech.common.entity.LiftStickApplier;
import thut.tech.common.network.PacketLift;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.MOD_ID)
public class CommonHandler
{
    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        TechCore.packets.registerMessage(PacketLift.class, PacketLift::new);
        
        CommonInit.HANDLERS.add(new LiftStickApplier());
    }
}
