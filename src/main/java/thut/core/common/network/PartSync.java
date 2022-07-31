package thut.core.common.network;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import thut.api.entity.multipart.IMultpart;
import thut.core.common.ThutCore;

public class PartSync extends NBTPacket
{
    public static final PacketAssembly<PartSync> ASSEMBLER = PacketAssembly.registerAssembler(PartSync.class,
            PartSync::new, ThutCore.packets);

    public static void sendUpdate(final Entity mob)
    {
        if (!(mob.level instanceof ServerLevel level)) return;
        if (!(mob instanceof IMultpart<?, ?> parts)) return;

        final CompoundTag tag = new CompoundTag();
        tag.putInt("i", mob.getId());
        int[] arr = new int[parts.getHolder().getParts().length];

        // Forge manually cleans up the dragonparts more "properly", hence
        // breaking our dynmaic parts. We get around this by manually re-adding
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
        PartSync.ASSEMBLER.sendToTracking(new PartSync(tag), mob);
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

        final net.minecraft.client.multiplayer.ClientLevel world = net.minecraft.client.Minecraft.getInstance().level;
        Entity mob = world.getEntity(id);
        if (!(mob instanceof IMultpart<?, ?> parts)) return;

        Int2ObjectMap<PartEntity<?>> partMap = ObfuscationReflectionHelper
                .getPrivateValue(net.minecraft.client.multiplayer.ClientLevel.class, world, "partEntities");
        // Clear out the old parts first.
        for (var part : parts.getHolder().allParts())
        {
            partMap.remove(part.getId());
        }
        for (int i = 0; i < Math.min(parts.getHolder().getParts().length, arr.length); i++)
        {
            PartEntity<?> part = parts.getHolder().getParts()[i];
            part.setId(arr[i]);
            partMap.put(arr[i], part);
        }
    }
}
