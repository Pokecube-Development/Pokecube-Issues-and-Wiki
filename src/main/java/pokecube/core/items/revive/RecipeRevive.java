package pokecube.core.items.revive;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;
import thut.api.item.ItemList;

public class RecipeRevive extends SpecialRecipe
{
    public static final IRecipeSerializer<RecipeRevive> SERIALIZER = new SpecialRecipeSerializer<>(RecipeRevive::new);
    public static final ResourceLocation                REVIVETAG  = new ResourceLocation("pokecube:revive");

    public RecipeRevive(final ResourceLocation idIn)
    {
        super(idIn);
    }

    @Override
    public boolean canFit(final int width, final int height)
    {
        return width * height > 1;
    }

    @Override
    public ItemStack getCraftingResult(final CraftingInventory inv)
    {
        ItemStack healed = ItemStack.EMPTY;
        boolean revive = false;
        boolean pokeseal = false;
        ItemStack other = ItemStack.EMPTY;
        ItemStack seal = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            final ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty())
            {
                if (PokecubeManager.isFilled(stack)) other = stack;
                if (ItemList.is(RecipeRevive.REVIVETAG, stack)) revive = true;
                if (stack.getItem() == PokecubeItems.getEmptyCube(PokecubeBehavior.POKESEAL)) seal = stack;
            }
        }
        revive = revive && !other.isEmpty();
        pokeseal = !seal.isEmpty() && !other.isEmpty();

        if (pokeseal)
        {
            if (seal.hasTag())
            {
                final IPokemob mob = PokecubeManager.itemToPokemob(other, PokecubeCore.proxy.getWorld());
                final CompoundNBT tag = seal.getTag().getCompound(TagNames.POKESEAL);
                final CompoundNBT mobtag = mob.getEntity().getPersistentData();
                mobtag.put("sealtag", tag);
                other = PokecubeManager.pokemobToItem(mob);
                healed = other;
            }
        }
        else if (revive)
        {
            final ItemStack stack = other;
            if (PokecubeManager.isFilled(stack))
            {
                healed = stack.copy();
                PokecubeManager.heal(healed, PokecubeCore.proxy.getWorld());
            }
        }
        return healed;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return RecipeRevive.SERIALIZER;
    }

    @Override
    public boolean matches(final CraftingInventory inv, final World worldIn)
    {
        boolean revive = false;
        boolean pokeseal = false;
        ItemStack other = ItemStack.EMPTY;
        ItemStack seal = ItemStack.EMPTY;

        int n = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            final ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty())
            {
                n++;
                if (PokecubeManager.isFilled(stack)) other = stack;
                if (ItemList.is(RecipeRevive.REVIVETAG, stack)) revive = true;
                if (stack.getItem() == PokecubeItems.getEmptyCube(PokecubeBehavior.POKESEAL)) seal = stack;
            }
        }
        revive = revive && !other.isEmpty();
        pokeseal = !seal.isEmpty() && !other.isEmpty();
        if (n != 2) return false;
        return pokeseal || revive && other.getDamage() == 0;
    }
}
