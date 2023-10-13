package pokecube.gimmicks.terastal;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.raids.IBossProvider;
import pokecube.api.raids.RaidManager.RaidContext;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.TaskBase.InventoryChange;
import pokecube.core.database.Database;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class TerastalRaid implements IBossProvider
{
    public static int RAID_DURATION = 600;

    public static ResourceLocation lootTable = new ResourceLocation("pokecube_legends", "raids/raid_drop");

    private static PokedexEntry getRandomEntry(ServerLevel level)
    {
        PokedexEntry ret = null;
        int n = 0;
        final var rand = level.getRandom();
        while (ret == null)
        {
            // Pick a random number from 1 to just below database size, this
            // ensures no missingnos
            final int num = rand.nextInt(Database.getSortedFormes().size());
            ret = Database.getSortedFormes().get(num);

            // If we took too many tries, just throw a missingno...
            if (ret == null && n++ > 10) ret = Database.missingno;
            if (ret == null || ret.dummy || ret.isLegendary() || ret.isMega()) ret = null;
            if (ret != null) break;
        }
        return ret;
    }

    @Override
    public LivingEntity makeBoss(RaidContext context)
    {
        PokedexEntry entry = getRandomEntry(context.level());
        if (entry != null && entry != Database.missingno)
        {
            final Mob entity = PokecubeCore.createPokemob(entry, context.level());
            final Vector3 v = new Vector3().set(context.pos());
            final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
            final List<AIRoutine> bannedAI = Lists.newArrayList();

            var genes = TerastalMechanic.getTeraGenes(entity);
            genes.setAllele(0, new TeraTypeGene().mutate());
            genes.setAllele(1, new TeraTypeGene().mutate());
            genes.refreshExpressed();

            bannedAI.add(AIRoutine.BURROWS);
            bannedAI.add(AIRoutine.BEEAI);
            bannedAI.add(AIRoutine.ANTAI);

            // Pokemob Level Spawm
            final int level = ThutCore.newRandom().nextInt(50);

            pokemob.setForSpawn(Tools.levelToXp(entry.getEvolutionMode(), level), false);

            long time = Tracker.instance().getTick();
            entity.getPersistentData().putLong("pokecube:tera_raid_start", time);
            entity.getPersistentData().putInt("pokecube:tera_raid_duration", RAID_DURATION);

            bannedAI.forEach(e -> pokemob.setRoutineState(e, false));

            pokemob.spawnInit();
            v.add(0.5, 3, 0.5).moveEntity(entity);
            return entity;
        }

        return null;
    }

    @Override
    public void postBossSpawn(LivingEntity boss, RaidContext context)
    {
        IPokemob pokemob = PokemobCaps.getPokemobFor(boss);
        TerastalMechanic.doTera(pokemob);

        final LootTable loottable = boss.level().getServer().getLootData().getLootTable(lootTable);
        LootParams params = new LootParams.Builder((ServerLevel) boss.level()).create(loottable.getParamSet());
        // Generate the loot list.
        final List<ItemStack> list = loottable.getRandomItems(params);

        if (!list.isEmpty()) Collections.shuffle(list);
        final int n = 1 + context.level().getRandom().nextInt(4);
        int i = 0;
        for (final ItemStack itemstack : list)
        {
            if (i == 0) pokemob.setHeldItem(itemstack);
            else new InventoryChange(boss, 2, itemstack, true).run(context.level());
            if (i++ >= n) break;
        }
        context.level().playLocalSound(boss.getX(), boss.getY(), boss.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE,
                SoundSource.NEUTRAL, 1, 1, false);
    }

    @Override
    public String getKey()
    {
        return "terastal";
    }

}
