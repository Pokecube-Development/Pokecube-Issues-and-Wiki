package thut.bot.entity.ai.modules;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.BotAI;

@BotAI(key = "thutbot:follow")
public class FollowBot extends AbstractBot
{
    ServerPlayer toFollow = null;

    public FollowBot(BotPlayer player)
    {
        super(player);
    }

    @Override
    public void botTick(final ServerLevel world)
    {
        if (toFollow != null)
        {
            double distance = toFollow.distanceToSqr(player);
            if (distance > 64)
            {
                this.teleBot(toFollow.getOnPos());
            }
            else if (distance > 2)
            {
                int x = toFollow.getOnPos().getX();
                int y = toFollow.getOnPos().getY();
                int z = toFollow.getOnPos().getZ();
                getTag().putBoolean("pathing", true);
                if (mob.getNavigation().isDone()) tryPath(new Vec3(x, y, z));
            }
        }
        else if (getTag().hasUUID("following") && player.tickCount % 100 == 0)
        {
            UUID following = getTag().getUUID("following");
            toFollow = player.server.getPlayerList().getPlayer(following);
        }
    }

    public void start(@Nullable ServerPlayer commander)
    {
        if (commander == null) return;
        toFollow = commander;
        getTag().putUUID("following", commander.getUUID());
    }

    public void end(@Nullable ServerPlayer commander)
    {
        getTag().remove("following");
    }
}
