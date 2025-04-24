package pokecube.adventures.ai.tasks.battle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import pokecube.adventures.ai.brain.MemoryTypes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.utils.Tools;
import pokecube.core.ai.brain.BrainUtils;

public class ChooseAttacks extends BaseBattleTask
{
    public ChooseAttacks()
    {
        super();
    }

    /**
     * Searches for pokemobs most damaging move against the target, and sets it as current attack
     */
    private void setMostDamagingMove(LivingEntity trainer)
    {
        final IPokemob outMob = this.getTrainer(trainer).getOutMob();
        int index = outMob.getMoveIndex();
        int max = 0;
        final LivingEntity target = BrainUtils.getAttackTarget(outMob.getEntity());
        for (int i = 0; i < outMob.getMovesCount(); i++)
        {
            final String s = outMob.getMove(i);
            if (s != null)
            {
                final int temp = Tools.getPower(s, outMob, target);
                if (temp > max)
                {
                    index = i;
                    max = temp;
                }
            }
        }
        outMob.setMoveIndex(index);
    }

    private void considerSwapMove(LivingEntity trainer)
    {
        // TODO choose between damaging/stats/status moves
        this.setMostDamagingMove(trainer);
    }

    @Override
    protected void tick(final ServerLevel worldIn, final LivingEntity owner, final long gameTime)
    {
        var target = owner.getBrain().getMemory(MemoryTypes.BATTLETARGET.get()).get();
        // If trainer has a living, real mob out, tell it to do stuff.
        // Check if pokemob has a valid Pokemob as a target.
        if (PokemobCaps.getPokemobFor(target) != null)
            // using best move for target.
            this.considerSwapMove(owner);
            // Otherwise just pick whatever does most damage
        else this.setMostDamagingMove(owner);
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final LivingEntity entityIn, final long gameTimeIn)
    {
        return this.getTrainer(entityIn).getOutMob() != null;
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final LivingEntity owner)
    {
        if (!super.checkExtraStartConditions(worldIn, owner)) return false;
        return this.getTrainer(owner).getOutMob() != null;
    }
}
