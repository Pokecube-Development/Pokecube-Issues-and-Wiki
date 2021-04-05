package pokecube.legends.spawns;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.world.IWorldTickListener;
import pokecube.core.world.WorldTickManager;
import pokecube.legends.Reference;
import pokecube.legends.entity.WormholeEntity;
import pokecube.legends.init.EntityInit;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;

public class WormholeSpawns implements IWorldTickListener
{
    public static interface IWormholeWorld extends INBTSerializable<CompoundNBT>
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
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT nbt = new CompoundNBT();
            final ListNBT list = new ListNBT();
            for (final BlockPos pos : this.getWormholes())
                list.add(NBTUtil.writeBlockPos(pos));
            nbt.put("wormholes", list);
            return nbt;
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            this.getWormholes().clear();
            final ListNBT list = nbt.getList("wormholes", 10);
            for (final INBT tag : list)
                this.getWormholes().add(NBTUtil.readBlockPos((CompoundNBT) tag));
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

    public static class Storage implements Capability.IStorage<IWormholeWorld>
    {

        @Override
        public void readNBT(final Capability<IWormholeWorld> capability, final IWormholeWorld instance,
                final Direction side, final INBT nbt)
        {
            if (nbt instanceof CompoundNBT) instance.deserializeNBT((CompoundNBT) nbt);
        }

        @Override
        public INBT writeNBT(final Capability<IWormholeWorld> capability, final IWormholeWorld instance,
                final Direction side)
        {
            return instance.serializeNBT();
        }

    }

    @CapabilityInject(IWormholeWorld.class)
    public static final Capability<IWormholeWorld> WORMHOLES_CAP = null;

    static WormholeSpawns INSTANCE = new WormholeSpawns();

    public static void init()
    {
        CapabilityManager.INSTANCE.register(IWormholeWorld.class, new Storage(), Wormholes::new);

        WorldTickManager.registerStaticData(() -> WormholeSpawns.INSTANCE, p -> true);
        MinecraftForge.EVENT_BUS.addGenericListener(World.class, WormholeSpawns::onWorldCaps);
    }

    private static void onWorldCaps(final AttachCapabilitiesEvent<World> event)
    {
        event.addCapability(new ResourceLocation(Reference.ID, "wormholes"), new Wormholes());
    }

    @Override
    public void onTickEnd(final ServerWorld world)
    {
        if (!SpawnHandler.canSpawnInWorld(world)) return;

        final IWormholeWorld holes = world.getCapability(WormholeSpawns.WORMHOLES_CAP).orElse(null);
        if (holes == null) return;

        final double rate = 0.01;
        final int distance = 64;
        final Random rand = world.getRandom();
        if (rand.nextDouble() > rate) return;

        final int wormholeSpacing = 128;

        final List<ServerPlayerEntity> players = world.players();
        if (players.isEmpty()) return;
        Collections.shuffle(players);

        final int dx = rand.nextInt(distance) - distance / 2;
        final int dz = rand.nextInt(distance) - distance / 2;
        final Vector3 v = Vector3.getNewVector().set(players.get(0)).add(dx, 0, dz);

        // Only spawn this if the nearby area is actually loaded.
        if (!TerrainManager.isAreaLoaded(world, v, 8)) return;

        v.y = world.getHeight(Type.WORLD_SURFACE, (int) v.x, (int) v.z) + 2 + rand.nextInt(10);
        if (v.y > world.getHeight()) return;

        final Vector3 pos = v.copy();

        for (final BlockPos p2 : holes.getWormholes())
            if (p2.closerThan(pos.getPos(), wormholeSpacing)) return;

        final WormholeEntity wormhole = EntityInit.WORMHOLE.get().create(world);
        pos.moveEntity(wormhole);
        holes.addWormhole(wormhole.getPos().getPos().pos());
        world.addFreshEntity(wormhole);

    }
}
