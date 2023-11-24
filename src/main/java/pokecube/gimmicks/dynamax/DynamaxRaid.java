package pokecube.gimmicks.dynamax;

import java.util.Collections;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.CaptureEvent.Post;
import pokecube.api.events.pokemobs.CaptureEvent.Pre;
import pokecube.api.events.pokemobs.FaintEvent;
import pokecube.api.raids.IBossProvider;
import pokecube.api.raids.RaidManager.RaidContext;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.tasks.TaskBase.InventoryChange;
import pokecube.core.database.Database;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokecubes.helper.CaptureManager;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class DynamaxRaid implements IBossProvider
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
    public LivingEntity makeBoss(RaidContext context, IPokemob pokemob)
    {
        boolean newMob = pokemob == null;
        if (newMob)
        {
            PokedexEntry entry = getRandomEntry(context.level());
            if (entry != null && entry != Database.missingno)
            {
                Mob entity = PokecubeCore.createPokemob(entry, context.level());
                pokemob = PokemobCaps.getPokemobFor(entity);
            }
        }

        if (pokemob != null)
        {
            var entity = pokemob.getEntity();
            var entry = pokemob.getPokedexEntry();
            var genes = DynamaxGene.getDyna(entity);
            genes.gigantamax = false;

            // Pokemob Level Spawm
            final int level = ThutCore.newRandom().nextInt(50);

            if (newMob)
            {
                pokemob.setForSpawn(Tools.levelToXp(entry.getEvolutionMode(), level), false);
                pokemob.spawnInit();
                final Vector3 v = new Vector3().set(context.pos());
                v.add(0.5, 3, 0.5).moveEntity(entity);
            }
            return entity;
        }

        return null;
    }

    @Override
    public void postBossSpawn(LivingEntity boss, RaidContext context)
    {
        IPokemob pokemob = PokemobCaps.getPokemobFor(boss);
        DynamaxHelper.doDynamax(pokemob, pokemob.getEvolutionEntry(), RAID_DURATION, null);

        final LootTable loottable = pokemob.getEntity().getLevel().getServer().getLootTables().get(lootTable);
        final LootContext.Builder lootcontext$builder = new LootContext.Builder(
                (ServerLevel) pokemob.getEntity().getLevel()).withRandom(boss.getRandom());
        // Generate the loot list.
        final List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(loottable.getParamSet()));

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
    public void onBossCaptureAttempt(Pre event)
    {
        // Super call checks the health, then sets allow if valid
        IBossProvider.super.onBossCaptureAttempt(event);
        if (event.getResult() == Result.ALLOW)
        {
            final ResourceLocation id = PokecubeItems.getCubeId(event.getFilledCube());
            final boolean dynamaxCube = id.toString().equals("pokecube:dynacube");

            if (!dynamaxCube)
            {
                final Entity catcher = event.pokecube.shootingEntity;
                if (catcher instanceof Player player)
                    thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokecube.denied"));
                event.setCanceled(true);
                event.setResult(Result.DENY);
                CaptureManager.onCaptureDenied(event.pokecube);
            }
        }
    }

    @Override
    public void postBossCapture(Post event, LivingEntity fromCube)
    {
        IBossProvider.super.postBossCapture(event, fromCube);

        final ResourceLocation id = PokecubeItems.getCubeId(event.getFilledCube());

        // Catch Raids
        if (id.toString().equals("pokecube:dynacube"))
        {
            final IPokemob pokemob = event.getCaught();
            pokemob.setPokecube(PokecubeItems.getStack("pokecube"));

            pokemob.getEntity().getPersistentData().putBoolean("pokecube:dyna_reverted", true);
            DynamaxHelper.removeDynamax(pokemob);

            // Pokemob Level Spawm
            int level = pokemob.getLevel();

            if (level <= 10 || level >= 40)
            {
                level = 20;
                pokemob.setForSpawn(level, false);
            }

            event.setFilledCube(PokecubeManager.pokemobToItem(pokemob), true);
        }
    }

    @Override
    public void onBossFaint(FaintEvent event)
    {

        if (event.pokemob.getEntity().getLastHurtByMob() instanceof ServerPlayer player)
            thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("pokecube.raid.capture.dynamax", event.pokemob.getDisplayName()));
    }

    @Override
    public String getKey()
    {
        return "dynamax";
    }

}
