package pokecube.adventures.blocks.genetics.helper;

import java.util.List;

import org.nfunk.jep.JEP;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.adventures.blocks.genetics.helper.recipe.IPoweredProgress;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredProcess;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.inventory.InvHelper;

public abstract class BaseGeneticsTile extends InteractableTile implements IPoweredProgress, ITickableTileEntity,
        ISidedInventory
{
    public static class IntHolder extends IntReferenceHolder
    {
        private int value;

        @Override
        public int get()
        {
            return this.value;
        }

        @Override
        public void set(final int arg0)
        {
            this.value = arg0;
        }
    }

    public static JEP parser;

    public static void initParser(final String function)
    {
        BaseGeneticsTile.parser = new JEP();
        BaseGeneticsTile.parser.initFunTab(); // clear the contents of the
                                              // function table
        BaseGeneticsTile.parser.addStandardFunctions();
        BaseGeneticsTile.parser.initSymTab(); // clear the contents of the
                                              // symbol table
        BaseGeneticsTile.parser.addStandardConstants();
        BaseGeneticsTile.parser.addComplex(); // among other things adds i to
                                              // the symbol
        // table
        BaseGeneticsTile.parser.addVariable("x", 0);
        BaseGeneticsTile.parser.parseExpression(function);
    }

    private final List<ItemStack> inventory;

    final int                                            outputSlot;
    private boolean                                      check          = true;
    public IntReferenceHolder                            progress       = new IntHolder();
    public IntReferenceHolder                            total          = new IntHolder();
    private PoweredProcess                               currentProcess = null;
    protected PoweredCraftingInventory                   craftMatrix;
    private PlayerEntity                                 user;
    private final LazyOptional<? extends IItemHandler>[] wrappers       = SidedInvWrapper.create(this, Direction
            .values());
    int[]                                                slots;

    public BaseGeneticsTile(final TileEntityType<?> tileEntityTypeIn, final int size, final int output)
    {
        super(tileEntityTypeIn);
        this.inventory = NonNullList.<ItemStack> withSize(size, ItemStack.EMPTY);
        this.outputSlot = output;
    }

    @Override
    public int addEnergy(final int energy, final boolean simulate)
    {
        if (this.getProcess() == null || !this.getProcess().valid()) return 0;
        BaseGeneticsTile.parser.setVarValue("x", energy);
        final int num = (int) Math.min(BaseGeneticsTile.parser.getValue(), this.getProcess().needed);
        if (!simulate) this.getProcess().needed -= num;
        return num;
    }

    @Override
    /**
     * Returns true if automation can extract the given item in the given slot
     * from the given side.
     */
    public boolean canExtractItem(final int index, final ItemStack stack, final Direction direction)
    {
        return !this.isItemValidForSlot(index, stack);
    }

    @Override
    /**
     * Returns true if automation can insert the given item in the given slot
     * from the given side.
     */
    public boolean canInsertItem(final int index, final ItemStack itemStackIn, final Direction direction)
    {
        return this.isItemValidForSlot(index, itemStackIn);
    }

    public void checkRecipes()
    {
        if (this.getProcess() == null || !this.getProcess().valid())
        {
            if (this.check)
            {
                this.check = false;
                if (this.getProcess() == null) this.setProcess(new PoweredProcess());
                this.getProcess().setTile(this);
                this.getProcess().reset();
                if (!this.getProcess().valid()) this.setProcess(null);
            }
            else this.setProcess(null);
        }
        else
        {
            final boolean valid = this.getProcess().valid();
            boolean done = true;
            if (valid)
            {
                this.total.set(this.getProcess().recipe.getEnergyCost());
                // TODO remove this when needed.
                this.getProcess().needed -= 230;
                done = !this.getProcess().tick();
            }
            if (!valid || done)
            {
                this.setProcess(null);
                this.progress.set(0);
                this.markDirty();
            }
        }
    }

    @Override
    public void clear()
    {
        this.inventory.clear();
    }

    @Override
    public void closeInventory(final PlayerEntity player)
    {
        if (this.user == player) this.user = null;
    }

    @Override
    public ItemStack decrStackSize(final int arg0, final int arg1)
    {
        final ItemStack stack = this.getStackInSlot(arg0);
        // TODO is this how it is supposed to happen?
        return stack.split(arg1);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
    {
        if (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return this.wrappers[facing
                .ordinal()].cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public PoweredCraftingInventory getCraftMatrix()
    {
        if (this.craftMatrix == null) this.craftMatrix = new PoweredCraftingInventory(null, this, 3, 3);
        return this.craftMatrix;
    }

    public List<ItemStack> getInventory()
    {
        return this.inventory;
    }

    @Override
    public List<ItemStack> getList()
    {
        return this.inventory;
    }

    @Override
    public int getOutputSlot()
    {
        return this.outputSlot;
    }

    @Override
    public PoweredProcess getProcess()
    {
        return this.currentProcess;
    }

    @Override
    public int getSizeInventory()
    {
        return this.getInventory().size();
    }

    @Override
    public int[] getSlotsForFace(final Direction side)
    {
        if (this.slots == null)
        {
            this.slots = new int[this.getSizeInventory()];
            for (int i = 0; i < this.slots.length; i++)
                this.slots[i] = i;
        }
        return this.slots;
    }

    @Override
    public ItemStack getStackInSlot(final int arg0)
    {
        return this.inventory.get(arg0);
    }

    @Override
    public PlayerEntity getUser()
    {
        return this.user;
    }

    @Override
    public boolean isEmpty()
    {
        return this.inventory.isEmpty();
    }

    @Override
    public boolean isUsableByPlayer(final PlayerEntity player)
    {
        return this.user == null || this.user == player;
    }

    @Override
    public void openInventory(final PlayerEntity player)
    {
        if (this.user == null) this.user = player;
    }

    @Override
    public void read(final CompoundNBT nbt)
    {
        super.read(nbt);
        InvHelper.load(this, nbt);
        if (nbt.contains("progress"))
        {
            final CompoundNBT tag = nbt.getCompound("progress");
            this.setProcess(PoweredProcess.load(tag, this));
            if (this.getProcess() != null) this.total.set(this.getProcess().recipe.getEnergyCost());
        }
    }

    /**
     * invalidates a tile entity
     */
    @Override
    public void remove()
    {
        super.remove();
        for (final LazyOptional<? extends IItemHandler> wrapper : this.wrappers)
            wrapper.invalidate();
    }

    @Override
    public ItemStack removeStackFromSlot(final int arg0)
    {
        return this.inventory.remove(arg0);
    }

    @Override
    public void setCraftMatrix(final PoweredCraftingInventory matrix)
    {
        this.craftMatrix = matrix;
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack)
    {
        this.check = true;
        if (stack.isEmpty()) this.getInventory().set(index, ItemStack.EMPTY);
        else this.getInventory().set(index, stack);
    }

    @Override
    public void setProcess(final PoweredProcess process)
    {
        this.currentProcess = process;
        if (process == null) this.setProgress(0);
    }

    @Override
    public void setProgress(final int progress)
    {
        this.progress.set(progress);
    }

    @Override
    public void tick()
    {
        if (this.world.isRemote) return;
        this.checkRecipes();
    }

    @Override
    public CompoundNBT write(final CompoundNBT nbt)
    {
        super.write(nbt);
        InvHelper.save(this, nbt);
        if (this.getProcess() != null) nbt.put("progress", this.getProcess().save());
        return nbt;
    }
}
