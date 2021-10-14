package pokecube.core.handlers.events;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.worldgen.StructureSpawnPresetLoader;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.events.NpcSpawn;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.pokemob.SpawnEvent;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.TimePeriod;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;

public class SpawnEventsHandler
{
    public static void register()
    {
        // This caps the level chosen based on the configs, it is highest to
        // then allow addons to override it later.
        PokecubeCore.POKEMOB_BUS.addListener(EventPriority.HIGHEST, SpawnEventsHandler::CapLevel);
        // This cancels the event if this world is blacklisted for pokemob
        // spawning.
        PokecubeCore.POKEMOB_BUS.addListener(SpawnEventsHandler::onSpawnCheck);
        // This determines which pokemob should be slated for spawn, It is
        // highest so addons can override the picked mob later.
        PokecubeCore.POKEMOB_BUS.addListener(EventPriority.HIGHEST, SpawnEventsHandler::PickSpawn);

        // This handles spawning in the NPCs, etc from the structure blocks with
        // appropriate data markers.
        MinecraftForge.EVENT_BUS.addListener(SpawnEventsHandler::onReadStructTag);
        // This handles setting of the subbiomes for structures as they spawn
        // in, it is lowest, and not listening for cancalling incase addons make
        // adjustments first.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, SpawnEventsHandler::onStructureSpawn);
    }

    private static void CapLevel(final SpawnEvent.PickLevel event)
    {
        int level = event.getInitialLevel();
        if (SpawnHandler.lvlCap) level = Math.min(level, SpawnHandler.capLevel);
        event.setLevel(level);
    }

    /**
     * This is done here for when pokedex is checked, to compare to blacklist.
     *
     * @param event
     */
    private static void onSpawnCheck(final SpawnEvent.Check event)
    {
        if (!SpawnHandler.canSpawnInWorld((Level) event.world, event.forSpawn)) event.setCanceled(true);
    }

    private static void PickSpawn(final SpawnEvent.Pick.Pre event)
    {
        Vector3 v = event.getLocation();
        final LevelAccessor world = event.world;
        final List<PokedexEntry> entries = Lists.newArrayList(Database.spawnables);
        Collections.shuffle(entries);
        int index = 0;
        PokedexEntry dbe = entries.get(index);
        final SpawnCheck checker = new SpawnCheck(v, world);
        float weight = dbe.getSpawnData().getWeight(dbe.getSpawnData().getMatcher(checker));

        /**
         * TODO instead of completely random spawns: <br>
         * <br>
         * Have the spawn picked based on exact subchunk and server tick. This
         * will then allow possibly later adding advanced scanning blocks which
         * can help predict what will spawn where.
         */

        final double random = Math.random();
        final int max = entries.size();
        final Vector3 vbak = v.copy();
        while (weight <= random && index++ < max)
        {
            dbe = entries.get(index % entries.size());
            weight = dbe.getSpawnData().getWeight(dbe.getSpawnData().getMatcher(checker));
            if (weight == 0) continue;
            if (!dbe.flys() && random >= weight) if (!(dbe.swims() && v.getBlockMaterial(world) == Material.WATER))
            {
                v = Vector3.getNextSurfacePoint(world, vbak, Vector3.secondAxisNeg, 20);
                if (v != null)
                {
                    v.offsetBy(Direction.UP);
                    weight = dbe.getSpawnData().getWeight(dbe.getSpawnData().getMatcher(world, v));
                }
                else weight = 0;
            }
            if (v == null) v = vbak.copy();
        }
        if (random > weight || v == null) return;
        if (dbe.isLegendary())
        {
            final int level = SpawnHandler.getSpawnLevel((Level) world, v, dbe);
            if (level < PokecubeCore.getConfig().minLegendLevel) return;
        }
        event.setLocation(v);
        event.setPick(dbe);
    }

    private static boolean oldSpawns(final StructureEvent.ReadTag event, final String function)
    {
        final boolean nurse = function.startsWith("nurse");
        final boolean professor = function.startsWith("professor");
        final boolean trader = function.startsWith("trader");
        final boolean npc = function.startsWith("npc");

        if (!(nurse || professor || trader || npc)) return false;

        final NpcMob mob = NpcMob.TYPE.create(event.worldActual);

        mob.setPersistenceRequired();
        mob.moveTo(event.pos, 0.0F, 0.0F);
        mob.finalizeSpawn((ServerLevelAccessor) event.worldBlocks, event.worldBlocks.getCurrentDifficultyAt(event.pos),
                MobSpawnType.STRUCTURE, (SpawnGroupData) null, (CompoundTag) null);

        JsonObject thing = new JsonObject();
        if (!function.isEmpty() && function.contains("{") && function.contains("}")) try
        {
            final String trimmed = function.substring(function.indexOf("{"), function.lastIndexOf("}") + 1);
            thing = PokedexEntryLoader.gson.fromJson(trimmed, JsonObject.class);
            // Check if we specify a preset instead, and if that exists,
            // use that.
            if (thing.has("preset") && StructureSpawnPresetLoader.presetMap.containsKey(thing.get("preset")
                    .getAsString())) thing = StructureSpawnPresetLoader.presetMap.get(thing.get("preset")
                            .getAsString());
        }
        catch (final JsonSyntaxException e)
        {
            PokecubeCore.LOGGER.error("Error parsing " + function, e);
        }
        if (!(thing.has("trainerType") || thing.has("type"))) thing.add("type", new JsonPrimitive(nurse ? "healer"
                : trader ? "trader" : "professor"));
        if (nurse) mob.setMale(false);
        SpawnEventsHandler.spawnNpc(event, mob, thing);
        return true;
    }

    private static void spawnNpc(final StructureEvent.ReadTag event, final NpcMob mob, final JsonObject thing)
    {
        if (!MinecraftForge.EVENT_BUS.post(new NpcSpawn.Check(mob, event.pos, event.worldActual, MobSpawnType.STRUCTURE,
                thing)))
        {
            event.setResult(Result.ALLOW);
            SpawnEventsHandler.spawnMob(event, mob, thing);
        }
    }

    private static void spawnMob(final StructureEvent.ReadTag event, final Mob mob, final JsonObject thing)
    {
        EventsHandler.Schedule(event.worldActual, w ->
        {
            SpawnEventsHandler.applyFunction(mob, thing);
            w.addFreshEntity(mob);
            return true;
        });
    }

    private static void newSpawns(final StructureEvent.ReadTag event, final String function)
    {
        final JsonObject thing = StructureSpawnPresetLoader.presetMap.get(function);
        if (thing.has("options"))
        {
            final JsonArray options = thing.get("options").getAsJsonArray();
            final int num = event.rand.nextInt(options.size());
            SpawnEventsHandler.newSpawns(event, options.get(num).getAsString());
            return;
        }
        else
        {
            final ResourceLocation mobId = new ResourceLocation(thing.get("mob").getAsString());
            final EntityType<?> type = ForgeRegistries.ENTITIES.getValue(mobId);

            final Entity entity = type.create(event.worldActual);

            if (entity instanceof Mob) ((Mob) entity).setPersistenceRequired();
            entity.moveTo(event.pos, 0.0F, 0.0F);
            if (entity instanceof Mob) ((Mob) entity).finalizeSpawn((ServerLevelAccessor) event.worldBlocks,
                    event.worldBlocks.getCurrentDifficultyAt(event.pos), MobSpawnType.STRUCTURE, (SpawnGroupData) null,
                    (CompoundTag) null);

            if (entity instanceof NpcMob) SpawnEventsHandler.spawnNpc(event, (NpcMob) entity, thing);
            else if (entity instanceof Mob) SpawnEventsHandler.spawnMob(event, (Mob) entity, thing);
            else PokecubeCore.LOGGER.warn("Unsupported Entity for spawning! {}", function);
        }
    }

    private static void onReadStructTag(final StructureEvent.ReadTag event)
    {
        if (event.function.startsWith("pokecube:mob:"))
        {
            final String function = event.function.replaceFirst("pokecube:mob:", "");

            if (StructureSpawnPresetLoader.presetMap.containsKey(function)) try
            {
                SpawnEventsHandler.newSpawns(event, function);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.warn("Error processing for {}", function, e);
            }
            else if (SpawnEventsHandler.oldSpawns(event, function)) PokecubeCore.LOGGER.info("Handled spawn for {}, {}",
                    function, event.pos);
            else PokecubeCore.LOGGER.warn("Warning, no preset found for {}", function);
        }
    }

    private static void onStructureSpawn(final StructureEvent.BuildStructure event)
    {
        if (event.getBiomeType() == null) return;
        if (event.getWorld() instanceof ServerLevel)
        {
            final BiomeType subbiome = BiomeType.getBiome(event.getBiomeType(), true);
            final BoundingBox box = event.getBoundingBox();
            final Stream<BlockPos> poses = BlockPos.betweenClosedStream(box);
            SpawnEventsHandler.queueForUpdate(poses, subbiome, (Level) event.getWorld());
        }
        else
        {
            PokecubeCore.LOGGER.warn("Warning, world is not server world, things may break!");
            final BiomeType subbiome = BiomeType.getBiome(event.getBiomeType(), true);
            final BoundingBox box = event.getBoundingBox();
            final Stream<BlockPos> poses = BlockPos.betweenClosedStream(box);
            final LevelAccessor world = event.getWorld();
            poses.forEach((p) ->
            {
                TerrainManager.getInstance().getTerrain(world, p).setBiome(p, subbiome);
            });
        }
    }

    private static void queueForUpdate(final Stream<BlockPos> poses, final BiomeType subbiome, final Level level)
    {
        final Map<ChunkPos, Set<BlockPos>> byChunk = Maps.newHashMap();
        poses.forEach((p) ->
        {
            final ChunkPos pos = new ChunkPos(p);
            Set<BlockPos> set = byChunk.get(pos);
            if (set == null) byChunk.put(pos, set = Sets.newHashSet());
            set.add(p.immutable());
        });
        byChunk.forEach((pos, s) ->
        {
            EventsHandler.Schedule(level, world ->
            {
                s.forEach((p) ->
                {
                    TerrainManager.getInstance().getTerrain(world, p).setBiome(p, subbiome);
                });
                return true;
            }, false);
        });
    }

    public static class GuardInfo
    {
        public String time = "";
        public int    roam = 0;
    }

    public static interface INpcProcessor
    {
        void process(final Mob mob, final JsonObject thing);
    }

    public static List<INpcProcessor> processors = Lists.newArrayList((mob, thing) ->
    {
        if (mob instanceof NpcMob)
        {
            // TODO some of these should handle from IHasPokemobs instead!
            final NpcMob npc = (NpcMob) mob;
            if (thing.has("name")) npc.setNPCName(thing.get("name").getAsString());
            else if (thing.has("names"))
            {
                final JsonArray options = thing.get("names").getAsJsonArray();
                final int num = npc.getRandom().nextInt(options.size());
                npc.setNPCName(options.get(num).getAsString());
            }
            if (thing.has("customTrades")) npc.customTrades = thing.get("customTrades").getAsString();
            if (thing.has("type")) npc.setNpcType(NpcType.byType(thing.get("type").getAsString()));
            if (thing.has("gender"))
            {
                final boolean male = thing.get("gender").getAsString().equalsIgnoreCase("male") ? true
                        : thing.get("gender").getAsString().equalsIgnoreCase("female") ? false
                                : npc.getRandom().nextBoolean();
                npc.setMale(male);
            }
        }

        if (thing.has("copyMob"))
        {
            final ICopyMob copyMob = CopyCaps.get(mob);
            final ResourceLocation copyID = new ResourceLocation(thing.get("copyMob").getAsString());
            if (copyMob != null)
            {
                copyMob.setCopiedID(copyID);
                if (thing.has("copyTag"))
                {
                    final String tagStr = thing.get("copyTag").getAsString();
                    try
                    {
                        final CompoundTag tag = new TagParser(new StringReader(tagStr)).readStruct();
                        copyMob.setCopiedNBT(tag);
                    }
                    catch (final CommandSyntaxException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        GuardInfo info = null;
        if (thing.has("guard")) try
        {
            final JsonElement guardthing = thing.get("guard");
            info = PokedexEntryLoader.gson.fromJson(guardthing, GuardInfo.class);
        }
        catch (final JsonSyntaxException e)
        {
            PokecubeCore.LOGGER.error("Error parsing " + thing.get("guard"), e);
            info = new GuardInfo();
        }
        if (info == null) return;
        // Set us to sit at this location.
        final IGuardAICapability guard = mob.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        mob.restrictTo(mob.blockPosition(), info.roam);
        if (guard != null)
        {
            TimePeriod duration = info.time.equals("allday") ? TimePeriod.fullDay : new TimePeriod(0.55, .95);
            duration = info.time.equals("day") ? new TimePeriod(0, 0.5) : duration;
            duration = info.time.equals("night") ? new TimePeriod(0.55, .95) : duration;
            guard.getPrimaryTask().setPos(mob.blockPosition());
            guard.getPrimaryTask().setRoamDistance(info.roam);
            guard.getPrimaryTask().setActiveTime(duration);
        }
    });

    public static void applyFunction(final Mob npc, final JsonObject thing)
    {
        SpawnEventsHandler.processors.forEach(i -> i.process(npc, thing));
    }

}
