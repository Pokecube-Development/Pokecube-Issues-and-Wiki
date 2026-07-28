package thut.core.common.network;

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
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import thut.api.Tracker;
import thut.api.attachments.TrackedAttachment;
import thut.api.entity.EntityProvider;
import thut.api.world.WorldTickManager;
import thut.api.world.WorldTickManager.DelayedTask;
import thut.core.common.ThutCore;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@EventBusSubscriber
@SuppressWarnings("rawtypes")
public class SyncAttachments extends Packet
{
    public static List<ResourceLocation> SYNCED = new ArrayList<>();
    public static Set<ResourceLocation> UNCHECKED_SYNC = new HashSet<>();
    public static Map<Predicate<Entity>, ResourceLocation> AUTOADD = new HashMap<>();
    private static final Map<ResourceLocation, Tag> DEFAULTS = new HashMap<>();

    private static final Field GETDEF;
    private static final Method ATTCHMAP;

    static
    {
        try
        {
            GETDEF = AttachmentType.class.getDeclaredField("defaultValueSupplier");
            GETDEF.setAccessible(true);

            ATTCHMAP = AttachmentHolder.class.getDeclaredMethod("getAttachmentMap");
            ATTCHMAP.setAccessible(true);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Syncs wearables to the player when they join a world. This fixes client issues when they use nether portals, etc
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void joinWorldLast(final EntityJoinLevelEvent event)
    {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof LivingEntity mob)
        {
            // Delay this execution, so the mob is actually tracked when it
            // runs.
            WorldTickManager.scheduleTask(mob.level().dimension(),
                    new DelayedTask(Tracker.instance().getTick(), () -> sendPackets(mob)));
        }
    }

    /**
     * Syncs wearables to the player when they join a world. This fixes client issues when they use nether portals, etc
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
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void startTracking(final StartTracking event)
    {
        if (event.getTarget() instanceof LivingEntity mob && event.getEntity().isEffectiveAi())
        {
            // Delay this execution, so the mob is actually tracked when it
            // runs.
            WorldTickManager.scheduleTask(mob.level().dimension(),
                    new DelayedTask(Tracker.instance().getTick(), () -> sendPackets(mob)));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void postTick(final EntityTickEvent.Post event)
            throws InvocationTargetException, IllegalAccessException
    {
        if (event.getEntity().level().isClientSide()) return;
        var mob = event.getEntity();
        @SuppressWarnings("unchecked")
        Map<AttachmentType<?>, Object> map = (Map<AttachmentType<?>, Object>) ATTCHMAP.invoke(mob);
        map.forEach((type, value) -> {
            if (value instanceof TrackedAttachment tracked && tracked.isDirty())
            {
                sendForKey(mob, NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type));
                tracked.markClean();
            }
        });
    }

    public static void syncChange(Entity mob, Collection<ResourceLocation> changes)
    {
        changes.forEach(key -> sendForKey(mob, key));
    }

    public static <T> void syncChange(AttachmentType<T> type, Entity mob)
    {
        var data = mob.getData(type);
        if (!(data instanceof INBTSerializable)) return;
        var key = NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type);
        sendForKey(mob, key);
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
        if(!(data instanceof TrackedAttachment tracked && tracked.isDirty())&&!UNCHECKED_SYNC.contains(key))
        {
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
        }
        var p = new SyncAttachments(mob, tag, key);
        ThutCore.packets.sendToTrackingAndSelf(p, mob);
    }

    private static void sendPackets(LivingEntity mob)
    {
        syncChange(mob, SYNCED);
        SYNCED.forEach(key -> sendForKey(mob, key));
    }

    CompoundTag data;

    public SyncAttachments()
    {
        this.data = new CompoundTag();
    }

    private SyncAttachments(final Entity wearer, Tag attach, ResourceLocation key)
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
        final Entity p = EntityProvider.provider.getEntity(world, this.data.getInt("I"));
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
    }

    @Override
    public void write(final FriendlyByteBuf buffer)
    {
        buffer.writeNbt(this.data);
    }

    private final static Type<Packet> TYPE = new Type<>(ResourceLocation.parse("thutcore:sync_attachments"));

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

}
