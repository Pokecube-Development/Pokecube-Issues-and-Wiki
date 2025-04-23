package pokecube.adventures.events;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerNpc;
import pokecube.adventures.init.EntityTypes;
import pokecube.adventures.utils.TrainerTracker;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.entity.trainers.IHasNPCAIStates;
import pokecube.api.entity.trainers.IHasNPCAIStates.AIState;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.IHasRewards;
import pokecube.api.entity.trainers.IHasRewards.Reward;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.events.StructureEvent;
import pokecube.api.events.npcs.NpcSpawn;
import pokecube.api.events.pokemobs.SpawnEvent.SpawnContext;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.worldgen.StructureSpawnPresetLoader;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.eventhandlers.SpawnEventsHandler;
import pokecube.core.eventhandlers.SpawnHandler;
import thut.api.level.terrain.TerrainManager;
import thut.api.maths.Vector3;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TrainerSpawnHandler
{
    private static final Vector3 vec1 = new Vector3();

    static
    {
        SpawnEventsHandler.processors.add((mob, thing) -> {
            final ServerLevel world = (ServerLevel) mob.level;
            // Then apply trainer specific stuff.
            int level = SpawnHandler.getSpawnLevel(new SpawnContext(world, Database.missingno, new Vector3().set(mob)));
            if (thing.has("level")) level = thing.get("level").getAsInt();
            String typeName = "";
            if (thing.has("aiStates"))
            {
                final IHasNPCAIStates aiStates = TrainerCaps.getNPCAIStates(mob);
                if (aiStates != null)
                {
                    aiStates.setTotalState(thing.get("aiStates").getAsInt());
                    mob.setInvulnerable(aiStates.getAIState(AIState.INVULNERABLE));
                }
            }

            // This is somewhat deprecated in favour of the "type" tag for npcs,
            // but
            // it will work here as well.
            if (!thing.has("type"))
            {
                if (thing.has("trainerType")) typeName = thing.get("trainerType").getAsString();
                else if (thing.has("trainerTypes"))
                {
                    final String[] types = thing.get("trainerTypes").getAsString().split(";");
                    typeName = types[world.getRandom().nextInt(types.length)];
                }
                else if (thing.has("trainerTag"))
                {
                    final ResourceLocation tag = ResourceLocation.parse(thing.get("trainerTag").getAsString());
                    final List<TypeTrainer> types = Lists.newArrayList();
                    TypeTrainer.typeMap.values().forEach(t -> {
                        if (t.tags.contains(tag)) types.add(t);
                    });
                    if (!types.isEmpty())
                    {
                        Collections.shuffle(types);
                        typeName = types.getFirst().getName();
                    }
                }
                if (typeName.isEmpty())
                {
                    final List<TypeTrainer> types = Lists.newArrayList(TypeTrainer.typeMap.values());
                    Collections.shuffle(types);
                    for (final TypeTrainer type : types)
                    {
                        if (type.spawns.isEmpty()) continue;
                        typeName = type.getName();
                        break;
                    }
                }
                if (!typeName.isEmpty())
                {
                    final TypeTrainer type = TypeTrainer.typeMap.get(typeName);
                    if (type != null && mob instanceof NpcMob npc) npc.setNpcType(type);
                    else PokecubeAPI.LOGGER.error("No trainer type registerd for {}", typeName);
                }
            }
            IHasPokemobs mobs = TrainerCaps.getHasPokemobs(mob);
            if (mobs != null && mob instanceof NpcMob npc && npc.getNpcType() instanceof TypeTrainer type)
            {
                mobs.setType(type);
            }
            if (mobs != null) initTrainer(mobs, level);
            if (mob instanceof TrainerNpc trainer) trainer.setTypes(!(thing.has("name") || thing.has("names")));
        });
    }

    public static void initTrainer(IHasPokemobs trainer, int level)
    {
        if (trainer.getType() == null) return;
        var entity = trainer.getTrainer();
        TypeTrainer.getRandomTeam(trainer, entity, level, entity.level());
        var type = trainer.getType();
        type.initTrainerItems(entity);
    }

    /** Given a player, find a random position near it. */
    public static Vector3 getRandomSpawningPointNearEntity(final Level world, final Entity player, final int maxRange)
    {
        if (player == null) return null;

        final Vector3 v = TrainerSpawnHandler.vec1.set(player);

        final Random rand = ThutCore.newRandom();

        // SElect random gaussians from here.
        double x = rand.nextGaussian() * maxRange;
        double z = rand.nextGaussian() * maxRange;

        // Cap x and z to distance.
        if (Math.abs(x) > maxRange) x = Math.signum(x) * maxRange;
        if (Math.abs(z) > maxRange) z = Math.signum(z) * maxRange;

        // Don't select distances too far up/down from current.
        final double y = Math.min(Math.max(5, rand.nextGaussian() * 10), 10);
        v.addTo(x, y, z);

        // Don't select unloaded areas.
        if (!TerrainManager.isAreaLoaded(world, v, 8)) return null;

        // Find surface
        final Vector3 temp1 = Vector3.getNextSurfacePoint(world, TrainerSpawnHandler.vec1, Vector3.secondAxisNeg, 10);

        if (temp1 != null)
        {
            if (!temp1.addTo(0, 1, 0).isClearOfBlocks(world)) return null;
            if (!temp1.add(0, 1, 0).isClearOfBlocks(world)) return null;
            return temp1;
        }
        return null;
    }

    public static TrainerNpc getTrainer(Vector3 v, final ServerLevel w)
    {
        NpcType ttype = NpcType.getRandomForLocation(v, w);
        if (ttype == null) return null;
        final int level = SpawnHandler.getSpawnLevel(new SpawnContext(w, Database.missingno, v));
        final TrainerNpc trainer = new TrainerNpc(EntityTypes.getTrainer(), w);
        trainer.setNpcType(ttype);
        trainer.getAIStates().setAIState(AIState.MATES, true);
        trainer.getAIStates().setAIState(AIState.TRADES_ITEMS, true);
        initTrainer(trainer.getPokemobs(), level);
        return trainer;
    }

    public static void randomizeTrainerTeam(final Entity trainer, final IHasPokemobs mobs)
    {
        final Vector3 loc = new Vector3().set(trainer);
        // Set level based on what wild pokemobs have.
        int level = SpawnHandler.getSpawnLevel(
                new SpawnContext((ServerLevel) trainer.level, Pokedex.getInstance().getFirstEntry(), loc));

        if (trainer instanceof LeaderNpc npc)
        {
            // Gym leaders are 10 lvls higher than others.
            level += 10;
            // Randomize badge for leader.

            final IHasRewards rewardsCap = npc.getData(TrainerCaps.REWARDS);
            final PokeType type = PokeType.values()[ThutCore.newRandom().nextInt(PokeType.values().length)];
            final Item item = BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID, ":badge_" + type));
            if (item != null)
            {
                final ItemStack badge = new ItemStack(item);
                if (!rewardsCap.getRewards().isEmpty()) rewardsCap.getRewards().set(0, new Reward(badge));
                else rewardsCap.getRewards().add(new Reward(badge));
            }
        }
        // Randomize team.
        if (trainer instanceof TrainerNpc t)
        {
            t.setNPCName("");
            // Reset their trades, as this will randomize them when trades are
            // needed later.
            t.resetTrades();
        }
        initTrainer(mobs, level);
    }

    public static void tick(final ServerLevel w)
    {
        if (w.isClientSide) return;
        if (SpawnHandler.canNotSpawnInWorld(w)) return;
        final List<ServerPlayer> players = w.players();
        if (players.isEmpty()) return;
        final Player p = players.get(w.random.nextInt(players.size()));
        Vector3 v = TrainerSpawnHandler.getRandomSpawningPointNearEntity(w, p, Config.instance.trainerBox);
        if (v == null) return;
        if (v.y <= 0) v.y = v.getMaxY(w);
        final Vector3 temp = Vector3.getNextSurfacePoint(w, v, Vector3.secondAxisNeg, 20);
        v = temp != null ? temp.offset(Direction.UP) : v;
        if (v.y <= 0 || v.y >= w.getMaxBuildHeight()) return;

        if (!SpawnHandler.checkNoSpawnerInArea(w, v.intX(), v.intY(), v.intZ())) return;

        final int count = TrainerTracker.countTrainers(w, v, PokecubeAdv.config.trainerBox);
        if (count < Config.instance.trainerDensity)
        {
            final Vector3 u = v.add(0, -1, 0);
            final Vector3 up = v.add(0, 1, 0);
            if (w.isEmptyBlock(v.getPos()) && w.isEmptyBlock(u.getPos()) || !w.isEmptyBlock(up.getPos())) return;

            final long time = System.nanoTime();
            final TrainerNpc t = TrainerSpawnHandler.getTrainer(v, w);
            if (t == null) return;
            final IHasPokemobs cap = TrainerCaps.getHasPokemobs(t);
            final NpcSpawn.Spawn event = new NpcSpawn.Spawn(t, v.getPos(), w, MobSpawnType.NATURAL);
            ThutCore.FORGE_BUS.post(event);
            if (event.isCanceled())
            {
                t.remove(RemovalReason.DISCARDED);
                return;
            }
            final double dt = (System.nanoTime() - time) / 1000000D;
            if (dt > 20) PokecubeAPI.LOGGER.warn("Trainer {} {}ms ", cap.getType().getName(), dt);
            v.offsetBy(Direction.UP).moveEntity(t);

            FluidState fluid = w.getFluidState(v.getPos());
            // Not valid spawning spot, so deny the spawn here.
            if (!fluid.isEmpty() && fluid.getType() != Fluids.WATER) return;

            if (t.getPokemobs().countPokemon() > 0 && SpawnHandler.checkNoSpawnerInArea(w, (int) t.getX(), (int) t.getY(),
                    (int) t.getZ()))
            {
                w.addFreshEntity(t);
                TrainerSpawnHandler.randomizeTrainerTeam(t, cap);
                // Force a re-fresh of the type for fixing bag, belt, etc.
                t.setNpcType(t.getNpcType());
                if (PokecubeCore.getConfig().debug_spawning) PokecubeAPI.logInfo("Spawned Trainer: " + t + " " + count);
            }
            else t.remove(RemovalReason.DISCARDED);
        }
    }

    @SubscribeEvent
    public static void tickEvent(final LevelTickEvent.Post evt)
    {
        if (Config.instance.trainerSpawn && evt.getLevel() instanceof ServerLevel level
                && evt.getLevel().getGameTime() % PokecubeCore.getConfig().spawnRate == 0)
        {
            final long time = System.nanoTime();
            TrainerSpawnHandler.tick(level);
            final double dt = (System.nanoTime() - time) / 1000000D;
            if (dt > 50) PokecubeAPI.LOGGER.warn("Trainer Spawn Tick took {}ms", dt);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoinLevel(EntityJoinLevelEvent event)
    {
        if (event.getEntity() instanceof NpcMob npc)
        {
            if (!npc.level().isAreaLoaded(npc.getOnPos(), 2) && npc.level() instanceof ServerLevel)
            {
                EventsHandler.Schedule(npc.level(), w -> {
                    if (!npc.isAddedToLevel()) return true;
                    if (!npc.level().isAreaLoaded(npc.getOnPos(), 2)) return false;
                    TrainerEventHandler.initTrainer(npc, null);
                    return true;
                });
                return;
            }
            TrainerEventHandler.initTrainer(npc, null);
        }
    }

    /**
     * This takes care of randomization for trainer teams when spawned in structuress.
     */
    @SubscribeEvent
    public static void StructureSpawn(final StructureEvent.ReadTag event)
    {
        if (!event.function.startsWith("pokecube_adventures:")) return;
        String function = event.function.replaceFirst("pokecube_adventures:", "");
        final boolean leader;
        // Here we process custom options for trainers or leaders in structures.
        if ((leader = function.startsWith("leader")) || function.startsWith("trainer"))
        {
            function = function.replaceFirst(leader ? "leader" : "trainer", "");
            final TrainerNpc mob = leader
                    ? EntityTypes.getLeader().create(event.worldActual)
                    : EntityTypes.getTrainer().create(event.worldActual);
            mob.setPersistenceRequired();
            mob.moveTo(event.pos, 0.0F, 0.0F);
            mob.finalizeSpawn((ServerLevelAccessor) event.worldBlocks,
                    event.worldBlocks.getCurrentDifficultyAt(event.pos), MobSpawnType.STRUCTURE, null);
            JsonObject thing = new JsonObject();
            if (function.contains("{") && function.contains("}")) try
            {
                final String trimmed = function.substring(function.indexOf("{"), function.lastIndexOf("}") + 1);
                thing = JsonUtil.gson.fromJson(trimmed, JsonObject.class);
                // Check if we specify a preset instead, and if that exists, use
                // that.
                if (thing.has("preset") && StructureSpawnPresetLoader.presetMap.containsKey(
                        thing.get("preset").getAsString()))
                    thing = StructureSpawnPresetLoader.presetMap.get(thing.get("preset").getAsString());
            }
            catch (final Exception e)
            {
                PokecubeAPI.LOGGER.error("Error parsing {}", function, e);
            }
            if (PokecubeCore.getConfig().debug_spawning) PokecubeAPI.logInfo("Adding trainer: " + mob);
            var checkEvent = new NpcSpawn.Check(mob, event.pos, event.worldActual, MobSpawnType.STRUCTURE, thing);
            ThutCore.FORGE_BUS.post(checkEvent);
            if (!checkEvent.isCanceled())
            {
                event.setResult(TriState.TRUE);
                mob.getPersistentData().putString("pokecube:structure_entity", JsonUtil.gson.toJson(thing));
                if (event.duringWorldgen)
                {
                    mob.save(event.nbt);
                    event.nbt.putBoolean("pokecube:structure_entity", true);
                }
                else
                {
                    EventsHandler.Schedule(event.worldActual, w -> {
                        w.addFreshEntity(mob);
                        return true;
                    });
                }
            }
        }
    }
}
