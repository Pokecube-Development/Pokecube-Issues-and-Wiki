package pokecube.mobs.moves.world;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.blocks.bases.BaseBlock;
import pokecube.core.commands.SecretBase;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.lib.TComponent;

/**
 * This is the implementation of Secret Power's secret base creation. When used
 * on a block, if it is a "terrain"
 * ({@link PokecubeTerrainChecker#isTerrain(BlockState)} or "wood"
 * ({@link PokecubeTerrainChecker#isWood(BlockState)} block, then it will
 * convert it into the secret base block ({@link BaseBlock}), after the player
 * clicks the provided link in chat.<br>
 * <br>
 * This action only applies out of combat, and if the owner is a player. It also
 * checks the relevant permissions, configs, etc for whether secret-power may be
 * used.<br>
 * <br>
 * It functions using the {@link SecretBase} command.
 */
public class ActionSecretPower implements IMoveWorldEffect
{
    public ActionSecretPower()
    {}

    @Override
    public boolean applyOutOfCombat(final IPokemob attacker, final Vector3 location)
    {
        if (!(attacker.getOwner() instanceof ServerPlayer player)) return false;
        if (!MoveEventsHandler.canAffectBlock(attacker, location, this.getMoveName())) return false;
        final long time = attacker.getEntity().getPersistentData().getLong("lastAttackTick");
        final long now = Tracker.instance().getTick();
        if (time + 20 * 3 > now) return false;
        final BlockState state = location.getBlockState(player.level());
        if (!(PokecubeTerrainChecker.isTerrain(state) || PokecubeTerrainChecker.isWood(state)))
        {
            final MutableComponent message = TComponent.translatable("pokemob.createbase.deny.wrongloc");
            thut.lib.ChatHelper.sendSystemMessage(player, message);
            return false;
        }
        SecretBase.pendingBaseLocations.put(player.getUUID(),
                GlobalPos.of(player.level().dimension(), location.getPos()));
        final MutableComponent message = TComponent.translatable("pokemob.createbase.confirm",
                location.set(location.getPos()));
        message.setStyle(message.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/pokebase confirm " + player.getX() + " " + player.getY() + " " + player.getZ())));
        thut.lib.ChatHelper.sendSystemMessage(player, message);
        return true;
    }

    @Override
    public String getMoveName()
    {
        return "secret-power";
    }
}
