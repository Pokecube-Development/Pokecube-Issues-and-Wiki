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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.ants.AntTasks.AntRoom;
import pokecube.core.ai.tasks.ants.sensors.NestSensor;
import pokecube.core.ai.tasks.ants.sensors.NestSensor.AntNest;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.database.PokedexEntry;
import pokecube.core.handlers.events.SpawnHandler.AABBRegion;
import pokecube.core.handlers.events.SpawnHandler.ForbidRegion;
import pokecube.core.interfaces.IInhabitable;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.world.IWorldTickListener;
import pokecube.core.world.WorldTickManager;
import thut.api.maths.Vector3;
import thut.lib.ItemStackTools;

public class AntHabitat implements IInhabitable, INBTSerializable<CompoundNBT>, IWorldTickListener
{
    final List<Ant> ants_in = Lists.newArrayList();

    final Map<AntJob, Set<UUID>> workers = Maps.newHashMap();

    public Tree rooms = new Tree();

    public Set<UUID> eggs = Sets.newHashSet();
    public Set<UUID> ants = Sets.newHashSet();

    public List<NearBlock>  blocks = Lists.newArrayList();
    public List<ItemEntity> items  = Lists.newArrayList();

    BlockPos    here;
    ServerWorld world;
    TileEntity  tile;

    int antExitCooldown = 0;

    ForbidRegion repelled = null;

    // This list gets shuffled every so often, so the order is not constant!
    public List<Node> allRooms = Lists.newArrayList();

    boolean hasItems = false;

    private boolean attached = false;

    public AntHabitat()
    {
        for (final AntJob job : AntJob.values())
            this.workers.put(job, Sets.newHashSet());
    }

    @Override
    public void onAttach(final ServerWorld world)
    {
        this.world = world;
        this.attached = true;
        WorldTickManager.pathHelpers.get(world.getDimensionKey()).add(this.rooms);
    }

    @Override
    public void onDetach(final ServerWorld world)
    {
        this.world = null;
        this.attached = false;
        WorldTickManager.pathHelpers.get(world.getDimensionKey()).remove(this.rooms);
    }

    @Override
    public void onTickEnd(final ServerWorld world)
    {
        // Things to add here:
        if (!world.isAreaLoaded(this.here, 1)) return;

        // Checks of if the tile entity is here, if not anger all ants
        // Possibly update a set of paths between nodes, so that we can speed up
        // path finding in the nest.
        if (world.getGameTime() % 100 == 0)
        {
            TileEntity tile = world.getTileEntity(this.here);
            if (tile == null)
            {
                this.onTick(world);
                this.ants.removeIf(uuid ->
                {
                    final Entity mob = world.getEntityByUuid(uuid);
                    if (AntTasks.isValid(mob)) return false;
                    return true;
                });
                if (this.ants.isEmpty() && this.eggs.isEmpty())
                {
                    PokecubeCore.LOGGER.debug("Dead Nest!");
                    WorldTickManager.removeWorldData(world.getDimensionKey(), this);
                    return;
                }
                else
                {
                    PokecubeCore.LOGGER.debug("Reviving Nest!");
                    this.world.setBlockState(this.here, PokecubeItems.NESTBLOCK.get().getDefaultState());
                    tile = this.world.getTileEntity(this.here);
                    if (!(tile instanceof NestTile)) return;
                    final NestTile nest = (NestTile) tile;
                    // Copy over the old habitat info.
                    nest.setWrappedHab(this);
                }
            }
        }
    }

    @Override
    public void updateRepelledRegion(final TileEntity tile, final ServerWorld world)
    {
        final AxisAlignedBB box = this.rooms.getBounds().grow(10, 0, 10);
        NestTile nest = null;
        this.tile = tile;
        if (this.tile instanceof NestTile)
        {
            nest = (NestTile) this.tile;
            nest.removeForbiddenSpawningCoord();
        }
        this.repelled = new AABBRegion(box);
        if (nest != null) nest.addForbiddenSpawningCoord();
    }

    @Override
    public ForbidRegion getRepelledRegion(final TileEntity tile, final ServerWorld world)
    {
        if (this.repelled == null) this.updateRepelledRegion(tile, world);
        return this.repelled;
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

        final float root_size = root.size - 0.5f;
        final float next_size = 2.5f + rng.nextFloat() * 3;

        BlockPos edgeShift = new BlockPos(root_size * dx / ds, 0, root_size * dz / ds);

        final BlockPos end1 = root.getCenter().add(edgeShift);
        final BlockPos end2 = end1.add(new BlockPos(dx, dy, dz));

        edgeShift = new BlockPos((next_size - 0.5) * dx / ds, 0, (next_size - 0.5) * dz / ds);

        final Vector3 vec2 = Vector3.getNewVector().set(end1.subtract(end2));
        vec2.y = 0;

        for (final Edge e : root.edges)
        {
            // Check how parallel horizontally this it to an existing edge,
            // if too much, skip.
            final Vector3 vec1 = Vector3.getNewVector().set(e.getEnd1().subtract(e.getEnd2()));
            if (e.node2 == root) vec1.set(e.getEnd2().subtract(e.getEnd1()));
            vec1.y = 0;
            if (vec1.norm().dot(vec2.norm()) > 0.7) return;
        }
        final BlockPos nodePos = end2.add(edgeShift);

        vec2.set(nodePos);
        centroid.y = 0;
        vec2.y = 0;
        if (centroid.distanceTo(vec2) > r) return;

        final Node room = new Node();
        room.type = type;
        room.depth = root.depth + 1;
        room.setCenter(nodePos, next_size);

        for (final Node n : nodes)
            if (room.getOutBounds().intersects(n.getOutBounds())) return;

        if (nodePos.getY() > entrance.getCenter().getY() - 2) return;

        final Edge edge = new Edge();

        // These ends are shifted up one so they are centred on the wall of
        // the room, rather than the floor
        edge.node1 = root;
        edge.node2 = room;
        edge.setEnds(end1.up(), end2.up());

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

        if (this.tile == null) this.tile = this.world.getTileEntity(this.here);
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
                for (final Node n : rooms)
                    if (n.started) return Optional.of(n.getCenter());
            }
        }
        return Optional.empty();
    }

    @Override
    public void onTick(final ServerWorld world)
    {
        if (!this.attached) WorldTickManager.addWorldData(world.getDimensionKey(), this);

        int x, y, z;
        x = this.here.getX();
        y = this.here.getY();
        z = this.here.getZ();

        final boolean playerNear = !world.getPlayers(p -> p.getDistanceSq(x, y, z) < PokecubeCore
                .getConfig().cullDistance).isEmpty();

        final int ants = this.ants_in.size() + this.ants.size();

        final Random rng = new Random();
        // Lets make the eggs not hatch for now, if we are about say 5 ants,
        // This also removes hatched/removed eggs
        this.eggs.removeIf(uuid ->
        {
            final Entity mob = world.getEntityByUuid(uuid);
            if (!(mob instanceof EntityPokemobEgg) || !mob.isAddedToWorld()) return true;
            final EntityPokemobEgg egg = (EntityPokemobEgg) mob;
            if (ants > 10 || !playerNear) egg.setGrowingAge(-100);
            else if (egg.getGrowingAge() < -100) egg.setGrowingAge(-rng.nextInt(100));
            return false;
        });

        if (!playerNear) return;

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
            final TileEntity nest = world.getTileEntity(this.here);
            this.hasItems = false;
            if (nest != null)
            {
                final IItemHandler handler = nest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(
                        null);
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
            if (AntTasks.isValid(mob))
            {
                // If we are a valid ant, ensure it has a job to do.
                if (world.getRandom().nextInt(50) == 0) this.assignJob(j, (MobEntity) mob);
                return false;
            }
            this.ants.remove(uuid);
            return true;
        }));
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
                        mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world.getDimensionKey(),
                                pos));
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
                    final BlockPos pos = n == a.node1 ? a.getEnd1() : a.getEnd2();
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
                        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Node Dig Finish Order for {} {} {} {}", pos,
                                mob.getEntityId(), n.type, n.dug.size());
                        final CompoundNBT tag = new CompoundNBT();
                        tag.putString("type", "node");
                        tag.put("data", n.serializeNBT());
                        mob.getBrain().setMemory(AntTasks.JOB_INFO, tag);
                        mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world.getDimensionKey(),
                                pos));
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
                    final BlockPos pos = n == a.node1 ? a.getEnd1() : a.getEnd2();
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
                    mob.getBrain().setMemory(AntTasks.WORK_POS, GlobalPos.getPosition(this.world.getDimensionKey(), x0
                            .getPos()));
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
    public void onBroken(final ServerWorld world)
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
        this.ants.add(mob.getUniqueID());
        AntTasks.setJob(mob, job);
        mob.setHomePosAndDistance(this.here, 64);
        if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Ant Left Nest, with Job {}", job);

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob != null)
        {
            pokemob.healStatus();
            if (PokecubeMod.debug) pokemob.setPokemonNickname("" + job);
        }

        if (mob.getPersistentData().hasUniqueId("spectated_by"))
        {
            final UUID id = mob.getPersistentData().getUniqueId("spectated_by");
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

        final int ants = this.ants_in.size() + this.ants.size();

        this.ants.remove(mob.getUniqueID());
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
        if (!AntTasks.isValid(mob)) return false;
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
        this.ants.clear();
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
            this.ants.add(id);
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
