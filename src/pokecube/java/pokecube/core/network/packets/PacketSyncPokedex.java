package pokecube.core.network.packets;

import java.nio.charset.Charset;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.pokedex.JsonPokedexEntry;
import thut.api.util.JsonUtil;
import thut.core.common.network.Packet;
import thut.core.common.network.bigpacket.JsonPacket;
import thut.core.common.network.bigpacket.PacketAssembly;

@EventBusSubscriber
public class PacketSyncPokedex extends JsonPacket
{
    @SubscribeEvent
    public static void onSyncData(OnDatapackSyncEvent event)
    {
        var packet = new PacketSyncPokedex(JsonPokedexEntry.ENTIRE_DATABASE_CACHE);
        ASSEMBLER.sendTo(packet.getData(), event.getPlayer());
    }

    public static final PacketAssembly<PacketSyncPokedex> ASSEMBLER = PacketAssembly
            .registerAssembler(PacketSyncPokedex.class, PacketSyncPokedex::new, PokecubeCore.packets);

    public PacketSyncPokedex()
    {}

    public void read(final FriendlyByteBuf buffer)
    {
        super.read(buffer);
    }

    public PacketSyncPokedex(String data)
    {
        super(data);
    }

    @Override
    protected void onCompleteClient(Player player)
    {
        String resp = new String(this.getData(), Charset.forName("UTF-8"));
        ArrayList<JsonPokedexEntry> list = new ArrayList<>();
        var obj = JsonUtil.gson.fromJson(resp, JsonElement.class);
        if (obj.isJsonArray())
        {
            JsonArray array = obj.getAsJsonArray();
            JsonPokedexEntry.populateFromArray(array, list, ResourceLocation.parse("pokecube:loaded_from_server"));
        }
        list.forEach(JsonPokedexEntry::loadFromJson);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:sync_pokedex"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
