package pokecube.core.database.recipes;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipeOutput;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class PokemobMoveRecipeParser implements IRecipeParser
{
    private static class MoveMatcher implements Predicate<String>
    {
        String type;

        int minPower = 0;
        int maxPower = 300;

        boolean contact = true;
        boolean ranged  = true;

        @Override
        public boolean test(final String t)
        {
            final Move_Base move = MovesUtils.getMoveFromName(t);
            final PokeType ptype = PokeType.getType(this.type);
            if (ptype == null) return false;
            if (move == null) return false;
            if (move.move.type != ptype) return false;
            if (!this.contact && (move.getAttackCategory() & IMoveConstants.CATEGORY_CONTACT) > 0) return false;
            if (!this.ranged && (move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) > 0) return false;
            final int power = move.getPWR();
            return power >= this.minPower && power <= this.maxPower;
        }

    }

    private static class WrappedRecipeMove implements IMoveAction
    {
        public IMoveAction parent;
        public IMoveAction other;

        public WrappedRecipeMove(final IMoveAction parent, final IMoveAction other)
        {
            this.parent = parent;
            this.other = other;
        }

        @Override
        public boolean applyEffect(final IPokemob user, final Vector3 location)
        {
            // Only applies other action if parent action failed.
            return this.parent.applyEffect(user, location) || this.other.applyEffect(user, location);
        }

        @Override
        public String getMoveName()
        {
            return this.parent.getMoveName();
        }

        @Override
        public void init()
        {
            this.parent.init();
            this.other.init();
        }

    }

    private static class RecipeAction implements IMoveAction
    {
        public final String name;

        public final RecipeMove recipe;

        public RecipeAction(final String name, final RecipeMove recipe)
        {
            this.name = name;
            this.recipe = recipe;
        }

        @Override
        public boolean applyEffect(final IPokemob user, final Vector3 location)
        {
            return this.recipe.applyEffect(user, location, this.getMoveName());
        }

        @Override
        public String getMoveName()
        {
            return this.name;
        }

    }

    public static class RecipeMove
    {

        public static final List<RecipeMove> ALLRECIPES = Lists.newArrayList();

        public static int uid = 0;

        public final ShapelessRecipe recipe;

        public final int hungerCost;

        public final List<RecipeAction> actions = Lists.newArrayList();

        final Container c = new Container(null, 0)
        {
            @Override
            public boolean stillValid(final PlayerEntity playerIn)
            {
                return false;
            }
        };

        public RecipeMove(final JsonObject json)
        {
            this.hungerCost = this.costFromJson(json);
            final ItemStack recipeOutputIn = this.outputFromJson(json);
            final NonNullList<Ingredient> recipeItemsIn = XMLRecipeHandler.getInputItems(json);
            this.recipe = new ShapelessRecipe(new ResourceLocation("pokecube:loaded_" + RecipeMove.uid++),
                    "pokecube_moves", recipeOutputIn, recipeItemsIn);
            RecipeMove.ALLRECIPES.add(this);
            final List<String> names = Lists.newArrayList();
            this.namesFromJson(json, names);
            for (final String name : names)
                this.actions.add(new RecipeAction(name, this));
        }

        private ItemStack outputFromJson(final JsonObject json)
        {
            ItemStack ret = ItemStack.EMPTY;
            final JsonObject output = json.get("output").getAsJsonObject();
            // New way, try vanilla parsing
            try
            {
                ret = ShapedRecipe.itemFromJson(output);
            }
            catch (final Exception e)
            {
                ret = ItemStack.EMPTY;
            }
            if (ret.isEmpty())
            {
                PokecubeCore.LOGGER.warn("Warning, Recipe {} using old output way!", json);
                final XMLRecipeOutput oldRes = PokedexEntryLoader.gson.fromJson(output, XMLRecipeOutput.class);
                ret = Tools.getStack(oldRes.getValues());
            }
            if (ret.isEmpty()) PokecubeCore.LOGGER.warn("Warning, Recipe {} has no output!", json);
            return ret;
        }

        private int costFromJson(final JsonObject json)
        {
            // New way
            if (json.has("hungerCost")) return json.get("hungerCost").getAsInt();
            // Old way
            if (json.has("values") && json.get("values").getAsJsonObject().has("cost")) return Integer.parseInt(json
                    .get("values").getAsJsonObject().get("cost").getAsString());
            return 50;
        }

        private void namesFromJson(final JsonObject json, final List<String> names)
        {
            // New way
            if (json.has("move")) names.add(json.get("move").getAsString());
            if (json.has("moves"))
            {
                final String[] args = json.get("moves").getAsString().split(",");
                for (final String s : args)
                    names.add(s);
            }
            if (json.has("match"))
            {
                MoveMatcher match;
                final JsonElement matchElement = json.get("match");
                if (matchElement.isJsonObject()) match = PokedexEntryLoader.gson.fromJson(matchElement,
                        MoveMatcher.class);
                else
                {
                    final String matchstring = json.get("match").getAsString();
                    match = PokedexEntryLoader.gson.fromJson(matchstring, MoveMatcher.class);
                }
                for (final String s : MovesUtils.getKnownMoveNames())
                    if (match.test(s)) names.add(s);
            }

            // Old way
            if (json.has("values"))
            {
                final JsonObject values = json.get("values").getAsJsonObject();
                this.namesFromJson(values, names);
            }
        }

        public boolean applyEffect(final IPokemob user, final Vector3 location, final String name)
        {
            return this.attemptCraft(user, location) || this.attemptWorldCraft(user, location, name);
        }

        public boolean attemptWorldCraft(final IPokemob user, final Vector3 location, final String name)
        {
            // Things below here all actually damage blocks, so check this.
            if (!MoveEventsHandler.canAffectBlock(user, location, name, false, true)) return false;
            // This should look at the block hit, and attempt to craft that into
            // a shapeless recipe.
            final World world = user.getEntity().getCommandSenderWorld();
            final BlockState block = location.getBlockState(world);
            if (block == null || world.isEmptyBlock(location.getPos())) return false;
            final ItemStack item = new ItemStack(block.getBlock());
            final CraftingInventory inven = new CraftingInventory(this.c, 1, 1);
            inven.setItem(0, item);
            if (!this.recipe.matches(inven, world)) return false;
            final ItemStack stack = this.recipe.assemble(inven);
            if (stack.isEmpty()) return false;
            final Block toSet = Block.byItem(stack.getItem());
            if (toSet == Blocks.AIR)
            {
                final ItemEntity drop = new ItemEntity(world, location.x, location.y, location.z, stack);
                world.addFreshEntity(drop);
            }
            location.setBlock(world, toSet.defaultBlockState());
            return true;
        }

        private int tryCraft(final List<ItemStack> items, final Vector3 location, final World world, int depth)
        {
            boolean allMatch = false;
            final List<ItemStack> toUse = Lists.newArrayList();
            for (final Ingredient i : this.recipe.getIngredients())
            {
                boolean matched = false;
                for (final ItemStack item : items)
                    if (i.test(item))
                    {
                        matched = true;
                        toUse.add(item);
                        break;
                    }
                allMatch = matched;
                if (!matched) break;
            }
            if (!allMatch) return depth;
            final CraftingInventory inven = new CraftingInventory(this.c, 1, toUse.size());
            for (int i = 0; i < toUse.size(); i++)
                inven.setItem(i, toUse.get(i));
            if (!this.recipe.matches(inven, world)) return depth;
            final ItemStack stack = this.recipe.assemble(inven);
            if (stack.isEmpty()) return depth;
            final List<ItemStack> remains = this.recipe.getRemainingItems(inven);
            toUse.forEach(e ->
            {
                final ItemStack item = e;
                item.shrink(1);
            });
            ItemEntity drop = new ItemEntity(world, location.x, location.y, location.z, stack);
            world.addFreshEntity(drop);
            depth++;
            for (final ItemStack left : remains)
                if (!left.isEmpty())
                {
                    drop = new ItemEntity(world, location.x, location.y, location.z, left);
                    world.addFreshEntity(drop);
                }
            // Do this until we run out of craftable stuff.
            depth = this.tryCraft(toUse, location, world, depth);
            return depth;
        }

        public boolean attemptCraft(final IPokemob attacker, final Vector3 location)
        {
            // This should look for items near the location, and try to stuff
            // them into a shapeless recipe.
            final World world = attacker.getEntity().getCommandSenderWorld();
            final List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, location.getAABB().inflate(2));
            final List<ItemStack> stacks = Lists.newArrayList();
            items.forEach(e -> stacks.add(e.getItem()));
            final int depth = this.tryCraft(stacks, location, world, 0);
            attacker.applyHunger(this.hungerCost * depth);
            return depth > 0;
        }

    }

    public PokemobMoveRecipeParser()
    {
    }

    @Override
    public void manageRecipe(final JsonObject json) throws NullPointerException
    {
        final RecipeMove loaded = new RecipeMove(json);
        for (IMoveAction action : loaded.actions)
        {
            if (MoveEventsHandler.customActions.containsKey(action.getMoveName()))
            {
                final IMoveAction prev = MoveEventsHandler.customActions.get(action.getMoveName());
                if (prev instanceof WrappedRecipeMove)
                {
                    final WrappedRecipeMove edit = (WrappedRecipeMove) prev;
                    edit.other = action;
                    action = prev;
                }
                else action = new WrappedRecipeMove(MoveEventsHandler.customActions.get(action.getMoveName()), action);
            }
            MoveEventsHandler.customActions.put(action.getMoveName(), action);
        }
    }

    @Override
    public void init()
    {
        RecipeMove.ALLRECIPES.clear();
        RecipeMove.uid = 0;
    }

}
