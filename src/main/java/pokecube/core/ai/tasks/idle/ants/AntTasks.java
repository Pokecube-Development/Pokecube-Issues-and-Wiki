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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
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
        public static class Edge implements INBTSerializable<CompoundNBT>
        {
            public Node node1;
            public Node node2;

            public BlockPos end1;
            public BlockPos end2;

            // Fraction of the way dug from end1 to end2.
            public double digged = 0;

            // Persistant value to track if we have started being mined.
            public boolean started = false;

            // If present, when loading this map will be used to sync the nodes
            // on the edges.
            Map<BlockPos, Node> _loadedNodes = null;

            boolean areSame(final Edge other)
            {
                return this.end1.equals(other.end1) && this.end2.equals(other.end2);
            }

            public boolean isOn(final BlockPos pos, final double size)
            {
                final AxisAlignedBB box = new AxisAlignedBB(this.end1, this.end2);
                if (!box.grow(1).contains(pos.getX(), pos.getY(), pos.getZ())) return false;
                final Vector3d e1 = new Vector3d(this.end1.getX() + 0.5, this.end1.getY() + 0.5, this.end1.getZ()
                        + 0.5);
                final Vector3d e2 = new Vector3d(this.end1.getX() + 0.5, this.end1.getY() + 0.5, this.end1.getZ()
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
                final CompoundNBT edgeNbt = new CompoundNBT();
                edgeNbt.put("n1", NBTUtil.writeBlockPos(this.node1.center));
                edgeNbt.put("e1", NBTUtil.writeBlockPos(this.end1));
                edgeNbt.put("n2", NBTUtil.writeBlockPos(this.node2.center));
                edgeNbt.put("e2", NBTUtil.writeBlockPos(this.end2));
                edgeNbt.putBoolean("s", this.started);
                return edgeNbt;
            }

            @Override
            public void deserializeNBT(final CompoundNBT nbt)
            {

                this.end1 = NBTUtil.readBlockPos(nbt.getCompound("e1"));
                this.end2 = NBTUtil.readBlockPos(nbt.getCompound("e2"));
                this.started = nbt.getBoolean("s");

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
        }

        public static class Node implements INBTSerializable<CompoundNBT>
        {
            public AntRoom type = AntRoom.NODE;

            public BlockPos center;

            public List<Edge> edges = Lists.newArrayList();

            public boolean started;

            public int depth = 0;

            // If present, when loading this map will be used to sync the nodes
            // on the edges.
            Map<BlockPos, Node> _loadedNodes = null;

            public Node()
            {
                this.center = BlockPos.ZERO;
            }

            public Node(final BlockPos center)
            {
                this.center = center;
            }

            @Override
            public CompoundNBT serializeNBT()
            {
                final CompoundNBT nbt = NBTUtil.writeBlockPos(this.center);
                nbt.putString("room", this.type.name());
                nbt.putBoolean("s", this.started);
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
                final BlockPos pos = NBTUtil.readBlockPos(nbt);
                Node n = this;
                this.center = pos;
                // This is a "real" node, it will be added to the maps
                if (this._loadedNodes != null)
                {
                    n = this._loadedNodes.getOrDefault(pos, this);
                    this._loadedNodes.put(pos, n);
                    n.center = pos;
                }
                n.started = nbt.getBoolean("s");
                n.type = AntRoom.valueOf(nbt.getString("room"));
                n.depth = nbt.getInt("d");
                final ListNBT edges = nbt.getList("edges", 10);
                for (int j = 0; j < edges.size(); ++j)
                {
                    final CompoundNBT edgeNbt = edges.getCompound(j);
                    final Edge e = new Edge();
                    try
                    {
                        e._loadedNodes = this._loadedNodes;
                        e.deserializeNBT(edgeNbt);
                        e._loadedNodes = null;
                        if (!(e.end1.equals(e.end2) || n.edges.contains(e))) n.edges.add(e);
                    }
                    catch (final Exception e1)
                    {
                        e1.printStackTrace();
                        PokecubeCore.LOGGER.error("Error loading an edge!");
                    }
                }
            }
        }

        public class Tree implements INBTSerializable<CompoundNBT>
        {
            public Map<AntRoom, List<Node>> rooms = Maps.newHashMap();

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
                    n._loadedNodes = null;
                    n = loadedNodes.get(n.center);
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
            for (final AntJob job : AntJob.values())
            {
                final int num = this.workers.get(job).size();
                if (low > num)
                {
                    least = job;
                    low = num;
                }
            }
            // if (least != null) return new Random().nextBoolean() ? AntJob.DIG
            // : AntJob.BUILD;
            return least;
        }

        public List<Node> getRooms(final AntRoom type)
        {
            return this.rooms.get(type);
        }

        public void addRandomNode(final AntRoom type)
        {
            if (type == AntRoom.ENTRANCE) return;
            // Force a free egg room check, we always want to start with an egg
            // room if possible!
            if (type != AntRoom.EGG && !this.getFreeEggRoom().isPresent()) return;
            final List<Node> nodes = this.rooms.allRooms;
            // This is only empty if we have not ticked yet.
            if (nodes.isEmpty()) return;
            int index = 0;
            final Random rng = new Random();
            if (nodes.size() > 1) index = rng.nextInt(nodes.size());
            final Node root = nodes.get(index);

            if (root.edges.size() > 2) return;

            final Vector3 centroid = Vector3.getNewVector();
            for (final Node n : nodes)
                if (n.type == AntRoom.ENTRANCE)
                {
                    centroid.set(n.center);
                    break;
                }

            final int r = 12;
            final int x = rng.nextInt(r * 2) - r;
            final int z = rng.nextInt(r * 2) - r;

            final int dx = x;
            final int dz = z;
            final double ds = Math.sqrt(dx * dx + dz * dz);
            final int dy = -(int) (ds / (2.0 + rng.nextDouble() * rng.nextDouble()));
            final BlockPos edgeShift = new BlockPos(2.5 * dx / ds, 0, 2.5 * dz / ds);

            final BlockPos end1 = root.center.add(edgeShift);
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
                if (nodePos.distanceSq(n.center) < 36) return;

            final Node room = new Node(nodePos);
            room.type = type;
            room.depth = root.depth + 1;

            final Edge edge = new Edge();

            edge.end1 = end1;
            edge.node1 = root;
            edge.end2 = end2;
            edge.node2 = room;

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
                if (!eggRooms.isEmpty()) for (final Node p : eggRooms)
                    return Optional.of(p.center);
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
                this.rooms.add(entrance);
            }

            int ants = this.ants_in.size() + this.eggs.size();
            for (final Set<UUID> s : this.workers.values())
                ants += s.size();
            final int num = ants;

            final int roomCount = this.rooms.allRooms.size();
            final Random rng = new Random();
            if (roomCount < 10) this.addRandomNode(AntRoom.values()[rng.nextInt(3)]);

            if (roomCount != this.allRooms.size())
            {
                this.allRooms.clear();
                this.allRooms.addAll(this.rooms.allRooms);
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
            }

            // Workers should only contain actual live ants! so if they are not
            // found here, remove them from the list
            this.workers.forEach((j, s) -> s.removeIf(uuid ->
            {
                final Entity mob = world.getEntityByUuid(uuid);
                if (AntTasks.isValidAnt(mob))
                {
                    Collections.shuffle(this.allRooms);
                    // If we are a valid ant, ensure it has a job to do.
                    this.assignJob(j, (MobEntity) mob);
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
            // Only assign the job if no work pos, the job should remove this
            // task if done or undoable
            if (mob.getBrain().hasMemory(AntTasks.WORK_POS)) return;

            switch (j)
            {
            case BUILD:
                if (this.hasItems) for (final Node n : this.allRooms)
                {
                    if (!n.started) continue;
                    // n = this.getRooms(AntRoom.ENTRANCE).get(0);

                    final BlockPos pos = n.center;
                    PokecubeCore.LOGGER.debug("Node Build Order for {} {} {}", pos, mob.getEntityId(), n.type);
                    final CompoundNBT tag = new CompoundNBT();
                    tag.putString("type", "node");
                    tag.put("data", n.serializeNBT());
                    mob.getBrain().setMemory(AntTasks.JOB_INFO, tag);
                    mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world.getDimensionKey(),
                            pos));
                    return;
                }
                break;
            case DIG:
                for (final Node n : this.allRooms)
                {
                    if (!n.started) continue;
                    final boolean doEdge = this.world.rand.nextBoolean();
                    if (doEdge)
                    {
                        final List<Edge> edges = Lists.newArrayList(n.edges);
                        Collections.shuffle(edges);
                        for (final Edge a : edges)
                        {
                            a.started = true;
                            final BlockPos pos = n == a.node1 ? a.end1 : a.end2;
                            final String info = a.node1.type + "<->" + a.node2.type;
                            PokecubeCore.LOGGER.debug("Edge Dig Order for {} {} {}", pos, mob.getEntityId(), info);
                            final CompoundNBT tag = new CompoundNBT();
                            tag.putString("type", "edge");
                            try
                            {
                                tag.put("data", a.serializeNBT());
                                mob.getBrain().setMemory(AntTasks.JOB_INFO, tag);
                                mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world
                                        .getDimensionKey(), pos));
                            }
                            catch (final Exception e)
                            {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            return;
                        }
                    }
                    else
                    {
                        final BlockPos pos = n.center;
                        PokecubeCore.LOGGER.debug("Node Dig Order for {} {} {}", pos, mob.getEntityId(), n.type);
                        final CompoundNBT tag = new CompoundNBT();
                        tag.putString("type", "node");
                        tag.put("data", n.serializeNBT());
                        mob.getBrain().setMemory(AntTasks.JOB_INFO, tag);
                        mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world.getDimensionKey(),
                                pos));
                        return;
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
                for (final Node n : this.allRooms)
                {
                    if (!n.started) continue;
                    final Vector3 x0 = Vector3.getNewVector().set(n.center);
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
            ant.ticksInHive += 20;
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
            this.workers.get(job).remove(mob.getUniqueID());
            job = this.getNextJob(job);
            this.workers.get(job).add(mob.getUniqueID());
            AntTasks.setJob(mob, job);
            mob.setHomePosAndDistance(this.here, 64);
            PokecubeCore.LOGGER.debug("Ant Left Nest, with Job {}", job);

            if (mob.getPersistentData().hasUniqueId("spectated_by"))
            {
                final UUID id = mob.getPersistentData().getUniqueId("spectated_by");
                System.out.println(id);
                final ServerPlayerEntity player = (ServerPlayerEntity) this.world.getPlayerByUuid(id);
                if (player != null) player.setSpectatingEntity(mob);
                mob.getPersistentData().remove("spectated_by");
            }
        }

        @Override
        public boolean onEnterHabitat(final MobEntity mob)
        {
            if (!this.canEnterHabitat(mob)) return false;

            int ants = this.ants_in.size();
            for (final Set<UUID> s : this.workers.values())
                ants += s.size();

            this.workers.get(AntTasks.getJob(mob)).remove(mob.getUniqueID());

            if (this.eggs.size() < Math.max(5, ants / 2))
            {
                final IPokemob poke = CapabilityPokemob.getPokemobFor(mob);
                Optional<BlockPos> room = this.getFreeEggRoom();
                if (poke != null && this.here != null)
                {
                    if (!room.isPresent()) room = Optional.of(this.here.up());
                    final PokedexEntry entry = poke.getPokedexEntry();
                    final ServerWorld world = (ServerWorld) mob.getEntityWorld();
                    if (world.isAirBlock(room.get()))
                    {
                        final EntityPokemobEgg egg = NestTile.spawnEgg(entry, room.get(), world, false);
                        if (egg != null) this.eggs.add(egg.getUniqueID());
                    }
                }
            }

            // enter:
            {
                for (final ServerPlayerEntity player : this.world.getPlayers())
                    if (player.getSpectatingEntity() == mob)
                    {
                        mob.getPersistentData().putUniqueId("spectated_by", player.getUniqueID());
                        player.setSpectatingEntity(null);
                    }
                PokecubeCore.LOGGER.debug("Ant Entered Nest");

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
                    for (int i = 3; i < itemhandler.getSlots(); i++)
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
                this.ants_in.add(new Ant(tag, 0, 1200 + mob.getRNG().nextInt(1200)));
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
