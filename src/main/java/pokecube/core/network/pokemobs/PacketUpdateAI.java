package pokecube.core.network.pokemobs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.INBTSerializable;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import thut.api.Tracker;
import thut.api.Tracker.UpdateHandler;
import thut.api.entity.ai.IAIRunnable;
import thut.core.common.network.GeneralUpdate;
import thut.core.common.network.Packet;

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
                Entity e = PokecubeAPI.getEntityProvider().getEntity(player.getLevel(), id, true);
                if (e != null) e.getPersistentData().putString("pokecube:mega_mode", mode);
            }
        }
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

    public static void sendUpdatePacket(IPokemob pokemob, IAIRunnable ai)
    {
        final CompoundTag tag = new CompoundTag();
        final Tag base = INBTSerializable.class.cast(ai).serializeNBT();
        tag.put(ai.getIdentifier(), base);
        final PacketUpdateAI packet = new PacketUpdateAI();
        packet.data = tag;
        packet.entityId = pokemob.getEntity().getId();
        PokecubeCore.packets.sendToServer(packet);
    }

    public int entityId;

    public CompoundTag data = new CompoundTag();

    public PacketUpdateAI()
    {}

    public PacketUpdateAI(final FriendlyByteBuf buffer)
    {
        this.entityId = buffer.readInt();
        this.data = buffer.readNbt();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleServer(final ServerPlayer player)
    {
        final int id = this.entityId;
        final CompoundTag data = this.data;
        final Entity e = PokecubeAPI.getEntityProvider().getEntity(player.getLevel(), id, true);
        final IPokemob pokemob = PokemobCaps.getPokemobFor(e);
        if (pokemob != null) for (final IAIRunnable runnable : pokemob.getTasks())
            if (runnable instanceof INBTSerializable && data.contains(runnable.getIdentifier()))
        {
            INBTSerializable.class.cast(runnable).deserializeNBT(data.get(runnable.getIdentifier()));
            break;
        }
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeInt(this.entityId);
        buffer.writeNbt(this.data);
    }
}
