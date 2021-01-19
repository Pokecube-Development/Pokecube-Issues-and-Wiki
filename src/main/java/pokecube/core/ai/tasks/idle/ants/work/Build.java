package pokecube.core.ai.tasks.idle.ants.work;

import java.util.Optional;
import java.util.Random;

import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraftforge.items.IItemHandlerModifiable;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.idle.ants.AbstractWorkTask;
import pokecube.core.ai.tasks.idle.ants.AntTasks;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntHabitat.Edge;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntHabitat.Node;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntJob;
import pokecube.core.ai.tasks.idle.ants.AntTasks.AntRoom;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class Build extends AbstractWorkTask
{
    ItemStack to_place = ItemStack.EMPTY;
    int       storeInd = -1;

    int build_timer = 0;

    Node n = null;
    Edge e = null;

    BlockPos build_pos = null;

    boolean going_to_nest = false;

    public Build(final IPokemob pokemob)
    {
        super(pokemob, j -> j == AntJob.BUILD);
    }

    @Override
    public void reset()
    {
        this.to_place = ItemStack.EMPTY;
        this.going_to_nest = false;
        this.storeInd = -1;
        this.build_timer = 0;
        this.n = null;
        this.e = null;
        this.build_pos = null;
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
                    brain.removeMemory(AntTasks.WORK_POS);
                    brain.removeMemory(AntTasks.JOB_INFO);
                    brain.setMemory(AntTasks.NO_WORK_TIME, -100);
                    PokecubeCore.LOGGER.error("Corrupted Dig Edge Info!");
                    return false;
                }
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
                    brain.removeMemory(AntTasks.WORK_POS);
                    brain.removeMemory(AntTasks.JOB_INFO);
                    brain.setMemory(AntTasks.NO_WORK_TIME, -100);
                    PokecubeCore.LOGGER.error("Corrupted Dig Node Info!");
                    return false;
                }
            }
        }
        if (!(edge || node))
        {
            brain.removeMemory(AntTasks.WORK_POS);
            brain.removeMemory(AntTasks.JOB_INFO);
            brain.setMemory(AntTasks.NO_WORK_TIME, -100);
            PokecubeCore.LOGGER.debug("Invalid Dig Info!");
            return false;
        }
        return true;
    }

    private boolean selectJobSite()
    {
        final Brain<?> brain = this.entity.getBrain();
        final Optional<GlobalPos> room = brain.getMemory(AntTasks.WORK_POS);
        select:
        if (this.build_pos == null)
        {
            final Vector3 x0 = Vector3.getNewVector().set(this.n.center);
            final Random rng = new Random();
            // Random hemispherical coordinate
            final double theta = rng.nextDouble() * Math.PI;
            final double phi = rng.nextDouble() * Math.PI * 2;

            double r = 5 * Math.sin(theta);
            final double h = 3;

            double y = h * Math.cos(theta);

            // This results in a dome with a floor
            if (y < 0)
            {
                y = -1;
                r = rng.nextInt((int) (r + 2));
            }

            final double x = r * Math.cos(phi);
            final double z = r * Math.sin(phi);

            if (this.n.type == AntRoom.ENTRANCE)
            {
                final boolean passage = y <= 2 && y >= 0;
                // This results in entrances in the cardinal directions.
                if ((int) x == 0 && passage) break select;
                if ((int) z == 0 && passage) break select;
            }
            // final Vector3 dr = Vector3.getNewVector().set(x, y, z);

            x0.x += x;
            x0.y += y;
            x0.z += z;
            final BlockPos pos = x0.getPos();
            if (!this.world.isAirBlock(pos)) break select;

            // This prevents filling in the passages
            for (final Edge e : this.nest.hab.rooms.allEdges)
                if (e.isOn(pos, 2)) break select;

            final AxisAlignedBB below = new AxisAlignedBB(x0.x - 1, x0.y - 2, x0.z - 1, x0.x + 1, x0.y, x0.z + 1);
            // We need a not air block somewhere in this box to stand on
            // to place the material.
            if (BlockPos.getAllInBox(below).anyMatch(p -> !this.world.isAirBlock(p))) this.build_pos = pos
                    .toImmutable();
        }
        this.build_timer++;
        if (this.build_pos == null)
        {
            this.setWalkTo(room.get().getPos(), 1, 1);
            // If we took too long. lets give up
            if (this.build_timer > 600)
            {
                brain.removeMemory(AntTasks.WORK_POS);
                brain.removeMemory(AntTasks.JOB_INFO);
                brain.setMemory(AntTasks.NO_WORK_TIME, -100);
                brain.setMemory(AntTasks.GOING_HOME, true);
                this.reset();
            }
        }
        return this.build_pos != null;
    }

    @Override
    public void run()
    {
        if (this.storage.firstEmpty == -1) return;

        if (!this.checkJob()) return;
        if (!this.selectJobSite()) return;

        this.pokemob.setRoutineState(AIRoutine.STORE, true);
        this.storage.storageLoc = this.nest.nest.getPos();
        this.storage.berryLoc = this.nest.nest.getPos();

        // First. we need to check our own inventory, see if we have any blocks.
        // If so, we will place one of those. Otherwise, we need to go to the
        // nest, and pick one up from there.
        if (this.to_place.isEmpty())
        {
            if (this.going_to_nest)
            {
                final BlockPos pos = this.nest.nest.getPos();
                if (pos.distanceSq(this.entity.getPosition()) > 9) this.setWalkTo(pos, 1, 1);
                else
                {
                    final IItemHandlerModifiable inv = this.storage.getInventory(this.world, pos,
                            this.storage.storageFace);
                    if (this.storage.firstEmpty > 0)
                    {
                        for (int i = 0; i < inv.getSlots(); i++)
                        {
                            final ItemStack stack = inv.getStackInSlot(i);
                            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem)
                            {
                                final BlockItem item = (BlockItem) stack.getItem();
                                if (!item.getBlock().getDefaultState().isSolid()) continue;

                                this.to_place = inv.extractItem(i, Math.min(stack.getCount(), 5), false);
                                this.storeInd = this.storage.firstEmpty;
                                this.pokemob.getInventory().setInventorySlotContents(this.storage.firstEmpty,
                                        this.to_place);
                                return;
                            }
                        }
                        PokecubeCore.LOGGER.debug("no blocks! making some dirt.");
                        this.to_place = new ItemStack(Blocks.PODZOL, 2);
                        this.storeInd = this.storage.firstEmpty;
                        this.pokemob.getInventory().setInventorySlotContents(this.storage.firstEmpty, this.to_place);
                    }
                }
                return;
            }
            this.going_to_nest = this.to_place.isEmpty();
            if (this.going_to_nest) return;
        }

        final BlockPos pos = this.build_pos;
        // if (pos.distanceSq(this.entity.getPosition()) > 10)
        // {
        this.setWalkTo(pos, 1, 1);
        // return;
        // }
        this.build_pos = null;
        final Vector3 v = Vector3.getNewVector();
        v.set(pos);
        if (!this.to_place.isEmpty() && this.to_place.getItem() instanceof BlockItem && this.storeInd != -1)
        {
            final BlockItem item = (BlockItem) this.to_place.getItem();
            v.setBlock(this.world, item.getBlock().getDefaultState());
            this.to_place.shrink(1);
            this.pokemob.getInventory().setInventorySlotContents(this.storeInd, this.to_place);

            final String info = this.e == null ? this.n.type.name() : "edge";

            PokecubeCore.LOGGER.debug("Built a block at {} for {}", v.getPos(), info);
        }
    }
}
