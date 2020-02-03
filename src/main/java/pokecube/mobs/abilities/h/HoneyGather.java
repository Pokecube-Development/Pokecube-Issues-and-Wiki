package pokecube.mobs.abilities.h;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.LivingEntity;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class HoneyGather extends Ability
{
    int range = 4;

    @Override
    public Ability init(Object... args)
    {
        for (int i = 0; i < 2; i++)
            if (args != null && args.length > i) if (args[i] instanceof Integer)
            {
                this.range = (int) args[i];
                return this;
            }
        return this;
    }

    @Override
    public void onUpdate(IPokemob mob)
    {
        double diff = 0.0002 * this.range * this.range;
        diff = Math.min(0.5, diff);
        if (Math.random() < 1 - diff) return;

        final LivingEntity entity = mob.getEntity();
        final Vector3 here = Vector3.getNewVector().set(entity);
        final Random rand = entity.getRNG();

        here.set(entity).addTo(this.range * (rand.nextDouble() - 0.5), Math.min(10, this.range) * (rand.nextDouble()
                - 0.5), this.range * (rand.nextDouble() - 0.5));

        final BlockState state = here.getBlockState(entity.getEntityWorld());
        final Block block = state.getBlock();
        if (block instanceof IGrowable)
        {
            final IGrowable growable = (IGrowable) block;
            if (growable.canGrow(entity.getEntityWorld(), here.getPos(), here.getBlockState(entity.getEntityWorld()),
                    entity.getEntityWorld().isRemote)) if (!entity.getEntityWorld().isRemote) if (growable
                            .canUseBonemeal(entity.getEntityWorld(), entity.getEntityWorld().rand, here.getPos(),
                                    state))
            {
                growable.grow(entity.getEntityWorld(), entity.getEntityWorld().rand, here.getPos(), state);
                return;
            }
        }
    }
}
