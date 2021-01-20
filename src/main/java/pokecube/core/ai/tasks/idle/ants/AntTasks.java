package pokecube.core.ai.tasks.idle.ants;

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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.Sensors;
import pokecube.core.ai.tasks.idle.ants.nest.CheckNest;
import pokecube.core.ai.tasks.idle.ants.nest.EnterNest;
import pokecube.core.ai.tasks.idle.ants.nest.MakeNest;
import pokecube.core.ai.tasks.idle.ants.sensors.EggSensor;
import pokecube.core.ai.tasks.idle.ants.sensors.GatherSensor;
import pokecube.core.ai.tasks.idle.ants.sensors.NestSensor;
import pokecube.core.ai.tasks.idle.ants.sensors.ThreatSensor;
import pokecube.core.ai.tasks.idle.ants.work.CarryEgg;
import pokecube.core.ai.tasks.idle.ants.work.Dig;
import pokecube.core.ai.tasks.idle.ants.work.Gather;
import pokecube.core.ai.tasks.idle.ants.work.Guard;
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

    public static final SensorType<NestSensor>   NEST_SENSOR   = new SensorType<>(NestSensor::new);
    public static final SensorType<GatherSensor> WORK_SENSOR   = new SensorType<>(GatherSensor::new);
    public static final SensorType<ThreatSensor> THREAT_SENSOR = new SensorType<>(ThreatSensor::new);
    public static final SensorType<EggSensor>    EGG_SENSOR    = new SensorType<>(EggSensor::new);

    public static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(AntTasks.NEST_POS,
            AntTasks.WORK_POS, AntTasks.OUT_OF_HIVE_TIMER, AntTasks.NO_WORK_TIME, AntTasks.NO_HIVE_TIMER,
            AntTasks.JOB_TYPE, AntTasks.JOB_INFO, AntTasks.EGG);

    public static final List<SensorType<?>> SENSOR_TYPES = ImmutableList.of(AntTasks.NEST_SENSOR, AntTasks.WORK_SENSOR,
            AntTasks.EGG_SENSOR, AntTasks.THREAT_SENSOR, Sensors.VISIBLE_BLOCKS, Sensors.INTERESTING_ENTITIES);

    public static final ResourceLocation NESTLOC = new ResourceLocation(PokecubeCore.MODID, "ant_nest");

    public static void registerMems(final Register<MemoryModuleType<?>> event)
    {
        event.getRegistry().register(AntTasks.JOB_TYPE.setRegistryName(PokecubeCore.MODID, "ant_job_type"));
        event.getRegistry().register(AntTasks.JOB_INFO.setRegistryName(PokecubeCore.MODID, "ant_job_info"));
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
        list.add(new Dig(pokemob).setPriority(3));

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
        public static class Edge
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

            @Override
            public boolean equals(final Object obj)
            {
                if (!(obj instanceof Edge)) return false;
                return this.areSame((Edge) obj);
            }
        }

        public static class Node
        {
            public AntRoom type = AntRoom.NODE;

            public final BlockPos center;

            public List<Edge> edges = Lists.newArrayList();

            public Node(final BlockPos center)
            {
                this.center = center;
            }
        }

        public class Tree implements INBTSerializable<CompoundNBT>
        {
            public Map<AntRoom, List<Node>> rooms = Maps.newHashMap();

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
                this.rooms.forEach((r, s) -> s.forEach(n ->
                {
                    final CompoundNBT nbt = NBTUtil.writeBlockPos(n.center);
                    nbt.putString("room", r.name());
                    final ListNBT edges = new ListNBT();
                    n.edges.forEach(edge ->
                    {
                        final CompoundNBT edgeNbt = new CompoundNBT();
                        edgeNbt.put("n1", NBTUtil.writeBlockPos(edge.node1.center));
                        edgeNbt.put("e1", NBTUtil.writeBlockPos(edge.end1));
                        edgeNbt.put("n2", NBTUtil.writeBlockPos(edge.node2.center));
                        edgeNbt.put("e2", NBTUtil.writeBlockPos(edge.end2));
                        edges.add(edgeNbt);
                    });
                    nbt.put("edges", edges);
                    list.add(nbt);
                }));
                tag.put("map", list);
                return tag;
            }

            @Override
            public void deserializeNBT(final CompoundNBT tag)
            {
                for (final AntRoom room : AntRoom.values())
                    this.rooms.put(room, Lists.newArrayList());
                final Map<BlockPos, Node> loadedNodes = Maps.newHashMap();

                // First we de-serialize the nodes, and stuff them in
                // loadedNodes, After we will need to sort through this, and
                // re-connect the edges accordingly
                final ListNBT list = tag.getList("map", 10);
                for (int i = 0; i < list.size(); ++i)
                {
                    final CompoundNBT nbt = list.getCompound(i);
                    final BlockPos pos = NBTUtil.readBlockPos(nbt);
                    final AntRoom room = AntRoom.valueOf(nbt.getString("room"));
                    // This is a "real" node, it will be added to the maps
                    final Node n = loadedNodes.getOrDefault(pos, new Node(pos));
                    n.type = room;
                    final ListNBT edges = nbt.getList("edges", 10);
                    for (int j = 0; j < edges.size(); ++j)
                    {
                        final CompoundNBT edgeNbt = edges.getCompound(i);
                        final Edge e = new Edge();
                        final BlockPos p1 = NBTUtil.readBlockPos(edgeNbt.getCompound("n1"));
                        e.node1 = loadedNodes.getOrDefault(p1, new Node(p1));
                        loadedNodes.put(p1, e.node1);
                        e.end1 = NBTUtil.readBlockPos(edgeNbt.getCompound("e1"));
                        final BlockPos p2 = NBTUtil.readBlockPos(edgeNbt.getCompound("n2"));
                        e.node2 = loadedNodes.getOrDefault(p2, new Node(p2));
                        loadedNodes.put(p2, e.node2);
                        e.end2 = NBTUtil.readBlockPos(edgeNbt.getCompound("e2"));
                        if (!(e.end1.equals(e.end2) || n.edges.contains(e))) n.edges.add(e);
                    }
                    loadedNodes.put(pos, n);
                    this.rooms.get(room).add(n);
                }

                // Now we need to re-build the tree from the loaded nodes. Edges
                // need to be replaced such that nodes share the same edges.
                loadedNodes.forEach((p, n) ->
                {
                    n.edges.forEach(edge ->
                    {
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
                // Now here we need to add edges to connect the node.
            }
        }

        final List<Ant> ants_in = Lists.newArrayList();

        final Map<AntJob, Set<UUID>> workers = Maps.newHashMap();

        Tree rooms = new Tree();

        public Set<UUID> eggs = Sets.newHashSet();

        BlockPos    here;
        ServerWorld world;

        int antExitCooldown = 0;

        public AntHabitat()
        {
            for (final AntJob job : AntJob.values())
                this.workers.put(job, Sets.newHashSet());
        }

        private AntJob getNextJob(final AntJob oldJob)
        {
            // Guard ants stay guard ants!
            if (oldJob == AntJob.GUARD) return oldJob;
            int prev = 0;
            for (final AntJob job : AntJob.values())
            {
                final int num = this.workers.get(job).size();
                if (prev > num) return job;
                prev = num;
            }
            return oldJob;
        }

        public List<Node> getRooms(final AntRoom type)
        {
            return this.rooms.get(type);
        }

        public void addRandomNode(final AntRoom type)
        {
            if (type == AntRoom.ENTRANCE) return;
            final List<Node> nodes = Lists.newArrayList();
            this.rooms.rooms.forEach((r, s) -> nodes.addAll(s));
            // This is only empty if we have not ticked yet.
            if (nodes.isEmpty()) return;
            int index = 0;
            final Random rng = new Random();
            if (nodes.size() > 1) index = rng.nextInt(nodes.size());
            final Node root = nodes.get(index);

            final int r = 16;
            final int x = rng.nextInt(r * 2) - r + this.here.getX();
            final int z = rng.nextInt(r * 2) - r + this.here.getZ();

            final int dx = root.center.getX() - x;
            final int dz = root.center.getZ() - z;
            final double ds = Math.sqrt(dx * dx + dz * dz);
            final int y = (int) (root.center.getY() - ds / 3);
            final BlockPos pos = new BlockPos(x, y, z);

            for (final Node n : nodes)
                if (pos.distanceSq(n.center) < 25) return;

            final Node room = new Node(pos);
            room.type = type;

            final Edge edge = new Edge();

            edge.end1 = root.center;
            edge.node1 = root;
            edge.end2 = room.center;
            edge.node2 = room;

            room.edges.add(edge);
            root.edges.add(edge);

            this.rooms.add(room);
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
                this.rooms.add(entrance);
            }

            int ants = this.ants_in.size() + this.eggs.size();
            for (final Set<UUID> s : this.workers.values())
                ants += s.size();
            final int num = ants;

            int roomCount = 0;
            for (final List<Node> rooms : this.rooms.rooms.values())
                roomCount += rooms.size();
            final Random rng = new Random();
            if (roomCount < 10) this.addRandomNode(AntRoom.values()[rng.nextInt(3)]);

            // Workers should only contain actual live ants! so if they are not
            // found here, remove them from the list
            this.workers.forEach((j, s) -> s.removeIf(uuid ->
            {
                final Entity mob = world.getEntityByUuid(uuid);
                if (AntTasks.isValidAnt(mob)) return false;
                return true;
            }));
            // Lets make the eggs not hatch for now, if we are about say 5 ants,
            // This also removes hatched/removed eggs
            this.eggs.removeIf(uuid ->
            {
                final Entity mob = world.getEntityByUuid(uuid);
                if (!(mob instanceof EntityPokemobEgg) || !mob.isAddedToWorld()) return true;
                final EntityPokemobEgg egg = (EntityPokemobEgg) mob;
                if (num > 10) egg.setGrowingAge(-100);
                else if (egg.getGrowingAge() < -100) egg.setGrowingAge(-100);
                return false;
            });

            // We need to assign each DIG ant to a task, this should be
            // digging out the nest
            this.workers.get(AntJob.DIG).forEach(uuid ->
            {
                final MobEntity mob = (MobEntity) world.getEntityByUuid(uuid);
                if (mob.getBrain().hasMemory(AntTasks.WORK_POS)) return;

                nodes:
                for (final List<Node> list : this.rooms.rooms.values())
                    for (final Node n : list)
                        for (final Edge a : n.edges)
                        {
                            if (a.end1 == null || a.end2 == null) continue;
                            double r = a.digged;
                            if (r >= 1) r = a.digged = 0;
                            final Vector3 x0 = Vector3.getNewVector().set(a.end1);
                            final Vector3 x1 = Vector3.getNewVector().set(a.end2).subtractFrom(x0);
                            final double d = x1.mag();
                            final double dr = d == 0 ? 0 : 1 / d;
                            if (r <= 0) r = a.digged += dr;
                            x0.set(a.end1).addTo(x1.scalarMult(r));
                            BlockPos p = x0.getPos();
                            while (world.isAirBlock(p) && r < 1 - dr && d > 0)
                            {
                                r = a.digged += dr;
                                x0.set(a.end1).addTo(x1.scalarMult(r));
                                p = x0.getPos();
                            }
                            if (world.isAirBlock(p)) continue;
                            final AxisAlignedBB box = x0.getAABB().grow(2);
                            final boolean valid = BlockPos.getAllInBox(box).anyMatch(b -> world.isAirBlock(b));
                            System.out.println(valid + " " + x0);
                            if (valid)
                            {
                                mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(world
                                        .getDimensionKey(), x0.getPos()));
                                break nodes;
                            }
                        }
            });
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
            if (mob.getBrain().hasMemory(AntTasks.WORK_POS)) mob.getBrain().removeMemory(AntTasks.WORK_POS);
            this.workers.get(job).remove(mob.getUniqueID());
            job = this.getNextJob(job);
            this.workers.get(job).add(mob.getUniqueID());
            AntTasks.setJob(mob, job);
            mob.setHomePosAndDistance(this.here, 64);
        }

        @Override
        public boolean onEnterHabitat(final MobEntity mob)
        {
            if (!this.canEnterHabitat(mob)) return false;
            mob.stopRiding();
            mob.removePassengers();
            final CompoundNBT tag = new CompoundNBT();
            mob.writeUnlessPassenger(tag);
            tag.remove("Leash");

            this.ants_in.add(new Ant(tag, 0, 1200 + mob.getRNG().nextInt(1200)));

            int ants = this.ants_in.size() + this.eggs.size();
            for (final Set<UUID> s : this.workers.values())
                ants += s.size();

            this.workers.get(AntTasks.getJob(mob)).remove(mob.getUniqueID());

            if (ants < 10)
            {
                final IPokemob poke = CapabilityPokemob.getPokemobFor(mob);
                final Optional<BlockPos> room = this.getFreeEggRoom();
                if (poke != null && room.isPresent())
                {
                    final PokedexEntry entry = poke.getPokedexEntry();
                    final ServerWorld world = (ServerWorld) mob.getEntityWorld();
                    final EntityPokemobEgg egg = NestTile.spawnEgg(entry, room.get(), world, false);
                    if (egg != null) this.eggs.add(egg.getUniqueID());
                }
            }
            mob.remove();
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
