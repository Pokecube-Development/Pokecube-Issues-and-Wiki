package pokecube.core.handlers.events;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.world.IWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.npc.NpcType;
import pokecube.core.events.NpcSpawn;
import pokecube.core.events.StructureEvent;
import pokecube.core.events.pokemob.SpawnEvent;
import thut.api.maths.Vector3;

public class SpawnEventsHandler
{

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void CapLevel(final SpawnEvent.Level event)
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
    @SubscribeEvent
    public static void onSpawnCheck(final SpawnEvent.Check event)
    {
        if (!SpawnHandler.canSpawnInWorld(event.world)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void PickSpawn(final SpawnEvent.Pick.Pre event)
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
        if (dbe.legendary)
        {
            final int level = SpawnHandler.getSpawnLevel(world, v, dbe);
            if (level < PokecubeCore.getConfig().minLegendLevel) return;
        }
        event.setLocation(v);
        event.setPick(dbe);
    }

    @SubscribeEvent
    public static void StructureSpawn(final StructureEvent.ReadTag event)
    {
        if (event.function.startsWith("pokecube:mob:"))
        {
            final String function = event.function.replaceFirst("pokecube:mob:", "");
            final boolean nurse = function.startsWith("nurse");
            final boolean professor = function.startsWith("professor");
            final boolean trader = function.startsWith("trader");
            if (nurse || professor || trader)
            {
                // Set it to air so mob can spawn.
                event.world.setBlockState(event.pos, Blocks.AIR.getDefaultState(), 2);
                final NpcMob mob = NpcMob.TYPE.create(event.world.getWorld());
                mob.setNpcType(nurse ? NpcType.HEALER : trader ? NpcType.TRADER : NpcType.PROFESSOR);
                if (nurse) mob.setMale(false);
                mob.enablePersistence();
                mob.moveToBlockPosAndAngles(event.pos, 0.0F, 0.0F);
                mob.onInitialSpawn(event.world, event.world.getDifficultyForLocation(event.pos), SpawnReason.STRUCTURE,
                        (ILivingEntityData) null, (CompoundNBT) null);
                final String args = function.replaceFirst(nurse ? "nurse" : "professor", "");
                if (!args.isEmpty() && args.contains("{")) try
                {
                    final JsonObject thing = PokedexEntryLoader.gson.fromJson(args, JsonObject.class);
                    SpawnEventsHandler.applyFunction(mob, thing);
                }
                catch (final JsonSyntaxException e)
                {
                    PokecubeCore.LOGGER.error("Error parsing " + args, e);
                }
                if (!MinecraftForge.EVENT_BUS.post(new NpcSpawn(mob, event.pos, event.world))) event.world.addEntity(
                        mob);
            }
            else if (function.startsWith("pokemob"))
            {

            }
        }
        else if (event.function.startsWith("pokecube:worldspawn"))
        {
            // Set it to air so player can spawn here.
            event.world.setBlockState(event.pos, Blocks.AIR.getDefaultState(), 2);
            event.world.getWorld().setSpawnPoint(event.pos);
            PokecubeCore.LOGGER.debug("Setting World Spawn to {}", event.pos);
        }
    }

    private static void applyFunction(final NpcMob prof, final JsonObject thing)
    {
        if (thing.has("name")) prof.name = thing.get("name").getAsString();
    }

}
