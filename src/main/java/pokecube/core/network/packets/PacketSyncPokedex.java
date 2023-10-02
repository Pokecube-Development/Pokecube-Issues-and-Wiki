package pokecube.core.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.core.PokecubeCore;
import thut.core.common.network.bigpacket.JsonPacket;
import thut.core.common.network.bigpacket.PacketAssembly;

@Mod.EventBusSubscriber
public class PacketSyncPokedex extends JsonPacket
{
    public static final PacketAssembly<PacketSyncPokedex> ASSEMBLER = PacketAssembly
            .registerAssembler(PacketSyncPokedex.class, PacketSyncPokedex::new, PokecubeCore.packets);

    @SubscribeEvent
    public static void onSyncData(OnDatapackSyncEvent event)
    {
//        ASSEMBLER.sendTo(new PacketSyncPokedex(PokemobsDatabases.compound), event.getPlayer());
    }

    public PacketSyncPokedex()
    {}

    public PacketSyncPokedex(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    @Override
    protected void onCompleteClient()
    {
        
    }
}
