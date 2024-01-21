package thut.core.common.network;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerEvent.StopTracking;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import thut.api.entity.EntityProvider;
import thut.api.entity.multipart.IMultpart;
import thut.core.common.ThutCore;
import thut.core.common.network.bigpacket.BigPacket;
import thut.core.common.network.bigpacket.PacketAssembly;

public class PartSync extends BigPacket
{
    public static final PacketAssembly<PartSync> ASSEMBLER = PacketAssembly.registerAssembler(PartSync.class,
            PartSync::new, ThutCore.packets);

    static
    {
        ThutCore.FORGE_BUS.addListener(PartSync::onStopTracking);
        ThutCore.FORGE_BUS.addListener(PartSync::onStartTracking);
    }

    public static void sendUpdate(final Entity mob)
    {
        if (!(mob.level instanceof ServerLevel)) return;
        if (!(mob instanceof IMultpart<?, ?>)) return;
        sendUpdate(mob, !mob.isAddedToWorld());
    }

    public static void sendUpdate(final Entity mob, boolean remove)
    {
        byte[] tag = makePacket(mob, remove);
        if (tag != null) PartSync.ASSEMBLER.sendToTracking(new PartSync(tag), mob);
    }

    private static byte[] makePacket(Entity mob, boolean remove)
    {
        if (!(mob.level instanceof ServerLevel level)) return null;
        if (!(mob instanceof IMultpart<?, ?> parts)) return null;
        if (parts.getHolder().allParts().isEmpty()) return null;

        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeInt(mob.getId());
        buffer.writeBoolean(remove);

        if (remove)
        {
            int[] arr = new int[parts.getHolder().allParts().size()];
            int i = 0;
            for (var part : parts.getHolder().allParts())
            {
                arr[i++] = part.getId();
            }
            buffer.writeVarIntArray(arr);
            return buffer.array();
        }
        else
        {
            int[] arr = new int[parts.getHolder().getParts().length];
            // Forge manually cleans up the dragonparts more "properly", hence
            // breaking our dynmaic parts. We get around this by manually
            // re-adding
            // them whenever this changes.
            Int2ObjectMap<PartEntity<?>> partMap = ObfuscationReflectionHelper.getPrivateValue(ServerLevel.class, level,
                    "f_143247_");
            var old = new ArrayList<>(level.getPartEntities());
            // Clear out the old parts first.
            for (var part : old)
            {
                if (part.getParent() == null || part.getParent() == mob || part.getParent().isRemoved())
                    partMap.remove(part.getId());
            }
            for (int i = 0; i < arr.length; i++)
            {
                PartEntity<?> part = parts.getHolder().getParts()[i];
                arr[i] = part.getId();
                partMap.put(arr[i], part);
            }
            buffer.writeVarIntArray(arr);
            return buffer.array();
        }
    }

    private static void onStopTracking(StopTracking event)
    {
        byte[] tag = makePacket(event.getTarget(), true);
        if (tag != null && event.getPlayer() instanceof ServerPlayer player)
            PartSync.ASSEMBLER.sendTo(new PartSync(tag), player);
    }

    private static void onStartTracking(StartTracking event)
    {
        byte[] tag = makePacket(event.getTarget(), false);
        if (tag != null && event.getPlayer() instanceof ServerPlayer player)
            PartSync.ASSEMBLER.sendTo(new PartSync(tag), player);
    }

    public PartSync()
    {
        super();
    }

    public PartSync(final byte[] tag)
    {
        super();
        this.setData(tag);
    }

    public PartSync(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient()
    {
        var buffer = new FriendlyByteBuf(Unpooled.copiedBuffer(this.getData()));
        final net.minecraft.client.multiplayer.ClientLevel world = net.minecraft.client.Minecraft.getInstance().level;
        int id = buffer.readInt();
        boolean remove = buffer.readBoolean();
        int[] arr = buffer.readVarIntArray();
        Int2ObjectMap<PartEntity<?>> partMap = ObfuscationReflectionHelper
                .getPrivateValue(net.minecraft.client.multiplayer.ClientLevel.class, world, "partEntities");
        if (remove)
        {
            List<PartEntity<?>> list = new ArrayList<>();
            for (int i : arr)
            {
                list.add(partMap.remove(i));
            }
            return;
        }
        Entity mob = EntityProvider.provider.getEntity(world, id);

        if (!(mob instanceof IMultpart<?, ?> parts)) return;

        // Clear out the old parts first.
        var old = new ArrayList<>(world.getPartEntities());
        // Clear out the old parts first.
        for (var part : old)
        {
            if (part.getParent() == null || part.getParent() == mob || part.getParent().isRemoved())
                partMap.remove(part.getId());
        }
        if (remove) return;
        for (int i = 0; i < Math.min(parts.getHolder().getParts().length, arr.length); i++)
        {
            PartEntity<?> part = parts.getHolder().getParts()[i];
            part.setId(arr[i]);
            partMap.put(arr[i], part);
        }
    }
}
