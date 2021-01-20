package pokecube.core.ai.tasks.idle.ants;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.pathfinding.Path;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.idle.ants.nest.CheckNest;
import pokecube.core.ai.tasks.idle.ants.nest.EnterNest;
import pokecube.core.ai.tasks.idle.ants.nest.MakeNest;
import pokecube.core.ai.tasks.idle.ants.sensors.EggSensor;
import pokecube.core.ai.tasks.idle.ants.sensors.GatherSensor;
import pokecube.core.ai.tasks.idle.ants.sensors.NestSensor;
import pokecube.core.ai.tasks.idle.ants.sensors.NestSensor.AntNest;
import pokecube.core.ai.tasks.idle.ants.sensors.ThreatSensor;
import pokecube.core.ai.tasks.idle.ants.work.Build;
import pokecube.core.ai.tasks.idle.ants.work.CarryEgg;
import pokecube.core.ai.tasks.idle.ants.work.Dig;
import pokecube.core.ai.tasks.idle.ants.work.Gather;
import pokecube.core.ai.tasks.idle.ants.work.Guard;
import pokecube.core.ai.tasks.idle.ants.work.Idle;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IInhabitor;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityInhabitable;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.api.entity.ai.IAIRunnable;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

public class AntTasks
{
    public static enum AntJob
    {
        NONE, GUARD, GATHER, DIG, BUILD;
    }

    public static enum AntRoom
    {
        EGG, FOOD, NODE, ENTRANCE;
    }

    public static final MemoryModuleType<GlobalPos> NEST_POS = MemoryModules.NEST_POS;
    public static final MemoryModuleType<GlobalPos> WORK_POS = MemoryModules.WORK_POS;

    public static final MemoryModuleType<Integer> OUT_OF_HIVE_TIMER = MemoryModules.OUT_OF_NEST_TIMER;
    public static final MemoryModuleType<Integer> NO_HIVE_TIMER     = MemoryModules.NO_NEST_TIMER;
    public static final MemoryModuleType<Integer> NO_WORK_TIME      = MemoryModules.NO_WORK_TIMER;

    public static final MemoryModuleType<EntityPokemobEgg> EGG = new MemoryModuleType<>(Optional.empty());

    public static final MemoryModuleType<Byte> JOB_TYPE = new MemoryModuleType<>(Optional.of(Codec.BYTE));

    public static final MemoryModuleType<CompoundNBT> JOB_INFO = new MemoryModuleType<>(Optional.of(CompoundNBT.CODEC));

    public static final MemoryModuleType<Boolean> GOING_HOME = new MemoryModuleType<>(Optional.empty());

    public static final SensorType<NestSensor>   NEST_SENSOR   = new SensorType<>(NestSensor::new);
    public static final SensorType<GatherSensor> WORK_SENSOR   = new SensorType<>(GatherSensor::new);
    public static final SensorType<ThreatSensor> THREAT_SENSOR = new SensorType<>(ThreatSensor::new);
    public static final SensorType<EggSensor>    EGG_SENSOR    = new SensorType<>(EggSensor::new);

    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(AntTasks.NEST_POS,
            AntTasks.WORK_POS, AntTasks.OUT_OF_HIVE_TIMER, AntTasks.NO_WORK_TIME, AntTasks.NO_HIVE_TIMER,
            AntTasks.JOB_TYPE, AntTasks.JOB_INFO, AntTasks.EGG, AntTasks.GOING_HOME);

    public static final List<SensorType<?>> SENSOR_TYPES = ImmutableList.of(AntTasks.NEST_SENSOR, AntTasks.WORK_SENSOR,
            AntTasks.EGG_SENSOR, AntTasks.THREAT_SENSOR, Sensors.VISIBLE_BLOCKS, Sensors.INTERESTING_ENTITIES);

    public static final ResourceLocation NESTLOC = new ResourceLocation(PokecubeCore.MODID, "ant_nest");

    public static void registerMems(final Register<MemoryModuleType<?>> event)
    {
        event.getRegistry().register(AntTasks.JOB_TYPE.setRegistryName(PokecubeCore.MODID, "ant_job_type"));
        event.getRegistry().register(AntTasks.JOB_INFO.setRegistryName(PokecubeCore.MODID, "ant_job_info"));
        event.getRegistry().register(AntTasks.GOING_HOME.setRegistryName(PokecubeCore.MODID, "ant_go_home"));
        event.getRegistry().register(AntTasks.EGG.setRegistryName(PokecubeCore.MODID, "ant_egg"));
    }

    public static void registerSensors(final Register<SensorType<?>> event)
    {
        event.getRegistry().register(AntTasks.NEST_SENSOR.setRegistryName(PokecubeCore.MODID, "ant_nests"));
        event.getRegistry().register(AntTasks.WORK_SENSOR.setRegistryName(PokecubeCore.MODID, "ant_work"));
        event.getRegistry().register(AntTasks.THREAT_SENSOR.setRegistryName(PokecubeCore.MODID, "ant_threat"));
        event.getRegistry().register(AntTasks.EGG_SENSOR.setRegistryName(PokecubeCore.MODID, "ant_eggs"));

        CapabilityInhabitable.Register(AntTasks.NESTLOC, () -> new AntHabitat());
    }

    public static void addAntIdleTasks(final IPokemob pokemob, final List<IAIRunnable> list)
    {
        if (!AIRoutine.ANTAI.isAllowed(pokemob)) return;

        list.add(new CheckNest(pokemob).setPriority(200));
        list.add(new MakeNest(pokemob));
        list.add(new EnterNest(pokemob).setPriority(0));
        list.add(new CarryEgg(pokemob).setPriority(0));
        list.add(new Guard(pokemob).setPriority(1));
        list.add(new Gather(pokemob).setPriority(2));
        list.add(new Build(pokemob).setPriority(2));
        list.add(new Dig(pokemob).setPriority(3));
        list.add(new Idle(pokemob).setPriority(4));

        BrainUtils.addToBrain(pokemob.getEntity().getBrain(), AntTasks.MEMORY_TYPES, AntTasks.SENSOR_TYPES);
    }

    public static boolean isValidAnt(final Entity entity)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null) return false;
        return pokemob.isRoutineEnabled(AIRoutine.ANTAI);
    }

    public static AntJob getJob(final MobEntity ant)
    {
        byte index = 0;
        if (ant.getBrain().hasMemory(AntTasks.JOB_TYPE)) index = ant.getBrain().getMemory(AntTasks.JOB_TYPE).get();
        final AntJob job = AntJob.values()[index];
        return job;
    }

    public static void setJob(final MobEntity ant, final AntJob job)
    {
        ant.getBrain().setMemory(AntTasks.JOB_TYPE, (byte) job.ordinal());
    }

    public static boolean shouldAntBeInNest(final ServerWorld world, final BlockPos pos)
    {
        return !world.isDaytime() || world.isRainingAt(pos);
    }

    public static class AntInhabitor implements IInhabitor
    {
        final MobEntity ant;

        public AntJob job = AntJob.NONE;

        public AntInhabitor(final MobEntity ant)
        {
            this.ant = ant;
        }

        @Override
        public GlobalPos getHome()
        {
            final Brain<?> brain = this.ant.getBrain();
            if (!brain.hasMemory(AntTasks.NEST_POS)) return null;
            return brain.getMemory(AntTasks.NEST_POS).get();
        }

        @Override
        public void onExitHabitat()
        {

        }

        @Override
        public GlobalPos getWorkSite()
        {
            final Brain<?> brain = this.ant.getBrain();
            if (!brain.hasMemory(AntTasks.WORK_POS)) return null;
            return brain.getMemory(AntTasks.WORK_POS).get();
        }

        @Override
        public void setWorldSite(final GlobalPos site)
        {
            final Brain<?> brain = this.ant.getBrain();
            if (site == null) brain.removeMemory(AntTasks.WORK_POS);
            else brain.setMemory(AntTasks.WORK_POS, site);
        }
    }

    public static class AntHabitat implements IInhabitable, INBTSerializable<CompoundNBT>
    {
        public static abstract class Part implements INBTSerializable<CompoundNBT>
        {
            // Persistant value to track if we have started being mined.
            public boolean started    = false;
            public long    dig_done   = 0;
            public long    build_done = 0;

            // If present, when loading this map will be used to sync the nodes
            // on the edges.
            Map<BlockPos, Node> _loadedNodes = null;

            @Override
            public CompoundNBT serializeNBT()
            {
                final CompoundNBT nbt = new CompoundNBT();
                nbt.putBoolean("s", this.started);
                nbt.putLong("dd", this.dig_done);
//                nbt.putLong("bd", this.build_done);
                return nbt;
            }

            @Override
            public void deserializeNBT(final CompoundNBT nbt)
            {
                this.started = nbt.getBoolean("s");
                this.dig_done = nbt.getLong("dd");
//                this.build_done = nbt.getLong("bd");
            }

            public boolean shouldDig(final long worldTime)
            {
                return this.started && this.dig_done < worldTime;
            }

            public boolean shouldBuild(final long worldTime)
            {
                return this.started && this.build_done < worldTime;
            }

            public abstract boolean isInside(BlockPos pos);

            public abstract boolean isOnShell(BlockPos pos);

        }

        public static class Edge extends Part
        {
            public Node node1;
            public Node node2;

            public BlockPos end1;
            public BlockPos end2;

            // Fraction of the way dug from end1 to end2.
            public double digged = 0;

            boolean areSame(final Edge other)
            {
                return this.end1.equals(other.end1) && this.end2.equals(other.end2);
            }

            private boolean isOn(final BlockPos pos, final double size)
            {
                final AxisAlignedBB box = new AxisAlignedBB(this.end1, this.end2);
                if (!box.grow(1.5).contains(pos.getX(), pos.getY(), pos.getZ())) return false;
                final Vector3d e1 = new Vector3d(this.end1.getX() + 0.5, this.end1.getY() + 0.5, this.end1.getZ()
                        + 0.5);
                final Vector3d e2 = new Vector3d(this.end2.getX() + 0.5, this.end2.getY() + 0.5, this.end2.getZ()
                        + 0.5);
                final Vector3d e3 = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                Vector3d n = e2.subtract(e1).normalize();
                final Vector3d diff = e1.subtract(e3);
                n = n.crossProduct(diff);
                return n.length() < size;
            }

            @Override
            public boolean equals(final Object obj)
            {
                if (!(obj instanceof Edge)) return false;
                return this.areSame((Edge) obj);
            }

            @Override
            public CompoundNBT serializeNBT()
            {
                final CompoundNBT edgeNbt = super.serializeNBT();
                edgeNbt.put("n1", NBTUtil.writeBlockPos(this.node1.getCenter()));
                edgeNbt.put("e1", NBTUtil.writeBlockPos(this.end1));
                edgeNbt.put("n2", NBTUtil.writeBlockPos(this.node2.getCenter()));
                edgeNbt.put("e2", NBTUtil.writeBlockPos(this.end2));
                return edgeNbt;
            }

            @Override
            public void deserializeNBT(final CompoundNBT nbt)
            {
                super.deserializeNBT(nbt);
                this.end1 = NBTUtil.readBlockPos(nbt.getCompound("e1"));
                this.end2 = NBTUtil.readBlockPos(nbt.getCompound("e2"));

                final BlockPos p1 = NBTUtil.readBlockPos(nbt.getCompound("n1"));
                final BlockPos p2 = NBTUtil.readBlockPos(nbt.getCompound("n2"));

                // The following 4 lines ensure that the nodes are the
                // correctly loaded ones.
                if (this._loadedNodes != null)
                {
                    this.node1 = this._loadedNodes.getOrDefault(p1, new Node(p1));
                    this.node2 = this._loadedNodes.getOrDefault(p2, new Node(p2));
                }
                else
                {
                    this.node1 = new Node(p1);
                    this.node2 = new Node(p2);
                }
            }

            @Override
            public boolean isInside(final BlockPos pos)
            {
                return this.isOn(pos, 1.5);
            }

            @Override
            public boolean isOnShell(final BlockPos pos)
            {
                return this.isOn(pos, 4) && !this.isOn(pos, 1.5);
            }
        }

        public static class Node extends Part
        {
            public AntRoom type = AntRoom.NODE;

            private BlockPos center;

            public Vector3d mid;

            public List<Edge> edges = Lists.newArrayList();

            public int depth = 0;

            // Non persistent set of open blocks
            public Set<BlockPos> dug = Sets.newHashSet();

            public Node()
            {
                this.setCenter(BlockPos.ZERO);
            }

            public Node(final BlockPos center)
            {
                this.setCenter(center);
            }

            @Override
            public boolean isInside(final BlockPos pos)
            {
                for (final Edge e : this.edges)
                    if (e.isInside(pos)) return true;
                if (this._loadedNodes != null) for (final Node n : this._loadedNodes.values())
                {
                    if (n == this) continue;
                    n._loadedNodes = null;
                    final boolean node = n.isInside(pos);
                    n._loadedNodes = this._loadedNodes;
                    if (node) return true;
                }
                final Vector3d x0 = this.mid;
                Vector3d x = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).subtract(x0);
                x = x.mul(1, 1.5, 1);
                final double r = x.length();
                // Some basic limits first
                if (r > 3 || x.y < 0) return false;
                return true;
            }

            @Override
            public boolean isOnShell(final BlockPos pos)
            {
                // Check main spot first.
                if (this.isInside(pos)) return false;

                // Alternate
                for (final Edge e : this.edges)
                    if (e.isOnShell(pos)) return true;

                final Vector3d x0 = this.mid;
                Vector3d x = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).subtract(x0);
                x = x.mul(1, 1.5, 1);
                final double r = x.length();
                return r < 6 && x.y > -2;
            }

            @Override
            public CompoundNBT serializeNBT()
            {
                final CompoundNBT nbt = super.serializeNBT();
                nbt.putInt("X", this.getCenter().getX());
                nbt.putInt("Y", this.getCenter().getY());
                nbt.putInt("Z", this.getCenter().getZ());
                nbt.putString("room", this.type.name());
                nbt.putInt("d", this.depth);
                final ListNBT edges = new ListNBT();
                this.edges.removeIf(edge ->
                {
                    CompoundNBT edgeNbt;
                    try
                    {
                        edgeNbt = edge.serializeNBT();
                        edges.add(edgeNbt);
                        return false;
                    }
                    catch (final Exception e)
                    {
                        e.printStackTrace();
                        PokecubeCore.LOGGER.error("Error saving an edge!");
                        return true;
                    }
                });
                nbt.put("edges", edges);
                return nbt;
            }

            @Override
            public void deserializeNBT(final CompoundNBT nbt)
            {
                super.deserializeNBT(nbt);
                final BlockPos pos = NBTUtil.readBlockPos(nbt);
                Node n = this;
                this.setCenter(pos);
                // This is a "real" node, it will be added to the maps
                if (this._loadedNodes != null)
                {
                    n = this._loadedNodes.getOrDefault(pos, this);
                    this._loadedNodes.put(pos, n);
                    n.setCenter(pos);
                    if (n != this)
                    {
                        n.deserializeNBT(nbt);
                        return;
                    }
                }
                n.type = AntRoom.valueOf(nbt.getString("room"));
                final ListNBT edges = nbt.getList("edges", 10);
                for (int j = 0; j < edges.size(); ++j)
                {
                    final CompoundNBT edgeNbt = edges.getCompound(j);
                    final Edge e = new Edge();
                    try
                    {
                        e._loadedNodes = this._loadedNodes;
                        e.deserializeNBT(edgeNbt);
                        if (!(e.end1.equals(e.end2) || n.edges.contains(e))) n.edges.add(e);
                    }
                    catch (final Exception e1)
                    {
                        e1.printStackTrace();
                        PokecubeCore.LOGGER.error("Error loading an edge!");
                    }
                }
            }

            public BlockPos getCenter()
            {
                return this.center;
            }

            public void setCenter(final BlockPos center)
            {
                this.center = center;
                this.mid = new Vector3d(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
            }
        }

        public class Tree implements INBTSerializable<CompoundNBT>
        {
            public Map<AntRoom, List<Node>> rooms = Maps.newHashMap();

            public Map<BlockPos, Node> roomMap = Maps.newHashMap();

            // This list has order of when the rooms were added.
            public List<Node> allRooms = Lists.newArrayList();
            public Set<Edge>  allEdges = Sets.newHashSet();

            public Tree()
            {
                for (final AntRoom room : AntRoom.values())
                    this.rooms.put(room, Lists.newArrayList());
            }

            public List<Node> get(final AntRoom type)
            {
                return this.rooms.get(type);
            }

            public Path getPath(final BlockPos from, final BlockPos to, final ServerWorld world, final MobEntity mob)
            {
                final Path p = mob.getNavigator().getPathToPos(to, 0);
                if (p.reachesTarget()) return p;

                return null;
            }

            @Override
            public CompoundNBT serializeNBT()
            {
                final CompoundNBT tag = new CompoundNBT();
                final ListNBT list = new ListNBT();
                this.allRooms.forEach(n ->
                {
                    final CompoundNBT nbt = n.serializeNBT();
                    list.add(nbt);
                });
                tag.put("map", list);
                return tag;
            }

            @Override
            public void deserializeNBT(final CompoundNBT tag)
            {
                for (final AntRoom room : AntRoom.values())
                    this.rooms.put(room, Lists.newArrayList());
                final Map<BlockPos, Node> loadedNodes = Maps.newHashMap();
                this.allRooms.clear();
                this.roomMap.clear();
                this.allEdges.clear();

                // First we de-serialize the nodes, and stuff them in
                // loadedNodes, After we will need to sort through this, and
                // re-connect the edges accordingly
                final ListNBT list = tag.getList("map", 10);
                for (int i = 0; i < list.size(); ++i)
                {
                    final CompoundNBT nbt = list.getCompound(i);
                    Node n = new Node();
                    n._loadedNodes = loadedNodes;
                    n.deserializeNBT(nbt);
                    n = loadedNodes.get(n.getCenter());
                    this.add(n);
                }

                // Now we need to re-build the tree from the loaded nodes. Edges
                // need to be replaced such that nodes share the same edges.
                this.allRooms.forEach(n ->
                {
                    n.edges.forEach(edge ->
                    {
                        this.allEdges.add(edge);
                        final Node n1 = edge.node1;
                        final Node n2 = edge.node2;
                        if (n1 != n)
                        {
                            final AtomicBoolean had = new AtomicBoolean(false);
                            n1.edges.replaceAll(e ->
                            {
                                if (edge.areSame(e))
                                {
                                    had.set(true);
                                    return edge;
                                }
                                return e;
                            });
                            if (!had.get()) n1.edges.add(edge);
                        }
                        if (n2 != n)
                        {
                            final AtomicBoolean had = new AtomicBoolean(false);
                            n2.edges.replaceAll(e ->
                            {
                                if (edge.areSame(e))
                                {
                                    had.set(true);
                                    return edge;
                                }
                                return e;
                            });
                            if (!had.get()) n2.edges.add(edge);
                        }
                    });
                });
            }

            public void add(final Node node)
            {
                this.rooms.get(node.type).add(node);
                this.allRooms.add(node);
                this.roomMap.put(node.getCenter(), node);
            }
        }

        final List<Ant> ants_in = Lists.newArrayList();

        final Map<AntJob, Set<UUID>> workers = Maps.newHashMap();

        public Tree rooms = new Tree();

        public Set<UUID> eggs = Sets.newHashSet();

        public List<NearBlock>  blocks = Lists.newArrayList();
        public List<ItemEntity> items  = Lists.newArrayList();

        BlockPos    here;
        ServerWorld world;

        int antExitCooldown = 0;

        // This list gets shuffled every so often, so the order is not constant!
        public List<Node> allRooms = Lists.newArrayList();

        boolean hasItems = false;

        public AntHabitat()
        {
            for (final AntJob job : AntJob.values())
                this.workers.put(job, Sets.newHashSet());
        }

        private AntJob getNextJob(final AntJob oldJob)
        {
            int low = Integer.MAX_VALUE;
            AntJob least = AntJob.NONE;
            int ready = 0;
            for (final Node n : this.rooms.allRooms)
                if (n.started) ready++;
            for (final AntJob job : AntJob.values())
            {
                if (ready < this.rooms.allRooms.size() && job != AntJob.DIG && job != AntJob.BUILD) continue;
                final int num = this.workers.get(job).size();
                if (low > num)
                {
                    least = job;
                    low = num;
                }
            }
            return least;
        }

        public List<Node> getRooms(final AntRoom type)
        {
            return this.rooms.get(type);
        }

        public void addRandomNode(final AntRoom type)
        {
            if (type == AntRoom.ENTRANCE) return;
            if (type == AntRoom.FOOD && this.getRooms(AntRoom.FOOD).size() > 3) return;
            final List<Node> nodes = this.rooms.allRooms;
            // This is only empty if we have not ticked yet.
            if (nodes.isEmpty()) return;

            final Node entrance = this.getRooms(AntRoom.ENTRANCE).get(0);

            final Random rng = new Random();
            final int index = rng.nextInt(nodes.size());
            final Node root = nodes.get(index);

            if (root.edges.size() > 2) return;

            final Vector3 centroid = Vector3.getNewVector();
            for (final Node n : nodes)
                if (n.type == AntRoom.ENTRANCE)
                {
                    centroid.set(n.getCenter());
                    break;
                }

            final int r = 12;
            final int x = rng.nextInt(r * 2) - r;
            final int z = rng.nextInt(r * 2) - r;

            final int dx = x;
            final int dz = z;
            final double ds = Math.sqrt(dx * dx + dz * dz);
            int dy = (int) (ds / (1.0 + rng.nextDouble()));

            if (dy > 4 && root.depth < 2) return;
            if (dy < 4 && root.depth == 0) return;

            final int s = root.depth > 2 ? rng.nextBoolean() ? -1 : 1 : -1;
            dy *= s;

            final BlockPos edgeShift = new BlockPos(2.5 * dx / ds, 0, 2.5 * dz / ds);

            final BlockPos end1 = root.getCenter().add(edgeShift);
            final BlockPos end2 = end1.add(new BlockPos(dx, dy, dz));

            final Vector3 vec2 = Vector3.getNewVector().set(end1.subtract(end2));
            vec2.y = 0;

            for (final Edge e : root.edges)
            {
                // Check how parallel horizontally this it to an existing edge,
                // if too much, skip.
                final Vector3 vec1 = Vector3.getNewVector().set(e.end1.subtract(e.end2));
                if (e.node2 == root) vec1.set(e.end2.subtract(e.end1));
                vec1.y = 0;
                if (vec1.norm().dot(vec2.norm()) > 0.7) return;
            }
            final BlockPos nodePos = end2.add(edgeShift);

            vec2.set(nodePos);
            centroid.y = 0;
            vec2.y = 0;
            if (centroid.distanceTo(vec2) > r) return;

            for (final Node n : nodes)
                if (nodePos.distanceSq(n.getCenter()) < 64) return;

            if (nodePos.getY() > entrance.getCenter().getY() - 2) return;

            final Node room = new Node(nodePos);
            room.type = type;
            room.depth = root.depth + 1;

            final Edge edge = new Edge();

            // These ends are shifted up one so they are centred on the wall of
            // the room, rather than the floor
            edge.end1 = end1.up();
            edge.node1 = root;
            edge.end2 = end2.up();
            edge.node2 = room;

            // Now check if the newly made edge gets too close to any existing
            // rooms, if so, cancel it.
            for (final Node n : nodes)
            {
                if (n == root) continue;
                if (edge.isOn(n.getCenter(), 3)) return;
            }

            if (root.started) edge.started = true;

            room.edges.add(edge);
            root.edges.add(edge);

            this.rooms.add(room);
            this.rooms.allEdges.add(edge);
        }

        public Optional<BlockPos> getFreeEggRoom()
        {
            if (this.world != null)
            {
                final List<Node> eggRooms = this.getRooms(AntRoom.EGG);
                if (eggRooms.isEmpty()) this.addRandomNode(AntRoom.EGG);
                // Here we should check if the room has too many eggs in it,
                // for now just do this though.
                if (!eggRooms.isEmpty())
                {
                    final List<Node> rooms = Lists.newArrayList(eggRooms);
                    Collections.shuffle(rooms);
                    for (final Node n : rooms)
                        if (n.started) return Optional.of(n.getCenter());
                }
            }
            return Optional.empty();
        }

        @Override
        public void onTick(final BlockPos pos, final ServerWorld world)
        {
            this.here = pos;
            this.world = world;
            // Here we need to release ants every so often as needed
            this.ants_in.removeIf(ant -> this.tryReleaseAnt(ant, world));

            if (this.rooms.get(AntRoom.ENTRANCE).isEmpty())
            {
                final Node entrance = new Node(this.here);
                entrance.type = AntRoom.ENTRANCE;
                entrance.started = true;
                entrance.depth = 0;
                this.rooms.add(entrance);
            }

            int ants = this.ants_in.size();
            for (final Set<UUID> s : this.workers.values())
                ants += s.size();
            final int num = ants;

            final int roomCount = this.rooms.allRooms.size();
            final Random rng = new Random();
            if (roomCount < 10) this.addRandomNode(AntRoom.values()[rng.nextInt(3)]);

            for (final Node n : this.rooms.allRooms)
            {
                if (!n.started) continue;
                if (n.type != AntRoom.ENTRANCE)
                {
                    if (!world.isAirBlock(n.getCenter())) continue;
                    this.world.setBlockState(n.getCenter(), Blocks.OAK_SIGN.getDefaultState());
                    final TileEntity tile = this.world.getTileEntity(n.getCenter());
                    if (tile instanceof SignTileEntity) ((SignTileEntity) tile).setText(0, new StringTextComponent(
                            n.type + " " + this.rooms.allRooms.indexOf(n)));
                }
            }

            if (world.getGameTime() % 200 == 0)
            {
                final TileEntity nest = world.getTileEntity(pos);
                this.hasItems = false;
                if (nest != null)
                {
                    final IItemHandler handler = nest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                            .orElse(null);
                    for (int i = 0; i < handler.getSlots(); i++)
                    {
                        final ItemStack stack = handler.getStackInSlot(i);
                        if (!stack.isEmpty() && stack.getItem() instanceof BlockItem)
                        {
                            final BlockItem item = (BlockItem) stack.getItem();
                            if (!item.getBlock().getDefaultState().isSolid()) continue;
                            this.hasItems = true;
                            break;
                        }
                    }
                }
                if (world.getGameTime() % 12000 == 0)
                {
                    for (final Node n : this.allRooms)
                        if (n.type == AntRoom.EGG) n.type = AntRoom.NODE;
                    int i = 0;
                    Collections.shuffle(this.allRooms);
                    for (final Node n : this.allRooms)
                        if (n.type == AntRoom.NODE && i++ < 3) n.type = AntRoom.EGG;
                    this.rooms.deserializeNBT(this.rooms.serializeNBT());
                }
            }

            // Workers should only contain actual live ants! so if they are not
            // found here, remove them from the list
            this.workers.forEach((j, s) -> s.removeIf(uuid ->
            {
                final Entity mob = world.getEntityByUuid(uuid);
                if (AntTasks.isValidAnt(mob))
                {
                    // If we are a valid ant, ensure it has a job to do.
                    if (world.getRandom().nextInt(50) == 0) this.assignJob(j, (MobEntity) mob);
                    return false;
                }
                return true;
            }));
            // Lets make the eggs not hatch for now, if we are about say 5 ants,
            // This also removes hatched/removed eggs
            this.eggs.removeIf(uuid ->
            {
                final Entity mob = world.getEntityByUuid(uuid);
                if (!(mob instanceof EntityPokemobEgg) || !mob.isAddedToWorld()) return true;
                final EntityPokemobEgg egg = (EntityPokemobEgg) mob;
                if (num > 10) egg.setGrowingAge(-20);
                else if (egg.getGrowingAge() < -20) egg.setGrowingAge(-20);
                return false;
            });
        }

        private void assignJob(final AntJob j, final MobEntity mob)
        {
            final int timer = mob.getBrain().getMemory(AntTasks.NO_WORK_TIME).orElse(0);
            mob.getBrain().setMemory(AntTasks.NO_WORK_TIME, timer + 1);
            if (timer < 0) return;
            final long time = this.world.getGameTime();
            // Only assign the job if no work pos, the job should remove this
            // task if done or undoable
            if (mob.getBrain().hasMemory(AntTasks.WORK_POS)) return;

            switch (j)
            {
            case BUILD:
                build:
                {
                    if (this.rooms.allRooms.size() == 0) break build;
                    if (this.hasItems)
                    {
                        // Check for un-finished nodes first
                        for (final Node n : this.rooms.allRooms)
                        {
                            if (!n.shouldBuild(time)) continue;
                            final BlockPos pos = n.getCenter();
                            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Node Build Order for {} {} {}", pos, mob
                                    .getEntityId(), n.type);
                            final CompoundNBT tag = new CompoundNBT();
                            tag.putString("type", "node");
                            tag.put("data", n.serializeNBT());
                            mob.getBrain().setMemory(AntTasks.JOB_INFO, tag);
                            mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world
                                    .getDimensionKey(), pos));
                            break build;
                        }
                        final Node n = this.rooms.allRooms.get(this.world.rand.nextInt(this.rooms.allRooms.size()));
                        if (!n.started) break build;
                        final List<Edge> edges = Lists.newArrayList(n.edges);
                        Collections.shuffle(edges);
                        edges.sort((e1, e2) ->
                        {
                            final int o1 = !e1.node1.started || !e1.node2.started ? 0 : 1;
                            final int o2 = !e2.node1.started || !e2.node2.started ? 0 : 1;
                            return Integer.compare(o1, o2);
                        });
                        final Edge a = edges.get(0);
                        if (!a.shouldBuild(time)) break build;
                        final BlockPos pos = n == a.node1 ? a.end1 : a.end2;
                        final String info = a.node1.type + "<->" + a.node2.type;
                        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Edge Build Order for {} {} {}", pos, mob
                                .getEntityId(), info);
                        final CompoundNBT tag = new CompoundNBT();
                        tag.putString("type", "node");
                        tag.put("data", n.serializeNBT());
                        mob.getBrain().setMemory(AntTasks.JOB_INFO, tag);
                        mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world.getDimensionKey(),
                                pos));
                    }
                }
                break;
            case DIG:
                dig:
                {
                    if (this.rooms.allRooms.size() == 0) break dig;
                    // Check for un-finished nodes first
                    for (final Node n : this.rooms.allRooms)
                    {
                        if (!n.shouldDig(time)) continue;
                        if (n.dug.size() < 10)
                        {
                            final BlockPos pos = n.getCenter();
                            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Node Dig Finish Order for {} {} {} {}",
                                    pos, mob.getEntityId(), n.type, n.dug.size());
                            final CompoundNBT tag = new CompoundNBT();
                            tag.putString("type", "node");
                            tag.put("data", n.serializeNBT());
                            mob.getBrain().setMemory(AntTasks.JOB_INFO, tag);
                            mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world
                                    .getDimensionKey(), pos));
                            break dig;
                        }
                    }
                    final Node n = this.rooms.allRooms.get(this.world.rand.nextInt(this.rooms.allRooms.size()));
                    if (!n.started) break dig;
                    if (n.shouldDig(time))
                    {
                        final BlockPos pos = n.getCenter();
                        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Node Dig Order for {} {} {}", pos, mob
                                .getEntityId(), n.type);
                        final CompoundNBT tag = new CompoundNBT();
                        tag.putString("type", "node");
                        tag.put("data", n.serializeNBT());
                        mob.getBrain().setMemory(AntTasks.JOB_INFO, tag);
                        mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world.getDimensionKey(),
                                pos));
                        break dig;
                    }
                    else
                    {
                        final List<Edge> edges = Lists.newArrayList(n.edges);
                        Collections.shuffle(edges);
                        edges.sort((e1, e2) ->
                        {
                            final int o1 = !e1.node1.started || !e1.node2.started ? 0 : 1;
                            final int o2 = !e2.node1.started || !e2.node2.started ? 0 : 1;
                            return Integer.compare(o1, o2);
                        });
                        final Edge a = edges.get(0);
                        a.started = true;
                        if (!a.shouldDig(time)) break dig;
                        final BlockPos pos = n == a.node1 ? a.end1 : a.end2;
                        final String info = a.node1.type + "<->" + a.node2.type;
                        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Edge Dig Order for {} {} {}", pos, mob
                                .getEntityId(), info);
                        final CompoundNBT tag = new CompoundNBT();
                        tag.putString("type", "edge");
                        tag.put("data", a.serializeNBT());
                        mob.getBrain().setMemory(AntTasks.JOB_INFO, tag);
                        mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world.getDimensionKey(),
                                pos));
                        break dig;
                    }
                }
                break;
            case GATHER:

                break;
            case GUARD:
                /**
                 * Guard ants do the following:
                 * <br>
                 * They Walk to a random node
                 * Check for hostiles near the node
                 * Attack anything they don't like at the nodes.
                 */
                nodes:
                for (final Node n : this.rooms.allRooms)
                {
                    if (!n.started) continue;
                    final Vector3 x0 = Vector3.getNewVector().set(n.getCenter());
                    final AxisAlignedBB box = x0.getAABB().grow(2);
                    final boolean valid = BlockPos.getAllInBox(box).anyMatch(b -> this.world.isAirBlock(b));
                    if (valid)
                    {
                        mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world.getDimensionKey(),
                                x0.getPos()));
                        break nodes;
                    }
                }
                break;
            case NONE:

                break;
            default:
                break;

            }
        }

        @Override
        public void onBroken(final BlockPos pos, final ServerWorld world)
        {
            this.ants_in.forEach(ant ->
            {
                final CompoundNBT tag = ant.entityData;
                final Entity entity = EntityType.loadEntityAndExecute(tag, world, (mob) ->
                {
                    // Here we should do things like heal the ant,
                    // maybe update the inventory of the nest/ant
                    // etc
                    return mob;
                });
                this.antExitCooldown = 100;
                if (entity != null) world.addEntity(entity);
            });
            // We clear this, as ants may re-make us as a habitat, this prevents
            // cloning ants.
            this.ants_in.clear();
        }

        private boolean tryReleaseAnt(final Ant ant, final ServerWorld world)
        {
            // Tick this anyway, all will leave at daybreak!
            ant.ticksInHive++;
            if (this.antExitCooldown-- > 0) return false;
            if (AntTasks.shouldAntBeInNest(world, this.here)) return false;
            final boolean release = ant.ticksInHive > ant.minOccupationTicks;
            if (release)
            {
                final CompoundNBT tag = ant.entityData;
                final Entity entity = EntityType.loadEntityAndExecute(tag, world, (mob) ->
                {
                    // Here we should do things like heal the ant,
                    // maybe update the inventory of the nest/ant
                    // etc
                    return mob;
                });
                this.antExitCooldown = 100;
                if (entity != null) return world.addEntity(entity);
            }
            return false;
        }

        @Override
        public void onExitHabitat(final MobEntity mob)
        {
            AntJob job = AntTasks.getJob(mob);
            // Remove the old work pos for now, we will decide which ones need
            // to keep it stored
            mob.getBrain().removeMemory(AntTasks.WORK_POS);
            mob.getBrain().removeMemory(AntTasks.GOING_HOME);
            mob.getBrain().removeMemory(AntTasks.NO_WORK_TIME);
            this.workers.get(job).remove(mob.getUniqueID());
            job = this.getNextJob(job);
            this.workers.get(job).add(mob.getUniqueID());
            AntTasks.setJob(mob, job);
            mob.setHomePosAndDistance(this.here, 64);
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Ant Left Nest, with Job {}", job);

            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            if (pokemob != null) pokemob.setPokemonNickname("" + job);

            if (mob.getPersistentData().hasUniqueId("spectated_by"))
            {
                final UUID id = mob.getPersistentData().getUniqueId("spectated_by");
                System.out.println(id);
                final ServerPlayerEntity player = (ServerPlayerEntity) this.world.getPlayerByUuid(id);
                if (player != null) player.setSpectatingEntity(mob);
                mob.getPersistentData().remove("spectated_by");
            }
            // Assign it a job as soon as it exits
            this.assignJob(job, mob);
        }

        @Override
        public boolean onEnterHabitat(final MobEntity mob)
        {
            if (!this.canEnterHabitat(mob)) return false;

            int ants = this.ants_in.size();
            for (final Set<UUID> s : this.workers.values())
                ants += s.size();

            this.workers.get(AntTasks.getJob(mob)).remove(mob.getUniqueID());

            if (this.eggs.size() < Math.max(10, ants / 2))
            {
                final IPokemob poke = CapabilityPokemob.getPokemobFor(mob);
                Optional<BlockPos> room = this.getFreeEggRoom();
                if (poke != null && this.here != null)
                {
                    if (!room.isPresent()) room = Optional.of(this.here);
                    final PokedexEntry entry = poke.getPokedexEntry();
                    final ServerWorld world = (ServerWorld) mob.getEntityWorld();
                    if (world.isAirBlock(room.get().up()))
                    {
                        final EntityPokemobEgg egg = NestTile.spawnEgg(entry, room.get().up(), world, false);
                        if (egg != null) this.eggs.add(egg.getUniqueID());
                    }
                }
            }

            // enter:
            {
                if (this.world != null) for (final ServerPlayerEntity player : this.world.getPlayers())
                    if (player.getSpectatingEntity() == mob)
                    {
                        mob.getPersistentData().putUniqueId("spectated_by", player.getUniqueID());
                        player.setSpectatingEntity(null);
                    }
                if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Ant Entered Nest");

                mob.stopRiding();
                mob.removePassengers();
                final CompoundNBT tag = new CompoundNBT();

                final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
                final Optional<AntNest> nest = NestSensor.getNest(mob);
                drop:
                if (pokemob != null && nest.isPresent())
                {
                    final IItemHandler handler = nest.get().nest.getCapability(
                            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
                    if (!(handler instanceof IItemHandlerModifiable)) break drop;
                    final IItemHandlerModifiable itemhandler = new InvWrapper(pokemob.getInventory());
                    for (int i = 2; i < itemhandler.getSlots(); i++)
                    {
                        ItemStack stack = itemhandler.getStackInSlot(i);
                        if (ItemStackTools.addItemStackToInventory(stack, (IItemHandlerModifiable) handler, 0))
                        {
                            if (stack.isEmpty()) stack = ItemStack.EMPTY;
                            itemhandler.setStackInSlot(i, stack);
                        }
                    }
                }
                mob.writeUnlessPassenger(tag);
                tag.remove("Leash");
                this.ants_in.add(new Ant(tag, 0, 20 + mob.getRNG().nextInt(200)));
                mob.remove();
            }

            return true;
        }

        @Override
        public boolean canEnterHabitat(final MobEntity mob)
        {
            if (!AntTasks.isValidAnt(mob)) return false;
            if (!(mob.getEntityWorld() instanceof ServerWorld)) return false;
            return true;
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT compound = new CompoundNBT();
            final ListNBT ants = new ListNBT();
            for (final Ant ant : this.ants_in)
            {
                final CompoundNBT tag = new CompoundNBT();
                tag.put("EntityData", ant.entityData);
                tag.putInt("TicksInHive", ant.ticksInHive);
                tag.putInt("MinOccupationTicks", ant.minOccupationTicks);
                ants.add(tag);
            }
            compound.put("ants", ants);
            final ListNBT workers = new ListNBT();
            this.workers.forEach((j, s) ->
            {
                s.forEach(u ->
                {
                    final CompoundNBT tag = new CompoundNBT();
                    tag.putString("job", j.name());
                    tag.putUniqueId("id", u);
                    workers.add(tag);
                });
            });
            compound.put("workers", workers);
            compound.put("rooms", this.rooms.serializeNBT());
            final ListNBT eggs = new ListNBT();
            this.eggs.forEach(uuid ->
            {
                final CompoundNBT tag = new CompoundNBT();
                tag.putUniqueId("id", uuid);
                eggs.add(tag);
            });
            compound.put("eggs", eggs);
            return compound;
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            this.ants_in.clear();
            this.workers.forEach((j, s) -> s.clear());
            this.rooms.deserializeNBT(nbt.getCompound("rooms"));
            this.eggs.clear();
            final int compoundId = 10;
            final ListNBT ants = nbt.getList("ants", compoundId);
            for (int i = 0; i < ants.size(); ++i)
            {
                final CompoundNBT tag = ants.getCompound(i);
                final Ant ant = new Ant(tag.getCompound("EntityData"), tag.getInt("TicksInHive"), tag.getInt(
                        "MinOccupationTicks"));
                this.ants_in.add(ant);
            }
            final ListNBT workers = nbt.getList("workers", compoundId);
            for (int i = 0; i < workers.size(); ++i)
            {
                final CompoundNBT tag = workers.getCompound(i);
                final AntJob job = AntJob.valueOf(tag.getString("job"));
                final UUID id = tag.getUniqueId("id");
                this.workers.get(job).add(id);
            }
            final ListNBT eggs = nbt.getList("eggs", compoundId);
            for (int i = 0; i < eggs.size(); ++i)
            {
                final CompoundNBT tag = eggs.getCompound(i);
                this.eggs.add(tag.getUniqueId("id"));
            }
        }

        @Override
        public ResourceLocation getKey()
        {
            return AntTasks.NESTLOC;
        }

        public static class Ant
        {
            public final CompoundNBT entityData;

            public int       ticksInHive;
            public final int minOccupationTicks;

            private Ant(final CompoundNBT nbt, final int ticksInHive, final int minOccupationTicks)
            {
                this.entityData = nbt;
                this.ticksInHive = ticksInHive;
                this.minOccupationTicks = minOccupationTicks;
            }
        }
    }
}
