package thut.core.common.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import thut.api.Tracker;
import thut.core.common.ThutCore;

public class GeneralUpdate extends NBTPacket
{
    public static final PacketAssembly<GeneralUpdate> ASSEMBLER = PacketAssembly.registerAssembler(GeneralUpdate.class,
            GeneralUpdate::new, ThutCore.packets);

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(GeneralUpdate::onTick);
        MinecraftForge.EVENT_BUS.addListener(GeneralUpdate::onLogin);
    }

    private static void onTick(final PlayerTickEvent event)
    {
        if (event.player instanceof ServerPlayer && event.player.tickCount % 1000 == 0
                && event.phase == Phase.END) GeneralUpdate.sendUpdate((ServerPlayer) event.player);
    }

    private static void onLogin(final PlayerLoggedInEvent event)
    {
        if (event.getPlayer() instanceof ServerPlayer) GeneralUpdate.sendUpdate((ServerPlayer) event
                .getPlayer());
    }

    private static void sendUpdate(final ServerPlayer player)
    {
        final CompoundTag tag = Tracker.write();
        GeneralUpdate.ASSEMBLER.sendTo(new GeneralUpdate(tag), player);
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
    protected void onCompleteClient()
    {
        Tracker.read(this.getTag());
    }
}
