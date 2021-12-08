package thut.bot.entity.ai.modules;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import pokecube.core.utils.EntityTools;
import thut.api.entity.ICopyMob;
import thut.bot.entity.BotPlayer;
import thut.bot.entity.ai.IBotAI;
import thut.bot.entity.ai.helper.PathMob;

public abstract class AbstractBot implements IBotAI
{
    // The bot that goes with this routemaker
    protected final BotPlayer player;

    // The mob used for pathfinding
    protected PathfinderMob mob = null;

    private String key;

    public AbstractBot(final BotPlayer player)
    {
        this.player = player;
    }

    @Override
    public BotPlayer getBot()
    {
        return player;
    }

    @Override
    public final String getKey()
    {
        return key;
    }

    @Override
    public void setKey(String key)
    {
        this.key = key;
    }

    protected void preBotTick(final ServerLevel world)
    {
        this.player.setGameMode(GameType.CREATIVE);
        this.player.setInvulnerable(true);

        if (this.mob == null || mob.level != player.level)
        {
            this.mob = new PathMob(world);
            this.mob.setInvulnerable(true);
            this.mob.setInvisible(true);

            ICopyMob.copyPositions(this.mob, this.player);
            ICopyMob.copyRotations(this.mob, this.player);
            ICopyMob.copyEntityTransforms(this.mob, this.player);
        }

        this.mob.setSilent(true);
        this.mob.maxUpStep = 1.25f;

        // Prevent the mob from having its own ideas of what to do
        this.mob.getBrain().removeAllBehaviors();

        this.mob.setOldPosAndRot();
        this.mob.tickCount = this.player.tickCount;
    }

    protected void postBotTick(final ServerLevel world)
    {
        this.mob.tick();

        if (this.mob.isInWater()) this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, 0.05, 0));
        else this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0, -0.08, 0));

        ICopyMob.copyEntityTransforms(this.player, this.mob);
        ICopyMob.copyPositions(this.player, this.mob);
        ICopyMob.copyRotations(this.player, this.mob);
    }

    public abstract void botTick(final ServerLevel world);

    @Override
    public void tick()
    {
        if (ForgeHooks.onLivingUpdate(this.player)) return;
        if (!(this.player.level instanceof final ServerLevel world)) return;

        preBotTick(world);
        botTick(world);
        postBotTick(world);
    }

    protected void tryPath(final Vec3 targ)
    {
        final PathNavigation navi = this.mob.getNavigation();
        final BlockPos pos = new BlockPos(targ);

        if (navi.isInProgress() && navi.getTargetPos().closerThan(pos, 1)) return;

        final Path p = navi.createPath(pos, 16, 16);
        if (p != null)
        {
            final double spd = getTag().getBoolean("pathing") ? 1.2 : 0.6;
            navi.moveTo(p, spd);
        }
    }

    /**
     * Here we will teleport the botplayer to a location. This will also
     * transfer all spectators as well.
     *
     * @param tpTo pos to teleport botplayer to
     */
    protected void teleBot(final BlockPos tpTo)
    {
        // Collect and remove the spectators, prevent them from spectating while
        // this transfer is done.
        final List<ServerPlayer> readd = Lists.newArrayList();
        for (final ServerPlayer player : ((ServerLevel) this.player.level).players())
            if (player.getCamera() == this.player && player != this.player)
        {
            player.setCamera(player);
            player.teleportTo(tpTo.getX(), tpTo.getY(), tpTo.getZ());
            readd.add(player);
        }
        // Move us to the nearest village to the target.
        this.player.teleportTo(tpTo.getX(), tpTo.getY(), tpTo.getZ());

        // Re-add the specators
        for (final ServerPlayer player : readd) player.setCamera(this.player);

        // Transfer the pathing mob as well.
        EntityTools.copyPositions(this.mob, this.player);
        EntityTools.copyRotations(this.mob, this.player);
        EntityTools.copyEntityTransforms(this.mob, this.player);
    }
}
