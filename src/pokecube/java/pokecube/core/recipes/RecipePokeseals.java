package pokecube.core.recipes;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import pokecube.api.effects.IMoveAnimation;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.api.items.PokesealContents;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.effects.presets.AnimationPowder;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.item.ItemList;

public class RecipePokeseals extends CustomRecipe
{
    public static final ResourceLocation ANYDYE = ResourceLocation.fromNamespaceAndPath("c", "dyes");
    public static final ResourceLocation[] DYES = new ResourceLocation[DyeColor.values().length];

    public static Map<String, Function<Tag, IMoveAnimation>> POKESEAL_EFFECTS = new HashMap<>();

    static
    {
        for (final DyeColor colour : DyeColor.values())
        {
            final ResourceLocation dyeTag = ResourceLocation.fromNamespaceAndPath("c",
                    "dyes/" + colour.name().toLowerCase(Locale.ROOT));
            RecipePokeseals.DYES[colour.getId()] = dyeTag;
        }

        POKESEAL_EFFECTS.put("dye", tag -> {
            if (tag instanceof IntTag intTag)
            {
                var id = intTag.getAsInt();
                int colour = DyeColor.byId(id).getTextColor();
                var powder = new AnimationPowder();
                var json = new JsonObject();
                json.add("v_y", new JsonPrimitive("0"));
                powder.init(json);
                powder.values.density = 0.2f;
                powder.values.width = 0.5f;
                powder.values.rgba = colour | 0xFF000000;
                powder.setDuration(PokecubeCore.getConfig().exitCubeDuration);
                return powder;
            }
            return null;
        });

        POKESEAL_EFFECTS.put("Leaves", tag -> {
            var powder = new AnimationPowder();
            var json = new JsonObject();
            json.add("v_y", new JsonPrimitive("0"));
            powder.init(json);
            powder.values.density = 1.2f;
            powder.values.width = 0.25f;
            powder.values.particle = "leaf";
            powder.setDuration(PokecubeCore.getConfig().exitCubeDuration);
            return powder;
        });

        POKESEAL_EFFECTS.put("Flames", tag -> {
            var powder = new AnimationPowder();
            var json = new JsonObject();
            json.add("v_y", new JsonPrimitive("0"));
            powder.init(json);
            powder.values.density = 1.2f;
            powder.values.width = 0.25f;
            powder.values.particle = "flame";
            powder.setDuration(PokecubeCore.getConfig().exitCubeDuration);
            return powder;
        });

        POKESEAL_EFFECTS.put("Bubbles", tag -> {
            var powder = new AnimationPowder();
            var json = new JsonObject();
            json.add("v_y", new JsonPrimitive("0"));
            powder.init(json);
            powder.values.density = 1.2f;
            powder.values.width = 0.25f;
            powder.values.particle = "bubble";
            powder.setDuration(PokecubeCore.getConfig().exitCubeDuration);
            return powder;
        });

        POKESEAL_EFFECTS.put("Shiny", tag -> {
            var powder = new AnimationPowder();
            var json = new JsonObject();
            json.add("v_y", new JsonPrimitive("0"));
            powder.init(json);
            powder.values.density = 1.7f;
            powder.values.width = 0.25f;
            powder.values.particle = "happy_villager";
            powder.setDuration(PokecubeCore.getConfig().exitCubeDuration);
            return powder;
        });
    }

    public static ItemStack process(final ItemStack cube, final ItemStack seal)
    {
        var sealData = PokemobCaps.getPokeseal(seal);
        if (sealData == null) return cube;
        PokemobCaps.updatePokeseal(cube, new PokesealContents(sealData.tag().copy()));
        return cube;
    }

    public RecipePokeseals(CraftingBookCategory category)
    {
        super(category);
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 1;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, Provider access)
    {
        ItemStack toCraft = new ItemStack(PokecubeItems.getEmptyCube(PokecubeBehaviour.POKESEAL), 1);
        final CompoundTag tag1 = new CompoundTag();
        boolean dye;
        for (int l1 = 0; l1 < inv.size(); ++l1)
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
        PokemobCaps.updatePokeseal(toCraft, new PokesealContents(tag1));
        return toCraft;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return RecipeHandler.APPLYSEAL.get();
    }

    @Override
    public boolean matches(final CraftingInput inv, final Level worldIn)
    {
        int cube = 0;
        int addons = 0;
        boolean dye;
        for (int k1 = 0; k1 < inv.size(); ++k1)
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
            if (!itemstack.isEmpty()) if (itemstack.getItem() == PokecubeItems.getEmptyCube(PokecubeBehaviour.POKESEAL)
                    && !PokecubeManager.isFilled(itemstack))
                ++cube;
            else if (itemstack.getItem() == Items.WATER_BUCKET) ++addons;
            else if (itemstack.getItem() == Items.COAL) ++addons;
        }
        return cube == 1 && addons > 0;
    }
}
