package pokecube.legends.init.function;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.legends.blocks.customblocks.PortalWarp;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.FeaturesInit;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class PortalActiveFunction
{
    public static PokedexEntry getRandomEntry()
    {
        PokedexEntry ret = null;
        int n = 0;
        final Random rand = ThutCore.newRandom();
        while (ret == null)
        {
            // Pick a random number from 1 to just below database size, this
            // ensures no missingnos
            final int num = rand.nextInt(Database.baseFormes.size() - 1) + 1;
            ret = Database.getEntry(num);
            // If we took too many tries, just throw a missingno...
            if (ret == null && n++ > 10) ret = Database.missingno;
        }
        // Select a random sub-forme of this mob
        try
        {
            final Collection<PokedexEntry> forms = ret.getFormes();
            if (!forms.isEmpty())
            {
                final List<PokedexEntry> values = Lists.newArrayList(forms);
                Collections.shuffle(values);
                final int num = values.size();
                if (num == 0) return ret;
                PokedexEntry val = values.get(0);
                if (!(val.dummy || val.isMega()) || num == 1) return val;
                for (int i = 1; i < num; i++)
                {
                    val = values.get(i);
                    if (!(val.dummy || val.isMega())) break;
                }
                return val;
            }
        }
        catch (final IllegalArgumentException e)
        {
            PokecubeMod.LOGGER.warn("Error finding subforms for " + ret, e);
        }

        return ret;
    }

    public static void executeProcedure(final BlockPos pos, final BlockState state, final ServerLevel world)
    {
        if (state.getBlock() != BlockInit.PORTAL.get()) return;

        final PokedexEntry entityToSpawn = PortalActiveFunction.getRandomEntry();
        final Mob entity = PokecubeCore.createPokemob(entityToSpawn, world);
        final Vector3 v = new Vector3().set(pos);
        final ResourceKey<Level> key = world.dimension();

        // // Normal Worlds
        if (entity != null && !entityToSpawn.isLegendary() && !entityToSpawn.isMega()
                && key != FeaturesInit.ULTRASPACE_KEY)
        {
            entity.setHealth(entity.getMaxHealth());
            v.add(0, 1, 0).moveEntity(entity);
            entity.setPos(v.x, v.y, v.z);
            world.addFreshEntity(entity);
        }

        // Ultra Space
        else if (entity != null && !entityToSpawn.isMega() && key == FeaturesInit.ULTRASPACE_KEY)
        {
            entity.setHealth(entity.getMaxHealth());
            v.add(0, 1, 0).moveEntity(entity);
            entity.setPos(v.x, v.y, v.z);
            world.addFreshEntity(entity);
        }
        world.setBlockAndUpdate(pos, state.setValue(PortalWarp.ACTIVE, false));
        world.playLocalSound(v.x, v.y, v.z, SoundEvents.WITHER_DEATH, SoundSource.NEUTRAL, 1, 1, false);

    }
}