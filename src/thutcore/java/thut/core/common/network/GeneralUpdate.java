package thut.core.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
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

    private static void onTick(final PlayerTickEvent event)
    {
        if (event.player instanceof ServerPlayer player && event.player.tickCount % 1000 == 0
                && event.phase == Phase.END)
            GeneralUpdate.sendUpdate(player);
    }

    private static void onLogin(final PlayerLoggedInEvent event)
    {
        if (event.getPlayer() instanceof ServerPlayer player) GeneralUpdate.sendUpdate(player);
    }

    private static void sendUpdate(final ServerPlayer player)
    {
        final CompoundTag tag = Tracker.write();
        GeneralUpdate.ASSEMBLER.sendTo(new GeneralUpdate(tag), player);
    }

    public static void sendToServer(CompoundTag nbt, String key)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("key", key);
        tag.put("tag", nbt);
        GeneralUpdate.ASSEMBLER.sendToServer(new GeneralUpdate(tag));
    }

    public static void sendToTracking(CompoundTag nbt, String key, Entity tracked)
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("key", key);
        tag.put("tag", nbt);
        GeneralUpdate.ASSEMBLER.sendToTracking(new GeneralUpdate(tag), tracked);
    }

    public GeneralUpdate()
    {
        super();
    }

    public GeneralUpdate(final CompoundTag tag)
    {
        super(tag);
    }

    public GeneralUpdate(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    @Override
    protected void onCompleteServer(ServerPlayer player)
    {
        Tracker.read(this.getTag(), player);
    }

    @Override
    protected void onCompleteClient()
    {
        Tracker.read(this.getTag(), null);
    }
}
