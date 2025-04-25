package thut.bot.entity.ai.modules;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.BotAI;

@BotAI(key = "thutbot:wander")
public class RandomWalk extends AbstractBot
{
    public RandomWalk(BotPlayer player)
    {
        super(player);
    }

    @Override
    public void botTick(final ServerLevel world)
    {
        if (mob.getNavigation().isDone())
        {
            Vec3 rand = LandRandomPos.getPos(mob, 32, 8);
            if (rand != null) tryPath(rand);
        }
    }
}
