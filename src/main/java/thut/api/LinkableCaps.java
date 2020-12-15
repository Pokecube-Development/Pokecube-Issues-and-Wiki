package thut.api;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LinkableCaps
{

    public static interface ILinkStorage
    {
        /**
         * This gets the UUID for a mob, if this
         * is null, then it does not have a linked mob
         */
        @Nullable
        UUID getLinkedMob(Entity user);

        /**
         * This gets the UUID for a pos, if this
         * is null, then it does not have a linked pos
         */
        @Nullable
        GlobalPos getLinkedPos(Entity user);

        /**
         * This will set the linked mob, returns whether
         * this setting actually occured.
         */
        boolean setLinkedMob(@Nullable UUID mobid, @Nullable Entity user);

        /**
         * This will set the linked pos, returns whether
         * this setting actually occured.
         */
        boolean setLinkedPos(@Nullable GlobalPos pos, @Nullable Entity user);
    }

    public static interface ILinkable
    {
        /**
         * @param link
         *            - who to link to
         * @return whether the link connected.
         */
        boolean link(ILinkStorage link, @Nullable Entity user);

        /**
         * This will return an ILinkStorage which
         * will link to this ILinkable.
         */
        @Nonnull
        ILinkStorage getLink(@Nullable Entity user);
    }

    public static class PosStorage implements ILinkStorage
    {
        GlobalPos pos;

        @Override
        public UUID getLinkedMob(final Entity user)
        {
            return null;
        }

        @Override
        public GlobalPos getLinkedPos(final Entity user)
        {
            return this.pos;
        }

        @Override
        public boolean setLinkedMob(final UUID mobid, final Entity user)
        {
            return false;
        }

        @Override
        public boolean setLinkedPos(final GlobalPos pos, final Entity user)
        {
            this.pos = pos;
            return true;
        }

    }

    public static class LinkStorage implements ILinkStorage, ICapabilityProvider
    {
        private final LazyOptional<ILinkStorage> holder = LazyOptional.of(() -> this);

        UUID uuid;

        GlobalPos pos;

        @Override
        public UUID getLinkedMob(final Entity user)
        {
            return this.uuid;
        }

        @Override
        public GlobalPos getLinkedPos(final Entity user)
        {
            return this.pos;
        }

        @Override
        public boolean setLinkedMob(final UUID mobid, final Entity user)
        {
            this.uuid = mobid;
            return true;
        }

        @Override
        public boolean setLinkedPos(final GlobalPos pos, final Entity user)
        {
            this.pos = pos;
            return true;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ThutCaps.STORE.orEmpty(cap, this.holder);
        }

    }

    public static class Linkable implements ILinkable, ICapabilityProvider
    {
        private final LazyOptional<ILinkable> holder = LazyOptional.of(() -> this);
        LinkStorage                           store  = new LinkStorage();

        @Override
        public boolean link(final ILinkStorage link, final Entity user)
        {
            this.store.setLinkedMob(link.getLinkedMob(user), user);
            this.store.setLinkedPos(link.getLinkedPos(user), user);
            return true;
        }

        @Override
        public ILinkStorage getLink(final Entity user)
        {
            return this.store;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ThutCaps.LINK.orEmpty(cap, this.holder);
        }

    }

    private static class LinkStore implements Capability.IStorage<ILinkable>
    {
        @Override
        public void readNBT(final Capability<ILinkable> capability, final ILinkable instance, final Direction side,
                final INBT nbt)
        {
        }

        @Override
        public INBT writeNBT(final Capability<ILinkable> capability, final ILinkable instance, final Direction side)
        {
            return null;
        }
    }

    private static class StoreStore implements Capability.IStorage<ILinkStorage>
    {
        @Override
        public void readNBT(final Capability<ILinkStorage> capability, final ILinkStorage instance,
                final Direction side, final INBT nbt)
        {
        }

        @Override
        public INBT writeNBT(final Capability<ILinkStorage> capability, final ILinkStorage instance,
                final Direction side)
        {
            return null;
        }
    }

    public static void setup()
    {
        CapabilityManager.INSTANCE.register(ILinkable.class, new LinkStore(), Linkable::new);
        CapabilityManager.INSTANCE.register(ILinkStorage.class, new StoreStore(), LinkStorage::new);
        MinecraftForge.EVENT_BUS.register(OwnableCaps.class);
    }

    @SubscribeEvent
    public static void linkBlock(final RightClickBlock event)
    {
        // Only run for items
        if (event.getItemStack().isEmpty()) return;
        // Check if stack is a linkstore
        final LazyOptional<ILinkStorage> test_stack = event.getItemStack().getCapability(ThutCaps.STORE, event
                .getFace());
        if (!test_stack.isPresent()) return;
        final ILinkStorage storage = test_stack.orElse(null);
        final TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        final LazyOptional<ILinkable> test_tile;
        // Only run for tile entities
        if (tile != null && (test_tile = tile.getCapability(ThutCaps.LINK, event.getFace())).isPresent())
        {
            // Only run for linkable ones
            test_tile.orElse(null).link(storage, event.getPlayer());
            event.setCanceled(true);
            event.setUseBlock(Result.DENY);
            event.setUseItem(Result.DENY);
        }
        // Otherwise try to save the location instead
        else
        {
            final GlobalPos pos = GlobalPos.getPosition(event.getPlayer().getEntityWorld().getDimensionKey(), event
                    .getPos());
            storage.setLinkedPos(pos, event.getPlayer());
            event.setCanceled(true);
            event.setUseBlock(Result.DENY);
            event.setUseItem(Result.DENY);
        }
    }
}
