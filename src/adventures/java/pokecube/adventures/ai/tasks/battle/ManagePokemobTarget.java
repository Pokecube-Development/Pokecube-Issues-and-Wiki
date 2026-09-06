package pokecube.adventures.ai.tasks.battle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.Battle;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.network.pokemobs.PacketBattleTargets;

public class ManagePokemobTarget extends BaseBattleTask
{

    public ManagePokemobTarget()
    {
        super();
    }

    @Override
    protected void tick(final ServerLevel worldIn, final LivingEntity owner, final long gameTime)
    {
        // Only run this every 10 ticks
        if (gameTime % 10 != 0) return;

        var target = getTarget(owner);

        final IPokemob mob = this.getTrainer(owner).getOutMob();

        if (mob == null || target == null) return;

        Battle ourBattle = Battle.getBattle(owner);
        Battle battle = mob.getBattle();
        // Ensure we are still in battle with the target.
        if (ourBattle == null) Battle.createOrAddToBattle(owner, target);

        if (battle != null)
        {
            LivingEntity enemy = mob.getMoveStats().getTargetEnemy();
            if (enemy != target && enemy != null)
            {
                PacketBattleTargets.setEnemy(mob, enemy);
            }
            BrainUtils.setAttackTarget(mob.getEntity(), target);
            mob.onSetTarget(target, true);
            if (enemy != null)
            {
                var enemyTarget = BrainUtils.getAttackTarget(enemy);
                if (enemyTarget == owner)
                {
                    var entity = mob.getTrackedEntity();
                    BrainUtils.setAttackTarget(enemy, entity);
                    var enemyPokemob = PokemobCaps.getPokemobFor(enemy);
                    if (enemyPokemob != null)
                    {
                        PacketBattleTargets.setEnemy(enemyPokemob, entity);
                    }
                }
            }
        }
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        return super.checkExtraStartConditions(worldIn, entityIn);
    }
}
