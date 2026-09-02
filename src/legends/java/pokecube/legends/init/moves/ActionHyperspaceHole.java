package pokecube.legends.init.moves;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.eventhandlers.MoveEventsHandler.UseContext;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.customblocks.PortalWarp;
import pokecube.legends.init.BlockInit;
import pokecube.legends.tileentity.RingTile;
import thut.api.Tracker;
import thut.api.maths.Vector3;

public class ActionHyperspaceHole implements IMoveWorldEffect
{
    public ActionHyperspaceHole()
    {}

    @Override
    public boolean applyOutOfCombat(IPokemob user, Vector3 location, HitResult hit)
    {
        if (user.inCombat()) return false;
        final LivingEntity owner = user.getOwner();
        if (!(owner instanceof ServerPlayer player)) return false;
        final MutableComponent message;
        int count = 1;
        final int level = user.getLevel();
        final int hungerValue = PokecubeCore.getConfig().pokemobLifeSpan / 16;
        count = (int) Math.max(1, Math.ceil(count * Math.pow((100 - level) / 100d, 3))) * hungerValue;
        if (level < PokecubeLegends.config.levelCreatePortal)
        {
            message = Component.translatable("msg.hoopaportal.deny.too_weak");
            player.displayClientMessage(message, true);
            return false;
        }
        else
        {
            final Level world = user.getEntity().level();
            final long lastUse = user.getEntity().getPersistentData().getLong("pokecube_legends:last_portal_make");
            final long now = Tracker.instance().getTick();
            if (lastUse != 0)
            {
                final long diff = now - lastUse;
                if (diff < PokecubeLegends.config.ticksPerPortalSpawn)
                {
                    message = Component.translatable("msg.hoopaportal.deny.too_soon");
                    player.displayClientMessage(message, true);
                    return false;
                }
            }
            final PortalWarp block = (PortalWarp) BlockInit.MIRAGE_SPOTS.get();
            final UseContext context = MoveEventsHandler.getContext(world, user, block.defaultBlockState(),
                    location.add(0, 1, 0));
            final BlockPos prevPos = context.getClickedPos();
            final BlockState state = BlockInit.MIRAGE_SPOTS.get().getStateForPlacement(context);

            // Didn't place, so lets skip
            if (state == null)
            {
                message = Component.translatable("msg.hoopaportal.deny.invalid");
                user.applyHunger(count);
            }
            else
            {
                user.getEntity().getPersistentData().putLong("pokecube_legends:last_portal_make", now);
                block.place(world, prevPos, context.getHorizontalDirection());
                final BlockEntity tile = world.getBlockEntity(prevPos.above());
                if (tile instanceof RingTile) ((RingTile) tile).despawns = true;
                message = Component.translatable("msg.hoopaportal.accept.info");
                user.applyHunger(count);
            }
            player.displayClientMessage(message, true);
            return true;
        }
    }

    @Override
    public String getMoveName()
    {
        return "hyperspace-hole";
    }
}
