package pokecube.core.ai.logic;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import pokecube.core.interfaces.IPokemob;

public abstract class LogicBase implements Logic
{
    protected final IPokemob  pokemob;
    protected final Mob entity;
    protected BlockGetter    world;

    public LogicBase(final IPokemob pokemob_)
    {
        this.pokemob = pokemob_;
        this.entity = this.pokemob.getEntity();
    }

    @Override
    public boolean shouldRun()
    {
        return this.entity.getHealth() > 0;
    }

    @Override
    public void tick(final Level world)
    {
        this.world = world;
    }
}
