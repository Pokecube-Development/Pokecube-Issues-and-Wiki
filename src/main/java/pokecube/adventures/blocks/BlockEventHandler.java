package pokecube.adventures.blocks;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.warppad.WarppadTile;
import thut.api.LinkableCaps.ILinkStorage;
import thut.api.LinkableCaps.Linkable;
import thut.api.maths.Vector4;

public class BlockEventHandler
{
    private static class WarpPadStore implements ILinkStorage
    {
        final WarppadTile tile;

        public WarpPadStore(final WarppadTile tile)
        {
            this.tile = tile;
        }

        @Override
        public UUID getLinkedMob(final Entity user)
        {
            return null;
        }

        @Override
        public Vector4 getLinkedPos(final Entity user)
        {
            return this.tile.getDest().loc;
        }

        @Override
        public boolean setLinkedMob(final UUID mobid, final Entity user)
        {
            return false;
        }

        @Override
        public boolean setLinkedPos(final Vector4 pos, final Entity user)
        {
            // TODO confirm owner of tile.

            // Assume that we right clicked the top of the block.
            pos.y += 1;
            this.tile.getDest().loc = pos;
            user.sendMessage(new TranslationTextComponent("block.pokecube_adventures.warppad.link", pos.x, pos.y, pos.z,
                    pos.w));
            return true;
        }

    }

    private static class WarpPadLink extends Linkable
    {
        final WarpPadStore store;

        public WarpPadLink(final WarppadTile store)
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

    protected static final ResourceLocation ENERGYSTORECAP  = new ResourceLocation(PokecubeAdv.ID, "energystore");
    protected static final ResourceLocation ENERGYSIPHONCAP = new ResourceLocation(PokecubeAdv.ID, "energysiphon");
    protected static final ResourceLocation LINKABLECAP     = new ResourceLocation(PokecubeAdv.ID, "linkable");

    @SubscribeEvent
    public static void attachCaps(final AttachCapabilitiesEvent<TileEntity> event)
    {
        if (event.getObject() instanceof WarppadTile && !event.getCapabilities().containsKey(
                BlockEventHandler.LINKABLECAP)) event.addCapability(BlockEventHandler.LINKABLECAP, new WarpPadLink(
                        (WarppadTile) event.getObject()));

    }
}
