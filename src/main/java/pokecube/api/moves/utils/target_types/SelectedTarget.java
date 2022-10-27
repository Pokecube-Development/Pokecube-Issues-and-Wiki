package pokecube.api.moves.utils.target_types;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.moves.utils.MoveApplication;

public class SelectedTarget implements IMoveTargetter
{
    public static final IMoveTargetter INSTANCE = new SelectedTarget();

    @Override
    public boolean test(MoveApplication move)
    {
        if (move.getTarget() == null) return false;
        boolean isTargetEnemy = move.getTarget() == move.getUser().getMoveStats().targetEnemy;
        if (isTargetEnemy) return true;
        LivingEntity targ = move.getUser().getMoveStats().targetAlly;
        boolean isTargetAlly = move.getUser().getMoveStats().targetEnemy == null && targ != move.getUser().getEntity()
                && targ == move.getTarget();
        return isTargetAlly;
    }
}
