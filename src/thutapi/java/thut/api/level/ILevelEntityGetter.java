package thut.api.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

public interface ILevelEntityGetter
{
    LevelEntityGetter<Entity> thutcore$getEntityGetter();
}
