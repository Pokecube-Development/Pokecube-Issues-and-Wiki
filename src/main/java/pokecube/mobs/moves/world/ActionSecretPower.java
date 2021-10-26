package pokecube.mobs.moves.world;

import net.minecraft.Util;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.commands.SecretBase;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.Tracker;
import thut.api.maths.Vector3;

public class ActionSecretPower implements IMoveAction
{
    public ActionSecretPower()
    {
    }

    @Override
    public boolean applyEffect(final IPokemob attacker, final Vector3 location)
    {
        if (attacker.inCombat()) return false;
        if (!(attacker.getOwner() instanceof ServerPlayer)) return false;
        if (!MoveEventsHandler.canAffectBlock(attacker, location, this.getMoveName())) return false;
        final long time = attacker.getEntity().getPersistentData().getLong("lastAttackTick");
        final long now = Tracker.instance().getTick();
        if (time + 20 * 3 > now) return false;
        final ServerPlayer owner = (ServerPlayer) attacker.getOwner();
        final BlockState state = location.getBlockState(owner.getCommandSenderWorld());
        if (!(PokecubeTerrainChecker.isTerrain(state) || PokecubeTerrainChecker.isWood(state)))
        {
            final TranslatableComponent message = new TranslatableComponent("pokemob.createbase.deny.wrongloc");
            owner.sendMessage(message, Util.NIL_UUID);
            return false;
        }
        SecretBase.pendingBaseLocations.put(owner.getUUID(), GlobalPos.of(owner.getCommandSenderWorld().dimension(),
                location.getPos()));
        final TranslatableComponent message = new TranslatableComponent("pokemob.createbase.confirm", location
                .set(location.getPos()));
        message.setStyle(message.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/pokebase confirm " + owner.getX() + " " + owner.getY() + " " + owner.getZ())));
        owner.sendMessage(message, Util.NIL_UUID);
        return true;
    }

    @Override
    public String getMoveName()
    {
        return "secretpower";
    }
}
