package pokecube.core.items.revive;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;
import thut.api.item.ItemList;

public class RecipeRevive extends CustomRecipe
{
    public static final ResourceLocation REVIVETAG = new ResourceLocation("pokecube:revive");

    public RecipeRevive(final ResourceLocation idIn)
    {
        super(idIn);
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 1;
    }

    @Override
    public ItemStack assemble(final CraftingContainer inv)
    {
        ItemStack healed = ItemStack.EMPTY;
        boolean revive = false;
        boolean pokeseal = false;
        ItemStack other = ItemStack.EMPTY;
        ItemStack seal = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack stack = inv.getItem(i);
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
                final CompoundTag tag = seal.getTag().getCompound(TagNames.POKESEAL);
                final CompoundTag mobtag = mob.getEntity().getPersistentData();
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
    public RecipeSerializer<?> getSerializer()
    {
        return RecipeHandler.REVIVE.get();
    }

    @Override
    public boolean matches(final CraftingContainer inv, final Level worldIn)
    {
        boolean revive = false;
        boolean pokeseal = false;
        ItemStack other = ItemStack.EMPTY;
        ItemStack seal = ItemStack.EMPTY;

        int n = 0;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack stack = inv.getItem(i);
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
        return pokeseal || revive && other.getDamageValue() == 255;
    }
}
