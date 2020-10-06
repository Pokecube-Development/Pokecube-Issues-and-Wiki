package pokecube.mobs.moves.world;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import pokecube.core.commands.SecretBase;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
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
        if (!(attacker.getOwner() instanceof ServerPlayerEntity)) return false;
        if (!MoveEventsHandler.canEffectBlock(attacker, location)) return false;
        // TODO secret base stuff.
        final long time = attacker.getEntity().getPersistentData().getLong("lastAttackTick");
        if (time + 20 * 3 > attacker.getEntity().getEntityWorld().getGameTime()) return false;
        final ServerPlayerEntity owner = (ServerPlayerEntity) attacker.getOwner();
        final BlockState state = location.getBlockState(owner.getEntityWorld());
        if (!(PokecubeTerrainChecker.isTerrain(state) || PokecubeTerrainChecker.isWood(state)))
        {
            System.out.println(state);
            final TranslationTextComponent message = new TranslationTextComponent("pokemob.createbase.deny.wrongloc");
            owner.sendMessage(message, Util.DUMMY_UUID);
            return false;
        }
        SecretBase.pendingBaseLocations.put(owner.getUniqueID(), GlobalPos.getPosition(owner.getEntityWorld().getDimensionKey(), location.getPos()));
        final TranslationTextComponent message = new TranslationTextComponent("pokemob.createbase.confirm", location
                .set(location.getPos()));
        message.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pokebase confirm " + owner.getPosX()
                + " " + owner.getPosY() + " " + owner.getPosZ()));
        owner.sendMessage(message, Util.DUMMY_UUID);
        return true;
    }

    @Override
    public String getMoveName()
    {
        return "secretpower";
    }
}
