package thut.api;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thut.api.maths.Vector4;

@Mod.EventBusSubscriber
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
        Vector4 getLinkedPos(Entity user);

        /**
         * This will set the linked mob, returns whether
         * this setting actually occured.
         */
        boolean setLinkedMob(@Nullable UUID mobid, @Nullable Entity user);

        /**
         * This will set the linked pos, returns whether
         * this setting actually occured.
         */
        boolean setLinkedPos(@Nullable Vector4 pos, @Nullable Entity user);
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

    public static class LinkStorage implements ILinkStorage, ICapabilityProvider
    {
        private final LazyOptional<ILinkStorage> holder = LazyOptional.of(() -> this);
        UUID                                     uuid;
        Vector4                                  pos;

        @Override
        public UUID getLinkedMob(Entity user)
        {
            return uuid;
        }

        @Override
        public Vector4 getLinkedPos(Entity user)
        {
            return pos;
        }

        @Override
        public boolean setLinkedMob(UUID mobid, Entity user)
        {
            uuid = mobid;
            return true;
        }

        @Override
        public boolean setLinkedPos(Vector4 pos, Entity user)
        {
            this.pos = pos;
            return true;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
        {
            return STORE.orEmpty(cap, this.holder);
        }

    }

    public static class Linkable implements ILinkable, ICapabilityProvider
    {
        private final LazyOptional<ILinkable> holder = LazyOptional.of(() -> this);
        LinkStorage                           store  = new LinkStorage();

        @Override
        public boolean link(ILinkStorage link, Entity user)
        {
            store.setLinkedMob(link.getLinkedMob(user), user);
            store.setLinkedPos(link.getLinkedPos(user), user);
            return true;
        }

        @Override
        public ILinkStorage getLink(Entity user)
        {
            return store;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side)
        {
            return LINK.orEmpty(cap, this.holder);
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

    @CapabilityInject(ILinkable.class)
    public static final Capability<ILinkable>    LINK  = null;
    @CapabilityInject(ILinkStorage.class)
    public static final Capability<ILinkStorage> STORE = null;

    public static void setup()
    {
        CapabilityManager.INSTANCE.register(ILinkable.class, new LinkStore(), Linkable::new);
        CapabilityManager.INSTANCE.register(ILinkStorage.class, new StoreStore(), LinkStorage::new);
        MinecraftForge.EVENT_BUS.register(OwnableCaps.class);
    }

    @SubscribeEvent
    public static void linkBlock(RightClickBlock event)
    {
        // Only run server side
        if (event.getWorld().isRemote) return;
        // Only run for items
        if (event.getItemStack().isEmpty()) return;
        // Check if stack is a linkstore
        LazyOptional<ILinkStorage> test_stack = event.getItemStack().getCapability(STORE, event.getFace());
        if (!test_stack.isPresent()) return;
        ILinkStorage storage = test_stack.orElse(null);
        TileEntity tile = event.getWorld().getTileEntity(event.getPos());
        // Only run for tile entities
        if (tile != null)
        {
            LazyOptional<ILinkable> test_tile = tile.getCapability(LINK, event.getFace());
            // Only run for linkable ones
            if (test_tile.isPresent())
            {
                if (test_tile.orElse(null).link(storage, event.getPlayer()))
                {
                    event.setCanceled(true);
                    event.setUseBlock(Result.DENY);
                    event.setUseItem(Result.DENY);
                }
            }
        }
        // Otherwise try to save the location instead
        else
        {
            Vector4 loc = new Vector4(event.getPos(), event.getPlayer().dimension);
            if (storage.setLinkedPos(loc, event.getPlayer()))
            {
                event.setCanceled(true);
                event.setUseBlock(Result.DENY);
                event.setUseItem(Result.DENY);
            }
        }
    }
}
