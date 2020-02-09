package pokecube.legends.init.function;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.maths.Vector3;

public class MaxRaidActivationFunction
{
    // Max Size Pokemob Raids
    public static float h = 24;

    public static Field form_field;
    static
    {
        try
        {
            MaxRaidActivationFunction.form_field = PokedexEntry.class.getDeclaredField("forms");
            MaxRaidActivationFunction.form_field.setAccessible(true);
        }
        catch (NoSuchFieldException | SecurityException e)
        {
            e.printStackTrace();
            MaxRaidActivationFunction.form_field = null;
        }
    }

    public static PokedexEntry getRandomEntry()
    {
        PokedexEntry ret = null;
        int n = 0;
        final Random rand = new Random();
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
            @SuppressWarnings("unchecked")
            final Map<String, PokedexEntry> forms = (Map<String, PokedexEntry>) MaxRaidActivationFunction.form_field
                    .get(ret);
            if (!forms.isEmpty())
            {
                final List<PokedexEntry> values = Lists.newArrayList(forms.values());
                Collections.shuffle(values);
                final int num = values.size();
                if (num == 0) return ret;
                PokedexEntry val = values.get(0);
                if (!(val.dummy || val.isMega) || num == 1) return val;
                for (int i = 1; i < num; i++)
                {
                    val = values.get(i);
                    if (!(val.dummy || val.isMega)) break;
                }
                return val;
            }
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            PokecubeMod.LOGGER.warn("Error finding subforms for " + ret, e);
        }

        return ret;
    }

    public static void executeProcedure(final int x, final int y, final int z, final ServerWorld world)
    {
        if (!world.isRemote)
        {
            final PokedexEntry entityToSpawn = MaxRaidActivationFunction.getRandomEntry();
            final BlockPos pos = null;
            final MobEntity entity = PokecubeCore.createPokemob(entityToSpawn, world);
            final Vector3 location = Vector3.getNewVector().set(pos);
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (entity != null && !entityToSpawn.hasMegaForm)
            {
                entity.setHealth(entity.getMaxHealth() + 50f);
                location.add(0, 6, 0).moveEntity(entity);
                entity.setPosition(x, y, z);
                world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState());
                // entity.tags.add("raid");

                // Size for MaxPokemob
                pokemob.setSize(MaxRaidActivationFunction.h);
                pokemob.getLevel();
                // pokemob.setHeldItem(new ItemStack(ItemInit.FRAGMENTDYN));
                world.addEntity(entity);
            }
        }
    }
}