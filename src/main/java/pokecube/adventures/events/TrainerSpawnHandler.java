package pokecube.adventures.events;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.Material;
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
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates.AIState;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerBase;
import pokecube.adventures.entity.trainer.TrainerNpc;
import pokecube.adventures.utils.TrainerTracker;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.worldgen.StructureSpawnPresetLoader;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.events.NpcSpawn;
import pokecube.core.events.StructureEvent;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.handlers.events.SpawnEventsHandler;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.utils.PokeType;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.core.common.ThutCore;

public class TrainerSpawnHandler
{
    private static Vector3 vec1 = Vector3.getNewVector();

    static JEP parser = new JEP();

    static
    {
        SpawnEventsHandler.processors.add((mob, thing) ->
        {
            final Level world = mob.getCommandSenderWorld();
            // Then apply trainer specific stuff.
            int level = SpawnHandler.getSpawnLevel(world, Vector3.getNewVector().set(mob), Database.missingno);
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
                    final ResourceLocation tag = new ResourceLocation(thing.get("trainerTag").getAsString());
                    final List<TypeTrainer> types = Lists.newArrayList();
                    TypeTrainer.typeMap.values().forEach(t ->
                    {
                        if (t.tags.contains(tag)) types.add(t);
                    });
                    if (!types.isEmpty())
                    {
                        Collections.shuffle(types);
                        typeName = types.get(0).getName();
                    }
                }
                if (typeName.isEmpty())
                {
                    final List<TypeTrainer> types = Lists.newArrayList(TypeTrainer.typeMap.values());
                    Collections.shuffle(types);
                    for (final TypeTrainer type : types)
                    {
                        if (type.matchers.isEmpty()) continue;
                        typeName = type.getName();
                        break;
                    }
                }
                if (!typeName.isEmpty())
                {
                    final TypeTrainer type = TypeTrainer.typeMap.get(typeName);
                    // TODO some of these should handle from IHasPokemobs
                    // instead!
                    if (type != null && mob instanceof NpcMob) ((NpcMob) mob).setNpcType(type);
                    else PokecubeCore.LOGGER.error("No trainer type registerd for {}", typeName);
                }
            }
            if (mob instanceof TrainerBase) ((TrainerBase) mob).initTeam(level);
        });
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
            int y_Test;
            if (temp1.y > (y_Test = world.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, temp1.intX(), temp1.intY())))
                temp1.y = y_Test;
            temp1.y++;

            if (temp1.getBlockMaterial(world).blocksMotion()) return null;
            if (temp1.addTo(0, 1, 0).getBlockMaterial(world).blocksMotion()) return null;
            temp1.y--;
            return temp1;
        }
        return null;
    }

    public static TrainerNpc getTrainer(Vector3 v, final Level w)
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
                if (w.random.nextFloat() < value && matcher.matches(checker))
                {
                    ttype = type;
                    break types;
                }
            }
        if (ttype == null) return null;
        final int level = SpawnHandler.getSpawnLevel(w, v, Database.missingno);
        final TrainerNpc trainer = new TrainerNpc(TrainerNpc.TYPE, w).setType(ttype).setLevel(level);
        trainer.aiStates.setAIState(AIState.MATES, true);
        trainer.aiStates.setAIState(AIState.TRADES_ITEMS, true);
        return trainer;
    }

    public static void randomizeTrainerTeam(final Entity trainer, final IHasPokemobs mobs)
    {
        final Vector3 loc = Vector3.getNewVector().set(trainer);
        // Set level based on what wild pokemobs have.
        int level = SpawnHandler.getSpawnLevel(trainer.getCommandSenderWorld(), loc, Pokedex.getInstance()
                .getFirstEntry());

        if (trainer instanceof LeaderNpc)
        {
            // Gym leaders are 10 lvls higher than others.
            level += 10;
            // Randomize badge for leader.

            final IHasRewards rewardsCap = ((LeaderNpc) trainer).rewardsCap;
            final PokeType type = PokeType.values()[ThutCore.newRandom().nextInt(PokeType.values().length)];
            final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(PokecubeAdv.MODID, ":badge_" + type));
            if (item != null)
            {
                final ItemStack badge = new ItemStack(item);
                if (!rewardsCap.getRewards().isEmpty()) rewardsCap.getRewards().set(0, new Reward(badge));
                else rewardsCap.getRewards().add(new Reward(badge));
                ((LeaderNpc) trainer).setItemInHand(InteractionHand.OFF_HAND, rewardsCap.getRewards().get(0).stack);
            }
        }
        // Randomize team.
        if (trainer instanceof TrainerNpc)
        {
            final TrainerNpc t = (TrainerNpc) trainer;
            t.setNPCName("");
            // Reset their trades, as this will randomize them when trades are
            // needed later.
            t.resetTrades();
            // Init for trainers randomizes their teams
            if (mobs.getType() != null) t.setType(mobs.getType()).setLevel(level);
        }
        else if (mobs.getType() != null) TypeTrainer.getRandomTeam(mobs, (LivingEntity) trainer, level, trainer
                .getCommandSenderWorld());
    }

    public static void tick(final ServerLevel w)
    {
        if (w.isClientSide) return;
        if (!SpawnHandler.canSpawnInWorld(w)) return;
        final List<ServerPlayer> players = w.players();
        if (players.size() < 1) return;
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
            if (w.isEmptyBlock(v.getPos()) && w.isEmptyBlock(u.getPos())) return;

            final long time = System.nanoTime();
            final TrainerNpc t = TrainerSpawnHandler.getTrainer(v, w);
            if (t == null) return;
            final IHasPokemobs cap = TrainerCaps.getHasPokemobs(t);
            final NpcSpawn event = new NpcSpawn.Spawn(t, v.getPos(), w, MobSpawnType.NATURAL);
            if (MinecraftForge.EVENT_BUS.post(event))
            {
                t.remove();
                return;
            }
            final double dt = (System.nanoTime() - time) / 1000000D;
            if (dt > 20) PokecubeCore.LOGGER.warn("Trainer " + cap.getType().getName() + " " + dt + "ms ");
            v.offsetBy(Direction.UP).moveEntity(t);

            // Not valid spawning spot, so deny the spawn here.
            if (!(NaturalSpawner.isSpawnPositionOk(Type.ON_GROUND, w, v.getPos(), t.getType())
                    || NaturalSpawner.isSpawnPositionOk(Type.IN_WATER, w, v.getPos(), t.getType())))
                return;

            if (t.pokemobsCap.countPokemon() > 0 && SpawnHandler.checkNoSpawnerInArea(w, (int) t.getX(), (int) t.getY(),
                    (int) t.getZ()))
            {
                w.addFreshEntity(t);
                TrainerSpawnHandler.randomizeTrainerTeam(t, cap);
                PokecubeCore.LOGGER.debug("Spawned Trainer: " + t + " " + count);
            }
            else t.remove();
        }
    }

    @SubscribeEvent
    public static void tickEvent(final WorldTickEvent evt)
    {
        if (Config.instance.trainerSpawn && evt.phase == Phase.END && evt.world instanceof ServerLevel && evt.world
                .getGameTime() % PokecubeCore.getConfig().spawnRate == 0)
        {
            final long time = System.nanoTime();
            TrainerSpawnHandler.tick((ServerLevel) evt.world);
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
        final boolean leader;
        // Here we process custom options for trainers or leaders in structures.
        if ((leader = function.startsWith("leader")) || function.startsWith("trainer"))
        {
            function = function.replaceFirst(leader ? "leader" : "trainer", "");
            final TrainerNpc mob = leader ? LeaderNpc.TYPE.create(event.worldActual)
                    : TrainerNpc.TYPE.create(event.worldActual);
            mob.setPersistenceRequired();
            mob.moveTo(event.pos, 0.0F, 0.0F);
            mob.finalizeSpawn((ServerLevelAccessor) event.worldBlocks, event.worldBlocks.getCurrentDifficultyAt(event.pos),
                    MobSpawnType.STRUCTURE, (SpawnGroupData) null, (CompoundTag) null);
            JsonObject thing = new JsonObject();
            if (!function.isEmpty() && function.contains("{") && function.contains("}")) try
            {
                final String trimmed = function.substring(function.indexOf("{"), function.lastIndexOf("}") + 1);
                thing = PokedexEntryLoader.gson.fromJson(trimmed, JsonObject.class);
                // Check if we specify a preset instead, and if that exists, use
                // that.
                if (thing.has("preset") && StructureSpawnPresetLoader.presetMap.containsKey(thing.get("preset")
                        .getAsString())) thing = StructureSpawnPresetLoader.presetMap.get(thing.get("preset")
                                .getAsString());
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error parsing " + function, e);
            }
            if (PokecubeCore.getConfig().debug) PokecubeCore.LOGGER.debug("Adding trainer: " + mob);
            if (!MinecraftForge.EVENT_BUS.post(new NpcSpawn.Check(mob, event.pos, event.worldActual,
                    MobSpawnType.STRUCTURE, thing)))
            {
                event.setResult(Result.ALLOW);
                final JsonObject apply = thing;
                EventsHandler.Schedule(event.worldActual, w ->
                {
                    w.getChunk(mob.blockPosition());
                    SpawnEventsHandler.applyFunction(mob, apply);
                    w.addFreshEntity(mob);
                    return true;
                });
            }
        }
    }
}
