package thut.api.attachments;

import com.google.common.collect.Sets;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.ThutCaps;
import thut.api.block.IOwnableTE;
import thut.api.data.HolderProvider;
import thut.api.item.ItemList;
import thut.core.common.ThutCore;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class Ownable
{
    public static interface IOwnableSerializable extends IOwnable, INBTSerializable<CompoundTag>
    {

    }

    public static class WrapperImpl implements IOwnableSerializable
    {
        final IOwnable wrapped;

        public WrapperImpl(IOwnable wrap)
        {
            this.wrapped = wrap;
        }

        @Override
        public LivingEntity getOwner()
        {
            return wrapped.getOwner();
        }

        @Override
        public UUID getOwnerId()
        {
            return wrapped.getOwnerId();
        }

        @Override
        public boolean isPlayerOwned()
        {
            return wrapped.isPlayerOwned();
        }

        @Override
        public void setOwner(LivingEntity e)
        {
            wrapped.setOwner(e);
        }

        @Override
        public void setOwner(UUID id)
        {
            wrapped.setOwner(id);
        }

        @Override
        public CompoundTag serializeNBT(Provider provider)
        {
            return null;
        }

        @Override
        public void deserializeNBT(Provider provider, CompoundTag nbt)
        {}

        @Override
        public void setPlayerOwned(boolean playerOwned)
        {
            wrapped.setPlayerOwned(playerOwned);
        }

    }

    public abstract static class VanillaWrapper<M extends Mob> implements IOwnableSerializable
    {

        boolean playerOwned = false;

        protected M wrapped;

        protected VanillaWrapper()
        {}

        public IOwnableSerializable Attach(final M toWrap)
        {
            this.wrapped = toWrap;
            return this;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider registries)
        {
            final CompoundTag tag = new CompoundTag();
            tag.putBoolean("p", this.playerOwned);
            return tag;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider registries, final CompoundTag tag)
        {
            this.playerOwned = tag.getBoolean("p");
        }
    }

    public static class HorseWrapper extends VanillaWrapper<AbstractHorse>
    {
        LivingEntity owner;

        protected HorseWrapper()
        {}

        @Override
        public IOwnableSerializable Attach(AbstractHorse toWrap)
        {
            super.Attach(toWrap);
            if (!this.playerOwned && toWrap.getOwnerUUID() != null && toWrap.getServer() != null)
                this.playerOwned = toWrap.getServer().getProfileCache().get(this.getOwnerId()).isPresent();
            return this;
        }

        @Override
        public LivingEntity getOwner()
        {
            if (this.getOwnerId() == null) this.owner = null;
            if (this.getOwnerId() != null && this.owner == null && this.wrapped.level() instanceof ServerLevel level)
                return this.owner = this.getOwner(level, this.owner);
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

        @Override
        public void setPlayerOwned(boolean playerOwned)
        {
            this.playerOwned = playerOwned;
        }

    }

    public static class TameWrapper extends VanillaWrapper<TamableAnimal>
    {
        LivingEntity owner = null;

        protected TameWrapper()
        {}

        @Override
        public IOwnableSerializable Attach(TamableAnimal toWrap)
        {
            super.Attach(toWrap);
            this.playerOwned = toWrap.getOwner() instanceof Player;
            if (!this.playerOwned && toWrap.getOwnerUUID() != null && toWrap.getServer() != null)
                this.playerOwned = toWrap.getServer().getProfileCache().get(this.getOwnerId()).isPresent();
            return this;
        }

        @Override
        public LivingEntity getOwner()
        {
            if (this.getOwnerId() == null) this.owner = null;
            if (this.getOwnerId() != null) this.owner = this.wrapped.getOwner();
            if (this.getOwnerId() != null && this.wrapped.level() instanceof ServerLevel level)
                return this.owner = this.getOwner(level, this.owner);
            this.playerOwned = this.owner instanceof Player;
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
            boolean wasTame = getOwnerId() != null;
            boolean isTame = id != null;
            this.wrapped.setOwnerUUID(id);
            this.wrapped.setTame(isTame, isTame && !wasTame);
        }

        @Override
        public void setPlayerOwned(boolean playerOwned)
        {
            this.playerOwned = playerOwned;
        }
    }

    public static class BaseImpl implements IOwnable, TrackedAttachment
    {
        UUID ownerId;
        LivingEntity ownerMob;

        boolean playerOwned = false;

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
            if (id != this.ownerId) this.markDirty();
            this.ownerId = id;
        }

        @Override
        public void setPlayerOwned(boolean playerOwned)
        {
            if (playerOwned != this.playerOwned) this.markDirty();
            this.playerOwned = playerOwned;
        }

        private boolean isDirty = false;

        @Override
        public void markDirty()
        {
            this.isDirty = true;
        }

        @Override
        public void markClean()
        {
            this.isDirty = false;
        }

        @Override
        public boolean isDirty()
        {
            return isDirty;
        }
    }

    public static class Impl extends BaseImpl implements IOwnableSerializable
    {
        @Override
        public void deserializeNBT(HolderLookup.Provider registries, final CompoundTag nbt)
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
        public CompoundTag serializeNBT(HolderLookup.Provider registries)
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
    {}

    public static final Set<EntityType<?>> MOBS = Sets.newHashSet();
    public static final Set<BlockEntityType<?>> TILES = Sets.newHashSet();

    public static final ResourceLocation ID = ResourceLocation.parse("thutcore:ownable");
    public static final ResourceLocation STICKTAG = ResourceLocation.parse("thutcore:pokeystick");

    public static final HolderProvider<IOwnableSerializable> _REGISTRY = new HolderProvider<>(ID);
    public static Supplier<AttachmentType<IOwnableSerializable>> TYPE;

    public static IOwnable get(final IAttachmentHolder in)
    {
        if (in == null) return null;
        if (in.hasData(TYPE.get())) return in.getData(TYPE.get());
        return null;
    }

    public static LivingEntity getOwner(final LivingEntity target)
    {
        final IOwnable ownable = Ownable.get(target);
        if (ownable != null) return ownable.getOwner();
        return null;
    }

    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        TYPE = registry.register(ID.getPath(), () -> AttachmentType.serializable(_REGISTRY::make).build());

        _REGISTRY.register(new HolderProvider.Provider<>()
        {
            @Override
            public IOwnableSerializable apply(IAttachmentHolder mob)
            {
                if (mob instanceof TamableAnimal animal) return new TameWrapper().Attach(animal);
                else if (mob instanceof AbstractHorse horse) return new HorseWrapper().Attach(horse);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });
    }

    @SubscribeEvent
    public static void onblockPlace(final BlockEvent.EntityPlaceEvent event)
    {
        final BlockEntity tile = event.getLevel().getBlockEntity(event.getPos());
        if (tile != null && event.getEntity() instanceof LivingEntity living)
        {
            final IOwnable ownable = ThutCaps.getOwnable(tile);
            if (ownable instanceof IOwnableTE te) te.setPlacer(living);
            else if (ownable != null) ownable.setOwner(living);
        }
    }

    @SubscribeEvent
    public static void onBlockHit(final PlayerInteractEvent.LeftClickBlock event)
    {
        final BlockEntity tile = event.getLevel().getBlockEntity(event.getPos());
        if (tile != null && tile.getLevel() instanceof ServerLevel level)
        {
            final IOwnable ownable = ThutCaps.getOwnable(tile);
            if (ownable instanceof IOwnableTE te && te.canEdit(event.getEntity()) && ItemList.is(Ownable.STICKTAG,
                    event.getItemStack()) && te.getOwnerId() != null)
            {
                BlockState state = level.getBlockState(event.getPos());
                List<ItemStack> drops = Block.getDrops(state, level, event.getPos(), tile, event.getEntity(),
                        event.getItemStack());
                if (drops.isEmpty()) state.onDestroyedByPlayer(level, event.getPos(), event.getEntity(), true,
                        level.getFluidState(event.getPos()));
                else event.getLevel().destroyBlock(event.getPos(), true);
                event.setUseBlock(TriState.FALSE);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(final BlockEvent.BreakEvent event)
    {
        final BlockEntity tile = event.getLevel().getBlockEntity(event.getPos());
        if (tile != null)
        {
            final IOwnable ownable = ThutCaps.getOwnable(tile);
            if (ownable instanceof IOwnableTE te && !te.canEdit(event.getPlayer())) event.setCanceled(true);
        }
    }
}
