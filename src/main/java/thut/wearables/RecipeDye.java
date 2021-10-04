package thut.wearables;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Maps;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeDye extends CustomRecipe
{
    private static Map<DyeColor, Tag<Item>> DYETAGS = Maps.newHashMap();

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS, ThutWearables.MODID);

    public static final RegistryObject<SimpleRecipeSerializer<RecipeDye>> SERIALIZER = RecipeDye.RECIPE_SERIALIZERS
            .register("dye", RecipeDye.special(RecipeDye::new));

    private static <T extends Recipe<?>> Supplier<SimpleRecipeSerializer<T>> special(
            final Function<ResourceLocation, T> create)
    {
        return () -> new SimpleRecipeSerializer<>(create);
    }

    public static Map<DyeColor, Tag<Item>> getDyeTagMap()
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
    public ItemStack assemble(final CraftingContainer inv)
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
            final Tag<Item> dyeTag = Tags.Items.DYES;
            if (stack.getItem().is(dyeTag))
            {
                dye = stack;
                continue;
            }
            return ItemStack.EMPTY;
        }
        final ItemStack output = wearable.copy();
        if (!output.hasTag()) output.setTag(new CompoundTag());
        DyeColor dyeColour = null;

        final Map<DyeColor, Tag<Item>> tags = RecipeDye.getDyeTagMap();
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
    public NonNullList<ItemStack> getRemainingItems(final CraftingContainer inv)
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
    public RecipeSerializer<?> getSerializer()
    {
        return RecipeDye.SERIALIZER.get();
    }

    @Override
    public boolean matches(final CraftingContainer inv, final Level worldIn)
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
            final Tag<Item> dyeTag = Tags.Items.DYES;
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

    public ItemStack toKeep(final int slot, final ItemStack stackIn, final CraftingContainer inv)
    {
        return net.minecraftforge.common.ForgeHooks.getContainerItem(stackIn);
    }

}
