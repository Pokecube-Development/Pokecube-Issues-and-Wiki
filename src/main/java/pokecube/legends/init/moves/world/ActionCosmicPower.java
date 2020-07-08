package pokecube.legends.init.moves.world;

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
import pokecube.legends.blocks.UltraSpacePortal;
import pokecube.legends.init.BlockInit;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

public class ActionCosmicPower implements IMoveAction
{
    public ActionCosmicPower()
    {
    }

    @Override
    public boolean applyEffect(final IPokemob user, final Vector3 location)
    {
        if (user.inCombat()) return false;
        final LivingEntity owner = user.getOwner();
        if (!(owner instanceof ServerPlayerEntity)) return false;
        final TranslationTextComponent message;
        final IHungrymob mob = user;
        int count = 1;
        final int level = user.getLevel();
        final int hungerValue = PokecubeCore.getConfig().pokemobLifeSpan / 16;
        count = (int) Math.max(1, Math.ceil(count * Math.pow((100 - level) / 100d, 3))) * hungerValue;
        if (level < 20)
        {
            message = new TranslationTextComponent("msg.spaceacess.deny.too_weak");
            owner.sendMessage(message);
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
                    message = new TranslationTextComponent("msg.spaceacess.deny.too_soon");
                    owner.sendMessage(message);
                    return false;
                }
            }
            final UltraSpacePortal block = (UltraSpacePortal) BlockInit.ULTRASPACE_PORTAL.get();
            final UseContext context = MoveEventsHandler.getContext(owner.getEntityWorld(), user, block
                    .getDefaultState(), location.add(0, 2, 0));
            final BlockPos prevPos = context.getPos();
            final BlockState state = BlockInit.ULTRASPACE_PORTAL.get().getStateForPlacement(context);

            // Didn't place, so lets skip
            if (state == null)
            {
                message = new TranslationTextComponent("msg.spaceacess.deny.invalid");
                mob.setHungerTime(mob.getHungerTime() + count);
            }
            else
            {
                user.getEntity().getPersistentData().putLong("pokecube_legends:last_portal_make", user.getEntity()
                        .getEntityWorld().getGameTime());
                block.place(owner.getEntityWorld(), prevPos, context.getFace());
                message = new TranslationTextComponent("msg.spaceacess.accept.info");
                mob.setHungerTime(mob.getHungerTime() + count);
            }
            owner.sendMessage(message);
            return true;
        }
    }

    @Override
    public String getMoveName()
    {
        return "cosmicpower";
    }
}