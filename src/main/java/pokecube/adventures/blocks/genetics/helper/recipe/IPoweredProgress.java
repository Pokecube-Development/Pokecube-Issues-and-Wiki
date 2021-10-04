package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.List;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;

public interface IPoweredProgress extends Container, IEnergyStorage
{
    PoweredCraftingInventory getCraftMatrix();

    List<ItemStack> getList();

    int getOutputSlot();

    PoweredProcess getProcess();

    Player getUser();

    boolean isValid(Class<? extends PoweredRecipe> recipe);

    void setCraftMatrix(PoweredCraftingInventory matrix);

    void setProcess(PoweredProcess process);

    void setProgress(int progress);
}
