package thut.core.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
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
        if (event.player instanceof ServerPlayerEntity) GeneralUpdate.sendUpdate((ServerPlayerEntity) event.player);
    }

    private static void onLogin(final PlayerLoggedInEvent event)
    {
        if (event.getPlayer() instanceof ServerPlayerEntity && event.getPlayer().tickCount % 1000 == 0) GeneralUpdate
                .sendUpdate((ServerPlayerEntity) event.getPlayer());
    }

    private static void sendUpdate(final ServerPlayerEntity player)
    {
        final CompoundNBT tag = Tracker.write();
        ThutCore.packets.sendTo(new GeneralUpdate(tag), player);
    }

    public GeneralUpdate()
    {
        super();
    }

    public GeneralUpdate(final CompoundNBT tag)
    {
        super(tag);
    }

    public GeneralUpdate(final PacketBuffer buffer)
    {
        super(buffer);
    }

    @Override
    protected void onCompleteClient()
    {
        Tracker.read(this.getTag());
    }
}
