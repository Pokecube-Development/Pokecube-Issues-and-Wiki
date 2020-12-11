package pokecube.core.moves;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.handlers.TeamManager;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.utils.AITools;

public class Battle
{
    @Mod.EventBusSubscriber
    public static class BattleManager
    {
        private static final Map<RegistryKey<World>, BattleManager> managers = Maps.newHashMap();

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
            return this.battlesById.get(mob.getUniqueID());
        }

        private void tick()
        {
            for (final Battle battle : this.battles)
                battle.tick();
            this.battles.removeIf(b -> b.ended);
        }

        @SubscribeEvent
        public static void worldLoad(final WorldEvent.Load event)
        {
            if (!(event.getWorld() instanceof ServerWorld)) return;
            final ServerWorld world = (ServerWorld) event.getWorld();
            BattleManager.managers.put(world.getDimensionKey(), new BattleManager());
        }

        @SubscribeEvent
        public static void onTick(final WorldTickEvent event)
        {
            if (!(event.world instanceof ServerWorld)) return;
            final BattleManager manager = BattleManager.managers.get(event.world.getDimensionKey());
            manager.tick();
        }
    }

    public static Battle getBattle(final LivingEntity mob)
    {
        if (!(mob.getEntityWorld() instanceof ServerWorld))
        {
            PokecubeCore.LOGGER.error("Error checking for a battle on wrong side!");
            PokecubeCore.LOGGER.error(new IllegalAccessError());
            return null;
        }
        final ServerWorld world = (ServerWorld) mob.getEntityWorld();
        final BattleManager manager = BattleManager.managers.get(world.getDimensionKey());
        return manager.getFor(mob);
    }

    public static boolean createOrAddToBattle(final LivingEntity mobA, final LivingEntity mobB)
    {
        if (mobB == null || !AITools.validTargets.test(mobB)) return false;
        if (mobA == null || !(mobA.getEntityWorld() instanceof ServerWorld)) return false;

        final Battle existingA = Battle.getBattle(mobA);
        final Battle existingB = Battle.getBattle(mobB);

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
            PokecubeCore.LOGGER.warn("Need to merge battles! what do?");
            return false;
        }
        if (existingA != null) existingA.addToBattle(mobA, mobB);
        else if (existingB != null) existingB.addToBattle(mobA, mobB);
        else
        {
            final Battle battle = new Battle();
            final ServerWorld world = (ServerWorld) mobA.getEntityWorld();
            final BattleManager manager = BattleManager.managers.get(world.getDimensionKey());
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

    boolean valid = false;
    boolean ended = false;

    private void addToSide(final Map<UUID, LivingEntity> side, final Set<String> teams, final LivingEntity mob,
            final String team, final LivingEntity target)
    {
        side.put(mob.getUniqueID(), mob);
        teams.add(team);

        final ServerWorld world = (ServerWorld) mob.getEntityWorld();
        final BattleManager manager = BattleManager.managers.get(world.getDimensionKey());
        manager.battlesById.put(mob.getUniqueID(), this);

        // This means we have already been started, and are actually adding to
        // an existing battle!
        if (this.valid)
        {
            final IPokemob poke = CapabilityPokemob.getPokemobFor(mob);
            if (!(mob instanceof MobEntity)) return;
            BrainUtils.initiateCombat((MobEntity) mob, target);
            if (poke != null && poke.getAbility() != null) poke.getAbility().startCombat(poke);
        }
    }

    public void addToBattle(final LivingEntity mobA, final LivingEntity mobB)
    {
        final String teamA = TeamManager.getTeam(mobA);
        final String teamB = TeamManager.getTeam(mobB);

        boolean aIs1 = this.side1.containsKey(mobA.getUniqueID());
        boolean aIs2 = this.side2.containsKey(mobA.getUniqueID());

        final boolean bIs1 = this.side1.containsKey(mobB.getUniqueID());
        final boolean bIs2 = this.side2.containsKey(mobB.getUniqueID());

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
        final UUID id = mob.getUniqueID();
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
        for (final LivingEntity mob1 : this.side1.values())
            if (!mob1.isAlive())
            {
                stale.add(mob1);
                continue;
            }
        for (final LivingEntity mob2 : this.side2.values())
            if (!mob2.isAlive())
            {
                stale.add(mob2);
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
            if (!(mob1 instanceof MobEntity)) continue;
            BrainUtils.initiateCombat((MobEntity) mob1, main2);
            if (poke != null && poke.getAbility() != null) poke.getAbility().startCombat(poke);
        }
        for (final LivingEntity mob2 : this.side2.values())
        {
            final IPokemob poke = CapabilityPokemob.getPokemobFor(mob2);
            // This was already handled
            if (mob2 == main2) continue;
            if (!(mob2 instanceof MobEntity)) continue;
            BrainUtils.initiateCombat((MobEntity) mob2, main1);
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
