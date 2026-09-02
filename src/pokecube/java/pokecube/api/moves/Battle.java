package pokecube.api.moves;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.TeamManager;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.events.combat.ExitBattleEvent;
import pokecube.api.events.combat.JoinBattleEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.utils.AITools;
import thut.api.entity.EntityProvider;
import thut.api.maths.Vector3;
import thut.api.world.IWorldTickListener;
import thut.api.world.WorldTickManager;
import thut.core.common.ThutCore;

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
            for (final UUID id : battle.side1.keySet()) this.battlesById.put(id, battle);
            for (final UUID id : battle.side2.keySet()) this.battlesById.put(id, battle);
        }

        @Nullable
        public Battle getFor(LivingEntity mob)
        {
            mob = EntityProvider.getTracked(mob);
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
            for (final Battle battle : this.battles) battle.tick();
            this.battles.removeIf(b -> {
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
        if (mob == null) return null;
        if (!(mob.level() instanceof ServerLevel level)) return null;
        final BattleManager manager = BattleManager.managers.get(level.dimension());
        var b = manager.getFor(mob);
        // Prevents trying to add things to an ended battle.
        if (b != null && b.ended) b = null;
        return b;
    }

    public static boolean createOrAddToBattle(LivingEntity agressor, LivingEntity target)
    {
        if (target == null || !AITools.validCombatTargets.test(target)) return false;
        if (agressor == null || !(agressor.level() instanceof ServerLevel level)) return false;
        if(TeamManager.sameTeam(agressor, target)) return false;

        final Battle existingA = Battle.getBattle(agressor);
        final Battle existingB = Battle.getBattle(target);

        var event = new JoinBattleEvent(agressor, target, existingA, existingB);
        ThutCore.FORGE_BUS.post(event);
        if (event.isCanceled()) return false;
        target = event.getNewTarget();

        if (target == null || !AITools.validCombatTargets.test(target)) return false;
        if(TeamManager.sameTeam(agressor, target)) return false;

        var pokeA = PokemobCaps.getPokemobFor(agressor);
        var pokeB = PokemobCaps.getPokemobFor(target);

        if (pokeA != null) pokeA.setCombatState(CombatStates.BATTLING, true);
        if (pokeB != null) pokeB.setCombatState(CombatStates.BATTLING, true);

        if (existingA != null && existingB != null)
        {
            // This only occurs if the mob had de-agroed quickly before
            // re-agroing, so we will tell the battle to re-add the one to it.
            if (existingA == existingB)
            {
                // Already in battle, no need to proceed.
                return true;
            }
            existingA.mergeFrom(agressor, target, existingB);
            return false;
        }
        if (existingA != null) existingA.addToBattle(agressor, target);
        else if (existingB != null) existingB.addToBattle(agressor, target);
        else
        {
            final BattleManager manager = BattleManager.managers.get(level.dimension());
            final Battle battle = new Battle(level, manager);
            battle.addToBattle(agressor, target);
            Vector3 centre = new Vector3(agressor).addTo(target.getX(), target.getY(), target.getZ()).scalarMultBy(0.5);
            battle.setCentre(centre);
            manager.addBattle(battle);
            battle.start();
        }
        return true;
    }

    private static final Comparator<LivingEntity> BATTLESORTER = Comparator.comparingInt(Entity::getId);

    public static record ValidBattler(Battle battle, LivingEntity mob,
            List<LivingEntity> mobSide, List<LivingEntity> otherSide,
            Map<UUID, LivingEntity> mobSideMap, Map<UUID, LivingEntity> otherSideMap,
            AtomicBoolean changed, AtomicBoolean invalid){}

    public static final List<Consumer<ValidBattler>> BATTLE_TESTS = new ArrayList<>();

    static
    {
        // Add a check for too far from battle
        BATTLE_TESTS.add(testSet -> {
            var mob1 = testSet.mob;
            if (testSet.battle.getCentre().distToEntity(mob1) > PokecubeCore.getConfig().chaseDistance)
            {
                testSet.battle.markAsValid(mob1, -10);
                testSet.invalid.set(true);
            }
        });
        // Add a check to mark mobs who have a valid target
        BATTLE_TESTS.add(testSet -> {
            var mob1 = testSet.mob;
            var mob1_e = EntityProvider.getTracked(mob1);
            var mob1_t = BrainUtils.getAttackTarget(mob1);
            var mob1_t_e = EntityProvider.getTracked(mob1_t);
            if (mob1_t_e != null && testSet.otherSideMap.containsKey(mob1_t_e.getUUID()))
            {
                testSet.battle.markAsValid(mob1);
                testSet.battle.markAsValid(testSet.otherSideMap.get(mob1_t_e.getUUID()));
                return;
            }
            else if (mob1_t != null && testSet.otherSideMap.containsKey(mob1_t.getUUID()))
            {
                testSet.battle.markAsValid(mob1);
                testSet.battle.markAsValid(testSet.otherSideMap.get(mob1_t.getUUID()));
                return;
            }
            if (mob1_e == mob1) return;
            mob1_t = BrainUtils.getAttackTarget(mob1_e);
            mob1_t_e = EntityProvider.getTracked(mob1_e);
            if (mob1_t_e != null && testSet.otherSideMap.containsKey(mob1_t_e.getUUID()))
            {
                testSet.battle.markAsValid(mob1);
                testSet.battle.markAsValid(testSet.otherSideMap.get(mob1_t_e.getUUID()));
            }
            else if (mob1_t != null && testSet.otherSideMap.containsKey(mob1_t.getUUID()))
            {
                testSet.battle.markAsValid(mob1);
                testSet.battle.markAsValid(testSet.otherSideMap.get(mob1_t.getUUID()));
            }
        });
        // Add one to re-map removed and re-added mobs
        BATTLE_TESTS.add(testSet -> {
            var mob1 = testSet.mob;
            var set = testSet.mobSide;
            var side = testSet.mobSideMap;
            var battle = testSet.battle;
            if (mob1.isRemoved())
            {
                // Discarded is set if it was recalled and re-sent out, or if it evolved, etc
                if (mob1.getRemovalReason() != Entity.RemovalReason.DISCARDED)
                {
                    testSet.battle.markAsValid(mob1, -10);
                    testSet.invalid.set(true);
                    return;
                }
                set.remove(mob1);
                final UUID id = mob1.getUUID();
                final Entity mob = battle.world.getEntity(id);
                if (mob != null && mob != mob1)
                {
                    if (mob instanceof LivingEntity living)
                    {
                        side.put(id, living);
                        if (!set.contains(living)) set.add(living);
                        // We changed if we had to adjust the sets.
                        testSet.battle.markAsValid(living);
                        testSet.changed.set(true);
                    }
                }
            }
        });
    }

    private final Map<UUID, LivingEntity> side1 = Maps.newHashMap();
    private final Map<UUID, LivingEntity> side2 = Maps.newHashMap();

    private final List<LivingEntity> s1 = Lists.newArrayList();
    private final List<LivingEntity> s2 = Lists.newArrayList();

    private final Set<String> teams1 = Sets.newHashSet();
    private final Set<String> teams2 = Sets.newHashSet();

    private final UUID battleID = UUID.randomUUID();

    private final ServerLevel world;
    private final BattleManager manager;

    private Vector3 centre = null;

    private final Object2IntArrayMap<LivingEntity> aliveTracker = new Object2IntArrayMap<>();

    boolean valid = false;
    boolean ended = false;

    private Battle(final ServerLevel world, final BattleManager manager)
    {
        this.aliveTracker.defaultReturnValue(0);
        this.manager = manager;
        this.world = world;
    }

    public List<LivingEntity> getAllies(LivingEntity mob)
    {
        mob = EntityProvider.getTracked(mob);
        if (side1.containsKey(mob.getUUID())) return s1;
        if (side2.containsKey(mob.getUUID())) return s2;
        return Lists.newArrayList();
    }

    public List<LivingEntity> getEnemies(LivingEntity mob)
    {
        mob = EntityProvider.getTracked(mob);
        if (side1.containsKey(mob.getUUID())) return s2;
        if (side2.containsKey(mob.getUUID())) return s1;
        return Lists.newArrayList();
    }

    /**
     * Adds the given entity onto our allied side of a battle, without triggering agression
     */
    public void addAlly(LivingEntity entity, LivingEntity toAdd)
    {
        entity = EntityProvider.getTracked(entity);
        toAdd = EntityProvider.getTracked(toAdd);

        var sideMap = side1.containsKey(entity.getUUID()) ? side1 : side2;
        var uuid = toAdd.getUUID();
        var otherSideMap = sideMap == side1 ? side2 : side1;
        if (otherSideMap.containsKey(uuid)) return;
        if (manager.battlesById.containsKey(uuid)) return;
        var sideList = sideMap == side1 ? s1 : s2;
        markAsValid(toAdd);
        sideList.add(toAdd);
        sideMap.put(uuid, toAdd);
        final BattleManager manager = BattleManager.managers.get(world.dimension());
        manager.battlesById.put(uuid, this);
    }
    /**
     * Adds the given entity onto the enemy side of a battle, without triggering agression
     */
    public void addEnemy(LivingEntity entity, LivingEntity toAdd)
    {
        entity = EntityProvider.getTracked(entity);
        toAdd = EntityProvider.getTracked(toAdd);

        var sideMap = side1.containsKey(entity.getUUID()) ? side2 : side1;
        var uuid = toAdd.getUUID();
        var otherSideMap = sideMap == side1 ? side2 : side1;
        if (otherSideMap.containsKey(uuid)) return;
        if (manager.battlesById.containsKey(uuid)) return;
        var sideList = sideMap == side1 ? s1 : s2;
        markAsValid(toAdd);
        sideList.add(toAdd);
        sideMap.put(toAdd.getUUID(), toAdd);
        final BattleManager manager = BattleManager.managers.get(world.dimension());
        manager.battlesById.put(toAdd.getUUID(), this);
    }

    private void addToSide(final Map<UUID, LivingEntity> side, final Set<String> teams, final LivingEntity mob,
            final String team, final LivingEntity target)
    {
        side.put(mob.getUUID(), mob);
        teams.add(team);

        List<LivingEntity> s = side == side1 ? s1 : s2;
        s.add(mob);
        markAsValid(mob);

        final ServerLevel world = (ServerLevel) mob.level();
        final BattleManager manager = BattleManager.managers.get(world.dimension());
        manager.battlesById.put(mob.getUUID(), this);

        // This means we have already been started, and are actually adding to
        // an existing battle!
        if (this.valid)
        {
            final IPokemob poke = PokemobCaps.getPokemobFor(mob);
            if (!(mob instanceof Mob mob2)) return;
            BrainUtils.initiateCombat(mob2, target);
            if (poke != null && poke.getAbility() != null) poke.getAbility().startCombat(poke);
        }
    }

    private void mergeFrom(LivingEntity mobA, LivingEntity mobB, final Battle other)
    {
        mobA = EntityProvider.getTracked(mobA);
        mobB = EntityProvider.getTracked(mobB);

        final boolean mobAisSide1 = this.side1.containsKey(mobA.getUUID());
        final boolean mobBisSide1 = other.side1.containsKey(mobB.getUUID());

        final Map<UUID, LivingEntity> sideAUs = mobAisSide1 ? this.side1 : this.side2;
        final Map<UUID, LivingEntity> sideBThem = mobBisSide1 ? other.side1 : other.side2;

        final Map<UUID, LivingEntity> sideBUs = mobAisSide1 ? this.side2 : this.side1;
        final Map<UUID, LivingEntity> sideAThem = mobBisSide1 ? other.side2 : other.side1;

        sideBThem.forEach((id, mob) -> {
            sideBUs.put(id, mob);
            List<LivingEntity> s = sideBUs == side1 ? s1 : s2;
            if (!s.contains(mob)) s.add(mob);
            this.manager.battlesById.put(id, this);
        });
        sideAThem.forEach((id, mob) -> {
            sideAUs.put(id, mob);
            List<LivingEntity> s = sideBUs == side1 ? s1 : s2;
            if (!s.contains(mob)) s.add(mob);
            this.manager.battlesById.put(id, this);
        });

        other.side1.clear();
        other.side2.clear();

        this.sortSides();
    }

    private void sortSides()
    {
        // Remove removed mobs.
        s1.removeIf(Entity::isRemoved);
        s2.removeIf(Entity::isRemoved);

        Set<UUID> mask = Sets.newHashSet();
        // Remove duplicates
        s1.removeIf(v -> !mask.add(v.getUUID()));
        s2.removeIf(v -> !mask.add(v.getUUID()));

        s1.sort(BATTLESORTER);
        s2.sort(BATTLESORTER);
    }

    public void addToBattle(LivingEntity mobA, LivingEntity mobB)
    {
        mobA = EntityProvider.getTracked(mobA);
        mobB = EntityProvider.getTracked(mobB);

        final String teamA = TeamManager.getTeam(mobA);
        final String teamB = TeamManager.getTeam(mobB);

        boolean aIs1 = this.side1.containsKey(mobA.getUUID());
        boolean aIs2 = this.side2.containsKey(mobA.getUUID());

        final boolean bIs1 = this.side1.containsKey(mobB.getUUID());
        final boolean bIs2 = this.side2.containsKey(mobB.getUUID());

        // Already in the battle, so skip.
        if ((aIs1 || aIs2) && (bIs1 || bIs2))
        {
            if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Not Adding {}({}) and {}({}) to a battle, already in one",
                    mobA.getName().getString(), mobA.getId(), mobB.getName().getString(), mobB.getId());
            return;
        }

        if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Adding {}({}) and {}({}) to a battle!",
                mobA.getName().getString(), mobA.getId(), mobB.getName().getString(), mobB.getId());

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

        if (mobA instanceof Mob mob && mob.getTarget() != mobB)
        {
            BrainUtils.initiateCombat(mob, mobB);
        }
        if (mobB instanceof Mob mob && mob.getTarget() != mobA)
        {
            BrainUtils.initiateCombat(mob, mobA);
        }

        this.sortSides();
    }

    public void markAsValid(LivingEntity mob)
    {
        markAsValid(mob, BATTLE_END_TIMER);
    }

    public void markAsValid(LivingEntity mob, int duration)
    {
        this.aliveTracker.put(mob, duration);
    }

    public void removeFromBattle(final LivingEntity mob)
    {
        if (PokecubeCore.getConfig().debug_moves)
            PokecubeAPI.logInfo("Removing {}({}) from the battle!", mob.getName().getString(), mob.getId());

        final UUID id = mob.getUUID();
        this.aliveTracker.removeInt(mob);
        this.manager.battlesById.remove(mob.getUUID());
        if (this.side1.containsKey(id))
        {
            this.side1.remove(id);
            this.s1.remove(mob);
        }
        if (this.side2.containsKey(id))
        {
            this.side2.remove(id);
            this.s2.remove(mob);
        }
        final IPokemob poke = PokemobCaps.getPokemobFor(mob);
        if (poke != null && poke.getAbility() != null) poke.getAbility().endCombat(poke);

        ThutCore.FORGE_BUS.post(new ExitBattleEvent(mob, this));
    }

    private boolean checkStale()
    {
        // set itself might get changed during the tests
        AtomicBoolean changed = new AtomicBoolean(false);
        var _set1 = new ArrayList<>(side1.values());
        var _set2 = new ArrayList<>(side2.values());
        for (var mob1 : _set1)
        {
            AtomicBoolean invalid = new AtomicBoolean(false);
            ValidBattler testSet = new ValidBattler(this, mob1, s1, s2, side1, side2, changed, invalid);
            for (var test : BATTLE_TESTS)
            {
                test.accept(testSet);
                if (invalid.get()) break;
            }
            if (!invalid.get() && !s1.contains(mob1) && !s2.contains(mob1))
            {
                s1.add(mob1);
                changed.set(true);
            }
        }
        for (var mob2 : _set2)
        {
            AtomicBoolean invalid = new AtomicBoolean(false);
            ValidBattler testSet = new ValidBattler(this, mob2, s2, s1, side2, side1, changed, invalid);
            for (var test : BATTLE_TESTS)
            {
                test.accept(testSet);
                if (invalid.get()) break;
            }
            if (!invalid.get() && !s1.contains(mob2) && !s2.contains(mob2))
            {
                s2.add(mob2);
                changed.set(true);
            }
        }
        return changed.get();
    }

    private void tick()
    {
        if (this.ended) return;
        this.valid = true;
        final List<LivingEntity> stale = Lists.newArrayList();
        boolean changed;

        int numBefore = this.side1.size() + this.side2.size();

        // check if we have any stale mobs, this checks if they have revived
        // somehow using a timer. The function calls are before || so that both
        // sets get checked, and not optimised out.
        changed = checkStale();

        s1.forEach(e -> {
            int tick = this.aliveTracker.getInt(e) - 1;
            if (tick < 0) stale.add(e);
            else this.aliveTracker.put(e, tick);
        });
        s2.forEach(e -> {
            int tick = this.aliveTracker.getInt(e) - 1;
            if (tick < 0) stale.add(e);
            else this.aliveTracker.put(e, tick);
        });

        // Remove anything that is stale from the battle.
        stale.forEach(this::removeFromBattle);

        // We have changed if stale is not empty
        changed = changed || !stale.isEmpty();
        // If one side is empty, end the battle
        if (this.side1.isEmpty() || this.side2.isEmpty()) this.end();
        // Otherwise If we did change, sort the sides
        else if (changed) this.sortSides();

        int numAfter = this.side1.size()+this.side2.size();

        if(Math.abs(numAfter-numBefore) > 3 && numAfter > 1){
            // Recalculate centre
            centre.set(0,0,0);
            for(var a: this.side1.values()) centre.addTo(a.getX(), a.getY(), a.getZ());
            for(var a: this.side2.values()) centre.addTo(a.getX(), a.getY(), a.getZ());
            centre.scalarMultBy(1.0/numAfter);
        }
    }

    private void start()
    {
        if (this.side1.isEmpty() || this.side2.isEmpty()) return;
        this.valid = true;

        final LivingEntity main1 = this.side1.values().iterator().next();
        final LivingEntity main2 = this.side2.values().iterator().next();

        // Copy these over in-case the act of initiating combat draws in other
        // opponents.
        List<LivingEntity> mobs = Lists.newArrayList(this.side1.values());

        for (final LivingEntity mob1 : mobs)
        {
            this.hadPlayer |= mob1 instanceof Player;
            final IPokemob poke = PokemobCaps.getPokemobFor(mob1);
            this.hadPlayer |= poke != null && poke.isPlayerOwned();
            if (!(mob1 instanceof Mob mob)) continue;
            BrainUtils.initiateCombat(mob, main2);
            if (poke != null && poke.getAbility() != null) poke.getAbility().startCombat(poke);
        }
        mobs = Lists.newArrayList(this.side2.values());
        for (final LivingEntity mob2 : mobs)
        {
            this.hadPlayer |= mob2 instanceof Player;
            final IPokemob poke = PokemobCaps.getPokemobFor(mob2);
            this.hadPlayer |= poke != null && poke.isPlayerOwned();
            // This was already handled
            if (mob2 == main2) continue;
            if (!(mob2 instanceof Mob mob)) continue;
            BrainUtils.initiateCombat(mob, main1);
            if (poke != null && poke.getAbility() != null) poke.getAbility().startCombat(poke);
        }
    }

    public void end()
    {
        this.ended = true;
        for (final LivingEntity mob1 : this.side1.values())
        {
            final IPokemob poke = PokemobCaps.getPokemobFor(mob1);
            if (poke != null && poke.getAbility() != null) poke.getAbility().endCombat(poke);
            BrainUtils.deagro(mob1);
        }
        for (final LivingEntity mob2 : this.side2.values())
        {
            final IPokemob poke = PokemobCaps.getPokemobFor(mob2);
            if (poke != null && poke.getAbility() != null) poke.getAbility().endCombat(poke);
            BrainUtils.deagro(mob2);
        }
    }

    private boolean hadPlayer = false;
    public boolean hadPlayer()
    {
        return this.hadPlayer;
    }

    @Override
    public int hashCode()
    {
        return this.battleID.hashCode();
    }

    public Vector3 getCentre()
    {
        return centre;
    }

    public void setCentre(Vector3 centre)
    {
        this.centre = centre;
    }
}
