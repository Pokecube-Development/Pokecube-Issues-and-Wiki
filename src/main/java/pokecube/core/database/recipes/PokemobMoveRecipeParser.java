package pokecube.core.database.recipes;

import java.util.List;

import javax.xml.namespace.QName;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipeInput;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class PokemobMoveRecipeParser implements IRecipeParser
{

    private static final QName MOVENAME   = new QName("move");
    private static final QName HUNGERCOST = new QName("cost");

    public static class WrappedRecipeMove implements IMoveAction
    {
        public final IMoveAction parent;
        public final IMoveAction other;

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

    public static class RecipeMove implements IMoveAction
    {
        public final String          name;
        public final ShapelessRecipe recipe;
        public final int             hungerCost;

        final Container c = new Container(null, 0)
        {
            @Override
            public boolean canInteractWith(final PlayerEntity playerIn)
            {
                return false;
            }
        };

        public RecipeMove(final XMLRecipe recipe)
        {
            this.name = recipe.values.get(PokemobMoveRecipeParser.MOVENAME);
            this.hungerCost = Integer.parseInt(recipe.values.get(PokemobMoveRecipeParser.HUNGERCOST));
            final ItemStack recipeOutputIn = Tools.getStack(recipe.output.getValues());
            final NonNullList<Ingredient> recipeItemsIn = NonNullList.create();

            for (final XMLRecipeInput value : recipe.inputs)
            {
                if (value.id == null) value.id = value.getValues().get(new QName("id"));
                // Tag
                if (value.id.startsWith("#"))
                {
                    final ResourceLocation id = new ResourceLocation(value.id.replaceFirst("#", ""));
                    final ITag<Item> tag = ItemTags.getCollection().getTagByID(id);
                    recipeItemsIn.add(Ingredient.fromTag(tag));
                }
                else recipeItemsIn.add(Ingredient.fromStacks(Tools.getStack(value.getValues())));
            }

            this.recipe = new ShapelessRecipe(new ResourceLocation("pokecube:loaded_" + this.name), "pokecube_mobes",
                    recipeOutputIn, recipeItemsIn);
        }

        @Override
        public boolean applyEffect(final IPokemob user, final Vector3 location)
        {
            return this.attemptCraft(user, location) || this.attemptWorldCraft(user, location);
        }

        public boolean attemptWorldCraft(final IPokemob user, final Vector3 location)
        {
            // This should look at the block hit, and attempt to craft that into
            // a shapeless recipe.
            final World world = user.getEntity().getEntityWorld();
            final BlockState block = location.getBlockState(world);
            if (block == null || world.isAirBlock(location.getPos())) return false;
            final ItemStack item = new ItemStack(block.getBlock());
            final CraftingInventory inven = new CraftingInventory(this.c, 1, 1);
            inven.setInventorySlotContents(0, item);
            if (!this.recipe.matches(inven, world)) return false;
            final ItemStack stack = this.recipe.getCraftingResult(inven);
            if (stack.isEmpty()) return false;
            final Block toSet = Block.getBlockFromItem(stack.getItem());
            if (toSet == Blocks.AIR)
            {
                final ItemEntity drop = new ItemEntity(world, location.x, location.y, location.z, stack);
                world.addEntity(drop);
            }
            location.setBlock(world, toSet.getDefaultState());
            return true;
        }

        public boolean attemptCraft(final IPokemob attacker, final Vector3 location)
        {
            // This should look for items near the location, and try to stuff
            // them into a shapeless recipe.
            final World world = attacker.getEntity().getEntityWorld();
            final List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, location.getAABB().grow(2));
            boolean allMatch = true;
            final List<ItemEntity> matches = Lists.newArrayList();
            for (final Ingredient i : this.recipe.getIngredients())
            {
                boolean matched = false;
                for (final ItemEntity item : items)
                    if (i.test(item.getItem()))
                    {
                        matched = true;
                        matches.add(item);
                        break;
                    }
                allMatch = matched;
                if (!matched) break;
            }
            if (!allMatch) return false;
            final CraftingInventory inven = new CraftingInventory(this.c, 1, matches.size());
            for (int i = 0; i < matches.size(); i++)
                inven.setInventorySlotContents(i, matches.get(i).getItem());
            if (!this.recipe.matches(inven, world)) return false;
            final ItemStack stack = this.recipe.getCraftingResult(inven);
            if (stack.isEmpty()) return false;
            final List<ItemStack> remains = this.recipe.getRemainingItems(inven);
            matches.forEach(e -> e.remove());
            ItemEntity drop = new ItemEntity(world, location.x, location.y, location.z, stack);
            world.addEntity(drop);
            for (final ItemStack left : remains)
                if (!left.isEmpty())
                {
                    drop = new ItemEntity(world, location.x, location.y, location.z, left);
                    world.addEntity(drop);
                }
            return true;
        }

        @Override
        public String getMoveName()
        {
            return this.name;
        }

    }

    public PokemobMoveRecipeParser()
    {
    }

    @Override
    public void manageRecipe(final XMLRecipe recipe) throws NullPointerException
    {
        IMoveAction action = new RecipeMove(recipe);
        if (MoveEventsHandler.customActions.containsKey(action.getMoveName())) action = new WrappedRecipeMove(
                MoveEventsHandler.customActions.get(action.getMoveName()), action);
        MoveEventsHandler.customActions.put(action.getMoveName(), action);
    }

}
