package pokecube.core.client.render.mobs.overlays;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;

public class ExitCube
{
    public static void render(final IPokemob pokemob, final Vec3d pos, final float partialTick)
    {
        if (!pokemob.getGeneralState(GeneralStates.EXITINGCUBE)) return;
        final Entity entity = pokemob.getEntity();
        final CompoundNBT sealTag = PokecubeManager.getSealTag(entity);
        Evolution.renderEffect(pokemob, pos, partialTick, LogicMiscUpdate.EXITCUBEDURATION, true);
        if (sealTag != null && !sealTag.isEmpty())
        {
            final Random rand = new Random();
            final Vector3 loc = Vector3.getNewVector().set(entity, true);
            final float width = entity.getWidth();
            final Vector3 vel = Vector3.getNewVector();
            if (sealTag.getBoolean("Bubbles"))
            {
                loc.x += (rand.nextDouble() - 0.5) * width;
                loc.y += rand.nextDouble();
                loc.z += (rand.nextDouble() - 0.5) * width;
                PokecubeCore.spawnParticle(entity.getEntityWorld(), ParticleTypes.BUBBLE.getRegistryName().toString(),
                        loc, vel);
            }
            if (sealTag.getBoolean("Flames"))
            {
                loc.x += (rand.nextDouble() - 0.5) * width;
                loc.y += rand.nextDouble();
                loc.z += (rand.nextDouble() - 0.5) * width;
                PokecubeCore.spawnParticle(entity.getEntityWorld(), ParticleTypes.FLAME.getRegistryName().toString(),
                        loc, vel);
            }
            if (sealTag.getBoolean("Leaves"))
            {
                vel.x = rand.nextGaussian() / 100;
                vel.y = rand.nextGaussian() / 100;
                vel.z = rand.nextGaussian() / 100;
                loc.x += rand.nextGaussian() / 2;
                loc.y += rand.nextGaussian() / 2;
                loc.z += rand.nextGaussian() / 2;
                PokecubeCore.spawnParticle(entity.getEntityWorld(), "leaf", loc, vel);
            }
            if (sealTag.contains("dye"))
            {
                vel.x = rand.nextGaussian() / 100;
                vel.y = rand.nextGaussian() / 100;
                vel.z = rand.nextGaussian() / 100;
                loc.x += width * rand.nextGaussian() / 2;
                loc.y += width * rand.nextGaussian() / 2;
                loc.z += width * rand.nextGaussian() / 2;
                final int id = sealTag.getInt("dye");
                final int colour = DyeColor.byId(id).field_218390_z;
                PokecubeCore.spawnParticle(entity.getEntityWorld(), "powder", loc, vel, colour | 0xFF000000);
            }
        }
    }
}
