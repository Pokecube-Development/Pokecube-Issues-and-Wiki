package pokecube.adventures.blocks;

import java.util.UUID;

import net.minecraft.Util;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.adventures.blocks.warp_pad.WarpPadTile;
import pokecube.adventures.blocks.statue.StatueEntity;
import thut.api.IOwnable;
import thut.api.LinkableCaps.ILinkStorage;
import thut.api.LinkableCaps.Linkable;
import thut.api.LinkableCaps.PosStorage;
import thut.api.OwnableCaps;
import thut.api.block.IOwnableTE;
import thut.api.entity.CopyCaps;

public class BlockEventHandler
{
    private static class WarpPadStore implements ILinkStorage
    {
        final WarpPadTile tile;

        public WarpPadStore(final WarpPadTile tile)
        {
            this.tile = tile;
        }

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
            final IOwnable own = OwnableCaps.getOwnable(this.tile);
            if (user instanceof LivingEntity && own instanceof IOwnableTE && !((IOwnableTE) own).canEdit(
                    (LivingEntity) user) || pos == null) return false;
            // Assume that we right clicked the top of the block.
            pos = GlobalPos.of(pos.dimension(), pos.pos().above());
            this.tile.getDest().setPos(pos);
            this.tile.getDest().shift(0.5, 0, 0.5);
            if (!user.getCommandSenderWorld().isClientSide)
            {
                if (user instanceof Player)
                {
                    final Player player = (Player) user;
                    player.displayClientMessage(new TranslatableComponent(
                        "block.pokecube_adventures.warp_pad.link", tile.getDest().getInfoName()), true);
                } else
                {
                    user.sendMessage(new TranslatableComponent(
                        "block.pokecube_adventures.warp_pad.link", tile.getDest().getInfoName()), Util.NIL_UUID);
                }
            }
            // Centre us properly.
            return true;
        }
    }

    private static class WarpPadLink extends Linkable
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

    private static class SiphonLink extends Linkable
    {
        final SiphonTile tile;
        final PosStorage pos;

        public SiphonLink(final SiphonTile tile)
        {
            this.tile = tile;
            this.pos = new PosStorage();
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
    }

    protected static final ResourceLocation ENERGYSTORECAP  = new ResourceLocation(PokecubeAdv.MODID, "energystore");
    protected static final ResourceLocation ENERGYSIPHONCAP = new ResourceLocation(PokecubeAdv.MODID, "energysiphon");
    protected static final ResourceLocation LINKABLECAP     = new ResourceLocation(PokecubeAdv.MODID, "linkable");

    @SubscribeEvent
    public static void attachCaps(final AttachCapabilitiesEvent<BlockEntity> event)
    {
        if (event.getObject() instanceof WarpPadTile && !event.getCapabilities().containsKey(
                BlockEventHandler.LINKABLECAP)) event.addCapability(BlockEventHandler.LINKABLECAP, new WarpPadLink(
                        (WarpPadTile) event.getObject()));
        if (event.getObject() instanceof SiphonTile && !event.getCapabilities().containsKey(
                BlockEventHandler.LINKABLECAP)) event.addCapability(BlockEventHandler.LINKABLECAP, new SiphonLink(
                        (SiphonTile) event.getObject()));
        if (event.getObject() instanceof StatueEntity) event.addCapability(CopyCaps.LOC, new CopyCaps.Impl());

    }
}
