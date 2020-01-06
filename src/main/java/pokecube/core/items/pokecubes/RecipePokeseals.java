package pokecube.core.items.pokecubes;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.utils.TagNames;

public class RecipePokeseals extends SpecialRecipe
{
    public static final IRecipeSerializer<RecipePokeseals> SERIALIZER = IRecipeSerializer.register(
            "pokecube:seal_apply", new SpecialRecipeSerializer<>(RecipePokeseals::new));

    public static ItemStack process(final ItemStack cube, final ItemStack seal)
    {
        if (!seal.hasTag()) return cube;
        final CompoundNBT pokecubeTag = TagNames.getPokecubePokemobTag(cube.getTag()).getCompound(TagNames.VISUALSTAG)
                .getCompound(TagNames.POKECUBE);
        if (!pokecubeTag.contains("tag")) pokecubeTag.put("tag", new CompoundNBT());
        final CompoundNBT cubeTag = pokecubeTag.getCompound("tag");
        cubeTag.put(TagNames.POKESEAL, seal.getTag().getCompound(TagNames.POKESEAL));
        return cube;
    }

    public RecipePokeseals(final ResourceLocation idIn)
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
        final ItemStack toCraft = new ItemStack(PokecubeItems.getEmptyCube(PokecubeBehavior.POKESEAL), 1);
        final CompoundNBT tag = new CompoundNBT();
        final CompoundNBT tag1 = new CompoundNBT();
        for (int l1 = 0; l1 < inv.getSizeInventory(); ++l1)
        {
            final ItemStack itemstack2 = inv.getStackInSlot(l1);
            if (!itemstack2.isEmpty())
            {
                if (itemstack2.getItem() == Items.COAL) tag1.putBoolean("Flames", true);
                if (itemstack2.getItem() == Items.WATER_BUCKET) tag1.putBoolean("Bubbles", true);
            }
        }
        tag.put(TagNames.POKESEAL, tag1);
        toCraft.setTag(tag);
        return toCraft;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return RecipePokeseals.SERIALIZER;
    }

    @Override
    public boolean matches(final CraftingInventory inv, final World worldIn)
    {
        int cube = 0;
        // int paper = 0;
        // int gunpowder = 0;
        // int dye = 0;
        // int fireworkcharge = 0;
        // int sparklystuff = 0;
        // int boomboomstuff = 0;
        int addons = 0;

        for (int k1 = 0; k1 < inv.getSizeInventory(); ++k1)
        {
            final ItemStack itemstack = inv.getStackInSlot(k1);

            if (!itemstack.isEmpty()) if (itemstack.getItem() == PokecubeItems.getEmptyCube(PokecubeBehavior.POKESEAL)
                    && PokecubeManager.isFilled(itemstack) == false) ++cube;
            else if (itemstack.getItem() == Items.WATER_BUCKET) ++addons;
            else if (itemstack.getItem() == Items.COAL) ++addons;
            // else if (itemstack.getItem() == Items.FEATHER) ++boomboomstuff;
            // else if (itemstack.getItem() == Items.GOLD_NUGGET)
            // ++boomboomstuff;
        }
        if (cube == 1 && addons > 0) return true;
        return false;
    }
}
