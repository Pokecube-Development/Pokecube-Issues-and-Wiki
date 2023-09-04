package pokecube.core.client.render.mobs.overlays;

import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

public class ExitCube
{
    public static void render(final IPokemob pokemob, final PoseStack mat, final MultiBufferSource iRenderTypeBuffer,
            final float partialTick)
    {
        if (!pokemob.getGeneralState(GeneralStates.EXITINGCUBE)) return;
        final Entity entity = pokemob.getEntity();
        final CompoundTag sealTag = PokecubeManager.getSealTag(entity);
        Evolution.renderEffect(pokemob, mat, iRenderTypeBuffer, partialTick, LogicMiscUpdate.EXITCUBEDURATION, true);
        if (sealTag != null && !sealTag.isEmpty())
        {
            final Random rand = ThutCore.newRandom();
            final Vector3 loc = new Vector3().set(entity, true);
            final float width = entity.getBbWidth();
            final Vector3 vel = new Vector3();
            if (sealTag.getBoolean("Bubbles"))
            {
                loc.x += (rand.nextDouble() - 0.5) * width;
                loc.y += rand.nextDouble();
                loc.z += (rand.nextDouble() - 0.5) * width;
                PokecubeCore.spawnParticle(entity.level(), RegHelper.getKey(ParticleTypes.BUBBLE).toString(),
                        loc, vel);
            }
            if (sealTag.getBoolean("Flames"))
            {
                loc.x += (rand.nextDouble() - 0.5) * width;
                loc.y += rand.nextDouble();
                loc.z += (rand.nextDouble() - 0.5) * width;
                PokecubeCore.spawnParticle(entity.level(), RegHelper.getKey(ParticleTypes.FLAME).toString(),
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
                PokecubeCore.spawnParticle(entity.level(), "leaf", loc, vel);
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
                final int colour = DyeColor.byId(id).getTextColor();
                PokecubeCore.spawnParticle(entity.level(), "powder", loc, vel, colour | 0xFF000000);
            }
        }
        if (pokemob.isShiny())
        {
            final Random rand = ThutCore.newRandom();
            final Vector3 loc = new Vector3().set(entity, true);
            final float width = entity.getBbWidth();
            final Vector3 vel = new Vector3();
            vel.x = rand.nextGaussian() / 100;
            vel.y = rand.nextGaussian() / 100;
            vel.z = rand.nextGaussian() / 100;
            loc.x += width * rand.nextGaussian() / 2;
            loc.y += width * rand.nextGaussian() / 2;
            loc.z += width * rand.nextGaussian() / 2;
            final int colour = DyeColor.GREEN.getTextColor();
            if (rand.nextFloat() < 0.125) PokecubeCore.spawnParticle(entity.level(), "happy_villager", loc,
                    vel, colour | 0xFF000000);
        }
    }
}
