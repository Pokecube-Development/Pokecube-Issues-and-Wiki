package pokecube.core.client.render.mobs.overlays;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class ExitCube
{
    public static void render(final IPokemob pokemob, final MatrixStack mat, final IRenderTypeBuffer iRenderTypeBuffer,
            final float partialTick)
    {
        if (!pokemob.getGeneralState(GeneralStates.EXITINGCUBE)) return;
        final Entity entity = pokemob.getEntity();
        final CompoundNBT sealTag = PokecubeManager.getSealTag(entity);
        Evolution.renderEffect(pokemob, mat, iRenderTypeBuffer, partialTick, LogicMiscUpdate.EXITCUBEDURATION, true);
        if (sealTag != null && !sealTag.isEmpty())
        {
            final Random rand = ThutCore.newRandom();
            final Vector3 loc = Vector3.getNewVector().set(entity, true);
            final float width = entity.getBbWidth();
            final Vector3 vel = Vector3.getNewVector();
            if (sealTag.getBoolean("Bubbles"))
            {
                loc.x += (rand.nextDouble() - 0.5) * width;
                loc.y += rand.nextDouble();
                loc.z += (rand.nextDouble() - 0.5) * width;
                PokecubeCore.spawnParticle(entity.getCommandSenderWorld(), ParticleTypes.BUBBLE.getRegistryName().toString(),
                        loc, vel);
            }
            if (sealTag.getBoolean("Flames"))
            {
                loc.x += (rand.nextDouble() - 0.5) * width;
                loc.y += rand.nextDouble();
                loc.z += (rand.nextDouble() - 0.5) * width;
                PokecubeCore.spawnParticle(entity.getCommandSenderWorld(), ParticleTypes.FLAME.getRegistryName().toString(),
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
                PokecubeCore.spawnParticle(entity.getCommandSenderWorld(), "leaf", loc, vel);
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
                final int colour = DyeColor.byId(id).textColor;
                PokecubeCore.spawnParticle(entity.getCommandSenderWorld(), "powder", loc, vel, colour | 0xFF000000);
            }
        }
        if (pokemob.isShiny())
        {
            final Random rand = ThutCore.newRandom();
            final Vector3 loc = Vector3.getNewVector().set(entity, true);
            final float width = entity.getBbWidth();
            final Vector3 vel = Vector3.getNewVector();
            vel.x = rand.nextGaussian() / 100;
            vel.y = rand.nextGaussian() / 100;
            vel.z = rand.nextGaussian() / 100;
            loc.x += width * rand.nextGaussian() / 2;
            loc.y += width * rand.nextGaussian() / 2;
            loc.z += width * rand.nextGaussian() / 2;
            final int colour = DyeColor.GREEN.textColor;
            if (rand.nextFloat() < 0.125) PokecubeCore.spawnParticle(entity.getCommandSenderWorld(), "happy_villager", loc,
                    vel, colour | 0xFF000000);
        }
    }
}
