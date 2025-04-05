package thut.core.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import thut.api.Tracker;
import thut.core.common.ThutCore;
import thut.core.common.network.nbtpacket.NBTPacket;
import thut.core.common.network.nbtpacket.PacketAssembly;

public class GeneralUpdate extends NBTPacket
{
    public static final PacketAssembly<GeneralUpdate> ASSEMBLER = PacketAssembly.registerAssembler(GeneralUpdate.class,
            GeneralUpdate::new, ThutCore.packets);

    public static void init()
    {
        ThutCore.FORGE_BUS.addListener(GeneralUpdate::onTick);
        ThutCore.FORGE_BUS.addListener(GeneralUpdate::onLogin);
    }

    private static void onTick(final PlayerTickEvent.Post event)
    {
        if (event.getEntity() instanceof ServerPlayer player && event.getEntity().tickCount % 1000 == 0)
            GeneralUpdate.sendUpdate(player);
    }

    private static void onLogin(final PlayerLoggedInEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer player) GeneralUpdate.sendUpdate(player);
    }

    private static void sendUpdate(final ServerPlayer player)
    {
        final CompoundTag tag = Tracker.write();
        GeneralUpdate.ASSEMBLER.sendTo(tag, player);
    }

    public static void sendToServer(CompoundTag nbt, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("key", key);
        tag.put("tag", nbt);
        GeneralUpdate.ASSEMBLER.sendToServer(tag);
    }

    public static void sendToTracking(CompoundTag nbt, String key, Entity tracked)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("key", key);
        tag.put("tag", nbt);
        GeneralUpdate.ASSEMBLER.sendToTracking(tag, tracked);
    }

    public GeneralUpdate()
    {
        super();
    }

    @Override
    protected void onCompleteServer(ServerPlayer player)
    {
        Tracker.read(this.getTag(), player);
    }

    @Override
    protected void onCompleteClient(Player player)
    {
        Tracker.read(this.getTag(), null);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("thutcore:general_sync"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
