package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;

public interface IPoweredProgress extends IInventory, IEnergyStorage
{
    PoweredCraftingInventory getCraftMatrix();

    List<ItemStack> getList();

    int getOutputSlot();

    PoweredProcess getProcess();

    PlayerEntity getUser();

    boolean isValid(Class<? extends PoweredRecipe> recipe);

    void setCraftMatrix(PoweredCraftingInventory matrix);

    void setProcess(PoweredProcess process);

    void setProgress(int progress);
}
