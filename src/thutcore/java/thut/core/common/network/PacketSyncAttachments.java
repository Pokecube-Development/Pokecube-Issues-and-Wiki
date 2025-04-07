package thut.core.common.network;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import thut.api.Tracker;
import thut.api.world.WorldTickManager;
import thut.api.world.WorldTickManager.DelayedTask;
import thut.core.common.ThutCore;

@EventBusSubscriber
@SuppressWarnings("rawtypes")
public class PacketSyncAttachments extends Packet
{
    public static List<ResourceLocation> SYNCED = new ArrayList<>();
    public static Map<Predicate<Entity>, ResourceLocation> AUTOADD = new HashMap<>();
    private static Map<ResourceLocation, Tag> DEFAULTS = new HashMap<>();

    private static Field GETDEF;

    static
    {
        try
        {
            GETDEF = AttachmentType.class.getDeclaredField("defaultValueSupplier");
            GETDEF.setAccessible(true);
        }
        catch (NoSuchFieldException | SecurityException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Syncs wearables to the player when they join a world. This fixes client issues when they use nether portals, etc
     *
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void joinWorldLast(final EntityJoinLevelEvent event)
    {
        if (event.getEntity() instanceof LivingEntity mob && !mob.level().isClientSide)
        {
            // Delay this execution, so the mob is actually tracked when it
            // runs.
            WorldTickManager.scheduleTask(mob.level().dimension(),
                    new DelayedTask(Tracker.instance().getTick() + (mob instanceof Player ? 10 : 0),
                            () -> sendPackets(mob)));
        }
    }

    /**
     * Syncs wearables to the player when they join a world. This fixes client issues when they use nether portals, etc
     *
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void joinWorld(final EntityJoinLevelEvent event)
    {
        AUTOADD.forEach((valid, key) -> {
            if (!valid.test(event.getEntity())) return;
            var type = NeoForgeRegistries.ATTACHMENT_TYPES.get(key);
            // This triggers an initialisation of it.
            event.getEntity().getData(type);
        });
    }

    /**
     * Syncs wearables of other mobs to player when they start tracking them.
     *
     * @param event
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void startTracking(final StartTracking event)
    {
        if (event.getTarget() instanceof LivingEntity mob && event.getEntity().isEffectiveAi())
        {
            // Delay this execution, so the mob is actually tracked when it
            // runs.
            WorldTickManager.scheduleTask(mob.level().dimension(),
                    new DelayedTask(Tracker.instance().getTick() + 1, () -> sendPackets(mob)));
        }
    }

    public static void syncChange(Entity mob, Collection<ResourceLocation> changes)
    {
        changes.forEach(key -> {
            sendForKey(mob, key);
        });
    }

    public static <T> void syncChange(AttachmentType<T> type, Entity mob)
    {
        var data = mob.getData(type);
        if (!(data instanceof INBTSerializable)) return;
        var key = NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type);
        var tag = ((INBTSerializable) data).serializeNBT(mob.registryAccess());
        @SuppressWarnings("unchecked")
        var test = DEFAULTS.computeIfAbsent(key, a -> {
            Function<IAttachmentHolder, T> _defact;
            try
            {
                _defact = (Function<IAttachmentHolder, T>) GETDEF.get(type);
                var def = _defact.apply(mob);
                return ((INBTSerializable) def).serializeNBT(mob.registryAccess());
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
            return new CompoundTag();
        });
        if (tag.equals(test)) return;
        var p = new PacketSyncAttachments(mob, tag, key);
        ThutCore.packets.sendToTrackingAndSelf(p, mob);
    }

    public static <T> void syncChange(Supplier<AttachmentType<T>> type, Entity mob)
    {
        syncChange(type.get(), mob);
    }

    private static void sendForKey(Entity mob, ResourceLocation key)
    {
        var type = NeoForgeRegistries.ATTACHMENT_TYPES.get(key);
        if (!mob.hasData(type)) return;
        var data = mob.getData(type);
        if (!(data instanceof INBTSerializable)) return;
        var tag = ((INBTSerializable) data).serializeNBT(mob.registryAccess());
        @SuppressWarnings("unchecked")
        var test = DEFAULTS.computeIfAbsent(key, a -> {
            Function<IAttachmentHolder, ?> _defact;
            try
            {
                _defact = (Function<IAttachmentHolder, ?>) GETDEF.get(type);
                var def = _defact.apply(mob);
                if (def != null) return ((INBTSerializable) def).serializeNBT(mob.registryAccess());
                else ThutCore.logInfo("No attachment for {} for {}", key, mob);
            }
            catch (IllegalArgumentException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
            return new CompoundTag();
        });
        if (tag.equals(test)) return;
        var p = new PacketSyncAttachments(mob, tag, key);
        ThutCore.packets.sendToTrackingAndSelf(p, mob);
    }

    private static void sendPackets(LivingEntity mob)
    {
        SYNCED.forEach(key -> {
            sendForKey(mob, key);
        });
    }

    CompoundTag data;

    public PacketSyncAttachments()
    {
        this.data = new CompoundTag();
    }

    private PacketSyncAttachments(final Entity wearer, Tag attach, ResourceLocation key)
    {
        this();
        this.data.putInt("I", wearer.getId());
        this.data.put("V", attach);
        this.data.putString("K", key.toString());
    }

    public void read(final FriendlyByteBuf buffer)
    {
        this.data = buffer.readNbt();
    }

    @SuppressWarnings("unchecked")
    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleClient(Player player)
    {
        final Level world = player.level;
        final Entity p = world.getEntity(this.data.getInt("I"));
        if (p != null)
        {
            ResourceLocation key = ResourceLocation.parse(this.data.getString("K"));
            var type = NeoForgeRegistries.ATTACHMENT_TYPES.get(key);
            if (p.hasData(type))
            {
                INBTSerializable ser = (INBTSerializable) p.getData(type);
                ser.deserializeNBT(p.registryAccess(), data.get("V"));
            }
        }
        return;
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeNbt(this.data);
    }

    private final static Type<Packet> TYPE = new Type<Packet>(ResourceLocation.parse("thutcore:sync_attachments"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

}
