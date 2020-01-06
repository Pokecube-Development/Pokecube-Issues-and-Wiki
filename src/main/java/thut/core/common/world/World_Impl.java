package thut.core.common.world;

import java.util.UUID;

import net.minecraft.world.IWorld;
import thut.api.world.World;
import thut.api.world.blocks.Block;
import thut.api.world.mobs.Mob;
import thut.api.world.utils.Vector;
import thut.core.common.world.blocks.Block_Impl;
import thut.core.common.world.utils.Vector_I;

public class World_Impl implements World
{
    IWorld wrapped;

    public World_Impl(IWorld iWorld)
    {
        this.wrapped = iWorld;
    }

    @Override
    public boolean addMob(Mob mob)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Block getBlock(Vector<Integer> position)
    {
        final Vector_I pos = new Vector_I(position);
        // TODO consider caching this and cleanup stuff?
        // Maybe store these in a chunk capability instead.
        final Block block = new Block_Impl(pos);

        return block;
    }

    @Override
    public int getLevel()
    {
        return this.wrapped.getDimension().getType().getId();
    }

    @Override
    public Mob getMob(UUID id)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean removeMob(Mob mob)
    {
        // TODO Auto-generated method stub
        return false;
    }

}
