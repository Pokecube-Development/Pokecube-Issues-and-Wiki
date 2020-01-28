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
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntryLoader;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.entity.professor.EntityProfessor;
import pokecube.core.entity.professor.EntityProfessor.ProfessorType;
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
        final World world = event.world;
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

        if (event.function.startsWith("pokecube:mob:nurse"))
        {
            // Set it to air so mob can spawn.
            event.world.setBlockState(event.pos, Blocks.AIR.getDefaultState(), 2);
            final EntityProfessor nurse = EntityProfessor.TYPE.create(event.world.getWorld());
            nurse.type = ProfessorType.HEALER;
            nurse.enablePersistence();
            nurse.moveToBlockPosAndAngles(event.pos, 0.0F, 0.0F);
            nurse.onInitialSpawn(event.world, event.world.getDifficultyForLocation(event.pos), SpawnReason.STRUCTURE,
                    (ILivingEntityData) null, (CompoundNBT) null);
            final String args = event.function.replaceFirst("pokecube:mob:nurse", "");
            if (!args.isEmpty()) try
            {
                final JsonObject thing = PokedexEntryLoader.gson.fromJson(args, JsonObject.class);
                if (thing.has("name")) nurse.name = thing.get("name").getAsString();
                System.out.println(thing.get("name").getAsString());
            }
            catch (final JsonSyntaxException e)
            {
                PokecubeCore.LOGGER.error("Error parsing " + args, e);
            }
            event.world.addEntity(nurse);
        }
        else if (event.function.startsWith("pokecube:mob:professor"))
        {
            // Set it to air so mob can spawn.
            event.world.setBlockState(event.pos, Blocks.AIR.getDefaultState(), 2);
            final EntityProfessor professor = EntityProfessor.TYPE.create(event.world.getWorld());
            professor.enablePersistence();
            professor.moveToBlockPosAndAngles(event.pos, 0.0F, 0.0F);
            professor.onInitialSpawn(event.world, event.world.getDifficultyForLocation(event.pos),
                    SpawnReason.STRUCTURE, (ILivingEntityData) null, (CompoundNBT) null);
            final String args = event.function.replaceFirst("pokecube:mob:professor", "");
            if (!args.isEmpty()) try
            {
                final JsonObject thing = PokedexEntryLoader.gson.fromJson(args, JsonObject.class);
                if (thing.has("name")) professor.name = thing.get("name").getAsString();
            }
            catch (final JsonSyntaxException e)
            {
                PokecubeCore.LOGGER.error("Error parsing " + args, e);
            }
            event.world.addEntity(professor);
        }

        // if (!(event.getEntity() instanceof MobEntity)) return;
        // MobEntity v = (MobEntity) event.getEntity();
        // Vector3 pos = Vector3.getNewVector().set(v);
        // IGuardAICapability capability = null;
        // TODO apply guard ai cap properly.
        // for (Object o2 : v.tasks.taskEntries)
        // {
        // EntityAITaskEntry taskEntry = (EntityAITaskEntry) o2;
        // if (taskEntry.action instanceof GuardAI)
        // {
        // capability = ((GuardAI) taskEntry.action).capability;
        // capability.getPrimaryTask().setPos(pos.getPos());
        // break;
        // }
        // }
    }
}
