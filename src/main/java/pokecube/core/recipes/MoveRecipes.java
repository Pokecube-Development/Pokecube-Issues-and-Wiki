package pokecube.core.recipes;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistryEntry;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.database.recipes.PokemobMoveRecipeParser;
import pokecube.core.database.recipes.PokemobMoveRecipeParser.MoveMatcher;
import pokecube.core.database.recipes.PokemobMoveRecipeParser.RecipeMove;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class MoveRecipes
{
    private static final ResourceLocation ID = new ResourceLocation("pokecube:move_recipe");

    public static final IRecipeType<MoveRecipe> MOVE_TYPE = IRecipeType.register(MoveRecipes.ID.toString());

    public static final RegistryObject<Serializer> SERIALIZER = RecipeHandler.RECIPE_SERIALIZERS.register("move_recipe",
            () -> new Serializer());

    public static Map<String, List<MoveRecipe>> MOVE_TO_RECIPES_MAP = Maps.newHashMap();

    public static class WorldCraftInventory extends CraftingInventory
    {
        final IPokemob pokemob;

        public WorldCraftInventory(final Container container, final int x, final int y, final IPokemob pokemob)
        {
            super(container, x, y);
            this.pokemob = pokemob;
        }
    }

    public static class MoveRecipe implements IRecipe<WorldCraftInventory>
    {
        private final ShapelessRecipe wrapped;

        final int hungerCost;

        public final MoveMatcher match;

        final boolean isCustom;

        final Container c = new Container(null, 0)
        {
            @Override
            public boolean stillValid(final PlayerEntity playerIn)
            {
                return false;
            }
        };

        public List<String> matchedMoves = Lists.newArrayList();

        public MoveRecipe(final ShapelessRecipe wrap, final int hunger, final MoveMatcher match, final boolean external)
        {
            this.wrapped = wrap;
            this.hungerCost = hunger;
            this.match = match;
            this.isCustom = external;
        }

        @Override
        public boolean matches(final WorldCraftInventory inventory, final World world)
        {
            return this.wrapped.matches(inventory, world);
        }

        @Override
        public ItemStack assemble(final WorldCraftInventory inventory)
        {
            final ItemStack stack = this.wrapped.assemble(inventory);
            inventory.pokemob.applyHunger(this.hungerCost);
            return stack;
        }

        @Override
        public boolean canCraftInDimensions(final int x, final int y)
        {
            return this.wrapped.canCraftInDimensions(x, y);
        }

        @Override
        public ItemStack getResultItem()
        {
            return this.wrapped.getResultItem();
        }

        @Override
        public NonNullList<Ingredient> getIngredients()
        {
            return this.wrapped.getIngredients();
        }

        @Override
        public ResourceLocation getId()
        {
            return MoveRecipes.ID;
        }

        @Override
        public IRecipeSerializer<?> getSerializer()
        {
            return MoveRecipes.SERIALIZER.get();
        }

        @Override
        public IRecipeType<?> getType()
        {
            return MoveRecipes.MOVE_TYPE;
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
            final WorldCraftInventory inven = new WorldCraftInventory(this.c, 1, 1, user);
            inven.setItem(0, item);
            if (!this.matches(inven, world)) return false;
            final ItemStack stack = this.assemble(inven);
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

        private int tryCraft(final List<ItemStack> items, final Vector3 location, final World world, int depth,
                final IPokemob user)
        {
            boolean allMatch = false;
            final List<ItemStack> toUse = Lists.newArrayList();
            for (final Ingredient i : this.getIngredients())
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
            final WorldCraftInventory inven = new WorldCraftInventory(this.c, 1, toUse.size(), user);
            for (int i = 0; i < toUse.size(); i++)
                inven.setItem(i, toUse.get(i));
            if (!this.matches(inven, world)) return depth;
            final ItemStack stack = this.assemble(inven);
            if (stack.isEmpty()) return depth;
            final List<ItemStack> remains = this.getRemainingItems(inven);
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
            depth = this.tryCraft(toUse, location, world, depth, user);
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
            final int depth = this.tryCraft(stacks, location, world, 0, attacker);
            return depth > 0;
        }
    }

    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements
            IRecipeSerializer<MoveRecipe>
    {

        @Override
        public MoveRecipe fromJson(final ResourceLocation id, final JsonObject json)
        {
            final ShapelessRecipe wrap = IRecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json);
            final int cost = JSONUtils.getAsInt(json, "hungerCost", 50);
            final MoveMatcher matcher = PokedexEntryLoader.gson.fromJson(JSONUtils.getAsJsonObject(json, "move"),
                    MoveMatcher.class);
            final boolean isCustom = JSONUtils.getAsBoolean(json, "loading_from_other", false);
            final MoveRecipe recipe = new MoveRecipe(wrap, cost, matcher, isCustom);
            if (!isCustom)
            {
                final RecipeMove loaded = new RecipeMove(recipe);
                if (!loaded.actions.isEmpty()) RecipeMove.CUSTOM.put(id, recipe);
                for (final IMoveAction action : loaded.actions)
                    PokemobMoveRecipeParser.addOrMergeActions(action);
            }
            return recipe;
        }

        @Override
        public MoveRecipe fromNetwork(final ResourceLocation id, final PacketBuffer buffer)
        {
            final ShapelessRecipe wrap = IRecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buffer);
            final int cost = buffer.readInt();
            final boolean isCustom = buffer.readBoolean();
            final MoveMatcher matcher = PokedexEntryLoader.gson.fromJson(buffer.readUtf(), MoveMatcher.class);
            final MoveRecipe recipe = new MoveRecipe(wrap, cost, matcher, isCustom);
            if (!isCustom)
            {
                final RecipeMove loaded = new RecipeMove(recipe);
                if (!loaded.actions.isEmpty()) RecipeMove.CUSTOM.put(id, recipe);
                for (final IMoveAction action : loaded.actions)
                    PokemobMoveRecipeParser.addOrMergeActions(action);
            }
            return recipe;
        }

        @Override
        public void toNetwork(final PacketBuffer buffer, final MoveRecipe recipe)
        {
            IRecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe.wrapped);
            buffer.writeInt(recipe.hungerCost);
            buffer.writeBoolean(recipe.isCustom);
            buffer.writeUtf(PokedexEntryLoader.gson.toJson(recipe.match));
        }

    }

    public static void init()
    {

    }

}
