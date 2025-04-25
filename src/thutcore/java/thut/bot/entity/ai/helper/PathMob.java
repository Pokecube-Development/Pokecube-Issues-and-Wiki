package thut.bot.entity.ai.helper;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;

public class PathMob extends PathfinderMob
{
    public PathMob(final ServerLevel level)
    {
        super(EntityType.VILLAGER, level);
    }
}
