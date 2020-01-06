package pokecube.adventures.entity.trainer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.nfunk.jep.JEP;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.adventures.Config;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.events.TrainerSpawnEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.database.SpawnBiomeMatcher.SpawnCheck;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.utils.ChunkCoordinate;
import thut.api.maths.Vector3;

@Mod.EventBusSubscriber
public class TrainerSpawnHandler
{
    public static Map<UUID, ChunkCoordinate> trainerMap = Maps.newConcurrentMap();
    private static Vector3                   vec1       = Vector3.getNewVector();
    private static Vector3                   vec2       = Vector3.getNewVector();

    static Vector3 v = Vector3.getNewVector(), v1 = Vector3.getNewVector(), v2 = Vector3.getNewVector();

    static JEP parser = new JEP();

    /**
     * Adds or updates the location of the trainer.
     *
     * @param e
     * @return
     */
    public static void addTrainerCoord(final Entity e)
    {
        final int x = (int) e.posX / 16;
        final int y = (int) e.posY / 16;
        final int z = (int) e.posZ / 16;
        final int dim = e.dimension.getId();
        final ChunkCoordinate coord = new ChunkCoordinate(x, y, z, dim);
        TrainerSpawnHandler.trainerMap.put(e.getUniqueID(), coord);
    }

    public static int countTrainersInArea(final World world, final int chunkPosX, final int chunkPosY,
            final int chunkPosZ, final int trainerBox)
    {
        final int tolerance = trainerBox / 16;
        int ret = 0;
        for (final ChunkCoordinate o : TrainerSpawnHandler.trainerMap.values())
        {
            final ChunkCoordinate coord = o;
            if (chunkPosX >= coord.getX() - tolerance && chunkPosZ >= coord.getZ() - tolerance && chunkPosY >= coord
                    .getY() - tolerance && chunkPosY <= coord.getY() + tolerance && chunkPosX <= coord.getX()
                            + tolerance && chunkPosZ <= coord.getZ() + tolerance && world.getDimension().getType()
                                    .getId() == coord.dim) ret++;
        }
        return ret;
    }

    public static int countTrainersNear(final Entity e, final int trainerBox)
    {
        final int x = (int) e.posX / 16;
        final int y = (int) e.posY / 16;
        final int z = (int) e.posZ / 16;
        return TrainerSpawnHandler.countTrainersInArea(e.getEntityWorld(), x, y, z, trainerBox);
    }

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
        if (!world.isAreaLoaded(v.getPos(), 8)) return null;

        // Find surface
        final Vector3 temp1 = Vector3.getNextSurfacePoint(world, TrainerSpawnHandler.vec1, TrainerSpawnHandler.vec2.set(
                Direction.DOWN), 10);

        if (temp1 != null)
        {
            temp1.y++;
            // Check for headroom
            if (!temp1.addTo(0, 1, 0).isClearOfBlocks(world)) return null;
            temp1.y--;
            return temp1;
        }
        return null;
    }

    public static EntityTrainer getTrainer(Vector3 v, final World w)
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
        final EntityTrainer trainer = new EntityTrainer(EntityTrainer.TYPE, w).setType(ttype).setLevel(level);
        trainer.aiStates.setAIState(IHasNPCAIStates.MATES, true);
        trainer.aiStates.setAIState(IHasNPCAIStates.TRADES, true);
        return trainer;
    }

    public static void randomizeTrainerTeam(final Entity trainer, final IHasPokemobs mobs)
    {
        final Vector3 loc = Vector3.getNewVector().set(trainer);
        // Set level based on what wild pokemobs have.
        final int level = SpawnHandler.getSpawnLevel(trainer.getEntityWorld(), loc, Pokedex.getInstance()
                .getFirstEntry());
        // TODO add leaders and handle them.
        // if (trainer instanceof EntityLeader)
        // {
        // // Gym leaders are 10 lvls higher than others.
        // level += 10;
        // // Randomize badge for leader.
        // if (((EntityLeader) trainer).randomBadge())
        // {
        // final IHasRewards rewardsCap = ((EntityLeader) trainer).rewardsCap;
        // final PokeType type = PokeType.values()[new
        // Random().nextInt(PokeType.values().length)];
        // final Item item = Item.getByNameOrId(PokecubeAdv.ID + ":badge_" +
        // type);
        // if (item != null)
        // {
        // final ItemStack badge = new ItemStack(item);
        // if (!rewardsCap.getRewards().isEmpty())
        // rewardsCap.getRewards().set(0, new Reward(badge));
        // else rewardsCap.getRewards().add(new Reward(badge));
        // ((EntityLeader) trainer).setHeldItem(Hand.OFF_HAND,
        // rewardsCap.getRewards().get(0).stack);
        // }
        // }
        // }
        // Randomize team.
        if (trainer instanceof EntityTrainer)
        {
            final EntityTrainer t = (EntityTrainer) trainer;
            t.name = "";
            // Reset their trades, as this will randomize them when trades are
            // needed later.
            t.resetTrades();
            // Init for trainers randomizes their teams
            if (mobs.getType() != null) t.setType(mobs.getType()).setLevel(level);
        }
        else if (mobs.getType() != null)
        {
            mobs.setType(mobs.getType());
            final byte genders = mobs.getType().genders;
            if (genders == 1) mobs.setGender((byte) 1);
            if (genders == 2) mobs.setGender((byte) 2);
            if (genders == 3) mobs.setGender((byte) (Math.random() < 0.5 ? 1 : 2));
            TypeTrainer.getRandomTeam(mobs, (LivingEntity) trainer, level, trainer.getEntityWorld());
        }
    }

    public static void removeTrainer(final Entity e)
    {
        TrainerSpawnHandler.trainerMap.remove(e.getUniqueID());
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
        if (v.y < 0) v.y = v.getMaxY(w);
        final Vector3 temp = Vector3.getNextSurfacePoint(w, v, Vector3.secondAxisNeg, 20);
        v = temp != null ? temp.offset(Direction.UP) : v;

        if (!SpawnHandler.checkNoSpawnerInArea(w, v.intX(), v.intY(), v.intZ())) return;
        final int count = TrainerSpawnHandler.countTrainersInArea(w, v.intX() / 16, v.intY() / 16, v.intZ() / 16,
                Config.instance.trainerBox);

        if (count < Config.instance.trainerDensity)
        {
            final long time = System.nanoTime();
            final EntityTrainer t = TrainerSpawnHandler.getTrainer(v, w);
            if (t == null) return;
            final IHasPokemobs cap = CapabilityHasPokemobs.getHasPokemobs(t);
            final TrainerSpawnEvent event = new TrainerSpawnEvent(cap.getType(), t, v.getPos(), w);
            if (MinecraftForge.EVENT_BUS.post(event))
            {
                t.remove();
                return;
            }
            final double dt = (System.nanoTime() - time) / 1000000D;
            if (dt > 20) PokecubeCore.LOGGER.warn("Trainer " + cap.getType().name + " " + dt + "ms ");
            v.offsetBy(Direction.UP).moveEntity(t);
            if (t.pokemobsCap.countPokemon() > 0 && SpawnHandler.checkNoSpawnerInArea(w, (int) t.posX, (int) t.posY,
                    (int) t.posZ))
            {
                w.addEntity(t);
                TrainerSpawnHandler.addTrainerCoord(t);
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
}
