package pokecube.core.ai.tasks.idle.ants.work;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.ai.tasks.idle.ants.AbstractWorkTask;
import pokecube.core.ai.tasks.idle.ants.AntTasks;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntHabitat.Edge;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntHabitat.Node;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class Dig extends AbstractWorkTask
{
    private static final Map<MemoryModuleType<?>, MemoryModuleStatus> mems = Maps.newHashMap();
    static
    {
        Dig.mems.put(AntTasks.JOB_INFO, MemoryModuleStatus.VALUE_PRESENT);
    }

    int progressTimer    = 0;
    int progressDistance = 0;

    Node n = null;
    Edge e = null;

    BlockPos digChoice = null;

    Predicate<BlockPos> hasEmptySpace = pos -> BlockPos.getAllInBox(pos.add(-2, -2, -2), pos.add(2, 2, 2)).anyMatch(
            p -> this.world.isAirBlock(p));

    Predicate<BlockState> diggable = state -> (PokecubeTerrainChecker.isTerrain(state) || PokecubeTerrainChecker.isRock(
            state) || PokecubeTerrainChecker.isCutablePlant(state) || PokecubeTerrainChecker.isLeaves(state)
            || PokecubeTerrainChecker.isWood(state)) && state.getBlock() != PokecubeItems.NESTBLOCK.get();

    public Dig(final IPokemob pokemob)
    {
        super(pokemob, Dig.mems, j -> j == AntJob.DIG);
    }

    @Override
    public void reset()
    {
        this.progressTimer = 0;
        this.progressDistance = 0;
        this.n = null;
        this.e = null;
        this.digChoice = null;
        final Brain<?> brain = this.entity.getBrain();
        brain.removeMemory(AntTasks.WORK_POS);
        brain.removeMemory(AntTasks.JOB_INFO);
        brain.setMemory(AntTasks.NO_WORK_TIME, -20);
    }

    private boolean checkJob()
    {
        final Brain<?> brain = this.entity.getBrain();

        boolean edge = this.e != null;
        boolean node = this.n != null;
        if (!(edge || node))
        {
            final CompoundNBT tag = brain.getMemory(AntTasks.JOB_INFO).get();
            edge = tag.getString("type").equals("edge");
            node = tag.getString("type").equals("node");
            final CompoundNBT data = tag.getCompound("data");
            if (edge)
            {
                this.e = new Edge();
                this.e.deserializeNBT(data);

                if (this.e.node1 == null || this.e.node2 == null)
                {
                    tag.remove("type");
                    tag.remove("data");
                    PokecubeCore.LOGGER.error("Corrupted Dig Edge Info!");
                    this.reset();
                    return false;
                }
                this.nest.hab.allRooms.forEach(n ->
                {
                    if (n.center.equals(this.e.node1.center)) this.e.node1 = n;
                    if (n.center.equals(this.e.node2.center)) this.e.node2 = n;
                });
            }
            if (node)
            {
                this.n = new Node();
                try
                {
                    this.n.deserializeNBT(data);
                }
                catch (final Exception e1)
                {
                    e1.printStackTrace();
                    tag.remove("type");
                    tag.remove("data");
                    PokecubeCore.LOGGER.error("Corrupted Dig Node Info!");
                    this.reset();
                    return false;
                }
            }
        }
        if (!(edge || node))
        {
            PokecubeCore.LOGGER.debug("Invalid Dig Info!");
            this.reset();
            return false;
        }
        return true;
    }

    private boolean tryLine(final BlockPos start, final BlockPos end, final Edge e)
    {
        final Vector3 x0 = Vector3.getNewVector();
        final Vector3 dr = Vector3.getNewVector();
        x0.set(start);
        dr.set(end).subtractFrom(x0);
        final double r = dr.mag();
        dr.scalarMultBy(1 / r);
        if (e != null && (int) e.digged >= (int) r)
        {
            final boolean node1 = e.node1.started;
            e.digged = 0.0;
            if (node1) e.node2.started = true;
            else e.node1.started = true;
        }
        final Vector3 tmp = Vector3.getNewVector();
        for (double i = 0; i <= r; i++)
        {
            if (e != null) e.digged = i;
            x0.set(start).addTo(dr.x * i, dr.y * i, dr.z * i);
            if (x0.sameBlock(tmp)) continue;
            tmp.set(x0);
            BlockPos pos = x0.getPos();
            // final boolean hasRoom = this.hasEmptySpace.test(pos);
            // if (!hasRoom)
            // {
            // this.e.digged = 0;
            // return false;
            // }
            final BlockPos min = pos.add(0, 0, 0);
            final BlockPos max = pos.add(1, 1, 1);
            final Optional<BlockPos> valid = BlockPos.getAllInBox(min, max).filter(p -> this.diggable.test(this.world
                    .getBlockState(p))).findAny();
            if (!valid.isPresent()) continue;
            pos = valid.get();
            this.digChoice = pos.toImmutable();
            this.progressTimer = -60;
            return true;
        }
        return false;
    }

    private boolean selectJobSite()
    {
        final boolean edge = this.e != null;
        dig_select:
        if (this.digChoice == null)
        {
            if (PokecubeMod.debug) PokecubeCore.LOGGER.debug("Selecting dig site");
            if (edge)
            {
                final boolean node1 = this.e.node1.started;
                BlockPos start = node1 ? this.e.end1 : this.e.end2;
                BlockPos end = node1 ? this.e.end2 : this.e.end1;
                if (this.tryLine(start, end, this.e)) break dig_select;
                start = this.e.end1;
                end = this.e.node1.center;
                if (this.tryLine(start, end, null)) break dig_select;
                start = this.e.end2;
                end = this.e.node2.center;
                if (this.tryLine(start, end, null)) break dig_select;
            }
            else
            {
                final Vector3 x0 = Vector3.getNewVector().set(this.n.center);
                final Random rng = new Random();

                // Random hemispherical coordinate
                final double theta = rng.nextDouble() * Math.PI / 2;
                final double phi = rng.nextDouble() * Math.PI * 2;

                final double r = rng.nextInt(4);
                final double h = rng.nextInt(3);

                final double y = h * Math.cos(theta);
                final double x = r * Math.sin(theta) * Math.cos(phi);
                final double z = r * Math.sin(theta) * Math.sin(phi);

                x0.x += x;
                x0.y += y;
                x0.z += z;
                final BlockPos pos = x0.getPos();
                if (!this.diggable.test(this.world.getBlockState(pos))) break dig_select;
                // final boolean hasRoom = this.hasEmptySpace.test(pos);
                // if (hasRoom)
                {
                    this.digChoice = pos.toImmutable();
                    this.progressTimer = -600;
                }
            }
        }
        this.progressTimer++;
        final Brain<?> brain = this.entity.getBrain();
        final Optional<GlobalPos> room = brain.getMemory(AntTasks.WORK_POS);
        if (this.digChoice == null)
        {
            this.setWalkTo(room.get().getPos(), 1, 1);
            // We give up
            if (this.progressTimer > 400)
            {
                PokecubeCore.LOGGER.debug("Need New Dig Site");
                brain.setMemory(AntTasks.GOING_HOME, true);
                this.reset();
            }
        }
        return this.digChoice != null;
    }

    @Override
    public void run()
    {
        if (!this.checkJob()) return;
        if (!this.selectJobSite()) return;

        final BlockPos pos = this.digChoice;
        final double dr = pos.distanceSq(this.entity.getPosition());
        this.setWalkTo(pos, 1, 1);
        if (dr > 9 && dr < 9)
        {
            // Here we should try to walk to the node if too far, so should do
            // some tree analysis to determine pathing to that, to speed up the
            // checks that the vanilla stuff does, which gets confused above
            // ground.

            if (this.progressTimer > 100 && this.entity.getNavigator().hasPath())
            {
                this.progressTimer = 0;
                final Path p = this.entity.getNavigator().getPath();
                final BlockPos targ = p.getTarget();
                final BlockPos end = p.getFinalPathPoint().func_224759_a();
                final int dist = end.manhattanDistance(targ);
                if (dist > 1)
                {
                    final int num = this.nest.hab.allRooms.size();
                    final int index = new Random().nextInt(num);
                    final Node rand = this.nest.hab.allRooms.get(index);
                    if (rand.started)
                    {
                        this.progressTimer = -600;
                        this.setWalkTo(rand.center, 1, 1);
                        PokecubeCore.LOGGER.debug("Diverting to node {}", rand.type);
                    }
                }
                if (this.progressDistance == 0)
                {
                    this.progressDistance = dist;
                    return;
                }
            }
            return;
        }
        this.progressTimer = 0;
        final Vector3 v = Vector3.getNewVector();
        final BlockState state = this.world.getBlockState(pos);
        if (this.diggable.test(state) && MoveEventsHandler.canAffectBlock(this.pokemob, v.set(pos), "ant_dig", false,
                false))
        {
            this.world.destroyBlock(pos, true, this.entity);
            // attempt to collect the drops
            final List<ItemEntity> drops = this.world.getEntitiesWithinAABB(ItemEntity.class, v.getAABB().grow(3));
            for (final ItemEntity e : drops)
            {
                final ItemStack stack = e.getItem().copy();
                new InventoryChange(this.entity, 2, stack, true).run(this.world);
                e.setItem(ItemStack.EMPTY);
            }
        }
        this.digChoice = null;
    }
}
