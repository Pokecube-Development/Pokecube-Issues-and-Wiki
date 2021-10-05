package pokecube.legends.spawns;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.legends.Reference;
import pokecube.legends.entity.WormholeEntity;
import pokecube.legends.init.EntityInit;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.api.world.IWorldTickListener;
import thut.api.world.WorldTickManager;
import thut.core.common.ThutCore;

public class WormholeSpawns implements IWorldTickListener
{
    public static interface IWormholeWorld extends INBTSerializable<CompoundTag>
    {
        Collection<BlockPos> getWormholes();

        void addWormhole(BlockPos pos);

        void removeWormhole(BlockPos pos);
    }

    public static class Wormholes implements IWormholeWorld, ICapabilityProvider
    {
        private final LazyOptional<IWormholeWorld> holder = LazyOptional.of(() -> this);

        Set<BlockPos> wormholes = Sets.newHashSet();

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return WormholeSpawns.WORMHOLES_CAP.orEmpty(cap, this.holder);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag nbt = new CompoundTag();
            final ListTag list = new ListTag();
            for (final BlockPos pos : this.getWormholes())
                list.add(NbtUtils.writeBlockPos(pos));
            nbt.put("wormholes", list);
            return nbt;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.getWormholes().clear();
            final ListTag list = nbt.getList("wormholes", 10);
            for (final Tag tag : list)
                this.getWormholes().add(NbtUtils.readBlockPos((CompoundTag) tag));
        }

        @Override
        public Collection<BlockPos> getWormholes()
        {
            return this.wormholes;
        }

        @Override
        public void addWormhole(final BlockPos pos)
        {
            this.wormholes.add(pos);
        }

        @Override
        public void removeWormhole(final BlockPos pos)
        {
            this.wormholes.remove(pos);
        }

    }

    @CapabilityInject(IWormholeWorld.class)
    public static final Capability<IWormholeWorld> WORMHOLES_CAP = null;

    static WormholeSpawns INSTANCE = new WormholeSpawns();

    public static double randomWormholeChance   = 0.00001;
    public static double randomWormholeDistance = 64;
    public static double randomWormholeSpacing  = 128;

    public static double teleWormholeChanceNormal = 0.01;
    public static double teleWormholeChanceWorms  = 0.75;

    public static final ResourceLocation SPACE_WORMS = new ResourceLocation(Reference.ID, "space_worm");

    public static void init()
    {
        CapabilityManager.INSTANCE.register(IWormholeWorld.class);

        WorldTickManager.registerStaticData(() -> WormholeSpawns.INSTANCE, p -> true);
        MinecraftForge.EVENT_BUS.addGenericListener(Level.class, WormholeSpawns::onWorldCaps);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, WormholeEntity::onTeleport);
    }

    private static void onWorldCaps(final AttachCapabilitiesEvent<Level> event)
    {
        event.addCapability(new ResourceLocation(Reference.ID, "wormholes"), new Wormholes());
    }

    public static BlockPos getWormholePos(final ServerLevel world, final BlockPos base)
    {
        final Random rng = ThutCore.newRandom();

        // Ensusre the chunk is loaded.
        world.getChunk(base);

        final int x = base.getX();
        final int z = base.getZ();
        final int h = world.getHeight(Types.WORLD_SURFACE, x, z);

        // If h<10 or so we need to find a new spot.

        int y = h + 10 + rng.nextInt(30);
        y = Math.min(y, world.getHeight() - 5);

        return new BlockPos(x, y, z);
    }

    @Override
    public void onTickEnd(final ServerLevel world)
    {
        if (!SpawnHandler.canSpawnInWorld(world)) return;

        final IWormholeWorld holes = world.getCapability(WormholeSpawns.WORMHOLES_CAP).orElse(null);
        if (holes == null) return;

        final double rate = WormholeSpawns.randomWormholeChance;
        final double distance = WormholeSpawns.randomWormholeDistance;
        final Random rand = world.getRandom();
        if (rand.nextDouble() > rate) return;

        final double wormholeSpacing = WormholeSpawns.randomWormholeSpacing;

        final List<ServerPlayer> players = world.players();
        if (players.isEmpty()) return;
        Collections.shuffle(players);

        final double dx = rand.nextFloat() * distance - distance / 2;
        final double dz = rand.nextFloat() * distance - distance / 2;
        final Vector3 v = Vector3.getNewVector().set(players.get(0)).add(dx, 0, dz);

        // Only spawn this if the nearby area is actually loaded.
        if (!TerrainManager.isAreaLoaded(world, v, 8)) return;

        final BlockPos p = WormholeSpawns.getWormholePos(world, v.getPos());
        final Vector3 pos = Vector3.getNewVector().set(p);

        for (final BlockPos p2 : holes.getWormholes())
            if (p2.closerThan(pos.getPos(), wormholeSpacing)) return;

        final WormholeEntity wormhole = EntityInit.WORMHOLE.get().create(world);
        pos.moveEntity(wormhole);
        holes.addWormhole(wormhole.getPos().getPos().pos());
        world.addFreshEntity(wormhole);
    }
}
