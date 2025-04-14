package pokecube.core.recipes;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.handlers.RecipeHandler;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;
import thut.api.util.JsonUtil;
import thut.lib.TCodecs;

import java.util.List;
import java.util.function.Predicate;

public class MoveRecipe implements Recipe<MoveRecipe.WorldCraftInventory>
{
    public static class RecipeAction implements IMoveWorldEffect
    {
        public final String name;

        public final MoveRecipe recipe;

        public RecipeAction(final String name, final MoveRecipe recipe)
        {
            this.name = name;
            this.recipe = recipe;
        }

        @Override
        public boolean applyOutOfCombat(final IPokemob user, final Vector3 location)
        {
            return this.recipe.applyEffect(user, location, this.getMoveName());
        }

        @Override
        public String getMoveName()
        {
            return this.name;
        }

    }

    public static class WorldCraftInventory implements RecipeInput
    {
        final IPokemob pokemob;
        CraftingInput wrapped;

        public WorldCraftInventory(final List<ItemStack> inputs, final IPokemob pokemob)
        {
            this.pokemob = pokemob;
            wrapped = new CraftingInput(inputs.size(), 1, inputs);
        }

        @Override
        public ItemStack getItem(int index)
        {
            return wrapped.getItem(index);
        }

        @Override
        public int size()
        {
            return wrapped.size();
        }

        public void addItem(ItemStack item)
        {
            wrapped.items().add(item);
        }
    }

    public static class MoveMatcher implements Predicate<String>
    {
        // This one is what is actually loaded in from json
        public List<String> moves = Lists.newArrayList();
        // This one is a cache of moves which matched, for use in JEI, etc
        public List<String> _moves = Lists.newArrayList();

        String move = "";
        String type = "";

        int minPower = 0;
        int maxPower = 300;

        boolean contact = true;
        boolean ranged = true;

        @Override
        public boolean test(final String t)
        {
            if (!this.move.isEmpty()) return t.equals(this.move);
            if (!this.moves.isEmpty()) return this.moves.contains(t);

            final MoveEntry move = MovesUtils.getMove(t);
            final PokeType ptype = PokeType.getType(this.type);
            if (ptype == null) return false;
            if (move == null) return false;
            if (move.type != ptype) return false;
            if (!this.contact && move.getAttackCategory() == IMoveConstants.ContactCategory.CONTACT) return false;
            if (!this.ranged && move.getAttackCategory() == IMoveConstants.ContactCategory.RANGED) return false;
            final int power = move.getPWR();
            return power >= this.minPower && power <= this.maxPower;
        }
    }

    private final ShapelessRecipe wrapped;
    final int hungerCost;
    public final MoveMatcher match;

    public MoveRecipe(final ShapelessRecipe wrap, final int hunger, final MoveMatcher match)
    {
        this.wrapped = wrap;
        this.hungerCost = hunger;
        this.match = match;
    }

    @Override
    public boolean matches(final WorldCraftInventory inventory, final Level world)
    {
        return this.wrapped.matches(inventory.wrapped, world);
    }

    @Override
    public ItemStack assemble(WorldCraftInventory input, Provider registries)
    {
        final ItemStack stack = this.wrapped.assemble(input.wrapped, registries);
        input.pokemob.applyHunger(this.hungerCost);
        return stack;
    }

    @Override
    public boolean canCraftInDimensions(final int x, final int y)
    {
        return this.wrapped.canCraftInDimensions(x, y);
    }

    @Override
    public ItemStack getResultItem(Provider registries)
    {
        return this.wrapped.getResultItem(registries);
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        return this.wrapped.getIngredients();
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return RecipeHandler.MOVE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType()
    {
        return RecipeHandler.MOVE_TYPE.get();
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
        final Level world = user.getEntity().level();
        final BlockState block = location.getBlockState(world);
        if (block == null || world.isEmptyBlock(location.getPos())) return false;
        final ItemStack item = new ItemStack(block.getBlock());
        final WorldCraftInventory inven = new WorldCraftInventory(List.of(item), user);
        boolean matched = this.matches(inven, world);
        if (!matched) return false;
        final ItemStack stack = this.assemble(inven, world.registryAccess());
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

    private int tryCraft(final List<ItemStack> items, final Vector3 location, final Level world, int depth,
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
        final WorldCraftInventory inven = new WorldCraftInventory(toUse, user);
        if (!this.matches(inven, world)) return depth;
        final ItemStack stack = this.assemble(inven, world.registryAccess());
        if (stack.isEmpty()) return depth;
        final List<ItemStack> remains = this.getRemainingItems(inven);
        toUse.forEach(e -> e.shrink(1));
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
        final Level world = attacker.getEntity().level();
        final List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, location.getAABB().inflate(2));
        final List<ItemStack> stacks = Lists.newArrayList();
        items.forEach(e -> stacks.add(e.getItem()));
        final int depth = this.tryCraft(stacks, location, world, 0, attacker);
        return depth > 0;
    }

    public static class Serializer implements RecipeSerializer<MoveRecipe>
    {
        public static final StreamCodec<RegistryFriendlyByteBuf, MoveRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork);

        public static final MapCodec<MoveRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(ShapelessRecipe.Serializer.CODEC.forGetter(m -> m.wrapped),
                                Codec.INT.fieldOf("hungerCost").forGetter(m -> m.hungerCost),
                                TCodecs.jsonCodec(MoveMatcher.class).fieldOf("move").forGetter(m -> m.match))
                        .apply(instance, MoveRecipe::new));

        @Override
        public MapCodec<MoveRecipe> codec()
        {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MoveRecipe> streamCodec()
        {
            return STREAM_CODEC;
        }

        public static MoveRecipe fromNetwork(final RegistryFriendlyByteBuf buffer)
        {
            ShapelessRecipe wrap = fromNetworkW(buffer);
            int cost = buffer.readInt();
            MoveMatcher matcher = JsonUtil.gson.fromJson(buffer.readUtf(), MoveMatcher.class);
            return new MoveRecipe(wrap, cost, matcher);
        }

        public static void toNetwork(final RegistryFriendlyByteBuf buffer, final MoveRecipe recipe)
        {
            toNetworkW(buffer, recipe.wrapped);
            buffer.writeInt(recipe.hungerCost);
            buffer.writeUtf(JsonUtil.gson.toJson(recipe.match));
        }

        private static ShapelessRecipe fromNetworkW(RegistryFriendlyByteBuf buffer)
        {
            String s = buffer.readUtf();
            CraftingBookCategory craftingbookcategory = buffer.readEnum(CraftingBookCategory.class);
            int i = buffer.readVarInt();
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);
            nonnulllist.replaceAll(p_319735_ -> Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
            return new ShapelessRecipe(s, craftingbookcategory, itemstack, nonnulllist);
        }

        private static void toNetworkW(RegistryFriendlyByteBuf buffer, ShapelessRecipe recipe)
        {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeVarInt(recipe.getIngredients().size());

            for (Ingredient ingredient : recipe.getIngredients())
            {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }

            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(PokecubeCore.proxy.getRegistries()));
        }
    }
}
