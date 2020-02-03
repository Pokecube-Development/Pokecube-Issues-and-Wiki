package pokecube.mobs.moves.world;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class ActionHydropump implements IMoveAction
{
    public ActionHydropump()
    {
    }

    @Override
    public boolean applyEffect(IPokemob user, Vector3 location)
    {
        if (user.getCombatState(CombatStates.ANGRY)) return false;
        if (!MoveEventsHandler.canEffectBlock(user, location)) return false;
        MoveEventsHandler.doDefaultWater(user, MovesUtils.getMoveFromName(this.getMoveName()), location);
        final Vector3 source = Vector3.getNewVector().set(user.getEntity());
        final double dist = source.distanceTo(location);
        final Vector3 dir = location.subtract(source).norm();
        final Vector3 temp = Vector3.getNewVector();
        for (int i = 0; i < dist; i++)
        {
            final Entity player = user.getEntity();
            temp.set(dir).scalarMultBy(i).addTo(source);
            final BlockState state = temp.getBlockState(player.getEntityWorld());
            if (!state.getMaterial().isReplaceable()) continue;
            if (user.getOwner() instanceof PlayerEntity)
            {
                final BreakEvent evt = new BreakEvent(player.getEntityWorld(), temp.getPos(), state, (PlayerEntity) user
                        .getOwner());
                MinecraftForge.EVENT_BUS.post(evt);
                if (evt.isCanceled()) continue;
            }
            temp.setBlock(user.getEntity().getEntityWorld(), Blocks.WATER.getDefaultState().with(
                    FlowingFluidBlock.LEVEL, 2));
        }
        return true;
    }

    @Override
    public String getMoveName()
    {
        return "hydropump";
    }
}
