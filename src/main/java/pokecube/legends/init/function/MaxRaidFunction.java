package pokecube.legends.init.function;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.TaskBase.InventoryChange;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.utils.Tools;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.init.BlockInit;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

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
        final Random rand = ThutCore.newRandom();
        while (ret == null)
        {
            // Pick a random number from 1 to just below database size, this
            // ensures no missingnos
            final int num = rand.nextInt(Database.getSortedFormes().size());
            ret = Database.getSortedFormes().get(num);

            // If we took too many tries, just throw a missingno...
            if (ret == null && n++ > 10) ret = Database.missingno;
            if (ret == null || ret.dummy || ret.isLegendary() || ret.isMega() && !ret.isGMax()) ret = null;
            if (ret != null) break;
        }
        return ret;
    }

    public static void executeProcedure(final BlockPos pos, final BlockState state, final ServerLevel world)
    {
        if (state.getBlock() != BlockInit.RAID_SPAWNER.get()) return;

        final PokedexEntry entry = MaxRaidFunction.getRandomEntry();

        // Raid Battle We will move legendaries to the rare raids.
        if (entry != null && entry != Database.missingno)
        {
            final Mob entity = PokecubeCore.createPokemob(entry, world);
            final Vector3 v = Vector3.getNewVector().set(pos);
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            final LivingEntity poke = pokemob.getEntity();

            final LootTable loottable = pokemob.getEntity().getCommandSenderWorld().getServer().getLootTables().get(
                    MaxRaidFunction.lootTable);
            final LootContext.Builder lootcontext$builder = new LootContext.Builder((ServerLevel) pokemob.getEntity()
                    .getCommandSenderWorld()).withRandom(poke.getRandom());
            // Generate the loot list.
            final List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(loottable.getParamSet()));

            final List<AIRoutine> bannedAI = Lists.newArrayList();

            if (entry.isGMax()) pokemob.setCombatState(CombatStates.GIGANTAMAX, true);

            bannedAI.add(AIRoutine.BURROWS);
            bannedAI.add(AIRoutine.BEEAI);
            bannedAI.add(AIRoutine.ANTAI);

            // Pokemob Level Spawm
            final int level = ThutCore.newRandom().nextInt(50);

            pokemob.setForSpawn(Tools.levelToXp(entry.getEvolutionMode(), level), false);

            final Long time = Tracker.instance().getTick();
            entity.getPersistentData().putLong("pokecube:dynatime", time + PokecubeLegends.config.raidDuration);
            entity.getPersistentData().putBoolean("pokecube_legends:raid_mob", true);

            pokemob.setCombatState(CombatStates.DYNAMAX, true);

            bannedAI.forEach(e -> pokemob.setRoutineState(e, false));

            pokemob.spawnInit();

            v.add(0, 1, 0).moveEntity(entity);
            entity.setPos(v.x, v.y + 3, v.z);
            world.addFreshEntity(entity);
            entity.setHealth(entity.getMaxHealth());
            if (!list.isEmpty()) Collections.shuffle(list);
            final int n = 1 + world.getRandom().nextInt(4);
            int i = 0;
            for (final ItemStack itemstack : list)
            {
                if (i == 0) pokemob.setHeldItem(itemstack);
                else new InventoryChange(entity, 2, itemstack, true).run(world);
                if (i++ >= n) break;
            }
            world.playLocalSound(v.x, v.y, v.z, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.NEUTRAL, 1, 1,
                    false);

        }
    }
}