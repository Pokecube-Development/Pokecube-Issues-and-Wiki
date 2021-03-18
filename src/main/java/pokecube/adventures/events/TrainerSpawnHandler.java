package pokecube.adventures.events;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.adventures.Config;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.capabilities.CapabilityHasRewards.Reward;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerNpc;
import pokecube.adventures.utils.TrainerTracker;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.events.NpcSpawn;
import pokecube.core.events.StructureEvent;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.handlers.events.SpawnEventsHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;

public class TrainerSpawnHandler
{
    private static Vector3 vec1   = Vector3.getNewVector();
    static Vector3         v      = Vector3.getNewVector(), v1 = Vector3.getNewVector(), v2 = Vector3.getNewVector();
    static JEP             parser = new JEP();

    /** Given a player, find a random position near it. */
    public static Vector3 getRandomSpawningPointNearEntity(final World world, final Entity player, final int maxRange)
    {
        if (player == null) return null;

        final Vector3 v = TrainerSpawnHandler.vec1.set(player);

        final Random rand = new Random();

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
            int y_Test;
            if (temp1.y > (y_Test = world.getHeight(Type.MOTION_BLOCKING_NO_LEAVES, temp1.intX(), temp1.intY())))
                temp1.y = y_Test;
            temp1.y++;

            if (temp1.getBlockMaterial(world).blocksMovement()) return null;
            if (temp1.addTo(0, 1, 0).getBlockMaterial(world).blocksMovement()) return null;
            temp1.y--;
            return temp1;
        }
        return null;
    }

    public static TrainerNpc getTrainer(Vector3 v, final World w)
    {
        TypeTrainer ttype = null;
        final Material m = v.getBlockMaterial(w);
        if (m == Material.AIR && v.offset(Direction.DOWN).getBlockMaterial(w) == Material.AIR) v = v.getTopBlockPos(w)
                .offsetBy(Direction.UP);
        final SpawnCheck checker = new SpawnCheck(v, w);
        final List<TypeTrainer> types = Lists.newArrayList(TypeTrainer.typeMap.values());
        Collections.shuffle(types);
        types:
        for (final TypeTrainer type : types)
            for (final Entry<SpawnBiomeMatcher, Float> entry : type.matchers.entrySet())
            {
                final SpawnBiomeMatcher matcher = entry.getKey();
                final Float value = entry.getValue();
                if (w.rand.nextFloat() < value && matcher.matches(checker))
                {
                    ttype = type;
                    break types;
                }
            }
        if (ttype == null) return null;
        final int level = SpawnHandler.getSpawnLevel(w, v, Database.getEntry(1));
        final TrainerNpc trainer = new TrainerNpc(TrainerNpc.TYPE, w).setType(ttype).setLevel(level);
        trainer.aiStates.setAIState(IHasNPCAIStates.MATES, true);
        trainer.aiStates.setAIState(IHasNPCAIStates.TRADES, true);
        return trainer;
    }

    public static void randomizeTrainerTeam(final Entity trainer, final IHasPokemobs mobs)
    {
        final Vector3 loc = Vector3.getNewVector().set(trainer);
        // Set level based on what wild pokemobs have.
        int level = SpawnHandler.getSpawnLevel(trainer.getEntityWorld(), loc, Pokedex.getInstance().getFirstEntry());

        if (trainer instanceof LeaderNpc)
        {
            // Gym leaders are 10 lvls higher than others.
            level += 10;
            // Randomize badge for leader.

            final IHasRewards rewardsCap = ((LeaderNpc) trainer).rewardsCap;
            final PokeType type = PokeType.values()[new Random().nextInt(PokeType.values().length)];
            final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(PokecubeAdv.MODID, ":badge_" + type));
            if (item != null)
            {
                final ItemStack badge = new ItemStack(item);
                if (!rewardsCap.getRewards().isEmpty()) rewardsCap.getRewards().set(0, new Reward(badge));
                else rewardsCap.getRewards().add(new Reward(badge));
                ((LeaderNpc) trainer).setHeldItem(Hand.OFF_HAND, rewardsCap.getRewards().get(0).stack);
            }
        }
        // Randomize team.
        if (trainer instanceof TrainerNpc)
        {
            final TrainerNpc t = (TrainerNpc) trainer;
            t.name = "";
            // Reset their trades, as this will randomize them when trades are
            // needed later.
            t.resetTrades();
            // Init for trainers randomizes their teams
            if (mobs.getType() != null) t.setType(mobs.getType()).setLevel(level);
        }
        else if (mobs.getType() != null) TypeTrainer.getRandomTeam(mobs, (LivingEntity) trainer, level, trainer
                .getEntityWorld());
    }

    public static void tick(final ServerWorld w)
    {
        if (w.isRemote) return;
        if (!SpawnHandler.canSpawnInWorld(w)) return;
        final List<ServerPlayerEntity> players = w.getPlayers();
        if (players.size() < 1) return;
        final PlayerEntity p = players.get(w.rand.nextInt(players.size()));
        Vector3 v = TrainerSpawnHandler.getRandomSpawningPointNearEntity(w, p, Config.instance.trainerBox);
        if (v == null) return;
        if (v.y <= 0) v.y = v.getMaxY(w);
        final Vector3 temp = Vector3.getNextSurfacePoint(w, v, Vector3.secondAxisNeg, 20);
        v = temp != null ? temp.offset(Direction.UP) : v;
        if (v.y <= 0 || v.y >= w.getActualHeight()) return;

        if (!SpawnHandler.checkNoSpawnerInArea(w, v.intX(), v.intY(), v.intZ())) return;

        final int count = TrainerTracker.countTrainers(w, v, PokecubeAdv.config.trainerBox);
        if (count < Config.instance.trainerDensity)
        {
            final BlockState here = v.getBlockState(w);
            final Vector3 u = v.add(0, -1, 0);
            final BlockState down = u.getBlockState(w);
            if (here.isAir(w, v.getPos()) && down.isAir(w, u.getPos())) return;

            final long time = System.nanoTime();
            final TrainerNpc t = TrainerSpawnHandler.getTrainer(v, w);
            if (t == null) return;
            final IHasPokemobs cap = TrainerCaps.getHasPokemobs(t);
            final NpcSpawn event = new NpcSpawn(t, v.getPos(), w, SpawnReason.NATURAL);
            if (MinecraftForge.EVENT_BUS.post(event))
            {
                t.remove();
                return;
            }
            final double dt = (System.nanoTime() - time) / 1000000D;
            if (dt > 20) PokecubeCore.LOGGER.warn("Trainer " + cap.getType().getName() + " " + dt + "ms ");
            v.offsetBy(Direction.UP).moveEntity(t);

            // Not valid spawning spot, so deny the spawn here.
            if (!(WorldEntitySpawner.canCreatureTypeSpawnAtLocation(PlacementType.ON_GROUND, w, v.getPos(), t.getType())
                    || WorldEntitySpawner.canCreatureTypeSpawnAtLocation(PlacementType.IN_WATER, w, v.getPos(), t
                            .getType()))) return;

            if (t.pokemobsCap.countPokemon() > 0 && SpawnHandler.checkNoSpawnerInArea(w, (int) t.getPosX(), (int) t.getPosY(),
                    (int) t.getPosZ()))
            {
                w.addEntity(t);
                TrainerSpawnHandler.randomizeTrainerTeam(t, cap);
                PokecubeCore.LOGGER.debug("Spawned Trainer: " + t + " " + count);
            }
            else t.remove();
        }

    }

    @SubscribeEvent
    public static void tickEvent(final WorldTickEvent evt)
    {
        if (Config.instance.trainerSpawn && evt.phase == Phase.END && evt.world instanceof ServerWorld && evt.world
                .getGameTime() % PokecubeCore.getConfig().spawnRate == 0)
        {
            final long time = System.nanoTime();
            TrainerSpawnHandler.tick((ServerWorld) evt.world);
            final double dt = (System.nanoTime() - time) / 1000000D;
            if (dt > 50) PokecubeCore.LOGGER.warn("Trainer Spawn Tick took " + dt + "ms");
        }
    }

    @SubscribeEvent
    /**
     * This takes care of randomization for trainer teams when spawned in
     * structuress.
     *
     * @param event
     */
    public static void StructureSpawn(final StructureEvent.ReadTag event)
    {
        if (!event.function.startsWith("pokecube_adventures:")) return;
        String function = event.function.replaceFirst("pokecube_adventures:", "");
        boolean leader = false;
        // Here we process custom options for trainers or leaders in structures.
        if (function.startsWith("trainer") || (leader = function.startsWith("leader")))
        {
            // Set it to air so mob can spawn.
            event.world.setBlockState(event.pos, Blocks.AIR.getDefaultState(), 2);
            function = function.replaceFirst(leader ? "leader" : "trainer", "");
            final TrainerNpc mob = leader ? LeaderNpc.TYPE.create(event.world.getWorld())
                    : TrainerNpc.TYPE.create(event.world.getWorld());
            mob.enablePersistence();
            mob.moveToBlockPosAndAngles(event.pos, 0.0F, 0.0F);
            mob.onInitialSpawn(event.world, event.world.getDifficultyForLocation(event.pos), SpawnReason.STRUCTURE,
                    (ILivingEntityData) null, (CompoundNBT) null);
            JsonObject thing = new JsonObject();
            if (!function.isEmpty() && function.contains("{") && function.contains("}")) try
            {
                final String trimmed = function.substring(function.indexOf("{"), function.lastIndexOf("}") + 1);
                thing = PokedexEntryLoader.gson.fromJson(trimmed, JsonObject.class);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error parsing " + function, e);
            }
            // We apply it regardless, as this initializes defaults.
            TrainerSpawnHandler.applyFunction(event.world, mob, thing, leader);
            PokecubeCore.LOGGER.debug("Adding trainer: " + mob);
            if (!MinecraftForge.EVENT_BUS.post(new NpcSpawn(mob, event.pos, event.world, SpawnReason.STRUCTURE)))
            {
                EventsHandler.Schedule(event.world.getWorld(), w ->
                {
                    event.world.addEntity(mob);
                    return true;
                });
                event.setResult(Result.ALLOW);
            }
        }
    }

    private static void applyFunction(final IWorld world, final TrainerNpc npc, final JsonObject thing,
            final boolean leader)
    {
        // Apply and settings common to pokecube core.
        SpawnEventsHandler.applyFunction(npc, thing);

        // Then apply trainer specific stuff.
        int level = SpawnHandler.getSpawnLevel(world, Vector3.getNewVector().set(npc), Database.missingno);
        if (thing.has("customTrades")) npc.customTrades = thing.get("customTrades").getAsString();
        if (thing.has("level")) level = thing.get("level").getAsInt();
        if (thing.has("trainerType"))
        {
            final TypeTrainer type = TypeTrainer.typeMap.get(thing.get("trainerType").getAsString());
            if (type != null) npc.pokemobsCap.setType(type);
            else PokecubeCore.LOGGER.error("No trainer type registerd for {}", thing.get("trainerType").getAsString());
        }
        else
        {
            final List<TypeTrainer> types = Lists.newArrayList(TypeTrainer.typeMap.values());
            Collections.shuffle(types);
            for (final TypeTrainer type : types)
            {
                if (type.matchers.isEmpty()) continue;
                npc.pokemobsCap.setType(type);
                break;
            }
        }
        npc.initTeam(level);
    }
}
