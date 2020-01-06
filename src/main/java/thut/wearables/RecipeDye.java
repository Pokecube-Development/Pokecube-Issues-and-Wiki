package thut.wearables;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

public class RecipeDye extends SpecialRecipe
{
    private static Map<DyeColor, Tag<Item>> DYETAGS = Maps.newHashMap();

    public static final IRecipeSerializer<RecipeDye> SERIALIZER = IRecipeSerializer.register("thut_wearables:dye",
            new SpecialRecipeSerializer<>(RecipeDye::new));

    public static Map<DyeColor, Tag<Item>> getDyeTagMap()
    {
        if (RecipeDye.DYETAGS.isEmpty()) for (final DyeColor colour : DyeColor.values())
        {
            final ResourceLocation tag = new ResourceLocation("forge", "dyes/" + colour.getName());
            RecipeDye.DYETAGS.put(colour, ItemTags.getCollection().getOrCreate(tag));
        }
        return RecipeDye.DYETAGS;
    }

    public RecipeDye(final ResourceLocation idIn)
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
        ItemStack wearable = ItemStack.EMPTY;
        ItemStack dye = ItemStack.EMPTY;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            final ItemStack stack = inv.getStackInSlot(i);
            IWearable wear = stack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null);
            if (wear == null && stack.getItem() instanceof IWearable) wear = (IWearable) stack.getItem();
            if (wear != null && wear.dyeable(stack))
            {
                wearable = stack;
                continue;
            }
            final Tag<Item> dyeTag = Tags.Items.DYES;
            if (stack.getItem().isIn(dyeTag))
            {
                dye = stack;
                continue;
            }
            return ItemStack.EMPTY;
        }
        final ItemStack output = wearable.copy();
        if (!output.hasTag()) output.setTag(new CompoundNBT());
        DyeColor dyeColour = null;

        final Map<DyeColor, Tag<Item>> tags = RecipeDye.getDyeTagMap();
        for (final DyeColor colour : DyeColor.values())
            if (dye.getItem().isIn(tags.get(colour)))
            {
                dyeColour = colour;
                break;
            }
        output.getTag().putInt("dyeColour", dyeColour.getId());
        return output;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInventory inv)
    {
        final NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack> withSize(inv.getSizeInventory(),
                ItemStack.EMPTY);
        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            final ItemStack itemstack = inv.getStackInSlot(i);
            nonnulllist.set(i, this.toKeep(i, itemstack, inv));
        }
        return nonnulllist;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return RecipeDye.SERIALIZER;
    }

    @Override
    public boolean matches(final CraftingInventory inv, final World worldIn)
    {
        boolean wearable = false;
        boolean dye = false;
        for (int i = 0; i < inv.getSizeInventory(); i++)
        {
            final ItemStack stack = inv.getStackInSlot(i);

            IWearable wear = stack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null);
            if (wear == null && stack.getItem() instanceof IWearable) wear = (IWearable) stack.getItem();
            if (wear != null && wear.dyeable(stack))
            {
                if (wearable) return false;
                wearable = true;
                continue;
            }
            final Tag<Item> dyeTag = Tags.Items.DYES;
            if (stack.getItem().isIn(dyeTag))
            {
                if (dye) return false;
                dye = true;
                continue;
            }
            return false;
        }
        return dye && wearable;
    }

    public ItemStack toKeep(final int slot, final ItemStack stackIn, final CraftingInventory inv)
    {
        return net.minecraftforge.common.ForgeHooks.getContainerItem(stackIn);
    }

}
