package pokecube.core.database.recipes;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import pokecube.core.PokecubeCore;
import pokecube.core.database.pokedex.PokedexEntryLoader.Drop;
import pokecube.core.utils.Tools;
import thut.api.util.JsonUtil;
import thut.core.xml.bind.annotation.XmlElement;

public class XMLRecipeHandler
{
    public static class XMLRecipe
    {
        public boolean shapeless = false;
        String handler = "default";
        public String map = "";
        public XMLRecipeOutput output;
        public List<XMLRecipeInput> inputs = Lists.newArrayList();
        public Map<String, String> values = Maps.newHashMap();

        @Override
        public String toString()
        {
            return "output: " + this.output + " inputs: " + this.inputs + " shapeless: " + this.shapeless + " map: "
                    + this.map;
        }
    }

    public static class XMLRecipeInput extends Drop
    {
        public String key = "";

        @Override
        public String toString()
        {
            return "values: " + this.values + " tag: " + this.tag + " key: " + this.key;
        }
    }

    public static class XMLRecipeOutput extends Drop
    {
        @Override
        public String toString()
        {
            return "values: " + this.values + " tag: " + this.tag;
        }
    }

    public static class XMLRecipes
    {
        @XmlElement(name = "Recipe")
        public List<XMLRecipe> recipes = Lists.newArrayList();
    }

    public static Map<String, IRecipeParser> recipeParsers = Maps.newHashMap();

    static
    {
        XMLRecipeHandler.recipeParsers.put("pokecube:move_recipe", new PokemobMoveRecipeParser());
    }

    private static NonNullList<Ingredient> itemsFromJson(final JsonArray array)
    {
        final NonNullList<Ingredient> nonnulllist = NonNullList.create();
        for (final JsonElement e : array)
        {
            Ingredient ingredient = null;
            if (e.isJsonObject() && e.getAsJsonObject().has("nbt"))
                ingredient = Ingredient.of(CraftingHelper.getItemStack(e.getAsJsonObject(), true));
            else ingredient = Ingredient.fromJson(e);
            if (!ingredient.isEmpty()) nonnulllist.add(ingredient);
        }
        return nonnulllist;
    }

    public static NonNullList<Ingredient> getInputItems(final JsonObject json)
    {
        final NonNullList<Ingredient> recipeItemsIn = NonNullList.create();
        final JsonElement inputs = json.get("inputs");
        if (inputs.isJsonArray())
        {
            try
            {
                final NonNullList<Ingredient> nonnulllist = XMLRecipeHandler.itemsFromJson(inputs.getAsJsonArray());
                // New way
                if (!nonnulllist.isEmpty()) return nonnulllist;
            }
            catch (final Exception e1)
            {}

            PokecubeCore.LOGGER.warn("Warning, Recipe {} using old inputs way!", json);

            // Old way
            for (final JsonElement e : inputs.getAsJsonArray())
            {
                final XMLRecipeInput value = JsonUtil.gson.fromJson(e, XMLRecipeInput.class);
                if (value.id == null) value.id = value.getValues().get("id");
                // Tag
                if (value.id.startsWith("#"))
                {
                    final ResourceLocation id = new ResourceLocation(value.id.replaceFirst("#", ""));
                    final Tag<Item> tag = ItemTags.getAllTags().getTagOrEmpty(id);
                    recipeItemsIn.add(Ingredient.of(tag));
                }
                else recipeItemsIn.add(Ingredient.of(Tools.getStack(value.getValues())));
            }
            if (recipeItemsIn.isEmpty()) PokecubeCore.LOGGER.warn("Warning, Recipe {} has no inputs!", json);
        }
        return recipeItemsIn;
    }

    public static void addRecipe(final JsonObject jsonObject)
    {
        if (!(jsonObject.has("handler") || jsonObject.has("type"))) return;
        try
        {
            final JsonElement type = jsonObject.has("handler") ? jsonObject.get("handler") : jsonObject.get("type");
            final String handler = type.getAsString();
            final IRecipeParser parser = XMLRecipeHandler.recipeParsers.get(handler);
            PokecubeCore.LOGGER.debug("Recipe Handler: " + handler + " Parser: " + parser);
            parser.manageRecipe(jsonObject);
        }
        catch (final NullPointerException e)
        {
            PokecubeCore.LOGGER.error("Error with a recipe, Error for: " + jsonObject, e);
        }
    }
}
