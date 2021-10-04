package pokecube.core.items.pokecubes;

import java.util.Locale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.utils.TagNames;
import thut.api.item.ItemList;

public class RecipePokeseals extends CustomRecipe
{
    public static final ResourceLocation   ANYDYE = new ResourceLocation("forge", "dyes");
    public static final ResourceLocation[] DYES   = new ResourceLocation[DyeColor.values().length];

    static
    {
        for (final DyeColor colour : DyeColor.values())
        {
            final ResourceLocation dyeTag = new ResourceLocation("forge", "dyes/" + colour.name().toLowerCase(
                    Locale.ROOT));
            RecipePokeseals.DYES[colour.getId()] = dyeTag;
        }
    }

    public static ItemStack process(final ItemStack cube, final ItemStack seal)
    {
        if (!seal.hasTag()) return cube;
        final CompoundTag pokecubeTag = TagNames.getPokecubePokemobTag(cube.getTag()).getCompound(TagNames.VISUALSTAG)
                .getCompound(TagNames.POKECUBE);
        if (!pokecubeTag.contains("id"))
        {
            final ItemStack blank = cube.copy();
            blank.setTag(new CompoundTag());
            blank.save(pokecubeTag);
        }
        if (!pokecubeTag.contains("tag")) pokecubeTag.put("tag", new CompoundTag());
        final CompoundTag cubeTag = pokecubeTag.getCompound("tag");
        cubeTag.put(TagNames.POKESEAL, seal.getTag().getCompound(TagNames.POKESEAL));
        pokecubeTag.put("tag", cubeTag);
        TagNames.getPokecubePokemobTag(cube.getTag()).getCompound(TagNames.VISUALSTAG).put(TagNames.POKECUBE,
                pokecubeTag);
        seal.grow(-1);
        return cube;
    }

    public RecipePokeseals(final ResourceLocation idIn)
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
        final ItemStack toCraft = new ItemStack(PokecubeItems.getEmptyCube(PokecubeBehavior.POKESEAL), 1);
        final CompoundTag tag = new CompoundTag();
        final CompoundTag tag1 = new CompoundTag();
        boolean dye;
        for (int l1 = 0; l1 < inv.getContainerSize(); ++l1)
        {
            final ItemStack itemstack = inv.getItem(l1);
            dye = ItemList.is(RecipePokeseals.ANYDYE, itemstack);
            if (dye)
            {
                DyeColor c = null;
                for (final DyeColor colour : DyeColor.values())
                {
                    final ResourceLocation dyeTag = RecipePokeseals.DYES[colour.getId()];
                    if (ItemList.is(dyeTag, itemstack))
                    {
                        c = colour;
                        break;
                    }
                }
                if (c != null) tag1.putInt("dye", c.getId());
            }
            if (!itemstack.isEmpty())
            {
                if (itemstack.getItem() == Items.COAL) tag1.putBoolean("Flames", true);
                if (itemstack.getItem() == Items.WATER_BUCKET) tag1.putBoolean("Bubbles", true);
            }
        }
        tag.put(TagNames.POKESEAL, tag1);
        toCraft.setTag(tag);
        return toCraft;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return RecipeHandler.APPLYSEAL.get();
    }

    @Override
    public boolean matches(final CraftingContainer inv, final Level worldIn)
    {
        int cube = 0;
        int addons = 0;
        boolean dye = false;
        for (int k1 = 0; k1 < inv.getContainerSize(); ++k1)
        {
            final ItemStack itemstack = inv.getItem(k1);
            dye = ItemList.is(RecipePokeseals.ANYDYE, itemstack);
            if (dye)
            {
                DyeColor c = null;
                for (final DyeColor colour : DyeColor.values())
                {
                    final ResourceLocation dyeTag = RecipePokeseals.DYES[colour.getId()];
                    if (ItemList.is(dyeTag, itemstack))
                    {
                        c = colour;
                        break;
                    }
                }
                if (c != null) addons++;
            }
            if (!itemstack.isEmpty()) if (itemstack.getItem() == PokecubeItems.getEmptyCube(PokecubeBehavior.POKESEAL)
                    && PokecubeManager.isFilled(itemstack) == false) ++cube;
            else if (itemstack.getItem() == Items.WATER_BUCKET) ++addons;
            else if (itemstack.getItem() == Items.COAL) ++addons;
        }
        if (cube == 1 && addons > 0) return true;
        return false;
    }
}
