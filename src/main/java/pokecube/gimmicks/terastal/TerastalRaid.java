package pokecube.gimmicks.terastal;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.SharedAttributes;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.CaptureEvent.Post;
import pokecube.api.raids.IBossProvider;
import pokecube.api.raids.RaidManager.RaidContext;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.TaskBase.InventoryChange;
import pokecube.core.database.Database;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.gimmicks.terastal.TeraTypeGene.TeraType;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class TerastalRaid implements IBossProvider
{
    public static int RAID_DURATION = 600;

    public static ResourceLocation lootTable = new ResourceLocation("pokecube_legends", "raids/raid_drop");

    private static final UUID TERAMOD = new UUID(343521462346243l, 23453246266457l);

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
            var genes = TerastalMechanic.getTeraGenes(entity);
            genes.setAllele(0, new TeraTypeGene().mutate());
            genes.setAllele(1, new TeraTypeGene().mutate());
            genes.refreshExpressed();

            // Pokemob Level Spawm
            final int level = 10 + ThutCore.newRandom().nextInt(50);

            if (newMob) pokemob.setForSpawn(Tools.levelToXp(entry.getEvolutionMode(), level), false);

            long time = Tracker.instance().getTick();
            int raidScale = level / 10;
            entity.getPersistentData().putLong("pokecube:tera_raid_start", time);
            entity.getPersistentData().putInt("pokecube:tera_raid_scale", raidScale);
            entity.getPersistentData().putInt("pokecube:raid_duration", RAID_DURATION * raidScale);

            // Add max health scale
            float scale = 5f * raidScale;
            var hpBoost = new AttributeModifier(TERAMOD, "pokecube:tera_raid", scale, Operation.MULTIPLY_TOTAL);
            var hpAttr = entity.getAttribute(Attributes.MAX_HEALTH);
            hpAttr.removeModifier(TERAMOD);
            float health = entity.getMaxHealth();
            hpAttr.addPermanentModifier(hpBoost);
            float toAdd = entity.getMaxHealth() - health;
            entity.heal(toAdd);

            // Scale mob larger if tiny, so easier to see/fight
            if (pokemob.getMobSizes().magSq() < 12
                    && entity.getAttributes().hasAttribute(SharedAttributes.MOB_SIZE_SCALE.get()))
            {
                var scaleAttr = entity.getAttribute(SharedAttributes.MOB_SIZE_SCALE.get());
                var sizeBoost = new AttributeModifier(TERAMOD, "pokecube:tera_raid",
                        Math.sqrt(12 / pokemob.getMobSizes().magSq()), Operation.MULTIPLY_BASE);
                scaleAttr.removeModifier(TERAMOD);
                scaleAttr.addPermanentModifier(sizeBoost);
            }
            if (newMob)
            {
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
        TerastalMechanic.doTera(pokemob);

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
        boss.setHealth(boss.getMaxHealth());
        context.level().playLocalSound(boss.getX(), boss.getY(), boss.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE,
                SoundSource.NEUTRAL, 1, 1, false);
    }

    @Override
    public void postBossCapture(Post event, LivingEntity fromCube)
    {
        var hpAttr = fromCube.getAttribute(Attributes.MAX_HEALTH);
        hpAttr.removeModifier(TERAMOD);
        if (fromCube.getAttributes().hasAttribute(SharedAttributes.MOB_SIZE_SCALE.get()))
        {
            var scaleAttr = fromCube.getAttribute(SharedAttributes.MOB_SIZE_SCALE.get());
            scaleAttr.removeModifier(TERAMOD);
        }
        TeraType type = TerastalMechanic.getTera(fromCube);
        type.isTera = false;
        PokecubeManager.addToCube(event.getFilledCube(), fromCube);
        event.setFilledCube(event.getFilledCube(), true);
    }

    @Override
    public String getKey()
    {
        return "terastal";
    }

}
