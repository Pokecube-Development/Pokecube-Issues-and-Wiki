package pokecube.mobs.moves.world;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.commandhandlers.TeleportHandler;
import pokecube.api.events.pokemobs.SpawnEvent;
import pokecube.api.events.pokemobs.TeleportEvent;
import pokecube.api.moves.Battle;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.eventhandlers.SpawnHandler;
import thut.api.maths.Vector3;

public class ActionTeleport implements IMoveWorldEffect
{
    /** Teleport the entity to a random nearby position */
    public static void teleportRandomly(final LivingEntity toTeleport)
    {
        var pokemob = PokemobCaps.getPokemobFor(toTeleport);
        var surface = SpawnEvent.SpawnSurface.any();
        if (pokemob != null) surface = SpawnEvent.SpawnSurface.of(pokemob.getPokedexEntry());
        double destX;
        double destY;
        double destZ;
        Vector3 v = SpawnHandler.getRandomPointNear(toTeleport, 32, surface);
        if (v == null) // Try a few more times to get a point.
            for (int i = 0; i < 32; i++)
            {
                v = SpawnHandler.getRandomPointNear(toTeleport, 32, surface);
                if (v != null) break;
            }
        if (v == null) return;
        v = Vector3.getNextSurfacePoint(toTeleport.level(), v, Vector3.secondAxisNeg, 20);
        if (v == null) return;
        destX = v.x;
        destY = v.y + 1;
        destZ = v.z;
        ActionTeleport.teleportTo(toTeleport, destX, destY, destZ);
    }

    /** Teleport the entity */
    protected static void teleportTo(final LivingEntity toTeleport, double posX, double posY, double posZ)
    {
        final TeleportEvent event = TeleportEvent.onUseTeleport(toTeleport, posX, posY, posZ);
        if (event.isCanceled()) return;
        posX = event.getTargetX();
        posY = event.getTargetY();
        posZ = event.getTargetZ();
        toTeleport.teleportTo(posX, posY, posZ);
    }

    public ActionTeleport()
    {}

    @Override
    public boolean applyOutOfCombat(final IPokemob user, final Vector3 location, HitResult hit)
    {
        if (user.getOwner() instanceof ServerPlayer target)
        {
            EventsHandler.recallAllPokemobsExcluding(target, null, false);
            try
            {
                new TeleportHandler().handleCommand(user);
            }
            catch (final Exception e)
            {
                PokecubeAPI.LOGGER.error("Error Teleporting {}", target, e);
            }
        }
        return true;
    }

    @Override
    public boolean applyInCombat(IPokemob user, Vector3 location, HitResult hit)
    {
        Battle battle = user.getBattle();
        BrainUtils.deagro(user.getEntity());
        if (battle != null) battle.removeFromBattle(user.getEntity());
        if (user.getGeneralState(GeneralStates.TAMED)) user.onRecall();
        else ActionTeleport.teleportRandomly(user.getEntity());
        return true;
    }

    @Override
    public String getMoveName()
    {
        return "teleport";
    }
}
