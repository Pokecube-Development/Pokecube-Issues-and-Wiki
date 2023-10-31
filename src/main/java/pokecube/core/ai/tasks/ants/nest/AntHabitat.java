package pokecube.core.ai.tasks.ants.nest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.api.PokecubeAPI;
import pokecube.api.blocks.IInhabitable;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.ants.AntTasks.AntRoom;
import pokecube.core.ai.tasks.ants.sensors.NestSensor;
import pokecube.core.ai.tasks.ants.sensors.NestSensor.AntNest;
import pokecube.core.ai.tasks.utility.GatherTask;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.eventhandlers.SpawnHandler.AABBRegion;
import pokecube.core.eventhandlers.SpawnHandler.ForbidRegion;
import pokecube.core.init.Config;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.api.world.IWorldTickListener;
import thut.api.world.WorldTickManager;
import thut.core.common.ThutCore;
import thut.lib.ItemStackTools;

public class AntHabitat implements IInhabitable, INBTSerializable<CompoundTag>, IWorldTickListener
{
    final List<Ant> ants_in = Lists.newArrayList();

    final Map<AntJob, Set<UUID>> workers = Maps.newHashMap();

    public Tree rooms = new Tree();

    public Set<UUID> eggs = Sets.newHashSet();
    public Set<UUID> ants = Sets.newHashSet();

    public List<NearBlock> blocks = Lists.newArrayList();
    public List<ItemEntity> items = Lists.newArrayList();

    BlockPos here;
    ServerLevel world;
    BlockEntity tile;

    int antExitCooldown = 0;

    ForbidRegion repelled = null;

    // This list gets shuffled every so often, so the order is not constant!
    public List<Node> allRooms = Lists.newArrayList();

    boolean hasItems = false;

    private boolean attached = false;

    public AntHabitat()
    {
        for (final AntJob job : AntJob.values()) this.workers.put(job, Sets.newHashSet());
    }

    @Override
    public void onAttach(final ServerLevel world)
    {
        this.world = world;
        this.attached = true;
        WorldTickManager.pathHelpers.get(world.dimension()).add(this.rooms);
    }

    @Override
    public void onDetach(final ServerLevel world)
    {
        this.world = null;
        this.attached = false;
        WorldTickManager.pathHelpers.get(world.dimension()).remove(this.rooms);
    }

    @Override
    public void onTickEnd(final ServerLevel level)
    {
        boolean canTick = world.isLoaded(this.here);
        if (!canTick) return;

        // Checks of if the tile entity is here, if not anger all ants
        // Possibly update a set of paths between nodes, so that we can speed up
        // path finding in the nest.
        if (level.getGameTime() % 100 == 0)
        {
            BlockEntity tile = level.getBlockEntity(this.here);
            if (tile == null)
            {
                this.onTick(level);
                this.ants.removeIf(uuid -> {
                    final Entity mob = level.getEntity(uuid);
                    if (AntTasks.isValid(mob)) return false;
                    return true;
                });
                if (this.ants.isEmpty() && this.eggs.isEmpty())
                {
                    PokecubeAPI.logInfo("Dead Nest!");
                    WorldTickManager.removeWorldData(level.dimension(), this);
                    return;
                }
                else
                {
                    if (Config.Rules.canAffectBlocks(level))
                    {
                        PokecubeAPI.logInfo("Reviving Nest!");
                        this.world.setBlockAndUpdate(this.here, PokecubeItems.NEST.get().defaultBlockState());
                        tile = this.world.getBlockEntity(this.here);
                        if (!(tile instanceof NestTile nest)) return;
                        // Copy over the old habitat info.
                        nest.setWrappedHab(this);
                    }
                }
            }
        }
    }

    private boolean removing = false;

    @Override
    public void updateRepelledRegion(final BlockEntity tile, final ServerLevel world)
    {
        final AABB box = this.rooms.getBounds().inflate(10, 0, 10);
        this.repelled = new AABBRegion(box);
        this.tile = tile;
        if (this.tile instanceof NestTile nest)
        {
            this.removing = true;
            if (this.repelled != null) SpawnHandler.removeForbiddenSpawningCoord(this.repelled.getPos(), world);
            this.removing = false;
            nest.addForbiddenSpawningCoord();
        }
    }

    @Override
    public ForbidRegion getRepelledRegion(final BlockEntity tile, final ServerLevel world)
    {
        if (this.repelled == null && !this.removing) this.updateRepelledRegion(tile, world);
        return this.repelled;
    }

    private AntJob getNextJob(final AntJob oldJob)
    {
        int low = Integer.MAX_VALUE;
        AntJob least = AntJob.NONE;
        int ready = 0;
        for (final Node n : this.rooms.allRooms) if (n.started) ready++;
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
        // least = AntJob.BUILD;
        return least;
    }

    public List<Node> getRooms(final AntRoom type)
    {
        return this.rooms.get(type);
    }

    public void addRandomNode(AntRoom type)
    {
        if (type == AntRoom.ENTRANCE) return;
        if (type == AntRoom.FOOD && this.getRooms(AntRoom.FOOD).size() > 3) return;
        final List<Node> nodes = this.rooms.allRooms;
        // This is only empty if we have not ticked yet.
        if (nodes.isEmpty()) return;
        final int existing = this.getRooms(type).size();
        if ((type == AntRoom.NODE || type == AntRoom.EGG) && existing > 1) type = AntRoom.FOOD;

        final Node entrance = this.getRooms(AntRoom.ENTRANCE).get(0);

        final Random rng = ThutCore.newRandom();
        final int index = rng.nextInt(nodes.size());
        final Node root = nodes.get(index);

        if (root.edges.size() > 2) return;

        final Vector3 centroid = new Vector3();
        for (final Node n : nodes) if (n.type == AntRoom.ENTRANCE)
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

        final float root_size = root.size - 0.5f;
        final float next_size = 2.5f + rng.nextFloat() * 3;

        BlockPos edgeShift = new BlockPos(root_size * dx / ds, 0, root_size * dz / ds);

        final BlockPos end1 = root.getCenter().offset(edgeShift);
        final BlockPos end2 = end1.offset(new BlockPos(dx, dy, dz));

        edgeShift = new BlockPos((next_size - 0.5) * dx / ds, 0, (next_size - 0.5) * dz / ds);

        final Vector3 vec2 = new Vector3().set(end1.subtract(end2));
        vec2.y = 0;

        for (final Edge e : root.edges)
        {
            // Check how parallel horizontally this it to an existing edge,
            // if too much, skip.
            final Vector3 vec1 = new Vector3().set(e.getEnd1().subtract(e.getEnd2()));
            if (e.node2 == root) vec1.set(e.getEnd2().subtract(e.getEnd1()));
            vec1.y = 0;
            if (vec1.norm().dot(vec2.norm()) > 0.7) return;
        }
        final BlockPos nodePos = end2.offset(edgeShift);

        vec2.set(nodePos);
        centroid.y = 0;
        vec2.y = 0;
        if (centroid.distanceTo(vec2) > r) return;

        final Node room = new Node();
        room.type = type;
        room.depth = root.depth + 1;
        room.setCenter(nodePos, next_size);

        for (final Node n : nodes) if (room.getOutBounds().intersects(n.getOutBounds())) return;

        if (nodePos.getY() > entrance.getCenter().getY() - 2) return;

        final Edge edge = new Edge();

        // These ends are shifted up one so they are centred on the wall of
        // the room, rather than the floor
        edge.node1 = root;
        edge.node2 = room;
        edge.setEnds(end1.above(), end2.above());

        // Now check if the newly made edge gets too close to any existing
        // rooms, if so, cancel it.
        for (final Node n : nodes)
        {
            if (n == root) continue;
            if (edge.withinDistance(n.getCenter(), n.size)) return;
        }

        if (root.started) edge.started = true;

        room.edges.add(edge);
        root.edges.add(edge);

        this.rooms.add(room);
        this.rooms.allEdges.add(edge);

        if (this.tile == null) this.tile = this.world.getBlockEntity(this.here);
        this.updateRepelledRegion(this.tile, this.world);
    }

    @Override
    public void setPos(final BlockPos pos)
    {
        this.here = pos;
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
                for (final Node n : rooms) if (n.started) return Optional.of(n.getCenter());
            }
        }
        return Optional.empty();
    }

    @Override
    public void onTick(final ServerLevel world)
    {
        if (!this.attached) WorldTickManager.addWorldData(world.dimension(), this);

        boolean canTick = world.isLoaded(this.here);
        if (!canTick) return;

        int x, y, z;
        x = this.here.getX();
        y = this.here.getY();
        z = this.here.getZ();

        final boolean playerNear = !world
                .getPlayers(p -> p.distanceToSqr(x, y, z) < Config.Rules.despawnDistance(world)).isEmpty();

        final int ants = this.ants_in.size() + this.ants.size();

        final Random rng = ThutCore.newRandom();
        // Lets make the eggs not hatch for now, if we are about say 5 ants,
        // This also removes hatched/removed eggs
        this.eggs.removeIf(uuid -> {
            final Entity mob = world.getEntity(uuid);
            if (!(mob instanceof EntityPokemobEgg egg) || !mob.isAddedToWorld()) return true;
            if (ants > PokecubeCore.getConfig().antNestMobNumber || !playerNear) egg.setAge(-100);
            else if (egg.getAge() < -100) egg.setAge(-rng.nextInt(100));
            return false;
        });

        if (!playerNear) return;

        boolean revive_nest = this.eggs.isEmpty() && world.getRandom().nextDouble() < 0.001;

        if (revive_nest)
        {
            Optional<BlockPos> room = this.getFreeEggRoom();
            if (this.here != null)
            {
                if (!room.isPresent()) room = Optional.of(this.here);
                final PokedexEntry entry = Database.getEntry("durant");
                if (world.isEmptyBlock(room.get().above()))
                {
                    final EntityPokemobEgg egg = NestTile.spawnEgg(entry, room.get().above(), world, false);
                    if (egg != null)
                    {
                        final CompoundTag nest = NbtUtils.writeBlockPos(this.here);
                        egg.getMainHandItem().getTag().put("nestLoc", nest);
                        this.eggs.add(egg.getUUID());
                    }
                }
            }
        }

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

        final int roomCount = this.rooms.allRooms.size();
        if (roomCount < 10) this.addRandomNode(AntRoom.values()[rng.nextInt(3)]);

        if (world.getGameTime() % 200 == 0)
        {
            final BlockEntity nest = world.getBlockEntity(this.here);
            this.hasItems = false;
            if (nest != null)
            {
                final IItemHandler handler = ThutCaps.getInventory(nest);
                for (int i = 0; i < handler.getSlots(); i++)
                {
                    final ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof BlockItem item)
                    {
                        if (!item.getBlock().defaultBlockState().canOcclude()) continue;
                        this.hasItems = true;
                        break;
                    }
                }
            }
            if (world.getGameTime() % 12000 == 0)
            {
                for (final Node n : this.allRooms) if (n.type == AntRoom.EGG) n.type = AntRoom.NODE;
                int i = 0;
                Collections.shuffle(this.allRooms);
                for (final Node n : this.allRooms) if (n.type == AntRoom.NODE && i++ < 3) n.type = AntRoom.EGG;
                this.rooms.deserializeNBT(this.rooms.serializeNBT());
            }
        }

        this.items.removeIf(GatherTask.deaditemmatcher);
        // Workers should only contain actual live ants! so if they are not
        // found here, remove them from the list
        this.workers.forEach((j, s) -> s.removeIf(uuid -> {
            final Entity mob = world.getEntity(uuid);
            if (AntTasks.isValid(mob))
            {
                // If we are a valid ant, ensure it has a job to do.
                if (world.getRandom().nextInt(50) == 0) this.assignJob(j, (Mob) mob);
                return false;
            }
            this.ants.remove(uuid);
            return true;
        }));
    }

    private void updateJob(Mob mob, CompoundTag tag, BlockPos pos)
    {
        mob.getBrain().setMemory(MemoryModules.JOB_INFO.get(), tag);
        mob.getBrain().setMemory(MemoryModules.WORK_POS.get(), GlobalPos.of(this.world.dimension(), pos));
    }

    private void assignJob(final AntJob j, final Mob mob)
    {
        final int timer = mob.getBrain().getMemory(MemoryModules.NO_WORK_TIMER.get()).orElse(0);
        mob.getBrain().setMemory(MemoryModules.NO_WORK_TIMER.get(), timer + 1);
        if (timer < 0) return;
        final long time = Tracker.instance().getTick();
        // Only assign the job if no work pos, the job should remove this
        // task if done or undoable
        if (mob.getBrain().hasMemoryValue(MemoryModules.WORK_POS.get())) return;

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
                        if (PokecubeCore.getConfig().debug_ai)
                            PokecubeAPI.logInfo("Node Build Order for {} {} {}", pos, mob.getId(), n.type);
                        final CompoundTag tag = new CompoundTag();
                        tag.putString("type", "node");
                        tag.put("data", n.serializeNBT());
                        updateJob(mob, tag, pos);
                        break build;
                    }
                    final Node n = this.rooms.allRooms.get(this.world.random.nextInt(this.rooms.allRooms.size()));
                    if (!n.started) break build;
                    final List<Edge> edges = Lists.newArrayList(n.edges);
                    Collections.shuffle(edges);
                    edges.sort((e1, e2) -> {
                        final int o1 = !e1.node1.started || !e1.node2.started ? 0 : 1;
                        final int o2 = !e2.node1.started || !e2.node2.started ? 0 : 1;
                        return Integer.compare(o1, o2);
                    });
                    final Edge a = edges.get(0);
                    if (!a.shouldBuild(time)) break build;
                    final BlockPos pos = n == a.node1 ? a.getEnd1() : a.getEnd2();
                    final String info = a.node1.type + "<->" + a.node2.type;
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logInfo("Edge Build Order for {} {} {}", pos, mob.getId(), info);
                    final CompoundTag tag = new CompoundTag();
                    tag.putString("type", "node");
                    tag.put("data", n.serializeNBT());
                    updateJob(mob, tag, pos);
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
                        if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.LOGGER
                                .debug("Node Dig Finish Order for {} {} {} {}", pos, mob.getId(), n.type, n.dug.size());
                        final CompoundTag tag = new CompoundTag();
                        tag.putString("type", "node");
                        tag.put("data", n.serializeNBT());
                        updateJob(mob, tag, pos);
                        break dig;
                    }
                }
                final Node n = this.rooms.allRooms.get(this.world.random.nextInt(this.rooms.allRooms.size()));
                if (!n.started) break dig;
                if (n.shouldDig(time))
                {
                    final BlockPos pos = n.getCenter();
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logInfo("Node Dig Order for {} {} {}", pos, mob.getId(), n.type);
                    final CompoundTag tag = new CompoundTag();
                    tag.putString("type", "node");
                    tag.put("data", n.serializeNBT());
                    updateJob(mob, tag, pos);
                    break dig;
                }
                else
                {
                    final List<Edge> edges = Lists.newArrayList(n.edges);
                    Collections.shuffle(edges);
                    edges.sort((e1, e2) -> {
                        final int o1 = !e1.node1.started || !e1.node2.started ? 0 : 1;
                        final int o2 = !e2.node1.started || !e2.node2.started ? 0 : 1;
                        return Integer.compare(o1, o2);
                    });
                    final Edge a = edges.get(0);
                    a.started = true;
                    if (!a.shouldDig(time)) break dig;
                    final BlockPos pos = n == a.node1 ? a.getEnd1() : a.getEnd2();
                    final String info = a.node1.type + "<->" + a.node2.type;
                    if (PokecubeCore.getConfig().debug_ai)
                        PokecubeAPI.logInfo("Edge Dig Order for {} {} {}", pos, mob.getId(), info);
                    final CompoundTag tag = new CompoundTag();
                    tag.putString("type", "edge");
                    tag.put("data", a.serializeNBT());
                    updateJob(mob, tag, pos);
                    break dig;
                }
            }
            break;
        case GATHER:
            if (!this.items.isEmpty())
            {
                var item = this.items.get(mob.getRandom().nextInt(this.items.size()));
                mob.getBrain().setMemory(MemoryModules.WORK_POS.get(),
                        GlobalPos.of(this.world.dimension(), item.blockPosition()));
            }
            break;
        case GUARD:
            /**
             * Guard ants do the following: <br>
             * They Walk to a random node Check for hostiles near the node
             * Attack anything they don't like at the nodes.
             */
            nodes:
            for (final Node n : this.rooms.allRooms)
            {
                if (!n.started) continue;
                final Vector3 x0 = new Vector3().set(n.getCenter());
                final AABB box = x0.getAABB().inflate(2);
                final boolean valid = BlockPos.betweenClosedStream(box).anyMatch(b -> this.world.isEmptyBlock(b));
                if (valid)
                {
                    mob.getBrain().setMemory(MemoryModules.WORK_POS.get(),
                            GlobalPos.of(this.world.dimension(), x0.getPos()));
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
    public void onBroken(final ServerLevel world)
    {
        this.ants_in.forEach(ant -> {
            final CompoundTag tag = ant.entityData;
            final Entity entity = EntityType.loadEntityRecursive(tag, world, (mob) -> {
                // Here we should do things like heal the ant,
                // maybe update the inventory of the nest/ant
                // etc
                return mob;
            });
            this.antExitCooldown = 100;
            if (world.getEntity(entity.getUUID()) != null) return;
            if (entity != null) world.addFreshEntity(entity);
        });
        // We clear this, as ants may re-make us as a habitat, this prevents
        // cloning ants.
        this.ants_in.clear();
    }

    private boolean tryReleaseAnt(final Ant ant, final ServerLevel world)
    {
        // Tick this anyway, all will leave at daybreak!
        ant.ticksInHive++;
        if (this.antExitCooldown-- > 0) return false;
        if (AntTasks.shouldAntBeInNest(world, this.here)) return false;
        final boolean release = ant.ticksInHive > ant.minOccupationTicks;
        if (release)
        {
            final CompoundTag tag = ant.entityData;
            final Entity entity = EntityType.loadEntityRecursive(tag, world, (mob) -> {
                // Here we should do things like heal the ant,
                // maybe update the inventory of the nest/ant
                // etc
                return mob;
            });
            if (world.getEntity(entity.getUUID()) != null) return true;
            this.antExitCooldown = 100;
            if (entity != null) return world.addFreshEntity(entity);
        }
        return false;
    }

    @Override
    public void onExitHabitat(final Mob mob)
    {
        if (this.world == null) this.world = (ServerLevel) mob.getLevel();

        AntJob job = AntTasks.getJob(mob);
        // Remove the old work pos for now, we will decide which ones need
        // to keep it stored
        mob.getBrain().eraseMemory(MemoryModules.WORK_POS.get());
        mob.getBrain().eraseMemory(MemoryModules.GOING_HOME.get());
        mob.getBrain().eraseMemory(MemoryModules.NO_WORK_TIMER.get());
        this.workers.get(job).remove(mob.getUUID());
        job = this.getNextJob(job);
        this.workers.get(job).add(mob.getUUID());
        this.ants.add(mob.getUUID());
        AntTasks.setJob(mob, job);
        mob.restrictTo(this.here, 64);
        if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Ant Left Nest, with Job {}", job);

        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        if (pokemob != null)
        {
            pokemob.healStatus();
            if (PokecubeCore.getConfig().debug_ai) pokemob.setPokemonNickname("" + job);
        }

        if (mob.getPersistentData().hasUUID("spectated_by"))
        {
            final UUID id = mob.getPersistentData().getUUID("spectated_by");
            final ServerPlayer player = (ServerPlayer) this.world.getPlayerByUUID(id);
            if (player != null) player.setCamera(mob);
            mob.getPersistentData().remove("spectated_by");
        }
        // Assign it a job as soon as it exits
        this.assignJob(job, mob);
    }

    @Override
    public void addResident(Mob mob)
    {
        if (!this.canEnterHabitat(mob)) return;
        this.ants.add(mob.getUUID());
        mob.setPersistenceRequired();
    }

    @Override
    public boolean onEnterHabitat(final Mob mob)
    {
        if (!this.canEnterHabitat(mob) || !(mob.getLevel() instanceof ServerLevel level)) return false;

        final int ants = this.ants_in.size() + this.ants.size();

        this.ants.remove(mob.getUUID());
        this.workers.get(AntTasks.getJob(mob)).remove(mob.getUUID());

        if (this.eggs.size() < Math.min(Math.max(PokecubeCore.getConfig().antNestMobNumber / 2, ants / 2),
                PokecubeCore.getConfig().antNestMobNumber / 2))
        {
            final IPokemob poke = PokemobCaps.getPokemobFor(mob);
            Optional<BlockPos> room = this.getFreeEggRoom();
            if (poke != null && this.here != null)
            {
                if (!room.isPresent()) room = Optional.of(this.here);
                final PokedexEntry entry = poke.getPokedexEntry();
                if (level.isEmptyBlock(room.get().above()))
                {
                    final EntityPokemobEgg egg = NestTile.spawnEgg(entry, room.get().above(), level, false);
                    if (egg != null)
                    {
                        final CompoundTag nest = NbtUtils.writeBlockPos(this.here);
                        egg.getMainHandItem().getTag().put("nestLoc", nest);
                        this.eggs.add(egg.getUUID());
                    }
                }
            }
        }

        // enter:
        {
            if (this.world != null)
                for (final ServerPlayer player : this.world.players()) if (player.getCamera() == mob)
            {
                mob.getPersistentData().putUUID("spectated_by", player.getUUID());
                player.setCamera(null);
            }
            if (PokecubeCore.getConfig().debug_ai) PokecubeAPI.logInfo("Ant Entered Nest");

            mob.stopRiding();
            mob.ejectPassengers();
            final CompoundTag tag = new CompoundTag();

            final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
            final Optional<AntNest> nest = NestSensor.getNest(mob);
            drop:
            if (pokemob != null && nest.isPresent())
            {
                final IItemHandler nestInventory = ThutCaps.getInventory(nest.get().nest);
                if (!(nestInventory instanceof IItemHandlerModifiable nestInv)) break drop;
                final IItemHandlerModifiable itemhandler = new InvWrapper(pokemob.getInventory());
                for (int i = 2; i < itemhandler.getSlots(); i++)
                {
                    ItemStack stack = itemhandler.getStackInSlot(i);
                    if (ItemStackTools.addItemStackToInventory(stack, nestInv, 0))
                    {
                        if (stack.isEmpty()) stack = ItemStack.EMPTY;
                        itemhandler.setStackInSlot(i, stack);
                    }
                }
            }
            mob.save(tag);
            tag.remove("Leash");
            this.ants_in.add(new Ant(tag, 0, 20 + mob.getRandom().nextInt(200)));
            mob.discard();
        }

        return true;
    }

    @Override
    public boolean canEnterHabitat(final Mob mob)
    {
        if (!AntTasks.isValid(mob)) return false;
        if (!(mob.getLevel() instanceof ServerLevel)) return false;
        return true;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compound = new CompoundTag();
        final ListTag ants = new ListTag();
        for (final Ant ant : this.ants_in)
        {
            final CompoundTag tag = new CompoundTag();
            tag.put("EntityData", ant.entityData);
            tag.putInt("TicksInHive", ant.ticksInHive);
            tag.putInt("MinOccupationTicks", ant.minOccupationTicks);
            ants.add(tag);
        }
        compound.put("ants", ants);
        final ListTag workers = new ListTag();
        this.workers.forEach((j, s) -> {
            s.forEach(u -> {
                final CompoundTag tag = new CompoundTag();
                tag.putString("job", j.name());
                tag.putUUID("id", u);
                workers.add(tag);
            });
        });
        compound.put("workers", workers);
        compound.put("rooms", this.rooms.serializeNBT());
        final ListTag eggs = new ListTag();
        this.eggs.forEach(uuid -> {
            final CompoundTag tag = new CompoundTag();
            tag.putUUID("id", uuid);
            eggs.add(tag);
        });
        compound.put("eggs", eggs);
        return compound;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        this.ants_in.clear();
        this.ants.clear();
        this.workers.forEach((j, s) -> s.clear());
        this.rooms.deserializeNBT(nbt.getCompound("rooms"));
        this.eggs.clear();
        final int compoundId = 10;
        final ListTag ants = nbt.getList("ants", compoundId);
        for (int i = 0; i < ants.size(); ++i)
        {
            final CompoundTag tag = ants.getCompound(i);
            final Ant ant = new Ant(tag.getCompound("EntityData"), tag.getInt("TicksInHive"),
                    tag.getInt("MinOccupationTicks"));
            this.ants_in.add(ant);
        }
        final ListTag workers = nbt.getList("workers", compoundId);
        for (int i = 0; i < workers.size(); ++i)
        {
            final CompoundTag tag = workers.getCompound(i);
            final AntJob job = AntJob.valueOf(tag.getString("job"));
            final UUID id = tag.getUUID("id");
            this.workers.get(job).add(id);
            this.ants.add(id);
        }
        final ListTag eggs = nbt.getList("eggs", compoundId);
        for (int i = 0; i < eggs.size(); ++i)
        {
            final CompoundTag tag = eggs.getCompound(i);
            this.eggs.add(tag.getUUID("id"));
        }
    }

    @Override
    public ResourceLocation getKey()
    {
        return AntTasks.NESTLOC;
    }

    public static class Ant
    {
        public final CompoundTag entityData;

        public int ticksInHive;
        public final int minOccupationTicks;

        private Ant(final CompoundTag nbt, final int ticksInHive, final int minOccupationTicks)
        {
            this.entityData = nbt;
            this.ticksInHive = ticksInHive;
            this.minOccupationTicks = minOccupationTicks;
        }
    }
}
