package pokecube.adventures.capabilities.utils;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.tasks.BaseTask;
import pokecube.adventures.ai.tasks.Tasks;
import pokecube.adventures.ai.tasks.battle.CaptureMob;
import pokecube.adventures.ai.tasks.battle.agro.AgroTargets;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerBase;
import pokecube.adventures.utils.TrainerTracker;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.trainers.TypeTrainer;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.events.pokemobs.SpawnEvent.Variance;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.database.Database;
import pokecube.core.database.tags.Tags;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.ThutAPI;
import thut.api.maths.Vector3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class TypeTrainerHandler
{

    public static interface ITypeMapper
    {
        /**
         * Mapping of LivingEntity to a TypeTrainer. EntityTrainers set this on spawn, so it isn't needed for them.
         * <br>
         * <br>
         * if forSpawn, it means this is being initialized, otherwise it is during the check for whether this mob should
         * have trainers.
         */
        TypeTrainer getType(LivingEntity mob, boolean forSpawn);
    }

    public static interface AIAdder
    {
        List<Pair<Integer, Behavior<? super LivingEntity>>> process(Mob mob);
    }

    private static final List<ITypeMapper> mappers = Lists.newArrayList();
    private static final List<AIAdder> aiAdders = Lists.newArrayList();

    public static void registerTypeMapper(final ITypeMapper mapper)
    {
        TypeTrainerHandler.mappers.add(mapper);
    }

    public static void registerAIAdder(final AIAdder adder)
    {
        TypeTrainerHandler.aiAdders.add(adder);
    }

    public static void addAI(final Mob mob)
    {
        final List<Pair<Integer, Behavior<? super LivingEntity>>> tasks = Lists.newArrayList();
        for (final AIAdder adder : TypeTrainerHandler.aiAdders) tasks.addAll(adder.process(mob));
        Tasks.addBattleTasks(mob, tasks);
    }

    public static TypeTrainer get(final LivingEntity mob, final boolean forSpawn)
    {
        for (final ITypeMapper mapper : TypeTrainerHandler.mappers)
        {
            final TypeTrainer type = mapper.getType(mob, forSpawn);
            if (type != null) return type;
        }
        return null;
    }

    public static Predicate<LivingEntity> validPlayerTarget(Mob npc)
    {
        return e -> e instanceof Player;
    }

    public static Predicate<LivingEntity> validPokemobTarget(Mob npc)
    {
        return e -> PokemobCaps.getPokemobFor(e) != null;
    }

    public static Predicate<LivingEntity> validZombieTarget(Mob npc)
    {
        return e -> e instanceof Zombie && !(e instanceof ZombifiedPiglin);
    }

    public static BaseTask CAPTURE_MOBS = new CaptureMob(1);

    // Register default instance.
    static
    {
        TypeTrainerHandler.registerTypeMapper((mob, forSpawn) -> {
            if (!forSpawn)
            {
                if (mob instanceof NpcMob npc) return TypeTrainer.getTrainer(npc.getNpcType());
                if (Config.instance.shouldBeCustomTrainer(mob)) return TypeTrainer.merchant;
                return null;
            }

            if (mob instanceof TrainerBase npc)
            {
                var type = npc.getPokemobs().getType();
                if (type != null) return type;
                return TypeTrainer.merchant;
            }
            else if (mob instanceof NpcMob npc) return TypeTrainer.getTrainer(npc.getNpcType());
            else if (Config.instance.npcsAreTrainers && mob instanceof Villager villager)
            {
                final String type = villager.getVillagerData().getProfession().toString();
                return TypeTrainer.getTrainer(type, true);
            }
            return null;
        });

        TypeTrainerHandler.registerAIAdder((npc) -> {
            final Predicate<LivingEntity> noRunIfCrowded = e -> {
                // Leaders don't care if crowded.
                if (npc instanceof LeaderNpc) return true;
                final int dist = PokecubeAdv.config.trainer_crowding_radius;
                final int num = PokecubeAdv.config.trainer_crowding_number;
                if (TrainerTracker.countTrainers(e.level(), new Vector3().set(e), dist) > num)
                {
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logDebug("NPC {} not agroing due to crowds", npc);
                    return false;
                }
                return true;
            };
            final Predicate<LivingEntity> noRunWhileRest = e -> {
                if (npc instanceof LeaderNpc) return true;
                if (e instanceof Villager villager)
                {
                    if (villager.isSleeping())
                    {
                        if (PokecubeCore.getConfig().debug_ai)
                            PokecubeAPI.logDebug("NPC {} not agroing due to sleeping", npc);
                        return false;
                    }
                }
                return noRunIfCrowded.test(e);
            };
            final Predicate<LivingEntity> noRunWhileMeet = e -> {
                if (npc instanceof LeaderNpc) return true;
                if (e instanceof Villager villager)
                {
                    final Schedule s = villager.getBrain().getSchedule();
                    final Activity a = s.getActivityAt((int) (e.level.getDayTime() % 24000L));
                    if (a == Activity.MEET)
                    {
                        if (PokecubeCore.getConfig().debug_ai)
                            PokecubeAPI.logDebug("NPC {} not agroing due to meeting", npc);
                        return false;
                    }
                }
                return noRunIfCrowded.test(e);
            };
            final Predicate<LivingEntity> onlyIfHasMobs = e -> {
                final IHasPokemobs other = TrainerCaps.getHasPokemobs(e);
                if (other == null) return noRunIfCrowded.test(e);
                final boolean hasMob = !other.getNextPokemob().isEmpty();
                if (hasMob) return noRunIfCrowded.test(e);
                if (other.getOutID() == null)
                {
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logDebug("NPC {} not agroing due to no mobs on target", npc);
                    return false;
                }
                return noRunIfCrowded.test(e);
            };
            final Predicate<LivingEntity> notNearHealer = e -> {
                if (npc instanceof LeaderNpc) return true;
                if (!PokecubeAdv.config.no_battle_near_pokecenter) return true;
                final ServerLevel world = (ServerLevel) npc.level();
                final BlockPos blockpos = e.blockPosition();
                final PoiManager pois = world.getPoiManager();
                final long num = pois.getCountInRange(PointsOfInterest.HEALER, blockpos,
                        PokecubeAdv.config.pokecenter_radius, Occupancy.ANY);
                if (num > 0)
                {
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logDebug("NPC {} not agroing due to nearby pokecenter", npc);
                    return false;
                }
                return num == 0;
            };

            final List<Pair<Integer, Behavior<? super LivingEntity>>> list = Lists.newArrayList();
            Behavior<? super LivingEntity> task = new AgroTargets(1, 0, validZombieTarget(npc));
            list.add(Pair.of(1, task));

            // Only trainers specifically target players.
            if (npc instanceof TrainerBase)
            {
                final Predicate<LivingEntity> validPlayer = onlyIfHasMobs.and(validPlayerTarget(npc));
                task = new AgroTargets(1, 0, validPlayer.and(notNearHealer)).setRunCondition(noRunWhileRest);
                list.add(Pair.of(1, task));
            }

            // 5% chance of battling a random nearby pokemob if they see it.
            if (Config.instance.trainersBattlePokemobs)
            {
                task = new AgroTargets(0.005f, 1200, validPokemobTarget(npc)).setRunCondition(noRunWhileRest);
                list.add(Pair.of(1, task));
                list.add(Pair.of(1, CAPTURE_MOBS));
            }
            // 1% chance of battling another of same class if seen
            // Also this will stop the battle after 1200 ticks.
            if (Config.instance.trainersBattleEachOther)
            {
                final Predicate<LivingEntity> shouldRun = noRunWhileMeet.and(noRunWhileRest);
                task = new AgroTargets(0.0015f, 1200, z -> z.getClass() == npc.getClass()).setRunCondition(shouldRun);
                list.add(Pair.of(1, task));
            }
            return list;
        });
    }

    public static void getRandomTeam(final IHasPokemobs trainer, final LivingEntity owner, int level,
            final LevelAccessor world, final List<PokedexEntry> values)
    {
        for (int i = 0; i < 6; i++) trainer.setPokemob(i, ItemStack.EMPTY);
        if (level == 0) level = 5;
        final Variance variance = SpawnHandler.DEFAULT_VARIANCE;
        int number = 1 + ThutAPI.newRandom().nextInt(6);
        number = Math.min(number, trainer.getMaxPokemobCount());
        for (int i = 0; i < number; i++)
        {
            Collections.shuffle(values);
            ItemStack item = ItemStack.EMPTY;
            for (final PokedexEntry s : values)
            {
                if (s != null) item = TypeTrainerHandler.makeStack(s, owner, world, variance.apply(level));
                if (!item.isEmpty()) break;
            }
            trainer.setPokemob(i, item);
        }
    }

    public static void getRandomTeam(final IHasPokemobs trainer, final LivingEntity owner, int level,
            final LevelAccessor world)
    {
        final TypeTrainer type = trainer.getType();
        final List<PokedexEntry> values = Lists.newArrayList();
        if (type.pokemon != null) values.addAll(type.pokemon);
        else PokecubeAPI.LOGGER.warn("No mobs for {}", type);
        if (type.overrideLevel != -1) level = type.overrideLevel;
        if (PokecubeCore.getConfig().debug_spawning) PokecubeAPI.logInfo("Initializing team for " + owner);
        TypeTrainerHandler.getRandomTeam(trainer, owner, level, world, values);
    }

    public static TypeTrainer getTrainer(final String name, final boolean create)
    {
        final TypeTrainer ret = TypeTrainer.typeMap.get(name);
        if (ret == null)
        {
            for (final TypeTrainer t : TypeTrainer.typeMap.values())
                if (t != null && t.getName().equalsIgnoreCase(name)) return t;
            if (create && !name.isEmpty())
            {
                NpcType existing = NpcType.byType(name);
                if (existing.getName().equalsIgnoreCase(name)) return new TypeTrainer(existing);
                return new TypeTrainer(name);
            }
            return TypeTrainer.merchant;
        }
        return ret;
    }

    public static void initSpawns()
    {
        for (final TypeTrainer type : TypeTrainer.typeMap.values())
            for (final SpawnBiomeMatcher matcher : type.spawns.keySet())
            {
                matcher.reset();
                matcher.parse();
            }
    }

    public static ItemStack makeStack(final PokedexEntry entry, final LivingEntity trainer, final LevelAccessor world,
            final int level)
    {
        IPokemob pokemob = PokemobCaps.getPokemobFor(PokecubeCore.createPokemob(entry, trainer.level()));
        if (pokemob != null)
        {
            final double x = trainer.getX();
            final double y = trainer.getY();
            final double z = trainer.getZ();
            pokemob.getEntity().setPosRaw(x, y, z);
            pokemob.getEntity().setHealth(pokemob.getEntity().getMaxHealth());
            pokemob.getEntity().getPersistentData().putBoolean("__need_init_evos__", true);
            pokemob.setPokedexEntry(entry);
            pokemob.setOwner(trainer.getUUID());
            pokemob.setPokecube(new ItemStack(PokecubeItems.getFilledCube(PokecubeBehaviour.DEFAULTCUBE)));
            final int exp = Tools.levelToXp(pokemob.getExperienceMode(), level);
            pokemob.setForSpawn(exp, false);
            return PokecubeManager.pokemobToItem(pokemob);
        }
        return ItemStack.EMPTY;
    }

    public static void postInitTrainers()
    {
        for (final TypeTrainer t : TypeTrainer.typeMap.values())
        {
            t.pokemon.clear();
            if (t.pokelist != null && !t.pokelist.isEmpty())
            {
                var list = new ArrayList<String>();
                for (var _entry : t.pokelist)
                {
                    if (_entry.startsWith("#"))
                    {
                        _entry = _entry.substring(1);
                        if (!_entry.contains(":")) _entry = "pokecube:" + _entry;
                        var tag = Tags.POKEMOB.getValues(_entry);
                        tag.forEach(v -> list.add(v.name));
                    }
                    else list.add(_entry);
                }
                // this can be the case on LAN servers
                if (list.isEmpty()) continue;
                if (!list.getFirst().startsWith("-")) for (final String s : t.pokelist)
                {
                    final PokedexEntry e = Database.getEntry(s);
                    if (e != null && !t.pokemon.contains(e)) t.pokemon.add(e);
                    else if (e == null) PokecubeAPI.LOGGER.error("Error in reading of {}", s);
                }
                else
                {
                    final String[] types = list.getFirst().replace("-", "").split(":");
                    if (types[0].equalsIgnoreCase("all"))
                    {
                        for (final PokedexEntry s : Database.spawnables) if (!s.isLegendary()) t.pokemon.add(s);
                    }
                    else for (final String type2 : types)
                    {
                        final PokeType pokeType = PokeType.getType(type2);
                        if (pokeType != PokeType.unknown) for (final PokedexEntry s : Database.spawnables)
                            if (s.isType(pokeType) && !s.isLegendary()) t.pokemon.add(s);
                    }
                }
                // Remove large pokemobs from their list.
                t.pokemon.removeIf(e -> (e.getLength() > 8 || e.getHeight() > 8 || e.getWidth() > 8));
            }
        }
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Loaded Trainer Types: " + TypeTrainer.typeMap);
    }
}
