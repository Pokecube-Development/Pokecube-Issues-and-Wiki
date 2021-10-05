package thut.api.entity;

import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import thut.core.common.network.CapabilitySync;

public class CopyCaps
{
    public static class Impl implements ICopyMob, ICapabilitySerializable<CompoundTag>
    {
        private final LazyOptional<ICopyMob> holder = LazyOptional.of(() -> this);

        public ResourceLocation copiedID  = null;
        public LivingEntity     copiedMob = null;
        public CompoundTag      copiedNBT = new CompoundTag();

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CopyCaps.CAPABILITY.orEmpty(cap, this.holder);
        }

        @Override
        public ResourceLocation getCopiedID()
        {
            return this.copiedID;
        }

        @Override
        public LivingEntity getCopiedMob()
        {
            return this.copiedMob;
        }

        @Override
        public CompoundTag getCopiedNBT()
        {
            return this.copiedNBT;
        }

        @Override
        public void setCopiedID(final ResourceLocation id)
        {
            this.copiedID = id;
        }

        @Override
        public void setCopiedMob(final LivingEntity mob)
        {
            this.copiedMob = mob;
        }

        @Override
        public void setCopiedNBT(final CompoundTag tag)
        {
            this.copiedNBT = tag;
        }
    }

    @CapabilityInject(ICopyMob.class)
    public static final Capability<ICopyMob> CAPABILITY = null;
    public static final ResourceLocation     LOC        = new ResourceLocation("thutcore:copymob");

    private static final Set<ResourceLocation> ATTACH_TO = Sets.newHashSet();

    public static ICopyMob get(final ICapabilityProvider in)
    {
        return in.getCapability(CopyCaps.CAPABILITY).orElse(null);
    }

    private static void attachMobs(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getCapabilities().containsKey(CopyCaps.LOC)) return;
        if (CopyCaps.ATTACH_TO.contains(event.getObject().getType().getRegistryName())) event.addCapability(
                CopyCaps.LOC, new Impl());
    }

    private static void onEntitySizeSet(final EntityEvent.Size event)
    {
        final ICopyMob copyMob = CopyCaps.get(event.getEntity());
        if (copyMob == null || copyMob.getCopiedMob() == null) return;
        final LivingEntity copied = copyMob.getCopiedMob();
        final Pose pose = event.getEntity().getPose();
        final EntityDimensions dims = copied.getDimensions(pose);
        final float height = dims.height;
        final float width = dims.width;
        final float eye = copied.getEyeHeight(pose, dims);
        event.setNewEyeHeight(eye);
        event.setNewSize(EntityDimensions.fixed(width, height));
    }

    private static void onLivingUpdate(final LivingUpdateEvent event)
    {
        final ICopyMob copyMob = CopyCaps.get(event.getEntity());
        if (copyMob == null) return;
        copyMob.onBaseTick(event.getEntity().level, event.getEntityLiving());
    }

    public static void setup()
    {
        CapabilityManager.INSTANCE.register(ICopyMob.class);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CopyCaps::attachMobs);
        MinecraftForge.EVENT_BUS.addListener(CopyCaps::onEntitySizeSet);
        MinecraftForge.EVENT_BUS.addListener(CopyCaps::onLivingUpdate);

        CapabilitySync.TO_SYNC.add(CopyCaps.LOC.toString());
    }

    public static void register(final EntityType<?> type)
    {
        synchronized (CopyCaps.ATTACH_TO)
        {
            CopyCaps.ATTACH_TO.add(type.getRegistryName());
        }
    }
}
