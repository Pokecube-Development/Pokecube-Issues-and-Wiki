package pokecube.adventures.ai.tasks.battle;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.moves.Battle;
import pokecube.core.utils.AITools;

public class ManagePokemobTarget extends BaseBattleTask
{

    public ManagePokemobTarget(final LivingEntity trainer)
    {
        super(trainer);
    }

    @Override
    protected void tick(final ServerLevel worldIn, final LivingEntity owner, final long gameTime)
    {
        final IHasPokemobs other = TrainerCaps.getHasPokemobs(this.target);
        if (other != null) other.onSetTarget(this.entity, true);

        final IPokemob mob = this.trainer.getOutMob();

        if (mob == null || this.target == null) return;
        LivingEntity ourMob = mob.getEntity();

        Battle ourBattle = Battle.getBattle(owner);
        Battle battle = mob.getBattle();
        // Ensure we are still in battle with the target.
        if (ourBattle == null)
        {
            if (Battle.createOrAddToBattle(owner, target)) ourBattle = Battle.getBattle(owner);
        }

        if (battle != null)
        {
            LivingEntity enemy = mob.getMoveStats().targetEnemy;
            enemy_check:
            if (enemy == this.target)
            {
                List<LivingEntity> mobs = Lists.newArrayList(battle.getEnemies(entity));
                // Ensure that the mobs are valid targets.
                mobs.removeIf(t2 -> !AITools.shouldBeAbleToAgro(ourMob, t2));
                for (int i = 0; i < mobs.size(); i++)
                {
                    enemy = mobs.get(i);
                    if (enemy == this.target) continue;
                    mob.getMoveStats().enemyIndex = i;
                    mob.updateBattleInfo();
                    break enemy_check;
                }
                mob.onSetTarget(target, true);
            }
        }
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        return super.checkExtraStartConditions(worldIn, entityIn);
    }
}
