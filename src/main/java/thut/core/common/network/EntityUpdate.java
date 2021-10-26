package thut.core.common.network;

import java.lang.reflect.Method;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import thut.api.item.ItemList;
import thut.core.common.ThutCore;

public class EntityUpdate extends NBTPacket
{

    public static final ResourceLocation NOREAD = new ResourceLocation(ThutCore.MODID, "additional_only_server");

    private static Set<EntityType<?>> errorSet = Sets.newHashSet();

    public static Method GETMOBCAPS;

    static
    {
        try
        {
            EntityUpdate.GETMOBCAPS = CapabilityProvider.class.getDeclaredMethod("getCapabilities");
            EntityUpdate.GETMOBCAPS.setAccessible(true);
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static final PacketAssembly<EntityUpdate> ASSEMBLER = PacketAssembly.registerAssembler(EntityUpdate.class,
            EntityUpdate::new, ThutCore.packets);

    public static void sendEntityUpdate(final Entity entity)
    {
        if (entity.getCommandSenderWorld().isClientSide)
        {
            ThutCore.LOGGER.error("Packet sent on wrong side!", new IllegalArgumentException());
            return;
        }
        final CompoundTag tag = new CompoundTag();
        tag.putInt("id", entity.getId());
        final CompoundTag mobtag = new CompoundTag();
        entity.saveWithoutId(mobtag);
        tag.put("tag", mobtag);
        final EntityUpdate message = new EntityUpdate(tag);
        EntityUpdate.ASSEMBLER.sendToTracking(message, entity);
    }

    public static void readMob(final Entity mob, final CompoundTag tag)
    {
        if ((mob.getCommandSenderWorld() instanceof ServerLevel || !ItemList.is(EntityUpdate.NOREAD, mob)) && !EntityUpdate.errorSet
                .contains(mob.getType())) try
        {
            mob.load(tag);
            mob.refreshDimensions();
            return;
        }
        catch (final Exception e)
        {
            // If we got to here then it means the above mob needs to be added
            // to the tag!
            ThutCore.LOGGER.error("Error loading " + mob.getType().getRegistryName() + " on client side!");
            EntityUpdate.errorSet.add(mob.getType());
        }

        // First get the name
        if (tag.contains("CustomName", 8))
        {
            final String s = tag.getString("CustomName");

            try
            {
                mob.setCustomName(Component.Serializer.fromJson(s));
            }
            catch (final Exception exception)
            {
                ThutCore.LOGGER.warn("Failed to parse entity custom name {}", s, exception);
            }
        }
        // Then try the capabilities
        if (tag.contains("ForgeCaps", 10)) try
        {
            final CapabilityDispatcher disp = (CapabilityDispatcher) EntityUpdate.GETMOBCAPS.invoke(mob);
            if (disp != null) disp.deserializeNBT(tag.getCompound("ForgeCaps"));
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error("Error Loading Caps for: {}", mob.getType().getRegistryName());
            ThutCore.LOGGER.error(e);
        }
        mob.refreshDimensions();

    }

    public EntityUpdate()
    {
        super();
    }

    public EntityUpdate(final CompoundTag tag)
    {
        super(tag);
    }

    public EntityUpdate(final FriendlyByteBuf buffer)
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
        if (mob != null) EntityUpdate.readMob(mob, this.getTag().getCompound("tag"));
    }
}
