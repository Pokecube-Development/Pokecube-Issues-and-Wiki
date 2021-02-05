package pokecube.legends.init.function;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootTable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.utils.Tools;
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
    public static ResourceLocation lootTable = new ResourceLocation("pokecube_legends", "raids/raid_drop");

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
        final LivingEntity poke = pokemob.getEntity();

        final LootTable loottable = pokemob.getEntity().getEntityWorld().getServer().getLootTableManager()
                .getLootTableFromLocation(MaxRaidFunction.lootTable);
        final LootContext.Builder lootcontext$builder = new LootContext.Builder((ServerWorld) pokemob.getEntity()
                .getEntityWorld()).withRandom(poke.getRNG());
        // Generate the loot list.
        final List<ItemStack> list = loottable.generate(lootcontext$builder.build(loottable.getParameterSet()));

        // Raid Battle We will move legendaries to the rare raids.
        if (entity != null && !entityToSpawn.isMega && !entityToSpawn.isLegendary())
        {
            final int level = new Random().nextInt(100);
            pokemob.setExp(Tools.levelToXp(entityToSpawn.getEvolutionMode(), level), false);
            final Long time = entity.getServer().getWorld(World.OVERWORLD).getGameTime();
            entity.getPersistentData().putLong("pokecube:dynatime", time + PokecubeLegends.config.raidDuration);
            entity.getPersistentData().putBoolean("pokecube_legends:raid_mob", true);
            pokemob.setCombatState(CombatStates.DYNAMAX, true);
            pokemob.spawnInit();
            v.add(0, 1, 0).moveEntity(entity);
            entity.setPosition(v.x, v.y + 3, v.z);

            if (!list.isEmpty()) Collections.shuffle(list);
            for (final ItemStack itemstack : list)
                // Pick first valid item in it.
                if (!itemstack.isEmpty())
                {
                    final ItemStack stack = itemstack.copy();
                    pokemob.setHeldItem(stack);
                    break;
                }
            world.addEntity(entity);
        }
        world.playSound(v.x, v.y, v.z, SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.NEUTRAL, 1, 1, false);

    }
}