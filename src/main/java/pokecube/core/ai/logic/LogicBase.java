package pokecube.core.ai.logic;

import net.minecraft.entity.MobEntity;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.core.interfaces.IPokemob;

public abstract class LogicBase implements Logic
{
    protected final IPokemob  pokemob;
    protected final MobEntity entity;
    protected IBlockReader    world;

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
    public void tick(final World world)
    {
        this.world = world;
    }
}
