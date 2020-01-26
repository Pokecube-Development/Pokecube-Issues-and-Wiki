package thut.api;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.block.IOwnableTE;

public class OwnableCaps
{
    public static class Impl implements IOwnable, ICapabilitySerializable<CompoundNBT>
    {
        private final LazyOptional<IOwnable> holder      = LazyOptional.of(() -> this);
        private UUID                         ownerId;
        private LivingEntity                 ownerMob;
        private boolean                      playerOwned = false;

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            if (nbt.contains("p"))
            {
                this.playerOwned = nbt.getBoolean("p");
                this.ownerId = nbt.getUniqueId("o");
            }
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return OwnableCaps.CAPABILITY.orEmpty(cap, this.holder);
        }

        @Override
        public LivingEntity getOwner()
        {
            return this.ownerMob;
        }

        @Override
        public UUID getOwnerId()
        {
            return this.ownerId;
        }

        @Override
        public boolean isPlayerOwned()
        {
            return this.playerOwned;
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT nbt = new CompoundNBT();
            if (this.ownerId != null)
            {
                nbt.putUniqueId("o", this.ownerId);
                nbt.putBoolean("p", this.playerOwned);
            }
            return nbt;
        }

        @Override
        public void setOwner(final LivingEntity e)
        {
            this.playerOwned = e instanceof PlayerEntity;
            this.ownerMob = e;
            if (e != null) this.setOwner(e.getUniqueID());
            else this.setOwner((UUID) null);
        }

        @Override
        public void setOwner(final UUID id)
        {
            this.ownerId = id;
        }
    }

    public static class ImplTE extends Impl implements IOwnableTE
    {
    }

    public static class Storage implements Capability.IStorage<IOwnable>
    {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void readNBT(final Capability<IOwnable> capability, final IOwnable instance, final Direction side,
                final INBT nbt)
        {
            if (instance instanceof ICapabilitySerializable) ((ICapabilitySerializable) instance).deserializeNBT(nbt);
        }

        @Override
        public INBT writeNBT(final Capability<IOwnable> capability, final IOwnable instance, final Direction side)
        {
            if (instance instanceof ICapabilitySerializable<?>) return ((ICapabilitySerializable<?>) instance)
                    .serializeNBT();
            return null;
        }
    }

    public static final Set<Class<? extends LivingEntity>> MOBS  = Sets.newHashSet();
    public static final Set<Class<? extends TileEntity>>   TILES = Sets.newHashSet();
    private static final ResourceLocation                  LOC   = new ResourceLocation("thutcore:ownable");

    @CapabilityInject(IOwnable.class)
    public static final Capability<IOwnable> CAPABILITY = null;

    @SubscribeEvent
    public static void attachMobs(final AttachCapabilitiesEvent<Entity> event)
    {
        if (OwnableCaps.MOBS.contains(event.getObject().getClass())) event.addCapability(OwnableCaps.LOC, new Impl());
    }

    @SubscribeEvent
    public static void attachTEs(final AttachCapabilitiesEvent<TileEntity> event)
    {
        if (OwnableCaps.TILES.contains(event.getObject().getClass())) event.addCapability(OwnableCaps.LOC,
                new ImplTE());
    }

    @SubscribeEvent
    public static void onblockPlace(final BlockEvent.EntityPlaceEvent event)
    {
        final TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (tile != null && event.getEntity() instanceof LivingEntity)
        {
            final IOwnable ownable = tile.getCapability(OwnableCaps.CAPABILITY).orElse(null);
            if (ownable instanceof IOwnableTE) ((IOwnableTE) ownable).setPlacer((LivingEntity) event.getEntity());
            else if (ownable != null) ownable.setOwner((LivingEntity) event.getEntity());
        }
    }

    public static void setup()
    {
        CapabilityManager.INSTANCE.register(IOwnable.class, new Storage(), Impl::new);
        MinecraftForge.EVENT_BUS.register(OwnableCaps.class);
    }
}
