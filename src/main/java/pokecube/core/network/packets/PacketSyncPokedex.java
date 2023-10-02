package pokecube.core.network.packets;

import java.nio.charset.Charset;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.core.PokecubeCore;
import pokecube.core.database.pokedex.JsonPokedexEntry;
import thut.api.util.JsonUtil;
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
        var packet = new PacketSyncPokedex(JsonPokedexEntry.ENTIRE_DATABASE_CACHE);
        ASSEMBLER.sendTo(packet, event.getPlayer());
    }

    public PacketSyncPokedex()
    {}

    public PacketSyncPokedex(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    public PacketSyncPokedex(String data)
    {
        super(data);
    }

    @Override
    protected void onCompleteClient()
    {
        String resp = new String(this.getData(), Charset.forName("UTF-8"));
        ArrayList<JsonPokedexEntry> list = new ArrayList<>();
        var obj = JsonUtil.gson.fromJson(resp, JsonElement.class);
        if (obj.isJsonArray())
        {
            JsonArray array = obj.getAsJsonArray();
            JsonPokedexEntry.populateFromArray(array, list, new ResourceLocation("pokecube:loaded_from_server"));
        }
        list.forEach(JsonPokedexEntry::loadFromJson);
    }
}
