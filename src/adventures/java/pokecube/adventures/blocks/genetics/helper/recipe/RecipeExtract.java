package pokecube.adventures.blocks.genetics.helper.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import pokecube.adventures.blocks.genetics.extractor.ExtractorTile;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.SelectorImpl.SelectorValue;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.ItemBasedSelector;
import pokecube.adventures.utils.RecipePokeAdv;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.genetics.DefaultGenetics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeExtract extends PoweredRecipe
{
    public record WeightedGene(Gene<?> gene, int weight)
    {
        public CompoundTag toTag()
        {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", gene.getKey().toString());
            tag.put("gene", gene.save(PokecubeCore.proxy.getRegistries()));
            tag.putInt("weight", weight);
            return tag;
        }

        public static WeightedGene parse(CompoundTag tag)
        {
            ResourceLocation id = ResourceLocation.parse(tag.getString("id"));
            int weight = tag.getInt("weight");
            if (!tag.contains("weight")) weight = 1;
            try
            {
                var gene = GeneRegistry.load(PokecubeCore.proxy.getRegistries(), tag.getCompound("gene"), id);
                return new WeightedGene(gene, weight);
            }
            catch (Exception e)
            {
                PokecubeAPI.LOGGER.error("Error loading genes for {}", tag, e);
            }
            return null;
        }

        public static final Codec<WeightedGene> CODEC = CompoundTag.CODEC.comapFlatMap(WeightedGene::read,
                WeightedGene::toTag).stable();
        public static final StreamCodec<ByteBuf, WeightedGene> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(
                WeightedGene::parse, WeightedGene::toTag);

        public static DataResult<WeightedGene> read(CompoundTag tag)
        {
            try
            {
                return DataResult.success(parse(tag));
            }
            catch (ResourceLocationException resourcelocationexception)
            {
                return DataResult.error(
                        () -> "Not a valid pokemob tag: " + tag + " " + resourcelocationexception.getMessage());
            }
        }
    }

    public static class Serializer implements RecipeSerializer<RecipeExtract>
    {
        public static final MapCodec<RecipeExtract> CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(Codec.INT.optionalFieldOf("cost", -1).forGetter(m -> m.cost),
                                Codec.list(WeightedGene.CODEC).optionalFieldOf("genes", Collections.emptyList())
                                        .forGetter(m -> m.genes),
                                Ingredient.CODEC.optionalFieldOf("input", Ingredient.EMPTY).forGetter(m -> m.input))
                        .apply(i, RecipeExtract::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RecipeExtract> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork);

        private static RecipeExtract fromNetwork(RegistryFriendlyByteBuf buffer)
        {
            int cost = buffer.readInt();
            int n = buffer.readInt();
            List<WeightedGene> list = n > 0 ? new ArrayList<>() : Collections.emptyList();
            for (int i = 0; i < n; i++) list.add(WeightedGene.STREAM_CODEC.decode(buffer));
            Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            return new RecipeExtract(cost, list, input);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, RecipeExtract recipe)
        {
            buffer.writeInt(recipe.cost);
            buffer.writeInt(recipe.genes.size());
            recipe.genes.forEach(g -> WeightedGene.STREAM_CODEC.encode(buffer, g));
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.input);
        }

        @Override
        public MapCodec<RecipeExtract> codec()
        {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RecipeExtract> streamCodec()
        {
            return STREAM_CODEC;
        }
    }

    public static int ENERGYCOST = 10000;

    public Ingredient input;
    public int cost;
    public List<WeightedGene> genes;
    private final Map<ResourceLocation, List<Gene<?>>> _genes = new HashMap<>();

    public RecipeExtract(int cost, List<WeightedGene> genes, Ingredient input)
    {
        super();
        this.cost = cost;
        this.genes = genes;
        this.input = input;
        this.genes.forEach(g -> {
            var list = _genes.computeIfAbsent(g.gene().getKey(), key -> new ArrayList<>());
            for (int i = 0; i < g.weight(); i++) list.add(g.gene());
        });
    }

    @Override
    public boolean canCraftInDimensions(final int width, final int height)
    {
        return width * height > 2;
    }

    @Override
    public boolean complete(final IPoweredProgress tile, Level world)
    {
        final List<ItemStack> remaining = this.getRemainingItems(tile.getCraftMatrix());
        var output = this.assemble(tile.getCraftMatrix(), world.registryAccess());
        for (int i = 0; i < remaining.size(); i++)
        {
            final ItemStack old = tile.getItem(i);
            final ItemStack stack = remaining.get(i);
            if (!stack.isEmpty())
            {
                if (PokecubeManager.isFilled(old)) PlayerPokemobCache.UpdateCache(old, false, true);
                tile.setItem(i, stack);
            }
            else
            {
                if (PokecubeManager.isFilled(old)) PlayerPokemobCache.UpdateCache(old, false, true);
                tile.removeItem(i, 1);
            }
        }
        tile.setItem(tile.getOutputSlot(), output);
        if (tile.getCraftMatrix().eventHandler != null) tile.getCraftMatrix().eventHandler.broadcastChanges();
        return true;
    }

    /** Used to check if a recipe matches current crafting inventory */
    @Override
    public boolean matches(final PoweredCraftingInventory inv, final Level worldIn)
    {
        if (!(inv.inventory instanceof ExtractorTile tile)) return false;
        var access = worldIn.registryAccess();

        ItemStack destination = inv.getItem(0);
        if (!ExtractorTile.isValidDestination(access, destination)) return false;

        ItemStack source = inv.getItem(2);
        if (!this.input.isEmpty() && !this.input.test(source)) return false;
        IMobGenetics genes = ClonerHelper.getGenes(access, source);
        if (genes == null) genes = new DefaultGenetics();

        ItemStack slottedSelector = inv.getItem(1);
        if (ClonerHelper.getGeneSelectors(access, slottedSelector).isEmpty()) return false;
        ItemStack selector = tile.override_selector.isEmpty() ? slottedSelector : tile.override_selector;
        if (ClonerHelper.getGeneSelectors(access, selector).isEmpty()) selector = ItemStack.EMPTY;
        if (!this._genes.isEmpty())
        {
            IMobGenetics finalGenes = genes;
            this._genes.forEach((k, list) -> {
                var gene_1 = list.get(tile.getLevel().getRandom().nextInt(list.size()));
                var gene_2 = list.get(tile.getLevel().getRandom().nextInt(list.size()));
                finalGenes.setGenes(gene_1, gene_2);
            });
        }
        return !source.isEmpty() && !genes.getAlleles().isEmpty() && !selector.isEmpty();
    }

    @Override
    public ItemStack assemble(final PoweredCraftingInventory inv, Provider access)
    {
        if (!(inv.inventory instanceof ExtractorTile tile)) return ItemStack.EMPTY;

        final ItemStack destination = inv.getItem(0);
        if (!ExtractorTile.isValidDestination(access, destination)) return ItemStack.EMPTY;
        ItemStack source = inv.getItem(2);
        if (!this.input.isEmpty() && !this.input.test(source)) return ItemStack.EMPTY;
        IMobGenetics genes = ClonerHelper.getGenes(access, source);
        if (genes == null) genes = new DefaultGenetics();

        ItemStack slottedSelector = inv.getItem(1);
        if (ClonerHelper.getGeneSelectors(access, slottedSelector).isEmpty()) return ItemStack.EMPTY;
        ItemStack selector = tile.override_selector.isEmpty() ? slottedSelector : tile.override_selector;
        if (ClonerHelper.getGeneSelectors(access, selector).isEmpty()) selector = ItemStack.EMPTY;
        boolean forcedGenes = false;
        if (!this._genes.isEmpty())
        {
            IMobGenetics finalGenes = genes;
            this._genes.forEach((k, list) -> {
                var gene_1 = list.get(tile.getLevel().getRandom().nextInt(list.size()));
                var gene_2 = list.get(tile.getLevel().getRandom().nextInt(list.size()));
                finalGenes.setGenes(gene_1, gene_2);
            });
        }
        final ItemStack output = destination.copy();
        output.setCount(1);
        if (source.isEmpty() || genes.getAlleles().isEmpty() || selector.isEmpty()) return ItemStack.EMPTY;
        ClonerHelper.mergeGenes(access, genes, output, new ItemBasedSelector(selector), forcedGenes);
        output.setCount(1);
        return output;
    }

    @Override
    public int getEnergyCost(final IPoweredProgress tile)
    {
        return RecipeExtract.ENERGYCOST;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return RecipePokeAdv.EXTRACT.get();
    }

    @Override
    public RecipeType<?> getType()
    {
        return RecipePokeAdv.EXTRACT_TYPE.get();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final PoweredCraftingInventory inv)
    {
        final NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        if (!(inv.inventory instanceof ExtractorTile tile)) return nonnulllist;
        final ItemStack selector = tile.override_selector.isEmpty() ? inv.getItem(1) : tile.override_selector;
        boolean keepDNA = false;
        boolean keepSelector = false;
        SelectorValue value = ClonerHelper.getSelectorValue(selector);
        if (value.dnaDestructChance < Math.random()) keepDNA = true;
        if (value.selectorDestructChance < Math.random()) keepSelector = true;

        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            ItemStack item = inv.getItem(i).copy();
            if (i == 1 && keepSelector) nonnulllist.set(i, item);
            if (i == 2)
            {
                boolean potion = item.getItem() == Items.POTION;
                boolean multiple = item.getCount() > 1;
                if (keepDNA) nonnulllist.set(i, item);
                else if (potion) nonnulllist.set(i, new ItemStack(Items.GLASS_BOTTLE));
                else if (!multiple)
                {
                    nonnulllist.set(i, clearDNA(item));
                }
            }
            if (item.hasCraftingRemainingItem()) nonnulllist.set(i, item.getCraftingRemainingItem());
        }
        tile.override_selector = ItemStack.EMPTY;
        return nonnulllist;
    }

    static ItemStack clearDNA(final ItemStack stack)
    {
        final ItemStack cleared = stack.copy();
        final var pokemob = cleared.remove(PokemobCaps.POKECUBE_DATA);
        final var genes = cleared.remove(DefaultGenetics.GENE_STORE);
        if (pokemob != null) cleared.remove(DataComponents.ITEM_NAME);
        return pokemob != null || genes != null ? cleared : ItemStack.EMPTY;
    }
}
