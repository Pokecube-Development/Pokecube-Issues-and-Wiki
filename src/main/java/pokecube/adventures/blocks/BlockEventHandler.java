package pokecube.adventures.blocks;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.warppad.WarppadTile;
import thut.api.LinkableCaps.ILinkStorage;
import thut.api.LinkableCaps.Linkable;
import thut.api.maths.Vector4;

@Mod.EventBusSubscriber
public class BlockEventHandler
{
    private static class WarpPadStore implements ILinkStorage
    {
        final WarppadTile tile;

        public WarpPadStore(WarppadTile tile)
        {
            this.tile = tile;
        }

        @Override
        public UUID getLinkedMob(Entity user)
        {
            return null;
        }

        @Override
        public Vector4 getLinkedPos(Entity user)
        {
            return tile.getDest().loc;
        }

        @Override
        public boolean setLinkedMob(UUID mobid, Entity user)
        {
            return false;
        }

        @Override
        public boolean setLinkedPos(Vector4 pos, Entity user)
        {
            // TODO confirm owner of tile.
            
            // Assume that we right clicked the top of the block.
            pos.y += 1;
            tile.getDest().loc = pos;
            user.sendMessage(new TranslationTextComponent("block.pokecube_adventures.warppad.link", pos.x, pos.y, pos.z,
                    pos.w));
            return true;
        }

    }

    private static class WarpPadLink extends Linkable
    {
        final WarpPadStore store;

        public WarpPadLink(WarppadTile store)
        {
            this.store = new WarpPadStore(store);
        }

        @Override
        public boolean link(ILinkStorage link, Entity user)
        {
            return store.setLinkedPos(link.getLinkedPos(user), user);
        }

        @Override
        public ILinkStorage getLink(Entity user)
        {
            return store;
        }

    }

    protected static final ResourceLocation ENERGYSTORECAP  = new ResourceLocation(PokecubeAdv.ID, "energystore");
    protected static final ResourceLocation ENERGYSIPHONCAP = new ResourceLocation(PokecubeAdv.ID, "energysiphon");
    protected static final ResourceLocation LINKABLECAP     = new ResourceLocation(PokecubeAdv.ID, "linkable");

    @SubscribeEvent
    public static void attachCaps(final AttachCapabilitiesEvent<TileEntity> event)
    {
        if (event.getObject() instanceof WarppadTile && !event.getCapabilities().containsKey(LINKABLECAP))
        {
            event.addCapability(LINKABLECAP, new WarpPadLink((WarppadTile) event.getObject()));
        }

    }
}
