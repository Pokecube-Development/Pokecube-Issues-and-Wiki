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

    public LogicBase(IPokemob pokemob_)
    {
        this.pokemob = pokemob_;
        this.entity = this.pokemob.getEntity();
    }

    @Override
    public void tick(World world)
    {
        this.world = world;
    }
}
