package pokecube.mobs;

import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.database.pokedex.PokedexEntryLoader.SpawnRule;
import pokecube.core.database.spawns.SpawnBiomeMatcher;
import pokecube.core.database.spawns.SpawnCheck;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.HappinessType;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.PokeType;
import thut.api.entity.IBreedingMob;
import thut.api.maths.Vector3;

public class PokecubeHelper
{
    private static SpawnBiomeMatcher moonMatcher;

    static
    {
        final SpawnRule rule = new SpawnRule();
        rule.values.put("rate", "1");
        rule.values.put("types", "moon");
        PokecubeHelper.moonMatcher = SpawnBiomeMatcher.get(rule);
    }

    public double dive(final IPokemob mob)
    {
        double x = 1;
        final Entity entity = mob.getEntity();
        if (entity.getLevel().getBlockState(entity.blockPosition()).getBlock() == Blocks.WATER
                && mob.isType(PokeType.getType("water")))
            x = 3.5;
        return x;
    }

    public double dusk(final IPokemob mob)
    {
        double x = 1;
        final Entity entity = mob.getEntity();
        final int light = entity.getLevel().getMaxLocalRawBrightness(entity.blockPosition());
        if (light < 5) x = 3.5;
        return x;
    }

    public double fast(final IPokemob mob)
    {
        return mob.getPokedexEntry().getStatVIT() < 100 ? 1 : 4;
    }

    public int heavy(final IPokemob mob)
    {
        final double mass = mob.getWeight();
        if (mass < 100) return -20;
        if (mass < 200) return 0;
        if (mass < 300) return 20;
        if (mass < 450) return 30;
        return 40;
    }

    public double level(final IPokemob mob)
    {
        final Mob entity = mob.getEntity();
        final int level = mob.getLevel();
        int otherLevel = 0;
        final LivingEntity target = BrainUtils.getAttackTarget(entity);
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob == null || (otherLevel = targetMob.getLevel()) <= level) return 1;
        if (otherLevel <= 2 * level) return 2;
        if (otherLevel <= 4 * level) return 4;
        return 8;
    }

    public double love(final IPokemob mob)
    {
        final Mob entity = mob.getEntity();
        final LivingEntity target = BrainUtils.getAttackTarget(entity);
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob == null || !(target instanceof Animal) || !(mob instanceof IBreedingMob)) return 1;
        if (((IBreedingMob) mob).canMate((Animal) target)) return 8;
        return 1;
    }

    public double lure(final IPokemob mob)
    {
        final Mob entity = mob.getEntity();
        if (mob.getPokedexEntry().swims())
        {// grow in 1.12
            final AABB bb = new Vector3().set(entity).addTo(0, entity.getEyeHeight(), 0).getAABB()
                    .inflate(PokecubeCore.getConfig().fishHookBaitRange);
            final List<FishingHook> hooks = entity.getLevel().getEntitiesOfClass(FishingHook.class, bb);
            if (!hooks.isEmpty()) for (final FishingHook hook : hooks) if (hook.getHookedIn() == entity) return 5;
        }
        return 1;
    }

    public void luxury(final IPokemob mob)
    {
        // Randomly increase happiness for being outside of pokecube.
        if (Math.random() > 0.999 && mob.getGeneralState(GeneralStates.TAMED))
        {
            HappinessType.applyHappiness(mob, HappinessType.TIME);
            HappinessType.applyHappiness(mob, HappinessType.TIME);
        }
    }

    public double moon(final IPokemob mob)
    {
        if (mob.getPokedexEntry().canEvolve(1, PokecubeItems.getStack("moonstone"))) return 4;
        if (PokecubeHelper.moonMatcher.matches(
                new SpawnCheck(new Vector3().set(mob.getEntity()), mob.getEntity().getLevel())))
            return 4;
        return 1;
    }

    public double nest(final IPokemob mob)
    {
        double x = 1;
        if (mob.getLevel() < 20) x = 3;
        if (mob.getLevel() > 19 && mob.getLevel() < 30) x = 2;
        return x;
    }

    public double net(final IPokemob mob)
    {
        double x = 1;
        if (mob.getType1() == PokeType.getType("bug")) x = 2;
        if (mob.getType1() == PokeType.getType("water")) x = 2;
        if (mob.getType2() == PokeType.getType("bug")) x = 2;
        if (mob.getType2() == PokeType.getType("water")) x = 2;
        return x;
    }

    public double premier(final IPokemob mob)
    {
        double x = 0.25;
        if (!mob.getCombatState(CombatStates.ANGRY)) x = 1;
        return x;
    }

    public double quick(final IPokemob mob)
    {
        double x = 1;
        final Entity entity = mob.getEntity();
        final double alive = entity.tickCount;
        if (!mob.getCombatState(CombatStates.ANGRY) && alive < 601) x = 4;
        return x;
    }

    public double timer(final IPokemob mob)
    {
        double x = 1;
        final Entity entity = mob.getEntity();
        final double alive = entity.tickCount;
        if (alive > 1500 && alive < 3001) x = 2;
        if (alive > 3000 && alive < 4501) x = 3;
        if (alive > 4500) x = 4;
        return x;
    }
}
