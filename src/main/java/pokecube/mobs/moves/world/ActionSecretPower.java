package pokecube.mobs.moves.world;

import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.commands.SecretBase;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.lib.TComponent;

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
        final BlockState state = location.getBlockState(player.getLevel());
        if (!(PokecubeTerrainChecker.isTerrain(state) || PokecubeTerrainChecker.isWood(state)))
        {
            final MutableComponent message = TComponent.translatable("pokemob.createbase.deny.wrongloc");
            thut.lib.ChatHelper.sendSystemMessage(player, message);
            return false;
        }
        SecretBase.pendingBaseLocations.put(player.getUUID(),
                GlobalPos.of(player.getLevel().dimension(), location.getPos()));
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
