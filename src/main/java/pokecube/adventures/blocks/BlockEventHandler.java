package pokecube.adventures.blocks;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.siphon.SiphonTile;
import pokecube.adventures.blocks.statue.StatueEntity;
import pokecube.adventures.blocks.warppad.WarppadTile;
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
            if (!user.getCommandSenderWorld().isClientSide) if (user instanceof PlayerEntity)
            {
                final PlayerEntity player = (PlayerEntity) user;
                player.displayClientMessage(new TranslationTextComponent("block.pokecube_adventures.warppad.link",
                        this.tile.getDest().getInfoName()), true);
            }
            else user.sendMessage(new TranslationTextComponent("block.pokecube_adventures.warppad.link", this.tile
                    .getDest().getInfoName()), Util.NIL_UUID);
            // Centre us properly.
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

    private static class SiphonLink extends Linkable
    {
        final SiphonTile tile;
        final PosStorage pos;

        public SiphonLink(final SiphonTile tile)
        {
            this.tile = tile;
            this.pos = new PosStorage();
            this.pos.setLinkedPos(GlobalPos.of(World.OVERWORLD, this.tile.getBlockPos()), null);
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
    public static void attachCaps(final AttachCapabilitiesEvent<TileEntity> event)
    {
        if (event.getObject() instanceof WarppadTile && !event.getCapabilities().containsKey(
                BlockEventHandler.LINKABLECAP)) event.addCapability(BlockEventHandler.LINKABLECAP, new WarpPadLink(
                        (WarppadTile) event.getObject()));
        if (event.getObject() instanceof SiphonTile && !event.getCapabilities().containsKey(
                BlockEventHandler.LINKABLECAP)) event.addCapability(BlockEventHandler.LINKABLECAP, new SiphonLink(
                        (SiphonTile) event.getObject()));
        if (event.getObject() instanceof StatueEntity) event.addCapability(CopyCaps.LOC, new CopyCaps.Impl());

    }
}
