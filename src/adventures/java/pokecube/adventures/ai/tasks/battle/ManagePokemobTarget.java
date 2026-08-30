package pokecube.adventures.ai.tasks.battle;

import com.google.common.collect.Lists;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.moves.Battle;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.utils.AITools;

import java.util.List;

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
        LivingEntity ourMob = mob.getEntity();

        Battle ourBattle = Battle.getBattle(owner);
        Battle battle = mob.getBattle();
        // Ensure we are still in battle with the target.
        if (ourBattle == null) Battle.createOrAddToBattle(owner, target);

        if (battle != null)
        {
            LivingEntity enemy = mob.getMoveStats().targetEnemy;
            enemy_check:
            if (enemy != target)
            {
                List<LivingEntity> mobs = Lists.newArrayList(battle.getEnemies(owner));
                // Ensure that the mobs are valid targets.
                mobs.removeIf(t2 -> !AITools.shouldBeAbleToAgro(ourMob, t2));
                for (int i = 0; i < mobs.size(); i++)
                {
                    enemy = mobs.get(i);
                    if (enemy != target) continue;
                    mob.getMoveStats().enemyIndex = i;
                    mob.updateBattleInfo();
                    break enemy_check;
                }
            }
            BrainUtils.setAttackTarget(mob.getEntity(), target);
            mob.onSetTarget(target, true);
            if (enemy != null)
            {
                var enemyTarget = BrainUtils.getAttackTarget(enemy);
                if (enemyTarget == owner)
                {
                    BrainUtils.setAttackTarget(enemy, ourMob);
                    var enemyPokemob = PokemobCaps.getPokemobFor(enemy);
                    if (enemyPokemob != null)
                    {
                        enemyPokemob.getMoveStats().enemyIndex++;
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
