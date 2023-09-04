package thut.core.common.network;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import thut.core.common.ThutCore;
import thut.core.common.network.nbtpacket.NBTPacket;
import thut.core.common.network.nbtpacket.PacketAssembly;
import thut.lib.RegHelper;

public class CapabilitySync extends NBTPacket
{
    private static Method GETMOBCAPS;
    private static Field CAPWRITERS;
    private static Field CAPNAMES;

    static
    {
        try
        {
            CapabilitySync.GETMOBCAPS = CapabilityProvider.class.getDeclaredMethod("getCapabilities");
            CapabilitySync.GETMOBCAPS.setAccessible(true);

            CapabilitySync.CAPWRITERS = CapabilityDispatcher.class.getDeclaredField("writers");
            CapabilitySync.CAPWRITERS.setAccessible(true);

            CapabilitySync.CAPNAMES = CapabilityDispatcher.class.getDeclaredField("names");
            CapabilitySync.CAPNAMES.setAccessible(true);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static final Set<String> TO_SYNC = Sets.newHashSet();

    public static final PacketAssembly<CapabilitySync> ASSEMBLER = PacketAssembly
            .registerAssembler(CapabilitySync.class, CapabilitySync::new, ThutCore.packets);

    private static CapabilitySync makePacket(final Entity entity, final Set<String> toSync)
    {
        if (entity.level().isClientSide)
        {
            ThutCore.LOGGER.error("Packet sent on wrong side!", new IllegalArgumentException());
            return null;
        }
        final CompoundTag tag = new CompoundTag();
        tag.putInt("id", entity.getId());
        final CompoundTag nbt = new CompoundTag();
        try
        {
            final CapabilityDispatcher disp = (CapabilityDispatcher) CapabilitySync.GETMOBCAPS.invoke(entity);
            @SuppressWarnings("unchecked")
            final INBTSerializable<Tag>[] writers = (INBTSerializable<Tag>[]) CapabilitySync.CAPWRITERS.get(disp);
            final String[] names = (String[]) CapabilitySync.CAPNAMES.get(disp);
            for (int x = 0; x < writers.length; x++)
                if (toSync.contains(names[x])) nbt.put(names[x], writers[x].serializeNBT());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        if (nbt.isEmpty()) return null;
        tag.put("tag", nbt);
        return new CapabilitySync(tag);
    }

    public static void sendUpdate(final Entity entity)
    {
        CapabilitySync.sendUpdate(entity, CapabilitySync.TO_SYNC);
    }

    public static void sendUpdate(final Entity entity, final Set<String> toSync)
    {
        final CapabilitySync message = CapabilitySync.makePacket(entity, toSync);
        if (message != null)
        {
            CapabilitySync.ASSEMBLER.sendToTracking(message, entity);
            if (entity instanceof ServerPlayer player) CapabilitySync.ASSEMBLER.sendTo(message, player);
        }
    }

    private static void onJoinWorld(final EntityJoinLevelEvent event)
    {
        if (event.getLevel().isClientSide()) return;
        if (event.getLevel() instanceof ServerLevel) CapabilitySync.sendUpdate(event.getEntity());
    }

    private static void onStartTracking(final StartTracking event)
    {
        if (event.getEntity() instanceof ServerPlayer player)
        {
            final CapabilitySync message = CapabilitySync.makePacket(event.getTarget(), CapabilitySync.TO_SYNC);
            if (message != null) CapabilitySync.ASSEMBLER.sendTo(message, player);
        }
    }

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(CapabilitySync::onStartTracking);
        MinecraftForge.EVENT_BUS.addListener(CapabilitySync::onJoinWorld);
    }

    private static void readMob(final Entity mob, final CompoundTag tag)
    {
        // Then try the capabilities
        try
        {
            final CapabilityDispatcher disp = (CapabilityDispatcher) CapabilitySync.GETMOBCAPS.invoke(mob);
            if (disp != null) disp.deserializeNBT(tag);
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error("Error Loading Caps for: {}", RegHelper.getKey(mob));
            ThutCore.LOGGER.error(e);
        }
        mob.refreshDimensions();
    }

    public CapabilitySync()
    {
        super();
    }

    public CapabilitySync(final CompoundTag tag)
    {
        super(tag);
    }

    public CapabilitySync(final FriendlyByteBuf buffer)
    {
        super(buffer);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient()
    {
        final int id = this.getTag().getInt("id");
        final Level world = net.minecraft.client.Minecraft.getInstance().level;
        final Entity mob = world.getEntity(id);
        if (mob != null) CapabilitySync.readMob(mob, this.getTag().getCompound("tag"));
    }
}
