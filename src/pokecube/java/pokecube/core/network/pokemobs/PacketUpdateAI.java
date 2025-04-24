package pokecube.core.network.pokemobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import thut.api.Tracker;
import thut.api.Tracker.UpdateHandler;
import thut.core.common.network.GeneralUpdate;
import thut.core.common.network.Packet;

import java.util.HashSet;
import java.util.Set;

public class PacketUpdateAI extends Packet
{
    public static class MegaModeHandler implements UpdateHandler
    {
        @Override
        public String getKey()
        {
            return "pokeube:mega_mode";
        }

        @Override
        public void read(CompoundTag nbt, ServerPlayer player)
        {
            if (player != null)
            {
                int id = nbt.getInt("I");
                String mode = nbt.getString("M");
                Entity e = PokecubeAPI.getEntityProvider().getEntity(player.level(), id, true);
                if (e != null) e.getPersistentData().putString("pokecube:mega_mode", mode);
            }
        }
    }

    public static final Set<String> ALLOWED_SYNC = new HashSet<>();

    static
    {
        ALLOWED_SYNC.add("pokecube:storage_ai");
    }

    public static MegaModeHandler MODE_HANDLER = new MegaModeHandler();

    public static void init()
    {
        Tracker.HANDLERS.put(MODE_HANDLER.getKey(), MODE_HANDLER);
    }

    public static void sendMegaModePacket(IPokemob pokemob, String mode)
    {
        CompoundTag nbt = new CompoundTag();
        String key = MODE_HANDLER.getKey();
        nbt.putInt("I", pokemob.getEntity().getId());
        nbt.putString("M", mode);
        GeneralUpdate.sendToServer(nbt, key);
    }

    public static void sendUpdatePacket(IPokemob pokemob, INBTSerializable<?> ai, String key)
    {
        final CompoundTag tag = new CompoundTag();
        tag.put(key, ai.serializeNBT(pokemob.getEntity().registryAccess()));
        final PacketUpdateAI packet = new PacketUpdateAI();
        packet.data = tag;
        packet.entityId = pokemob.getEntity().getId();
        PokecubeCore.packets.sendToServer(packet);
    }

    public int entityId;

    public CompoundTag data = new CompoundTag();

    public PacketUpdateAI()
    {}

    public void read(final FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
        this.data = buffer.readNbt();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void handleServer(final ServerPlayer player)
    {
        final int id = this.entityId;
        final CompoundTag data = this.data;
        final Entity e = PokecubeAPI.getEntityProvider().getEntity(player.level(), id, true);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(e);
        var reg = player.registryAccess();
        if (pokemob != null)
        {
            for (String key : data.getAllKeys())
            {
                if (ALLOWED_SYNC.contains(key)) continue;
                try
                {
                    ResourceLocation loc = ResourceLocation.parse(key);
                    var type = NeoForgeRegistries.ATTACHMENT_TYPES.get(loc);
                    if (type != null)
                    {
                        var _data = e.getData(type);
                        if (_data instanceof INBTSerializable ser) ser.deserializeNBT(reg, data.get(key));
                    }
                }
                catch (Exception ex)
                {
                    PokecubeAPI.LOGGER.error("Error loading tag {}", key, ex);
                }
            }
        }
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeNbt(this.data);
    }

    private final static Type<Packet> TYPE = new Type<>(ResourceLocation.parse("pokecube:pokemob_ai_update"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
