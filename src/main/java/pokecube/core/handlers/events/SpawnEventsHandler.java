package pokecube.core.handlers.events;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.block.material.Material;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.database.worldgen.StructureSpawnPresetLoader;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.events.NpcSpawn;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.pokemob.SpawnEvent;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.TimePeriod;
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

    private static void CapLevel(final SpawnEvent.Level event)
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
        if (!SpawnHandler.canSpawnInWorld((World) event.world)) event.setCanceled(true);
    }

    private static void PickSpawn(final SpawnEvent.Pick.Pre event)
    {
        Vector3 v = event.getLocation();
        final IWorld world = event.world;
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
            final int level = SpawnHandler.getSpawnLevel((World) world, v, dbe);
            if (level < PokecubeCore.getConfig().minLegendLevel) return;
        }
        event.setLocation(v);
        event.setPick(dbe);
    }

    private static void onReadStructTag(final StructureEvent.ReadTag event)
    {
        if (event.function.startsWith("pokecube:mob:"))
        {
            final String function = event.function.replaceFirst("pokecube:mob:", "");
            final boolean nurse = function.startsWith("nurse");
            final boolean professor = function.startsWith("professor");
            final boolean trader = function.startsWith("trader");
            final boolean npc = function.startsWith("npc");
            if (nurse || professor || trader || npc)
            {
                final NpcMob mob = NpcMob.TYPE.create(event.worldActual);

                mob.enablePersistence();
                mob.moveToBlockPosAndAngles(event.pos, 0.0F, 0.0F);
                mob.onInitialSpawn((IServerWorld) event.worldBlocks, event.worldBlocks.getDifficultyForLocation(
                        event.pos), SpawnReason.STRUCTURE, (ILivingEntityData) null, (CompoundNBT) null);

                mob.setNpcType(nurse ? NpcType.byType("healer")
                        : trader ? NpcType.byType("trader") : NpcType.byType("professor"));
                if (nurse) mob.setMale(false);

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

                if (!MinecraftForge.EVENT_BUS.post(new NpcSpawn.Check(mob, event.pos, event.worldActual,
                        SpawnReason.STRUCTURE, thing)))
                {
                    event.setResult(Result.ALLOW);
                    final JsonObject apply = thing;
                    EventsHandler.Schedule(event.worldActual, w ->
                    {
                        w.getChunk(mob.getPosition());
                        SpawnEventsHandler.applyFunction(mob, apply);
                        w.addEntity(mob);
                        return true;
                    });
                }
            }
            else if (function.startsWith("pokemob"))
            {

            }
        }
    }

    private static void onStructureSpawn(final StructureEvent.BuildStructure event)
    {
        if (event.getBiomeType() != null) if (event.getWorld() instanceof ServerWorld)
        {
            final BiomeType subbiome = BiomeType.getBiome(event.getBiomeType(), true);
            final MutableBoundingBox box = event.getBoundingBox();
            final Stream<BlockPos> poses = BlockPos.getAllInBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY,
                    box.maxZ);
            EventsHandler.Schedule((ServerWorld) event.getWorld(), world ->
            {
                poses.forEach((p) ->
                {
                    TerrainManager.getInstance().getTerrain(world, p).setBiome(p, subbiome.getType());
                });
                return true;
            });
        }
        else
        {
            PokecubeCore.LOGGER.warn("Warning, world is not server world, things may break!");
            final BiomeType subbiome = BiomeType.getBiome(event.getBiomeType(), true);
            final MutableBoundingBox box = event.getBoundingBox();
            final Stream<BlockPos> poses = BlockPos.getAllInBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY,
                    box.maxZ);
            final IWorld world = event.getWorld();
            poses.forEach((p) ->
            {
                TerrainManager.getInstance().getTerrain(world, p).setBiome(p, subbiome.getType());
            });
        }
    }

    public static class GuardInfo
    {
        public String time = "";
        public int    roam = 0;
    }

    public static interface INpcProcessor
    {
        void process(final NpcMob npc, final JsonObject thing);
    }

    public static List<INpcProcessor> processors = Lists.newArrayList((npc, thing) ->
    {
        if (thing.has("name")) npc.name = thing.get("name").getAsString();
        if (thing.has("customTrades")) npc.customTrades = thing.get("customTrades").getAsString();
        if (thing.has("type")) npc.setNpcType(NpcType.byType(thing.get("type").getAsString()));
        if (thing.has("gender"))
        {
            final boolean male = thing.get("gender").getAsString().equalsIgnoreCase("male") ? true
                    : thing.get("gender").getAsString().equalsIgnoreCase("female") ? false : npc.getRNG().nextBoolean();
            npc.setMale(male);
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
        final IGuardAICapability guard = npc.getCapability(CapHolders.GUARDAI_CAP).orElse(null);
        npc.setHomePosAndDistance(npc.getPosition(), info.roam);
        if (guard != null)
        {
            TimePeriod duration = info.time.equals("allday") ? TimePeriod.fullDay : new TimePeriod(0.55, .95);
            duration = info.time.equals("day") ? new TimePeriod(0, 0.5) : duration;
            duration = info.time.equals("night") ? new TimePeriod(0.55, .95) : duration;
            guard.getPrimaryTask().setPos(npc.getPosition());
            guard.getPrimaryTask().setRoamDistance(info.roam);
            guard.getPrimaryTask().setActiveTime(duration);
        }
    });

    public static void applyFunction(final NpcMob npc, final JsonObject thing)
    {
        SpawnEventsHandler.processors.forEach(i -> i.process(npc, thing));
    }

}
