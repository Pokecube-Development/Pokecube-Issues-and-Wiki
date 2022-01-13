package pokecube.core.moves;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.AITools;
import thut.api.world.IWorldTickListener;
import thut.api.world.WorldTickManager;

public class Battle
{
    public static int BATTLE_END_TIMER = 600;

    public static void register()
    {
        WorldTickManager.registerStaticData(BattleManager::new, p -> true);
    }

    public static class BattleManager implements IWorldTickListener
    {
        private static final Map<ResourceKey<Level>, BattleManager> managers = Maps.newHashMap();

        Map<UUID, Battle> battlesById = Maps.newHashMap();

        Set<Battle> battles = Sets.newHashSet();

        public void addBattle(final Battle battle)
        {
            this.battles.add(battle);
            for (final UUID id : battle.side1.keySet())
                this.battlesById.put(id, battle);
            for (final UUID id : battle.side2.keySet())
                this.battlesById.put(id, battle);
        }

        @Nullable
        public Battle getFor(final LivingEntity mob)
        {
            return this.battlesById.get(mob.getUUID());
        }

        @Override
        public void onAttach(final ServerLevel world)
        {
            BattleManager.managers.put(world.dimension(), this);
        }

        @Override
        public void onDetach(final ServerLevel world)
        {
            this.battlesById.clear();
            this.battles.clear();
            BattleManager.managers.remove(world.dimension());
        }

        @Override
        public void onTickStart(final ServerLevel world)
        {
            for (final Battle battle : this.battles)
                battle.tick();
            this.battles.removeIf(b ->
            {
                final boolean ended = b.ended;
                if (ended)
                {
                    b.side1.keySet().forEach(u -> this.battlesById.remove(u));
                    b.side2.keySet().forEach(u -> this.battlesById.remove(u));
                }
                return ended;
            });
        }
    }

    public static Battle getBattle(final LivingEntity mob)
    {
        if (!(mob.getLevel() instanceof ServerLevel))
        {
            PokecubeCore.LOGGER.error("Error checking for a battle on wrong side!");
            PokecubeCore.LOGGER.error(new IllegalAccessError());
            return null;
        }
        final ServerLevel world = (ServerLevel) mob.getLevel();
        final BattleManager manager = BattleManager.managers.get(world.dimension());
        return manager.getFor(mob);
    }

    public static boolean createOrAddToBattle(final LivingEntity mobA, final LivingEntity mobB)
    {
        if (mobB == null || !AITools.validTargets.test(mobB)) return false;
        if (mobA == null || !(mobA.getLevel() instanceof ServerLevel)) return false;

        final Battle existingA = Battle.getBattle(mobA);
        final Battle existingB = Battle.getBattle(mobB);

        final ServerLevel world = (ServerLevel) mobA.getLevel();

        if (existingA != null && existingB != null)
        {
            // This only occurs if the mob had de-agroed quickly before
            // re-agroing, so we will tell the battle to re-add the one to it.
            if (existingA == existingB)
            {
                // Need to ensure the mobs are still agressed.
                existingA.addToBattle(mobA, mobB);
                return true;
            }
            existingA.mergeFrom(mobA, mobB, existingB, world);
            return false;
        }
        if (existingA != null) existingA.addToBattle(mobA, mobB);
        else if (existingB != null) existingB.addToBattle(mobA, mobB);
        else
        {
            final BattleManager manager = BattleManager.managers.get(world.dimension());
            final Battle battle = new Battle(world, manager);
            battle.addToBattle(mobA, mobB);
            manager.addBattle(battle);
            battle.start();
        }
        return true;
    }

    private final Map<UUID, LivingEntity> side1 = Maps.newHashMap();
    private final Map<UUID, LivingEntity> side2 = Maps.newHashMap();

    private final Set<String> teams1 = Sets.newHashSet();
    private final Set<String> teams2 = Sets.newHashSet();

    private final UUID battleID = UUID.randomUUID();

    private final ServerLevel   world;
    private final BattleManager manager;

    private final Object2IntArrayMap<LivingEntity> aliveTracker = new Object2IntArrayMap<>();

    boolean valid = false;
    boolean ended = false;

    public Battle(final ServerLevel world, final BattleManager manager)
    {
        this.aliveTracker.defaultReturnValue(0);
        this.manager = manager;
        this.world = world;
    }

    private void addToSide(final Map<UUID, LivingEntity> side, final Set<String> teams, final LivingEntity mob,
            final String team, final LivingEntity target)
    {
        side.put(mob.getUUID(), mob);
        teams.add(team);

        final ServerLevel world = (ServerLevel) mob.getLevel();
        final BattleManager manager = BattleManager.managers.get(world.dimension());
        manager.battlesById.put(mob.getUUID(), this);

        // This means we have already been started, and are actually adding to
        // an existing battle!
        if (this.valid)
        {
            final IPokemob poke = CapabilityPokemob.getPokemobFor(mob);
            if (!(mob instanceof Mob)) return;
            BrainUtils.initiateCombat((Mob) mob, target);
            if (poke != null && poke.getAbility() != null) poke.getAbility().startCombat(poke);
        }
    }

    private void mergeFrom(final LivingEntity mobA, final LivingEntity mobB, final Battle other,
            final ServerLevel world)
    {
        final boolean mobAisSide1 = this.side1.containsKey(mobA.getUUID());
        final boolean mobBisSide1 = other.side1.containsKey(mobB.getUUID());

        final Map<UUID, LivingEntity> sideAUs = mobAisSide1 ? this.side1 : this.side2;
        final Map<UUID, LivingEntity> sideBThem = mobBisSide1 ? other.side1 : other.side2;

        final Map<UUID, LivingEntity> sideBUs = mobAisSide1 ? this.side2 : this.side1;
        final Map<UUID, LivingEntity> sideAThem = mobBisSide1 ? other.side2 : other.side1;

        sideBThem.forEach((id, mob) ->
        {
            sideBUs.put(id, mob);
            this.manager.battlesById.put(id, this);
        });
        sideAThem.forEach((id, mob) ->
        {
            sideAUs.put(id, mob);
            this.manager.battlesById.put(id, this);
        });

        other.side1.clear();
        other.side2.clear();
    }

    public void addToBattle(final LivingEntity mobA, final LivingEntity mobB)
    {
        final String teamA = TeamManager.getTeam(mobA);
        final String teamB = TeamManager.getTeam(mobB);

        boolean aIs1 = this.side1.containsKey(mobA.getUUID());
        boolean aIs2 = this.side2.containsKey(mobA.getUUID());

        final boolean bIs1 = this.side1.containsKey(mobB.getUUID());
        final boolean bIs2 = this.side2.containsKey(mobB.getUUID());

        if (aIs1 || bIs2)
        {
            if (aIs1) this.addToSide(this.side2, this.teams2, mobB, teamB, mobA);
            if (bIs2) this.addToSide(this.side1, this.teams1, mobA, teamA, mobB);
        }
        else if (aIs2 || bIs1)
        {
            if (aIs2) this.addToSide(this.side1, this.teams1, mobB, teamB, mobA);
            if (bIs1) this.addToSide(this.side2, this.teams2, mobA, teamA, mobB);
        }
        else
        {
            aIs1 = this.teams1.contains(teamA);
            aIs2 = this.teams2.contains(teamA);
            if (aIs1)
            {
                this.addToSide(this.side1, this.teams1, mobA, teamA, mobB);
                this.addToSide(this.side2, this.teams2, mobB, teamB, mobA);
            }
            else
            {
                this.addToSide(this.side1, this.teams1, mobB, teamB, mobA);
                this.addToSide(this.side2, this.teams2, mobA, teamA, mobB);
            }
        }
    }

    public void removeFromBattle(final LivingEntity mob)
    {
        final UUID id = mob.getUUID();
        this.aliveTracker.removeInt(mob);
        this.manager.battlesById.remove(mob.getUUID());
        if (this.side1.containsKey(id))
        {
            this.side1.remove(id);
            final IPokemob poke = CapabilityPokemob.getPokemobFor(mob);
            if (poke != null && poke.getAbility() != null) poke.getAbility().endCombat(poke);
        }
        if (this.side2.containsKey(id))
        {
            this.side2.remove(id);
            final IPokemob poke = CapabilityPokemob.getPokemobFor(mob);
            if (poke != null && poke.getAbility() != null) poke.getAbility().endCombat(poke);
        }
        if (this.side1.isEmpty() || this.side2.isEmpty()) this.end();
    }

    public void tick()
    {
        if (this.ended) return;
        this.valid = true;
        final Set<LivingEntity> stale = Sets.newHashSet();
        final int tooLong = Battle.BATTLE_END_TIMER;
        for (final LivingEntity mob1 : this.side1.values())
            if (!mob1.isAlive())
            {
                final int tick = this.aliveTracker.getInt(mob1) + 1;
                this.aliveTracker.put(mob1, tick);
                final UUID id = mob1.getUUID();
                final Entity mob = this.world.getEntity(id);
                if (mob != null && mob != mob1)
                {
                    this.aliveTracker.removeInt(mob1);
                    if (mob instanceof LivingEntity) this.side1.put(id, (LivingEntity) mob);
                    continue;
                }
                if (tick > tooLong) stale.add(mob1);
                continue;
            }
        for (final LivingEntity mob2 : this.side2.values())
            if (!mob2.isAlive())
            {
                final int tick = this.aliveTracker.getInt(mob2) + 1;
                final UUID id = mob2.getUUID();
                final Entity mob = this.world.getEntity(id);
                if (mob != null && mob != mob2)
                {
                    this.aliveTracker.removeInt(mob2);
                    if (mob instanceof LivingEntity) this.side2.put(id, (LivingEntity) mob);
                    continue;
                }
                this.aliveTracker.put(mob2, tick);
                if (tick > tooLong) stale.add(mob2);
                continue;
            }
        stale.forEach(mob ->
        {
            this.removeFromBattle(mob);
        });
        if (this.side1.isEmpty() || this.side2.isEmpty()) this.end();
    }

    public void start()
    {
        if (this.side1.isEmpty() || this.side2.isEmpty()) return;
        this.valid = true;

        final LivingEntity main1 = this.side1.values().iterator().next();
        final LivingEntity main2 = this.side2.values().iterator().next();

        for (final LivingEntity mob1 : this.side1.values())
        {
            final IPokemob poke = CapabilityPokemob.getPokemobFor(mob1);
            if (!(mob1 instanceof Mob)) continue;
            BrainUtils.initiateCombat((Mob) mob1, main2);
            if (poke != null && poke.getAbility() != null) poke.getAbility().startCombat(poke);
        }
        for (final LivingEntity mob2 : this.side2.values())
        {
            final IPokemob poke = CapabilityPokemob.getPokemobFor(mob2);
            // This was already handled
            if (mob2 == main2) continue;
            if (!(mob2 instanceof Mob)) continue;
            BrainUtils.initiateCombat((Mob) mob2, main1);
            if (poke != null && poke.getAbility() != null) poke.getAbility().startCombat(poke);
        }
    }

    public void end()
    {
        this.ended = true;
        for (final LivingEntity mob1 : this.side1.values())
        {
            final IPokemob poke = CapabilityPokemob.getPokemobFor(mob1);
            if (poke != null && poke.getAbility() != null) poke.getAbility().endCombat(poke);
            BrainUtils.deagro(mob1);
        }
        for (final LivingEntity mob2 : this.side2.values())
        {
            final IPokemob poke = CapabilityPokemob.getPokemobFor(mob2);
            if (poke != null && poke.getAbility() != null) poke.getAbility().endCombat(poke);
            BrainUtils.deagro(mob2);
        }
    }

    @Override
    public int hashCode()
    {
        return this.battleID.hashCode();
    }
}
