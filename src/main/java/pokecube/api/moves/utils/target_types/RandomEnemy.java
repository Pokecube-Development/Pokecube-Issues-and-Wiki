package pokecube.api.moves.utils.target_types;

import java.util.List;
import java.util.Random;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import pokecube.api.moves.Battle;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.ai.brain.BrainUtils;

public class RandomEnemy implements IMoveTargetter
{
    public static final IMoveTargetter INSTANCE = new RandomEnemy();

    @Override
    public boolean test(MoveApplication move)
    {
        Mob mob = move.getUser().getEntity();
        Battle battle = Battle.getBattle(mob);
        if (battle != null)
        {
            List<LivingEntity> enemies = battle.getEnemies(mob);
            if (!enemies.isEmpty())
            {
                if (enemies.size() == 1) return move.getTarget() == enemies.get(0);
                // TODO Test that this reliably picks one the same one, but randomly so.
                Random r = new Random(move.getUser().getRNGValue() ^ mob.getLevel().getGameTime());
                return move.getTarget() == enemies.get(r.nextInt(enemies.size()));
            }
        }
        return move.getTarget() == BrainUtils.getAttackTarget(mob);
    }
}
