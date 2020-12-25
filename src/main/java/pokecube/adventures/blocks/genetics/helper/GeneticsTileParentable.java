package pokecube.adventures.blocks.genetics.helper;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredProcess;
import pokecube.adventures.blocks.genetics.helper.recipe.PoweredRecipe;

public abstract class GeneticsTileParentable extends BaseGeneticsTile
{

    public GeneticsTileParentable(final TileEntityType<?> tileEntityTypeIn, final int size, final int output)
    {
        super(tileEntityTypeIn, size, output);
    }

    public abstract BaseGeneticsTile getParent();

    @Override
    public ItemStack getStackInSlot(final int arg0)
    {
        if (this.getParent() != null) return this.getParent().getStackInSlot(arg0);
        return super.getStackInSlot(arg0);
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack stack)
    {
        if (this.getParent() != null) this.getParent().setInventorySlotContents(index, stack);
        super.setInventorySlotContents(index, stack);
        this.progress = 0;
        this.total = 0;
    }

    @Override
    public boolean canExtractItem(final int index, final ItemStack stack, final Direction direction)
    {
        if (this.getParent() != null) return this.getParent().canExtractItem(index, stack, direction);
        return super.canExtractItem(index, stack, direction);
    }

    @Override
    public boolean canInsertItem(final int index, final ItemStack itemStackIn, final Direction direction)
    {
        if (this.getParent() != null) return this.getParent().canInsertItem(index, itemStackIn, direction);
        return super.canInsertItem(index, itemStackIn, direction);
    }

    @Override
    public boolean canExtract()
    {
        if (this.getParent() != null) return this.getParent().canExtract();
        return super.canExtract();
    }

    @Override
    public boolean canReceive()
    {
        if (this.getParent() != null) return this.getParent().canReceive();
        return super.canReceive();
    }

    @Override
    public void closeInventory(final PlayerEntity player)
    {
        if (this.getParent() != null) this.getParent().closeInventory(player);
        super.closeInventory(player);
    }

    @Override
    public int count(final Item itemIn)
    {
        if (this.getParent() != null) return this.getParent().count(itemIn);
        return super.count(itemIn);
    }

    @Override
    public ItemStack decrStackSize(final int arg0, final int arg1)
    {
        this.progress = 0;
        this.total = 0;
        if (this.getParent() != null) return this.getParent().decrStackSize(arg0, arg1);
        return super.decrStackSize(arg0, arg1);
    }

    @Override
    public int extractEnergy(final int maxExtract, final boolean simulate)
    {
        if (this.getParent() != null) return this.getParent().extractEnergy(maxExtract, simulate);
        return super.extractEnergy(maxExtract, simulate);
    }

    @Override
    public List<ItemStack> getInventory()
    {
        if (this.getParent() != null) return this.getParent().getInventory();
        return super.getInventory();
    }

    @Override
    public boolean isValid(final Class<? extends PoweredRecipe> recipe)
    {
        if (this.getParent() != null) return this.getParent().isValid(recipe);
        return false;
    }

    @Override
    public void checkRecipes()
    {
        // We do not process recipes otherwise
        if (this.getParent() == null) super.checkRecipes();
    }

    @Override
    public void tick()
    {
        // We do not tick otherwise
        if (this.getParent() == null) super.checkRecipes();
    }

    @Override
    public PoweredCraftingInventory getCraftMatrix()
    {
        if (this.getParent() != null) return this.getParent().getCraftMatrix();
        return super.getCraftMatrix();
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
    {
        if (this.getParent() != null) return this.getParent().getCapability(capability, facing);
        return super.getCapability(capability, facing);
    }

    @Override
    public int getEnergyStored()
    {
        if (this.getParent() != null) return super.getEnergyStored();
        return super.getEnergyStored();
    }

    @Override
    public int getInventoryStackLimit()
    {
        if (this.getParent() != null) return this.getParent().getInventoryStackLimit();
        return super.getInventoryStackLimit();
    }

    @Override
    public List<ItemStack> getList()
    {
        if (this.getParent() != null) return this.getParent().getList();
        return super.getList();
    }

    @Override
    public int getMaxEnergyStored()
    {
        if (this.getParent() != null) return this.getParent().getMaxEnergyStored();
        return super.getMaxEnergyStored();
    }

    @Override
    public int getOutputSlot()
    {
        if (this.getParent() != null) return this.getParent().getOutputSlot();
        return super.getOutputSlot();
    }

    @Override
    public PoweredProcess getProcess()
    {
        if (this.getParent() != null) return this.getParent().getProcess();
        return super.getProcess();
    }

    @Override
    public int getSizeInventory()
    {
        if (this.getParent() != null) return this.getParent().getSizeInventory();
        return super.getSizeInventory();
    }

    @Override
    public int[] getSlotsForFace(final Direction side)
    {
        if (this.getParent() != null) return this.getParent().getSlotsForFace(side);
        return super.getSlotsForFace(side);
    }

    @Override
    public void setProgress(final int progress)
    {
        if (this.getParent() != null) this.getParent().setProgress(progress);
        else super.setProgress(progress);
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack stack)
    {
        if (this.getParent() != null) return this.getParent().isItemValidForSlot(index, stack);
        return super.isItemValidForSlot(index, stack);
    }

    @Override
    public void setCraftMatrix(final PoweredCraftingInventory matrix)
    {
        if (this.getParent() != null) this.getParent().setCraftMatrix(matrix);
        else super.setCraftMatrix(matrix);
    }

    @Override
    public boolean isUsableByPlayer(final PlayerEntity player)
    {
        if (this.getParent() != null) return this.getParent().isUsableByPlayer(player);
        return super.isUsableByPlayer(player);
    }

    @Override
    public PlayerEntity getUser()
    {
        if (this.getParent() != null) return this.getParent().getUser();
        return super.getUser();
    }

    @Override
    public void setProcess(final PoweredProcess process)
    {
        if (this.getParent() != null) this.getParent().setProcess(process);
        else super.setProcess(process);
    }

    @Override
    public void openInventory(final PlayerEntity player)
    {
        if (this.getParent() != null) this.getParent().openInventory(player);
        else super.openInventory(player);
    }

    @Override
    public ItemStack removeStackFromSlot(final int arg0)
    {
        if (this.getParent() != null) return this.getParent().removeStackFromSlot(arg0);
        return super.removeStackFromSlot(arg0);
    }

    @Override
    public int receiveEnergy(final int maxReceive, final boolean simulate)
    {
        if (this.getParent() != null) return this.getParent().receiveEnergy(maxReceive, simulate);
        return super.receiveEnergy(maxReceive, simulate);
    }
}
