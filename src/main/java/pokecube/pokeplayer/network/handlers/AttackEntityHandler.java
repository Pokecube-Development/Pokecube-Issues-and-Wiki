package pokecube.pokeplayer.network.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.events.pokemob.combat.CommandAttackEvent;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

// Wrapper to ensure player attacks entity as pokeplayer
public class AttackEntityHandler extends pokecube.core.interfaces.pokemob.commandhandlers.AttackEntityHandler
{
    @Override
    public void handleCommand(IPokemob pokemob)
    {
        // Use default handling, which just agros stuff.
        if (!pokemob.getEntity().getPersistentData().getBoolean("is_a_player"))
        {
            super.handleCommand(pokemob);
            return;
        }

        // Actually execute the move if needed.
        final World world = pokemob.getEntity().getEntityWorld();
        final Entity target = PokecubeCore.getEntityProvider().getEntity(world, this.targetId, true);
        final Entity real = PokecubeCore.getEntityProvider().getEntity(world, this.targetId, false);
        if (target == null || !(target instanceof LivingEntity)) return;
        final int currentMove = pokemob.getMoveIndex();
        final CommandAttackEvent event = new CommandAttackEvent(pokemob.getEntity(), target);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled() && currentMove != 5 && MovesUtils.canUseMove(pokemob))
        {
            final Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
            pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
            pokemob.setCombatState(CombatStates.NOITEMUSE, false);
            if (move.isSelfMove()) pokemob.executeMove(pokemob.getEntity(), null, 0);
            else
            {
                pokemob.getEntity().setAttackTarget((LivingEntity) target);
                if (target instanceof MobEntity) BrainUtils.initiateCombat((MobEntity) target, (LivingEntity) real);
                ;
                final IPokemob targ = CapabilityPokemob.getPokemobFor(target);
                if (targ != null) targ.setCombatState(CombatStates.ANGRY, true);
                // Checks if within range
                final float dist = target.getDistance(pokemob.getEntity());
                double range = (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0 ? PokecubeCore
                        .getConfig().rangedAttackDistance : PokecubeCore.getConfig().contactAttackDistance;
                range = Math.max(pokemob.getMobSizes().x, range);
                range = Math.max(1, range);
                if (dist < range) pokemob.executeMove(target, Vector3.getNewVector().set(target), dist);
            }
        }
    }
}