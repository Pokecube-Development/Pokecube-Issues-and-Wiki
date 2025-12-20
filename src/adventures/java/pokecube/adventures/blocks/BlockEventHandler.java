package pokecube.adventures.blocks;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.adventures.blocks.warp_pad.WarpPadTile;
import thut.api.ThutCaps;
import thut.api.attachments.IOwnable;
import thut.api.attachments.Linkable;
import thut.api.attachments.Linkable.ILinkStorage;
import thut.api.attachments.Linkable.ILinkable;
import thut.api.attachments.Linkable.LinkStorage;
import thut.api.attachments.Linkable.LinkableImpl;
import thut.api.block.IOwnableTE;
import thut.api.data.HolderProvider;
import thut.lib.TComponent;

import java.util.UUID;

@EventBusSubscriber(bus = Bus.MOD, modid = PokecubeAdv.MODID)
public class BlockEventHandler
{
    private record WarpPadStore(WarpPadTile tile) implements ILinkStorage
    {
        @Override
        public UUID getLinkedMob(final Entity user)
        {
            return null;
        }

        @Override
        public GlobalPos getLinkedPos(final Entity user)
        {
            return this.tile.getDest().getPos();
        }

        @Override
        public boolean setLinkedMob(final UUID mobid, final Entity user)
        {
            return false;
        }

        @Override
        public boolean setLinkedPos(GlobalPos pos, final Entity user)
        {
            final IOwnable own = ThutCaps.getOwnable(this.tile);
            if (pos == null || user instanceof LivingEntity living && own instanceof IOwnableTE ownTe && !ownTe.canEdit(
                    living)) return false;
            // Assume that we right clicked the top of the block.
            pos = GlobalPos.of(pos.dimension(), pos.pos().above());
            this.tile.getDest().setPos(pos);
            if (!user.level().isClientSide)
            {
                if (user instanceof Player player)
                {
                    player.displayClientMessage(TComponent.translatable("block.pokecube_adventures.warp_pad.link",
                            tile.getDest().getInfoName()), true);
                }
            }
            // Centre us properly.
            return true;
        }
    }

    private static class WarpPadLink extends LinkableImpl
    {
        final WarpPadStore store;

        public WarpPadLink(final WarpPadTile store)
        {
            this.store = new WarpPadStore(store);
        }

        @Override
        public boolean link(final ILinkStorage link, final Entity user)
        {
            return this.store.setLinkedPos(link.getLinkedPos(user), user);
        }

        @Override
        public ILinkStorage getLink(final Entity user)
        {
            return this.store;
        }
    }

    private static class ImmutableLinkStore extends LinkStorage{
        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider)
        {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt)
        {
        }
    }

    private static class SiphonLink extends LinkableImpl
    {
        final SiphonTile tile;
        final LinkStorage pos;

        public SiphonLink(final SiphonTile tile)
        {
            this.tile = tile;
            this.pos = new ImmutableLinkStore();
            this.pos.setLinkedPos(GlobalPos.of(Level.OVERWORLD, this.tile.getBlockPos()), null);
        }

        @Override
        public boolean link(final ILinkStorage link, final Entity user)
        {
            return this.tile.tryLink(link, user);
        }

        @Override
        public ILinkStorage getLink(final Entity user)
        {
            return this.pos;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider)
        {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt)
        {
        }
    }

    protected static final ResourceLocation LINKSIPHONCAP = ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID,
            "energy_siphon");
    protected static final ResourceLocation LINKPADCAP = ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID,
            "warp_pad");

    @SubscribeEvent
    public static void attachCaps(final FMLLoadCompleteEvent event)
    {
        Linkable.DEFAULT().register(new HolderProvider.Provider<>()
        {
            @Override
            public ILinkable apply(IAttachmentHolder t)
            {
                if (t instanceof SiphonTile tile) return new SiphonLink(tile);
                return null;
            }
            @Override
            protected ResourceLocation key()
            {
                return LINKSIPHONCAP;
            }

            public int getPriority()
            {
                return 99;
            }
        });
        Linkable.DEFAULT().register(new HolderProvider.Provider<>()
        {

            @Override
            public ILinkable apply(IAttachmentHolder t)
            {
                if (t instanceof WarpPadTile tile) return new WarpPadLink(tile);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return LINKPADCAP;
            }

            public int getPriority()
            {
                return 99;
            }
        });
    }
}
