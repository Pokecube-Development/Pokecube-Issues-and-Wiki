package thut.core.common.network;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerEvent.StopTracking;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import thut.api.entity.multipart.IMultpart;
import thut.core.common.ThutCore;
import thut.core.common.network.nbtpacket.NBTPacket;
import thut.core.common.network.nbtpacket.PacketAssembly;

public class PartSync extends NBTPacket
{
    public static final PacketAssembly<PartSync> ASSEMBLER = PacketAssembly.registerAssembler(PartSync.class,
            PartSync::new, ThutCore.packets);

    static
    {
        MinecraftForge.EVENT_BUS.addListener(PartSync::onStopTracking);
        MinecraftForge.EVENT_BUS.addListener(PartSync::onStartTracking);
    }

    public static void sendUpdate(final Entity mob)
    {
        if (!(mob.level instanceof ServerLevel)) return;
        if (!(mob instanceof IMultpart<?, ?>)) return;
        sendUpdate(mob, !(mob.isAlive() && mob.isAddedToWorld()));
    }

    public static void sendUpdate(final Entity mob, boolean remove)
    {
        CompoundTag tag = makePacket(mob, remove);
        if (tag != null) PartSync.ASSEMBLER.sendToTracking(new PartSync(tag), mob);
    }

    private static CompoundTag makePacket(Entity mob, boolean remove)
    {
        if (!(mob.level instanceof ServerLevel level)) return null;
        if (!(mob instanceof IMultpart<?, ?> parts)) return null;
        if (parts.getHolder().allParts().isEmpty()) return null;
        final CompoundTag tag = new CompoundTag();
        tag.putInt("i", mob.getId());
        tag.putBoolean("r", remove);

        if (remove)
        {
            int[] arr = new int[parts.getHolder().allParts().size()];
            int i = 0;
            for (var part : parts.getHolder().allParts())
            {
                arr[i++] = part.getId();
            }
            tag.putIntArray("p", arr);
            return tag;
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
            // Clear out the old parts first.
            for (var part : parts.getHolder().allParts())
            {
                partMap.remove(part.getId());
            }
            for (int i = 0; i < arr.length; i++)
            {
                PartEntity<?> part = parts.getHolder().getParts()[i];
                arr[i] = part.getId();
                partMap.put(arr[i], part);
            }
            tag.putIntArray("p", arr);
            return tag;
        }
    }

    private static void onStopTracking(StopTracking event)
    {
        CompoundTag tag = makePacket(event.getTarget(), true);
        if (tag != null && event.getPlayer() instanceof ServerPlayer player)
            PartSync.ASSEMBLER.sendTo(new PartSync(tag), player);
    }

    private static void onStartTracking(StartTracking event)
    {
        CompoundTag tag = makePacket(event.getTarget(), false);
        if (tag != null && event.getPlayer() instanceof ServerPlayer player)
            PartSync.ASSEMBLER.sendTo(new PartSync(tag), player);
    }

    public PartSync()
    {
        super();
    }

    public PartSync(final CompoundTag tag)
    {
        super(tag);
    }

    public PartSync(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    @Override
    protected void onCompleteClient()
    {
        int id = this.getTag().getInt("i");
        int[] arr = this.getTag().getIntArray("p");
        boolean remove = this.getTag().getBoolean("r");
        final net.minecraft.client.multiplayer.ClientLevel world = net.minecraft.client.Minecraft.getInstance().level;
        if (remove)
        {
            Int2ObjectMap<PartEntity<?>> partMap = ObfuscationReflectionHelper
                    .getPrivateValue(net.minecraft.client.multiplayer.ClientLevel.class, world, "partEntities");
            List<PartEntity<?>> list = new ArrayList<>();
            for (int i : arr)
            {
                list.add(partMap.remove(i));
            }
            partMap.clear();
            return;
        }
        Entity mob = world.getEntity(id);

        if (!(mob instanceof IMultpart<?, ?> parts)) return;

        Int2ObjectMap<PartEntity<?>> partMap = ObfuscationReflectionHelper
                .getPrivateValue(net.minecraft.client.multiplayer.ClientLevel.class, world, "partEntities");
        // Clear out the old parts first.
        for (var part : parts.getHolder().allParts())
        {
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
