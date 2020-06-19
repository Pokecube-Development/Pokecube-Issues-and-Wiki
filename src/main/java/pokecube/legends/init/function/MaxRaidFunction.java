package pokecube.legends.init.function;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.init.BlockInit;
import thut.api.maths.Vector3;

/**
 * Uses player interact here to also prevent opening of inventories.
 *
 * @param dependencies
 */
public class MaxRaidFunction
{
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

            // Select a random sub-forme of this mob
            try
            {
                final Collection<PokedexEntry> forms = ret.getFormes();
                if (!forms.isEmpty())
                {
                    final List<PokedexEntry> values = Lists.newArrayList(forms);
                    Collections.shuffle(values);
                    final int num2 = values.size();
                    if (num2 == 0) return ret;
                    PokedexEntry val = values.get(0);
                    if (!(val.dummy || val.isMega) || num2 == 1) return val;
                    for (int i = 1; i < num2; i++)
                    {
                        val = values.get(i);
                        if (!(val.dummy || val.isMega)) break;
                    }
                    return val;
                }
            }
            catch (final IllegalArgumentException e)
            {
                PokecubeMod.LOGGER.warn("Error finding subforms for " + ret, e);
            }

            // If we took too many tries, just throw a missingno...
            if (ret == null && n++ > 10) ret = Database.missingno;
        }

        return ret;
    }

    public static void executeProcedure(final BlockPos pos, final BlockState state, final ServerWorld world)
    {
        if (state.getBlock() != BlockInit.RAID_SPAWN.get()) return;

        final PokedexEntry entityToSpawn = MaxRaidFunction.getRandomEntry();
        final MobEntity entity = PokecubeCore.createPokemob(entityToSpawn, world);
        final Vector3 v = Vector3.getNewVector().set(pos);
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);

        // Raid Battle
        if (entity != null && !entityToSpawn.isMega)
        {
            entity.setHealth(entity.getMaxHealth());
            v.add(0, 1, 0).moveEntity(entity);
            entity.setPosition(v.x, v.y + 3, v.z);
            //
            //pokemob.setHeldItem(new ItemStack(Items.END_STONE));
            //
            
            final Long time = entity.getServer().getWorld(DimensionType.OVERWORLD).getGameTime();
            entity.getPersistentData().putLong("pokecube:dynatime", time + PokecubeLegends.config.raidDuration);
            entity.getPersistentData().putBoolean("pokecube_legends:raid_mob", true);
            pokemob.setCombatState(CombatStates.DYNAMAX, true);
            world.addEntity(entity);
        }
        world.playSound(v.x, v.y, v.z, SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.NEUTRAL, 1, 1, false);

    }
}