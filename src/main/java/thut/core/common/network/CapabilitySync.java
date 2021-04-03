package thut.core.common.network;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import thut.core.common.ThutCore;

public class CapabilitySync extends NBTPacket
{
    private static Method GETMOBCAPS;
    private static Field  CAPWRITERS;
    private static Field  CAPNAMES;

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

    public static final PacketAssembly<CapabilitySync> ASSEMBLER = PacketAssembly.registerAssembler(
            CapabilitySync.class, CapabilitySync::new, ThutCore.packets);

    private static CapabilitySync makePacket(final Entity entity)
    {
        if (entity.getCommandSenderWorld().isClientSide)
        {
            ThutCore.LOGGER.error("Packet sent on wrong side!", new IllegalArgumentException());
            return null;
        }
        final CompoundNBT tag = new CompoundNBT();
        tag.putInt("id", entity.getId());
        final CompoundNBT nbt = new CompoundNBT();
        try
        {
            final CapabilityDispatcher disp = (CapabilityDispatcher) CapabilitySync.GETMOBCAPS.invoke(entity);
            @SuppressWarnings("unchecked")
            final INBTSerializable<INBT>[] writers = (INBTSerializable<INBT>[]) CapabilitySync.CAPWRITERS.get(disp);
            final String[] names = (String[]) CapabilitySync.CAPNAMES.get(disp);
            for (int x = 0; x < writers.length; x++)
                if (CapabilitySync.TO_SYNC.contains(names[x])) nbt.put(names[x], writers[x].serializeNBT());
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
        final CapabilitySync message = CapabilitySync.makePacket(entity);
        if (message != null)
        {
            CapabilitySync.ASSEMBLER.sendToTracking(message, entity);
            if (entity instanceof ServerPlayerEntity) CapabilitySync.ASSEMBLER.sendTo(message,
                    (ServerPlayerEntity) entity);
        }
    }

    private static void onJoinWorld(final EntityJoinWorldEvent event)
    {
        if (event.getWorld().isClientSide()) return;
        if (event.getWorld() instanceof ServerWorld) CapabilitySync.sendUpdate(event.getEntity());
    }

    private static void onStartTracking(final StartTracking event)
    {
        if (event.getPlayer() instanceof ServerPlayerEntity)
        {
            final CapabilitySync message = CapabilitySync.makePacket(event.getTarget());
            if (message != null) CapabilitySync.ASSEMBLER.sendTo(message, (ServerPlayerEntity) event.getPlayer());
        }
    }

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(CapabilitySync::onStartTracking);
        MinecraftForge.EVENT_BUS.addListener(CapabilitySync::onJoinWorld);
    }

    private static void readMob(final Entity mob, final CompoundNBT tag)
    {
        // Then try the capabilities
        try
        {
            final CapabilityDispatcher disp = (CapabilityDispatcher) CapabilitySync.GETMOBCAPS.invoke(mob);
            if (disp != null) disp.deserializeNBT(tag);
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error("Error Loading Caps for: {}", mob.getType().getRegistryName());
            ThutCore.LOGGER.error(e);
        }
        mob.refreshDimensions();
    }

    public CapabilitySync()
    {
        super();
    }

    public CapabilitySync(final CompoundNBT tag)
    {
        super(tag);
    }

    public CapabilitySync(final PacketBuffer buffer)
    {
        super(buffer);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient()
    {
        final int id = this.getTag().getInt("id");
        final World world = net.minecraft.client.Minecraft.getInstance().level;
        final Entity mob = world.getEntity(id);
        if (mob != null) CapabilitySync.readMob(mob, this.getTag().getCompound("tag"));
    }
}
