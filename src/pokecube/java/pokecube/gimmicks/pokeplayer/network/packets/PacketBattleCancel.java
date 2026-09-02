package pokecube.gimmicks.pokeplayer.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import pokecube.api.PokecubeAPI;
import pokecube.api.blocks.IHealer;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import thut.api.ThutCaps;
import thut.api.entity.EntityProvider;
import thut.core.common.network.Packet;

/// Very barebones packet, if you would like a more complex example see
/// PacketPokemobGui or PacketTMs in pokecube.core.network.
/// More specifically, this packet is sent from client to server to allow
/// pokeplayers to end all battles they are in.
/// This is needed as using moves happens involuntarily in a battle
/// and so could waste hunger.
public class PacketBattleCancel extends Packet
{
    /// Empty constructor in this case.
    public PacketBattleCancel()
    {}

    /// Functions like these are good for implicit exception handling
    public static void sendCancelPacket()
    {
        try {
            PokecubeCore.packets.sendToServer(new PacketBattleCancel());
        }
        catch (Exception e) {
            PokecubeAPI.logInfo("Pokeplayer cancelling Battle has failed! Check for key conflicts.");
        }
    }


    /// No need to read data from the buffer in this case.
    public void read(final FriendlyByteBuf buffer)
    {}

    @Override
    /// Handling for when the packet is received on the server.
    /// Here, a method is called for cancelling all battles as a pokeplayer.
    public void handleServer(final ServerPlayer player)
    {
        var copy = ThutCaps.getCopyMob(player);
        if (copy == null) return;
        LivingEntity pokemob = copy.getCopiedMob();
        if (pokemob == null) return;

        BrainUtils.deagro(pokemob);
    }

    @Override
    /// No need to write to the data buffer in this case.
    public void write(final FriendlyByteBuf buffer)
    {}

    /// Type of packet. No extra data required, just make a unique name (and make sure it starts with pokecube:, the game crashes otherwise)
    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("pokecube:battle_cancel"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
