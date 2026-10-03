package pokecube.core.network.packets;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.pokedex.EvolutionDataLoader;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.JsonPokedexEntry;
import pokecube.core.database.spawns.PokemobSpawns;
import thut.api.data.DataHelpers;
import thut.api.data.StringTag;
import thut.api.util.JsonUtil;
import thut.core.common.network.Packet;
import thut.core.common.network.bigpacket.JsonPacket;
import thut.core.common.network.bigpacket.PacketAssembly;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@EventBusSubscriber
public class PacketSyncPokedex extends JsonPacket
{
    public static Map<String, Consumer<JsonElement>> PACKET_PROCESSORS = new HashMap<>();
    public static Map<String, Supplier<JsonElement>> DATA_PROVIDERS = new HashMap<>();
    public static List<String> DATA_ORDER = new ArrayList<>();

    public static void register(String key, Consumer<JsonElement> deserialiser, Supplier<JsonElement> serialiser,
            boolean insertFirst)
    {
        PACKET_PROCESSORS.put(key, deserialiser);
        DATA_PROVIDERS.put(key, serialiser);
        if (insertFirst) DATA_ORDER.addFirst(key);
        else DATA_ORDER.addLast(key);
    }

    /**
     * Called during FMLLoadCompleteEvent
     */
    public static void init()
    {
        // Add the string tags
        DataHelpers.tagHelpers.forEach(e -> {
            if (e instanceof StringTag<?> dataType)
            {
                String key = dataType.getKey();
                Consumer<JsonElement> deser = dataType::handleSync;
                Supplier<JsonElement> ser = dataType::makeForSync;
                register(key, deser, ser, false);
            }
        });

        // Now add some manually defined data types

        // Evolution Data
        DataHelpers.ResourceData dataType = EvolutionDataLoader.INSTANCE;
        String key = dataType.getKey();
        Consumer<JsonElement> deser = dataType::handleSync;
        Supplier<JsonElement> ser = dataType::makeForSync;
        register(key, deser, ser, false);

        // Spawns data
        dataType = PokemobSpawns.INSTANCE;
        key = dataType.getKey();
        deser = dataType::handleSync;
        ser = dataType::makeForSync;
        register(key, deser, ser, false);

        // Then add the processor for the pokedex values
        Consumer<JsonElement> POKEDEX_HANDLER = json -> {
            var obj = JsonUtil.gson.fromJson(json.getAsString(), JsonElement.class);
            ArrayList<JsonPokedexEntry> list = new ArrayList<>();
            if (obj.isJsonArray())
            {
                JsonArray array = obj.getAsJsonArray();
                JsonPokedexEntry.populateFromArray(array, list, ResourceLocation.parse("pokecube:loaded_from_server"));
            }
            list.forEach(JsonPokedexEntry::loadFromJson);
            if (ServerLifecycleHooks.getCurrentServer() == null)
                Database.getSortedFormes().forEach(PokedexEntry::onResourcesReloaded);
        };
        Supplier<JsonElement> POKEDEX_PROVIDER = () -> new JsonPrimitive(JsonPokedexEntry.ENTIRE_DATABASE_CACHE);
        register("pokecube:pokedex_entries", POKEDEX_HANDLER, POKEDEX_PROVIDER, false);
    }

    private static String toSend = null;

    public static void resetData()
    {
        JsonObject send = new JsonObject();
        DATA_ORDER.forEach(key -> {
            send.add(key, DATA_PROVIDERS.get(key).get());
        });
        toSend = JsonUtil.smol_gson.toJson(send);
    }

    @SubscribeEvent
    public static void onSyncData(OnDatapackSyncEvent event)
    {
        if (event.getPlayer() == null) return;
        if (event.getPlayer().isLocalPlayer()) return;
        if (toSend == null) resetData();
        var packet = new PacketSyncPokedex(toSend);
        ASSEMBLER.sendTo(packet.getData(), event.getPlayer());
    }

    public static final PacketAssembly<PacketSyncPokedex> ASSEMBLER = PacketAssembly.registerAssembler(
            PacketSyncPokedex.class, PacketSyncPokedex::new, PokecubeCore.packets);

    public PacketSyncPokedex()
    {}

    public PacketSyncPokedex(String data)
    {
        super(data);
    }

    @Override
    protected void onCompleteClient(Player player)
    {
        String resp = new String(this.getData(), StandardCharsets.UTF_8);
        ArrayList<JsonPokedexEntry> list = new ArrayList<>();
        var data = JsonUtil.gson.fromJson(resp, JsonElement.class);
        // Legacy processing
        if (data.isJsonArray())
        {
            var array = data.getAsJsonArray();
            JsonPokedexEntry.populateFromArray(array, list, ResourceLocation.parse("pokecube:loaded_from_server"));
            list.forEach(JsonPokedexEntry::loadFromJson);
            if (ServerLifecycleHooks.getCurrentServer() == null)
                Database.getSortedFormes().forEach(PokedexEntry::onResourcesReloaded);
        }
        // New Processing
        if (data.isJsonObject())
        {
            var obj = data.getAsJsonObject();
            DATA_ORDER.forEach(key -> {
                if (obj.has(key))
                {
                    var e = obj.get(key);
                    PACKET_PROCESSORS.get(key).accept(e);
                }
            });
        }
    }

    private final static Type<Packet> TYPE = new Type<>(ResourceLocation.parse("pokecube:sync_pokedex"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
