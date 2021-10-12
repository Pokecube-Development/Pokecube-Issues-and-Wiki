package thut.api;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
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
    public abstract static class VanillaWrapper<M extends Mob> implements IOwnable, ICapabilitySerializable<Tag>
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
        public Tag serializeNBT()
        {
            final CompoundTag tag = new CompoundTag();
            tag.putBoolean("p", this.playerOwned);
            return tag;
        }

        @Override
        public void deserializeNBT(final Tag nbt)
        {
            if (nbt instanceof ByteTag) this.playerOwned = ((ByteTag) nbt).getAsByte() != 0;
            else if (nbt instanceof CompoundTag) this.playerOwned = ((CompoundTag) nbt).getBoolean("p");
        }
    }

    public static class HorseWrapper extends VanillaWrapper<AbstractHorse>
    {
        LivingEntity owner;

        public HorseWrapper(final AbstractHorse toWrap)
        {
            super(toWrap);
            if (!this.playerOwned && toWrap.getOwnerUUID() != null && toWrap.getServer() != null)
                this.playerOwned = toWrap.getServer().getProfileCache().get(this.getOwnerId()) != null;
        }

        @Override
        public LivingEntity getOwner()
        {
            if (this.getOwnerId() == null) this.owner = null;
            if (this.getOwnerId() != null && this.owner == null && this.wrapped
                    .getCommandSenderWorld() instanceof ServerLevel) return this.owner = this.getOwner(
                            (ServerLevel) this.wrapped.getCommandSenderWorld(), this.owner);
            return this.owner;
        }

        @Override
        public UUID getOwnerId()
        {
            return this.wrapped.getOwnerUUID();
        }

        @Override
        public boolean isPlayerOwned()
        {
            this.playerOwned = this.playerOwned || this.getOwner() instanceof Player;
            return this.playerOwned;
        }

        @Override
        public void setOwner(final LivingEntity e)
        {
            this.owner = e;
            this.wrapped.setOwnerUUID(e == null ? null : e.getUUID());
        }

        @Override
        public void setOwner(final UUID id)
        {
            this.wrapped.setOwnerUUID(id);
        }

    }

    public static class TameWrapper extends VanillaWrapper<TamableAnimal>
    {
        LivingEntity owner = null;

        public TameWrapper(final TamableAnimal toWrap)
        {
            super(toWrap);
            this.playerOwned = toWrap.getOwner() instanceof Player;
            if (!this.playerOwned && toWrap.getOwnerUUID() != null && toWrap.getServer() != null)
                this.playerOwned = toWrap.getServer().getProfileCache().get(this.getOwnerId()) != null;
        }

        @Override
        public LivingEntity getOwner()
        {
            if (this.getOwnerId() == null) this.owner = null;
            if (this.getOwnerId() != null) this.owner = this.wrapped.getOwner();
            if (this.getOwnerId() != null && this.wrapped.getCommandSenderWorld() instanceof ServerLevel)
                return this.owner = this.getOwner((ServerLevel) this.wrapped.getCommandSenderWorld(), this.owner);
            return this.owner;
        }

        @Override
        public UUID getOwnerId()
        {
            return this.wrapped.getOwnerUUID();
        }

        @Override
        public boolean isPlayerOwned()
        {
            this.playerOwned = this.playerOwned || this.getOwner() instanceof Player;
            return this.playerOwned;
        }

        @Override
        public void setOwner(final LivingEntity e)
        {
            this.setOwner(e == null ? null : e.getUUID());
            this.owner = e;
            this.playerOwned = e instanceof Player;
        }

        @Override
        public void setOwner(final UUID id)
        {
            this.wrapped.setOwnerUUID(id);
            this.wrapped.setTame(id != null);
        }

    }

    public static class BaseImpl implements IOwnable, ICapabilityProvider
    {
        final LazyOptional<IOwnable> holder = LazyOptional.of(() -> this);

        UUID         ownerId;
        LivingEntity ownerMob;

        boolean playerOwned = false;

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
            this.playerOwned = this.playerOwned || this.getOwner() instanceof Player;
            return this.playerOwned;
        }

        @Override
        public void setOwner(final LivingEntity e)
        {
            this.playerOwned = e instanceof Player;
            this.ownerMob = e;
            if (e != null) this.setOwner(e.getUUID());
            else this.setOwner((UUID) null);
        }

        @Override
        public void setOwner(final UUID id)
        {
            this.ownerId = id;
        }
    }

    public static class Impl extends BaseImpl implements IOwnable, ICapabilitySerializable<CompoundTag>
    {
        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            if (nbt.contains("p"))
            {
                this.playerOwned = nbt.getBoolean("p");
                try
                {
                    this.ownerId = nbt.getUUID("o");
                }
                catch (final Exception e)
                {
                    ThutCore.LOGGER.error("Error loading in UUID");
                    this.ownerId = new UUID(nbt.getLong("oMost"), nbt.getLong("oLeast"));
                }
            }
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag nbt = new CompoundTag();
            if (this.ownerId != null)
            {
                nbt.putUUID("o", this.ownerId);
                nbt.putBoolean("p", this.playerOwned);
            }
            return nbt;
        }
    }

    public static class ImplTE extends Impl implements IOwnableTE
    {
    }

    public static final Set<EntityType<?>>     MOBS  = Sets.newHashSet();
    public static final Set<BlockEntityType<?>> TILES = Sets.newHashSet();

    public static final ResourceLocation LOCBASE  = new ResourceLocation("thutcore:ownable_base");
    public static final ResourceLocation LOCWRAP  = new ResourceLocation("thutcore:ownable_wrap");
    public static final ResourceLocation STICKTAG = new ResourceLocation("thutcore:pokeystick");

    public static ICapabilitySerializable<?> makeMobOwnable(final Entity mob, final boolean nonNull)
    {
        if (mob instanceof TamableAnimal) return new TameWrapper((TamableAnimal) mob);
        else if (mob instanceof AbstractHorse) return new HorseWrapper((AbstractHorse) mob);
        else if (OwnableCaps.MOBS.contains(mob.getType())) return new Impl();
        return nonNull ? new Impl() : null;
    }

    public static IOwnable getOwnable(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
    }

    public static LivingEntity getOwner(final LivingEntity target)
    {
        final IOwnable ownable = OwnableCaps.getOwnable(target);
        if (ownable != null) return ownable.getOwner();
        return null;
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
    public static void attachTEs(final AttachCapabilitiesEvent<BlockEntity> event)
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
        final BlockEntity tile = event.getWorld().getBlockEntity(event.getPos());
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
        final BlockEntity tile = event.getWorld().getBlockEntity(event.getPos());
        if (tile != null)
        {
            final IOwnable ownable = tile.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
            if (ownable instanceof IOwnableTE && ((IOwnableTE) ownable).canEdit(event.getEntityLiving()) && ItemTags
                    .getAllTags().getTagOrEmpty(OwnableCaps.STICKTAG).contains(event.getItemStack().getItem())
                    && ((IOwnableTE) ownable).getOwnerId() != null) event.getWorld().destroyBlock(event.getPos(), true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(final BlockEvent.BreakEvent event)
    {
        final BlockEntity tile = event.getWorld().getBlockEntity(event.getPos());
        if (tile != null)
        {
            final IOwnable ownable = tile.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
            if (ownable instanceof IOwnableTE && !((IOwnableTE) ownable).canEdit(event.getPlayer())) event.setCanceled(
                    true);
        }
    }

    public static void setup()
    {
        MinecraftForge.EVENT_BUS.register(OwnableCaps.class);
    }
}
