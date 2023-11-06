package pokecube.api.moves;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        if (!(mob.getLevel() instanceof ServerLevel level)) return null;
        final BattleManager manager = BattleManager.managers.get(level.dimension());
        var b = manager.getFor(mob);
        // Prevents trying to add things to an ended battle.
        if (b != null && b.ended) b = null;
        return b;
    }

    public static boolean createOrAddToBattle(final LivingEntity mobA, final LivingEntity mobB)
    {
        if (mobB == null || !AITools.validCombatTargets.test(mobB)) return false;
        if (mobA == null || !(mobA.getLevel() instanceof ServerLevel level)) return false;

        final Battle existingA = Battle.getBattle(mobA);
        final Battle existingB = Battle.getBattle(mobB);

        var event = new JoinBattleEvent(mobA, mobB, existingA, existingB);
        ThutCore.FORGE_BUS.post(event);
        if (event.isCanceled()) return false;

        IPokemob pokemob = PokemobCaps.getPokemobFor(mobA);
        if (pokemob != null) pokemob.setCombatState(CombatStates.BATTLING, true);
        pokemob = PokemobCaps.getPokemobFor(mobB);
        if (pokemob != null) pokemob.setCombatState(CombatStates.BATTLING, true);

        if (existingA != null && existingB != null)
        {
            // This only occurs if the mob had de-agroed quickly before
            // re-agroing, so we will tell the battle to re-add the one to it.
            if (existingA == existingB)
            {
                // Already in battle, no need to proceed.
                return true;
            }
            existingA.mergeFrom(mobA, mobB, existingB, level);
            return false;
        }
        if (existingA != null) existingA.addToBattle(mobA, mobB);
        else if (existingB != null) existingB.addToBattle(mobA, mobB);
        else
        {
            final BattleManager manager = BattleManager.managers.get(level.dimension());
            final Battle battle = new Battle(level, manager);
            battle.addToBattle(mobA, mobB);
            Vector3 centre = new Vector3(mobA).addTo(mobB.getX(), mobB.getY(), mobB.getZ()).scalarMultBy(0.5);
            battle.setCentre(centre);
            manager.addBattle(battle);
            battle.start();
        }
        return true;
    }

    private static final Comparator<LivingEntity> BATTLESORTER = (o1, o2) -> Integer.compare(o1.id, o2.id);

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
        if (side1.containsKey(mob.getUUID())) return s1;
        if (side2.containsKey(mob.getUUID())) return s2;
        return Lists.newArrayList();
    }

    public List<LivingEntity> getEnemies(LivingEntity mob)
    {
        if (side1.containsKey(mob.getUUID())) return s2;
        if (side2.containsKey(mob.getUUID())) return s1;
        return Lists.newArrayList();
    }

    private void addToSide(final Map<UUID, LivingEntity> side, final Set<String> teams, final LivingEntity mob,
            final String team, final LivingEntity target)
    {
        side.put(mob.getUUID(), mob);
        teams.add(team);

        List<LivingEntity> s = side == side1 ? s1 : s2;
        s.add(mob);

        final ServerLevel world = (ServerLevel) mob.getLevel();
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

    private void mergeFrom(final LivingEntity mobA, final LivingEntity mobB, final Battle other,
            final ServerLevel world)
    {
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
        s1.removeIf(v -> v.isRemoved());
        s2.removeIf(v -> v.isRemoved());

        Set<LivingEntity> mask = Sets.newHashSet();
        // Remove duplicates
        s1.removeIf(v -> !mask.add(v));
        s2.removeIf(v -> !mask.add(v));

        s1.sort(BATTLESORTER);
        s2.sort(BATTLESORTER);
    }

    public void addToBattle(final LivingEntity mobA, final LivingEntity mobB)
    {
        final String teamA = TeamManager.getTeam(mobA);
        final String teamB = TeamManager.getTeam(mobB);

        if (PokecubeCore.getConfig().debug_moves) PokecubeAPI.logInfo("Adding {}({}) and {}({}) to a battle!",
                mobA.getName().getString(), mobA.getId(), mobB.getName().getString(), mobB.getId());

        boolean aIs1 = this.side1.containsKey(mobA.getUUID());
        boolean aIs2 = this.side2.containsKey(mobA.getUUID());

        final boolean bIs1 = this.side1.containsKey(mobB.getUUID());
        final boolean bIs2 = this.side2.containsKey(mobB.getUUID());

        // Already in the battle, so skip.
        if ((aIs1 || aIs2) && (bIs1 || bIs2)) return;

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

    private boolean checkStale(final Map<UUID, LivingEntity> side, List<LivingEntity> set, List<LivingEntity> stale)
    {
        final int tooLong = Battle.BATTLE_END_TIMER;
        boolean changed = false;
        for (final LivingEntity mob1 : side.values())
        {
            if (!mob1.isAlive())
            {
                set.remove(mob1);
                final int tick = this.aliveTracker.getInt(mob1) + 1;
                this.aliveTracker.put(mob1, tick);
                final UUID id = mob1.getUUID();
                final Entity mob = this.world.getEntity(id);
                if (mob != null && mob != mob1)
                {
                    this.aliveTracker.removeInt(mob1);
                    if (mob instanceof LivingEntity living)
                    {
                        side.put(id, living);
                        if (!set.contains(living)) set.add(living);
                        // We changed if we had to adjust the sets.
                        changed = true;
                    }
                    continue;
                }
                if (tick > tooLong) stale.add(mob1);
                continue;
            }
            else
            {

                LivingEntity target = BrainUtils.getAttackTarget(mob1);
                // No more target means we remove it from the battle.
                if (target == null)
                {
                    // Null target could also occur if the LivingEntity is not a
                    // Mob, such as for a player. So in that case, we need to
                    // check members of the other team, and see if any of them
                    // are still trying to attack it.
                    boolean valid = mob1 instanceof Mob;
                    if (!valid)
                    {
                        valid = true;
                        List<LivingEntity> otherSide = set == s1 ? s2 : s1;
                        for (LivingEntity e : otherSide)
                        {
                            if (BrainUtils.getAttackTarget(e) == mob1)
                            {
                                valid = false;
                                break;
                            }
                        }

                    }
                    if (valid) stale.add(mob1);
                }
                else if (!set.contains(mob1))
                {
                    set.add(mob1);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private void tick()
    {
        if (this.ended) return;
        this.valid = true;
        final List<LivingEntity> stale = Lists.newArrayList();
        boolean changed = false;

        // check if we have any stale mobs, this checks if they have revived
        // somehow using a timer. The function calls are before || so that both
        // sets get checked, and not optimised out.
        changed = checkStale(side1, s1, stale) || changed;
        changed = checkStale(side2, s2, stale) || changed;

        // Remove anything that is stale from the battle.
        stale.forEach(mob -> {
            this.removeFromBattle(mob);
        });

        // We have changed if stale is not empty
        changed = changed || !stale.isEmpty();
        // If one side is empty, end the battle
        if (this.side1.isEmpty() || this.side2.isEmpty()) this.end();
        // Otherwise If we did change, sort the sides
        else if (changed) this.sortSides();
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
            final IPokemob poke = PokemobCaps.getPokemobFor(mob1);
            if (!(mob1 instanceof Mob mob)) continue;
            BrainUtils.initiateCombat(mob, main2);
            if (poke != null && poke.getAbility() != null) poke.getAbility().startCombat(poke);
        }
        mobs = Lists.newArrayList(this.side2.values());
        for (final LivingEntity mob2 : mobs)
        {
            final IPokemob poke = PokemobCaps.getPokemobFor(mob2);
            // This was already handled
            if (mob2 == main2) continue;
            if (!(mob2 instanceof Mob mob)) continue;
            BrainUtils.initiateCombat(mob, main1);
            if (poke != null && poke.getAbility() != null) poke.getAbility().startCombat(poke);
        }
    }

    private void end()
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
