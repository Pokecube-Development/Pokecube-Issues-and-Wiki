package thut.api;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.block.IOwnableTE;
import thut.core.common.ThutCore;

public class OwnableCaps
{
    public abstract static class VanillaWrapper<M extends MobEntity> implements IOwnable,
            ICapabilitySerializable<ByteNBT>
    {
        private final LazyOptional<IOwnable> holder = LazyOptional.of(() -> this);

        boolean playerOwned = false;

        protected final M wrapped;

        public VanillaWrapper(final M toWrap)
        {
            this.wrapped = toWrap;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ThutCaps.OWNABLE_CAP.orEmpty(cap, this.holder);
        }

        @Override
        public ByteNBT serializeNBT()
        {
            final ByteNBT tag = ByteNBT.valueOf((byte) (this.playerOwned ? 1 : 0));
            return tag;
        }

        @Override
        public void deserializeNBT(final ByteNBT nbt)
        {
            this.playerOwned = nbt.getByte() != 0;
        }
    }

    public static class HorseWrapper extends VanillaWrapper<AbstractHorseEntity>
    {
        LivingEntity owner;

        public HorseWrapper(final AbstractHorseEntity toWrap)
        {
            super(toWrap);
            if (!this.playerOwned && toWrap.getOwnerUniqueId() != null && toWrap.getServer() != null)
                this.playerOwned = toWrap.getServer().getPlayerProfileCache().getProfileByUUID(this
                        .getOwnerId()) != null;
        }

        @Override
        public LivingEntity getOwner()
        {
            if (this.getOwnerId() == null) this.owner = null;
            if (this.getOwnerId() != null && this.owner == null && this.wrapped.getEntityWorld() instanceof ServerWorld)
                return this.owner = this.getOwner((ServerWorld) this.wrapped.getEntityWorld(), this.owner);
            return this.owner;
        }

        @Override
        public UUID getOwnerId()
        {
            return this.wrapped.getOwnerUniqueId();
        }

        @Override
        public boolean isPlayerOwned()
        {
            this.playerOwned = this.playerOwned || this.getOwner() instanceof PlayerEntity;
            return this.playerOwned;
        }

        @Override
        public void setOwner(final LivingEntity e)
        {
            this.owner = e;
            this.wrapped.setOwnerUniqueId(e == null ? null : e.getUniqueID());
        }

        @Override
        public void setOwner(final UUID id)
        {
            this.wrapped.setOwnerUniqueId(id);
        }

    }

    public static class TameWrapper extends VanillaWrapper<TameableEntity>
    {
        LivingEntity owner = null;

        public TameWrapper(final TameableEntity toWrap)
        {
            super(toWrap);
            this.playerOwned = toWrap.getOwner() instanceof PlayerEntity;
            if (!this.playerOwned && toWrap.getOwnerId() != null && toWrap.getServer() != null)
                this.playerOwned = toWrap.getServer().getPlayerProfileCache().getProfileByUUID(this
                        .getOwnerId()) != null;
        }

        @Override
        public LivingEntity getOwner()
        {
            if (this.getOwnerId() == null) this.owner = null;
            if (this.getOwnerId() != null) this.owner = this.wrapped.getOwner();
            if (this.getOwnerId() != null && this.wrapped.getEntityWorld() instanceof ServerWorld)
                return this.owner = this.getOwner((ServerWorld) this.wrapped.getEntityWorld(), this.owner);
            return this.owner;
        }

        @Override
        public UUID getOwnerId()
        {
            return this.wrapped.getOwnerId();
        }

        @Override
        public boolean isPlayerOwned()
        {
            this.playerOwned = this.playerOwned || this.getOwner() instanceof PlayerEntity;
            return this.playerOwned;
        }

        @Override
        public void setOwner(final LivingEntity e)
        {
            this.setOwner(e == null ? null : e.getUniqueID());
            this.owner = e;
            this.playerOwned = e instanceof PlayerEntity;
        }

        @Override
        public void setOwner(final UUID id)
        {
            this.wrapped.setOwnerId(id);
            this.wrapped.setTamed(id != null);
        }

    }

    public static class Impl implements IOwnable, ICapabilitySerializable<CompoundNBT>
    {
        private final LazyOptional<IOwnable> holder = LazyOptional.of(() -> this);

        private UUID         ownerId;
        private LivingEntity ownerMob;

        private boolean playerOwned = false;

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            if (nbt.contains("p"))
            {
                this.playerOwned = nbt.getBoolean("p");
                try
                {
                    this.ownerId = nbt.getUniqueId("o");
                }
                catch (final Exception e)
                {
                    ThutCore.LOGGER.error("Error loading in UUID");
                    this.ownerId = new UUID(nbt.getLong("oMost"), nbt.getLong("oLeast"));
                }
            }
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ThutCaps.OWNABLE_CAP.orEmpty(cap, this.holder);
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
            this.playerOwned = this.playerOwned || this.getOwner() instanceof PlayerEntity;
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

    public static final Set<EntityType<?>>     MOBS  = Sets.newHashSet();
    public static final Set<TileEntityType<?>> TILES = Sets.newHashSet();

    public static final ResourceLocation LOCBASE  = new ResourceLocation("thutcore:ownable_base");
    public static final ResourceLocation LOCWRAP  = new ResourceLocation("thutcore:ownable_wrap");
    public static final ResourceLocation STICKTAG = new ResourceLocation("thutcore:pokeystick");

    public static ICapabilitySerializable<?> makeMobOwnable(final Entity mob, final boolean nonNull)
    {
        if (mob instanceof TameableEntity) return new TameWrapper((TameableEntity) mob);
        else if (mob instanceof AbstractHorseEntity) return new HorseWrapper((AbstractHorseEntity) mob);
        else if (OwnableCaps.MOBS.contains(mob.getType())) return new Impl();
        return nonNull ? new Impl() : null;
    }

    public static IOwnable getOwnable(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attachMobs(final AttachCapabilitiesEvent<Entity> event)
    {
        // Check if someone else adds this first (like say an IPokemob
        for (final ICapabilityProvider p : event.getCapabilities().values())
            if (p.getCapability(ThutCaps.OWNABLE_CAP).isPresent()) return;
        // We check if it is already here, incase someone else wants to wrap a
        // tameable differently.
        final ICapabilitySerializable<?> own = OwnableCaps.makeMobOwnable(event.getObject(), false);
        if (own != null) event.addCapability(OwnableCaps.LOCBASE, own);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attachTEs(final AttachCapabilitiesEvent<TileEntity> event)
    {
        // Check if someone else adds this first (like say an IPokemob
        for (final ICapabilityProvider p : event.getCapabilities().values())
            if (p.getCapability(ThutCaps.OWNABLE_CAP).isPresent()) return;
        if (OwnableCaps.TILES.contains(event.getObject().getType())) event.addCapability(OwnableCaps.LOCBASE,
                new ImplTE());
    }

    @SubscribeEvent
    public static void onblockPlace(final BlockEvent.EntityPlaceEvent event)
    {
        final TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (tile != null && event.getEntity() instanceof LivingEntity)
        {
            final IOwnable ownable = tile.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
            if (ownable instanceof IOwnableTE) ((IOwnableTE) ownable).setPlacer((LivingEntity) event.getEntity());
            else if (ownable != null) ownable.setOwner((LivingEntity) event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onBlockHit(final PlayerInteractEvent.LeftClickBlock event)
    {
        final TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (tile != null)
        {
            final IOwnable ownable = tile.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
            if (ownable instanceof IOwnableTE && ((IOwnableTE) ownable).canEdit(event.getEntityLiving()) && ItemTags
                    .getCollection().getTagByID(OwnableCaps.STICKTAG).contains(event.getItemStack().getItem())
                    && ((IOwnableTE) ownable).getOwnerId() != null) event.getWorld().destroyBlock(event.getPos(), true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(final BlockEvent.BreakEvent event)
    {
        final TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        if (tile != null)
        {
            final IOwnable ownable = tile.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
            if (ownable instanceof IOwnableTE && !((IOwnableTE) ownable).canEdit(event.getPlayer())) event.setCanceled(
                    true);
        }
    }

    public static void setup()
    {
        CapabilityManager.INSTANCE.register(IOwnable.class, new Storage(), Impl::new);
        MinecraftForge.EVENT_BUS.register(OwnableCaps.class);
    }
}
