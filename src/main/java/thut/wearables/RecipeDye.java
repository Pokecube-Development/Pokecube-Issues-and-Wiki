package thut.wearables;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeDye extends SpecialRecipe
{
    private static Map<DyeColor, ITag<Item>> DYETAGS = Maps.newHashMap();

    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS, ThutWearables.MODID);

    public static final RegistryObject<SpecialRecipeSerializer<RecipeDye>> SERIALIZER = RecipeDye.RECIPE_SERIALIZERS
            .register("dye", RecipeDye.special(RecipeDye::new));

    private static <T extends IRecipe<?>> Supplier<SpecialRecipeSerializer<T>> special(
            final Function<ResourceLocation, T> create)
    {
        return () -> new SpecialRecipeSerializer<>(create);
    }

    public static Map<DyeColor, ITag<Item>> getDyeTagMap()
    {
        if (RecipeDye.DYETAGS.isEmpty()) for (final DyeColor colour : DyeColor.values())
        {
            final ResourceLocation tag = new ResourceLocation("forge", "dyes/" + colour.getName());
            RecipeDye.DYETAGS.put(colour, ItemTags.getAllTags().getTagOrEmpty(tag));
        }
        return RecipeDye.DYETAGS;
    }

    public RecipeDye(final ResourceLocation idIn)
    {
        super(idIn);
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 1;
    }

    @Override
    public ItemStack assemble(final CraftingInventory inv)
    {
        ItemStack wearable = ItemStack.EMPTY;
        ItemStack dye = ItemStack.EMPTY;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            IWearable wear = stack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null);
            if (wear == null && stack.getItem() instanceof IWearable) wear = (IWearable) stack.getItem();
            if (wear != null && wear.dyeable(stack))
            {
                wearable = stack;
                continue;
            }
            final ITag<Item> dyeTag = Tags.Items.DYES;
            if (stack.getItem().is(dyeTag))
            {
                dye = stack;
                continue;
            }
            return ItemStack.EMPTY;
        }
        final ItemStack output = wearable.copy();
        if (!output.hasTag()) output.setTag(new CompoundNBT());
        DyeColor dyeColour = null;

        final Map<DyeColor, ITag<Item>> tags = RecipeDye.getDyeTagMap();
        for (final DyeColor colour : DyeColor.values())
            if (dye.getItem().is(tags.get(colour)))
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
        final NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack> withSize(inv.getContainerSize(),
                ItemStack.EMPTY);
        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            final ItemStack itemstack = inv.getItem(i);
            nonnulllist.set(i, this.toKeep(i, itemstack, inv));
        }
        return nonnulllist;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return RecipeDye.SERIALIZER.get();
    }

    @Override
    public boolean matches(final CraftingInventory inv, final World worldIn)
    {
        boolean wearable = false;
        boolean dye = false;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            IWearable wear = stack.getCapability(ThutWearables.WEARABLE_CAP, null).orElse(null);
            if (wear == null && stack.getItem() instanceof IWearable) wear = (IWearable) stack.getItem();
            if (wear != null && wear.dyeable(stack))
            {
                if (wearable) return false;
                wearable = true;
                continue;
            }
            final ITag<Item> dyeTag = Tags.Items.DYES;
            if (stack.getItem().is(dyeTag))
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
