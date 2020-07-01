package pokecube.mobs.moves.world;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
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
        double var1;
        double var3;
        double var5;
        Vector3 v = SpawnHandler.getRandomPointNear(toTeleport, 32);
        if (v == null) // Try a few more times to get a point.
            for (int i = 0; i < 32; i++)
        {
            v = SpawnHandler.getRandomPointNear(toTeleport, 32);
            if (v != null) break;
        }
        if (v == null) return false;
        v = Vector3.getNextSurfacePoint(toTeleport.getEntityWorld(), v, Vector3.secondAxisNeg, 20);
        if (v == null) return false;
        var1 = v.x;
        var3 = v.y + 1;
        var5 = v.z;
        return ActionTeleport.teleportTo(toTeleport, var1, var3, var5);
    }

    /** Teleport the entity */
    protected static boolean teleportTo(final LivingEntity toTeleport, final double par1, final double par3,
            final double par5)
    {

        final short var30 = 128;
        int num;

        toTeleport.setPosition(par1, par3, par5);

        for (num = 0; num < var30; ++num)
        {
            final double var19 = num / (var30 - 1.0D);
            final float var21 = (toTeleport.getRNG().nextFloat() - 0.5F) * 0.2F;
            final float var22 = (toTeleport.getRNG().nextFloat() - 0.5F) * 0.2F;
            final float var23 = (toTeleport.getRNG().nextFloat() - 0.5F) * 0.2F;
            final double var24 = par1 + (toTeleport.getPosX() - par1) * var19 + (toTeleport.getRNG().nextDouble() - 0.5D)
                    * toTeleport.getWidth() * 2.0D;
            final double var26 = par3 + (toTeleport.getPosY() - par3) * var19 + toTeleport.getRNG().nextDouble() * toTeleport
                    .getHeight();
            final double var28 = par5 + (toTeleport.getPosZ() - par5) * var19 + (toTeleport.getRNG().nextDouble() - 0.5D)
                    * toTeleport.getWidth() * 2.0D;
            toTeleport.getEntityWorld().addParticle(ParticleTypes.PORTAL, var24, var26, var28, var21, var22, var23);
        }

        toTeleport.getEntityWorld().playSound(par1, par3, par5, SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.HOSTILE, 1.0F, 1.0F, false);
        toTeleport.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return true;

    }

    public ActionTeleport()
    {
    }

    @Override
    public boolean applyEffect(final IPokemob user, final Vector3 location)
    {
        final boolean inCombat = user.inCombat();
        if (!inCombat && user.getOwner() instanceof ServerPlayerEntity)
        {
            final ServerPlayerEntity target = (ServerPlayerEntity) user.getOwner();
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
