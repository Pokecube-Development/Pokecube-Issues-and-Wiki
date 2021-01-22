package pokecube.legends.init.moves;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.handlers.events.MoveEventsHandler.UseContext;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.customblocks.PortalWarp;
import pokecube.legends.init.BlockInit;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

public class ActionHyperspaceHole implements IMoveAction
{
    public ActionHyperspaceHole()
    {
    }

    @Override
    public boolean applyEffect(final IPokemob user, final Vector3 location)
    {
        if (user.inCombat()) return false;
        if (PokecubeLegends.config.portalDisappear == false) return false;
        final LivingEntity owner = user.getOwner();
        if (!(owner instanceof ServerPlayerEntity)) return false;
        final TranslationTextComponent message;
        final IHungrymob mob = user;
        int count = 1;
        final int level = user.getLevel();
        final int hungerValue = PokecubeCore.getConfig().pokemobLifeSpan / 16;
        count = (int) Math.max(1, Math.ceil(count * Math.pow((100 - level) / 100d, 3))) * hungerValue;
        if (level < PokecubeLegends.config.levelCreatePortal)
        {
            message = new TranslationTextComponent("msg.hoopaportal.deny.too_weak");
            owner.sendMessage(message, null);
            return false;
        }
        else
        {
            final long lastUse = user.getEntity().getPersistentData().getLong("pokecube_legends:last_portal_make");
            if (lastUse != 0)
            {
                final long diff = user.getEntity().getEntityWorld().getGameTime() - lastUse;
                if (diff < PokecubeLegends.config.ticksPerPortalSpawn)
                {
                    message = new TranslationTextComponent("msg.hoopaportal.deny.too_soon");
                    owner.sendMessage(message, null);
                    return false;
                }
            }
            final PortalWarp block = (PortalWarp) BlockInit.BLOCK_PORTALWARP.get();
            final UseContext context = MoveEventsHandler.getContext(owner.getEntityWorld(), user, block
                    .getDefaultState(), location.add(0, 2, 0));
            final BlockPos prevPos = context.getPos();
            final BlockState state = BlockInit.BLOCK_PORTALWARP.get().getStateForPlacement(context);

            // Didn't place, so lets skip
            if (state == null)
            {
                message = new TranslationTextComponent("msg.hoopaportal.deny.invalid");
                mob.setHungerTime(mob.getHungerTime() + count);
            }
            else
            {
                user.getEntity().getPersistentData().putLong("pokecube_legends:last_portal_make", user.getEntity()
                        .getEntityWorld().getGameTime());
                block.place(owner.getEntityWorld(), prevPos, context.getPlacementHorizontalFacing());
                message = new TranslationTextComponent("msg.hoopaportal.accept.info");
                mob.setHungerTime(mob.getHungerTime() + count);
            }
            owner.sendMessage(message, null);
            return true;
        }
    }

    @Override
    public String getMoveName()
    {
        return "hyperspacehole";
    }
}
