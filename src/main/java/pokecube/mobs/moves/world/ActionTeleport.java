package pokecube.mobs.moves.world;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.events.TeleportEvent;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import thut.api.maths.Vector3;

public class ActionTeleport implements IMoveAction
{
    /** Teleport the entity to a random nearby position */
    public static boolean teleportRandomly(final LivingEntity toTeleport)
    {
        double destX;
        double destY;
        double destZ;
        Vector3 v = SpawnHandler.getRandomPointNear(toTeleport, 32);
        if (v == null) // Try a few more times to get a point.
            for (int i = 0; i < 32; i++)
        {
            v = SpawnHandler.getRandomPointNear(toTeleport, 32);
            if (v != null) break;
        }
        if (v == null) return false;
        v = Vector3.getNextSurfacePoint(toTeleport.getCommandSenderWorld(), v, Vector3.secondAxisNeg, 20);
        if (v == null) return false;
        destX = v.x;
        destY = v.y + 1;
        destZ = v.z;
        return ActionTeleport.teleportTo(toTeleport, destX, destY, destZ);
    }

    /** Teleport the entity */
    protected static boolean teleportTo(final LivingEntity toTeleport,  double posX,  double posY,
             double posZ)
    {
        final TeleportEvent event = TeleportEvent.onUseTeleport(toTeleport, posX, posY, posZ);
        if(event.isCanceled()) return false;

        posX = event.getTargetX();
        posY = event.getTargetY();
        posZ = event.getTargetZ();

        final short particleCount = 128;
        int num;

        toTeleport.teleportTo(posX, posY, posZ);

        for (num = 0; num < particleCount; ++num)
        {
            final double var19 = num / (particleCount - 1.0D);
            final float var21 = (toTeleport.getRandom().nextFloat() - 0.5F) * 0.2F;
            final float var22 = (toTeleport.getRandom().nextFloat() - 0.5F) * 0.2F;
            final float var23 = (toTeleport.getRandom().nextFloat() - 0.5F) * 0.2F;
            final double var24 = posX + (toTeleport.getX() - posX) * var19 + (toTeleport.getRandom().nextDouble() - 0.5D)
                    * toTeleport.getBbWidth() * 2.0D;
            final double var26 = posY + (toTeleport.getY() - posY) * var19 + toTeleport.getRandom().nextDouble() * toTeleport
                    .getBbHeight();
            final double var28 = posZ + (toTeleport.getZ() - posZ) * var19 + (toTeleport.getRandom().nextDouble() - 0.5D)
                    * toTeleport.getBbWidth() * 2.0D;
            toTeleport.getCommandSenderWorld().addParticle(ParticleTypes.PORTAL, var24, var26, var28, var21, var22, var23);
        }
        toTeleport.getCommandSenderWorld().playLocalSound(posX, posY, posZ, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 1.0F, 1.0F, false);
        toTeleport.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return true;
    }

    public ActionTeleport()
    {
    }

    @Override
    public boolean applyEffect(final IPokemob user, final Vector3 location)
    {
        final boolean inCombat = user.inCombat();
        if (!inCombat && user.getOwner() instanceof ServerPlayer)
        {
            final ServerPlayer target = (ServerPlayer) user.getOwner();
            EventsHandler.recallAllPokemobsExcluding(target, null, false);
            try
            {
                new TeleportHandler().handleCommand(user);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error Teleporting " + target, e);
            }
        }
        else if (inCombat)
        {
            BrainUtils.deagro(user.getEntity());
            if (user.getGeneralState(GeneralStates.TAMED)) user.onRecall();
            else ActionTeleport.teleportRandomly(user.getEntity());
        }
        return true;
    }

    @Override
    public String getMoveName()
    {
        return "teleport";
    }
}
