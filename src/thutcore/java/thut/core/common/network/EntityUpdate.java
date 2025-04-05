package thut.core.common.network;

import java.lang.reflect.Method;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thut.api.entity.EntityProvider;
import thut.api.item.ItemList;
import thut.core.common.ThutCore;
import thut.core.common.network.nbtpacket.NBTPacket;
import thut.core.common.network.nbtpacket.PacketAssembly;
import thut.lib.RegHelper;

public class EntityUpdate extends NBTPacket
{

    public static final ResourceLocation NOREAD = ResourceLocation.fromNamespaceAndPath(ThutCore.MODID, "additional_only_server");

    private static Set<EntityType<?>> errorSet = Sets.newHashSet();

    public static Method GETMOBCAPS;

    static
    {
        try
        {
//            EntityUpdate.GETMOBCAPS = CapabilityProvider.class.getDeclaredMethod("getCapabilities");
//            EntityUpdate.GETMOBCAPS.setAccessible(true);
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
        if (entity.level().isClientSide)
        {
            ThutCore.LOGGER.error("Packet sent on wrong side!", new IllegalArgumentException());
            return;
        }
        final CompoundTag tag = new CompoundTag();
        tag.putInt("id", entity.getId());
        final CompoundTag mobtag = new CompoundTag();
        entity.saveWithoutId(mobtag);
        tag.put("tag", mobtag);
        EntityUpdate.ASSEMBLER.sendToTracking(tag, entity);
    }

    public static void readMob(final Entity mob, final CompoundTag tag)
    {
        if ((mob.level() instanceof ServerLevel || !ItemList.is(EntityUpdate.NOREAD, mob)) && !EntityUpdate.errorSet
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
            ThutCore.LOGGER.error("Error loading " + RegHelper.getKey(mob)+ " on client side!");
            EntityUpdate.errorSet.add(mob.getType());
        }

        // First get the name
        if (tag.contains("CustomName", 8))
        {
            final String s = tag.getString("CustomName");

            try
            {
                mob.setCustomName(Component.Serializer.fromJson(s, mob.registryAccess()));
            }
            catch (final Exception exception)
            {
                ThutCore.LOGGER.warn("Failed to parse entity custom name {}", s, exception);
            }
        }
        // Then try the capabilities
        if (tag.contains("ForgeCaps", 10)) try
        {
            // TODO sync caps?
//            final CapabilityDispatcher disp = (CapabilityDispatcher) EntityUpdate.GETMOBCAPS.invoke(mob);
//            if (disp != null) disp.deserializeNBT(tag.getCompound("ForgeCaps"));
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error("Error Loading Caps for: {}", RegHelper.getKey(mob));
            ThutCore.LOGGER.error(e);
        }
        mob.refreshDimensions();

    }

    public EntityUpdate()
    {
        super();
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    protected void onCompleteClient(Player player)
    {
        final int id = this.getTag().getInt("id");
        final Level world = player.level();
        final Entity mob = EntityProvider.provider.getEntity(world, id);
        if (mob != null) EntityUpdate.readMob(mob, this.getTag().getCompound("tag"));
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("thutcore:entity_sync"));
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
