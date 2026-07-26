package pokecube.adventures.blocks.genetics.helper.recipe;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.adventures.Config;
import pokecube.adventures.blocks.genetics.cloner.ClonerTile;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.adventures.utils.RecipePokeAdv;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.spawns.SpawnRule;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.CloneEvent;
import pokecube.api.utils.TagNames;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.entity.genetics.genes.SpeciesGene;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.genetics.DefaultGenetics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeClone extends PoweredRecipe
{
    public static int ENERGYCOST = 10000;

    public static RecipeClone DEFAULT = new RecipeClone(-1, 20, 1000, true, Collections.emptyList(),
            Collections.emptyList(), Collections.emptyList(), ItemStack.EMPTY);

    public static class Serializer implements RecipeSerializer<RecipeClone>
    {
        public static final MapCodec<RecipeClone> CODEC = RecordCodecBuilder.mapCodec(
                i -> i.group(Codec.INT.optionalFieldOf("cost", -1).forGetter(m -> m.cost),
                                Codec.INT.optionalFieldOf("level", 20).forGetter(m -> m.level),
                                Codec.INT.optionalFieldOf("priority", 100).forGetter(m -> m.priority),
                                Codec.BOOL.optionalFieldOf("tame", true).forGetter(m -> m.tame),
                                Codec.list(RecipeExtract.WeightedGene.CODEC).optionalFieldOf("genes", Collections.emptyList())
                                        .forGetter(m -> m.genes),
                                Codec.list(Ingredient.CODEC).optionalFieldOf("inputs", Collections.emptyList())
                                        .forGetter(m -> m.inputs),
                                Codec.list(Ingredient.CODEC).optionalFieldOf("nonConsumed", Collections.emptyList())
                                        .forGetter(m -> m.nonConsumed),
                                ItemStack.CODEC.optionalFieldOf("output", ItemStack.EMPTY).forGetter(m -> m.output))
                        .apply(i, RecipeClone::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RecipeClone> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork);

        private static RecipeClone fromNetwork(RegistryFriendlyByteBuf buffer)
        {
            int cost = buffer.readInt();
            int level = buffer.readInt();
            int priority = buffer.readInt();
            boolean tame = buffer.readBoolean();
            int n = buffer.readInt();
            List<RecipeExtract.WeightedGene> list = n > 0 ? new ArrayList<>() : Collections.emptyList();
            for (int i = 0; i < n; i++) list.add(RecipeExtract.WeightedGene.STREAM_CODEC.decode(buffer));
            n = buffer.readInt();
            List<Ingredient> inputs = n > 0 ? new ArrayList<>() : Collections.emptyList();
            for (int i = 0; i < n; i++) inputs.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            n = buffer.readInt();
            List<Ingredient> nonConsumed = n > 0 ? new ArrayList<>() : Collections.emptyList();
            for (int i = 0; i < n; i++) nonConsumed.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            var output = buffer.readBoolean() ? ItemStack.STREAM_CODEC.decode(buffer) : ItemStack.EMPTY;
            return new RecipeClone(cost, level, priority, tame, list, inputs, nonConsumed, output);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, RecipeClone recipe)
        {
            buffer.writeInt(recipe.cost);
            buffer.writeInt(recipe.level);
            buffer.writeInt(recipe.priority);
            buffer.writeBoolean(recipe.tame);
            buffer.writeInt(recipe.genes.size());
            recipe.genes.forEach(g -> RecipeExtract.WeightedGene.STREAM_CODEC.encode(buffer, g));
            buffer.writeInt(recipe.inputs.size());
            recipe.inputs.forEach(g -> Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, g));
            buffer.writeInt(recipe.nonConsumed.size());
            recipe.nonConsumed.forEach(g -> Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, g));
            buffer.writeBoolean(!recipe.output.isEmpty());
            if (!recipe.output.isEmpty()) ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
        }

        @Override
        public MapCodec<RecipeClone> codec()
        {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RecipeClone> streamCodec()
        {
            return STREAM_CODEC;
        }
    }

    public List<Ingredient> inputs;
    public List<Ingredient> nonConsumed;
    public ItemStack output;
    public int cost;
    public int level;
    public int priority;
    public boolean tame;
    public List<RecipeExtract.WeightedGene> genes;

    public final Map<ResourceLocation, List<Gene<?>>> _genes = new HashMap<>();

    public RecipeClone(int cost, int level, int priority, boolean tame, List<RecipeExtract.WeightedGene> genes,
            List<Ingredient> inputs, List<Ingredient> nonConsumed, ItemStack output)
    {
        this.genes = genes;
        this.cost = cost;
        this.level = level;
        this.priority = priority;
        this.inputs = inputs;
        this.nonConsumed = nonConsumed;
        this.output = output;
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

    @SuppressWarnings("rawtypes")
    private Alleles getEntry(IPoweredProgress tile, Level world)
    {
        ItemStack source = tile.getItem(0);
        IMobGenetics genes = ClonerHelper.getGenes(world.registryAccess(), source);
        if (genes == null) genes = new DefaultGenetics();
        var inputGenes = genes.getAlleles(GeneticsManager.SPECIESGENE);

        var list = _genes.get(GeneticsManager.SPECIESGENE);
        if (list == null || list.isEmpty())
        {
            return inputGenes;
        }

        SpeciesGene gene1 = (SpeciesGene) list.get(world.getRandom().nextInt(list.size()));
        SpeciesGene gene2 = (SpeciesGene) list.get(world.getRandom().nextInt(list.size()));

        if (inputGenes != null)
        {
            SpeciesGene _gene1 = (SpeciesGene) (Object) inputGenes.getAllele(0);
            SpeciesGene _gene2 = (SpeciesGene) (Object) inputGenes.getAllele(1);
            gene1 = (SpeciesGene) gene1.interpolate(_gene1);
            gene2 = (SpeciesGene) gene2.interpolate(_gene2);
        }
        return new Alleles<>(gene1, gene2);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean complete(final IPoweredProgress tile, Level world)
    {
        final BlockPos pos = ((BlockEntity) tile).getBlockPos();
        Alleles<SpeciesGene.SpeciesInfo, SpeciesGene> alleles = getEntry(tile, world);
        if (Config.instance.clonesDevolveToBaseSpecies) alleles = toBaseSpecies(alleles);
        if (alleles == null) return false;
        PokedexEntry entry = alleles.getExpressed().getValue().getEntry();
        if (entry == Database.missingno) return false;
        final boolean tame = !entry.isLegendary();
        Mob entity = PokecubeCore.createPokemob(entry, world);
        if (entity != null)
        {
            ItemStack dnaSource = tile.getItem(0);
            if (!dnaSource.isEmpty()) dnaSource = dnaSource.copy();
            IPokemob pokemob = PokemobCaps.getPokemobFor(entity);

            var genes = ClonerHelper.getGenes(world.registryAccess(), dnaSource);
            IMobGenetics sourceGenes = copyGenes(world.registryAccess(), genes);

            this._genes.forEach((k, list2) -> {
                var gene_1 = list2.get(world.getRandom().nextInt(list2.size()));
                var gene_2 = list2.get(world.getRandom().nextInt(list2.size()));
                sourceGenes.setGenes(gene_1, gene_2);
            });
            sourceGenes.getAlleles().put(GeneticsManager.SPECIESGENE, alleles);

            // to avoid the death on spawn
            entity.setHealth(entity.getMaxHealth());
            int exp = Tools.levelToXp(entry.getEvolutionMode(), this.level);

            if (sourceGenes != null) GeneticsManager.initFromGenes(sourceGenes, pokemob);
            pokemob.onGenesChanged();
            pokemob.getEntity().getPersistentData().putInt(TagNames.SPAWN_EXP, exp);
            // The new spawn rule ensures that it is marked as if it recently spawned.
            pokemob = pokemob.spawnInit(new SpawnRule());

            if (tile.getUser() != null && tame) pokemob.setOwner(tile.getUser().getUUID());

            final CloneEvent.Spawn event = new CloneEvent.Spawn((ClonerTile) tile, pokemob);
            if (PokecubeAPI.POKEMOB_BUS.post(event).isCanceled()) return false;

            pokemob = event.getPokemob();
            entity = pokemob.getEntity();
            final Direction dir = world.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
            entity.moveTo(pos.getX() + 0.5 + dir.getStepX(), pos.getY() + 1, pos.getZ() + 0.5 + dir.getStepZ(),
                    world.random.nextFloat() * 360F, 0.0F);
            entity.getPersistentData().putBoolean("cloned", true);
            world.addFreshEntity(entity);
            entity.playAmbientSound();
        }

        final List<ItemStack> remaining = Lists.newArrayList(this.getRemainingItems(tile.getCraftMatrix()));
        tile.setItem(tile.getOutputSlot(), this.assemble(tile.getCraftMatrix(), world.registryAccess()));
        for (int i = 0; i < remaining.size(); i++)
        {
            final ItemStack stack = remaining.get(i);
            if (!stack.isEmpty()) tile.setItem(i, stack);
            else
            {
                final ItemStack old = tile.getItem(i);
                if (PokecubeManager.isFilled(old)) PlayerPokemobCache.UpdateCache(old, false, true);
                tile.removeItem(i, 1);
            }
        }
        if (tile.getCraftMatrix().eventHandler != null) tile.getCraftMatrix().eventHandler.broadcastChanges();
        return true;
    }

    private static IMobGenetics copyGenes(final Provider access, final IMobGenetics genes)
    {
        final IMobGenetics copy = new DefaultGenetics();
        if (genes != null) copy.deserializeNBT(access, genes.serializeNBT(access));
        return copy;
    }

    private static Alleles<SpeciesGene.SpeciesInfo, SpeciesGene> toBaseSpecies(
            final Alleles<SpeciesGene.SpeciesInfo, SpeciesGene> source)
    {
        if (source == null) return null;
        final SpeciesGene first = toBaseSpecies(source.getAllele(0));
        final SpeciesGene second = toBaseSpecies(source.getAllele(1));
        final SpeciesGene expressed = toBaseSpecies(source.getExpressed());
        final Alleles<SpeciesGene.SpeciesInfo, SpeciesGene> result = new Alleles<>(first, second);
        result.setExpressed(expressed);
        return result;
    }

    private static SpeciesGene toBaseSpecies(final SpeciesGene source)
    {
        final SpeciesGene.SpeciesInfo sourceInfo = source.getValue();
        final SpeciesGene.SpeciesInfo resultInfo = new SpeciesGene.SpeciesInfo();
        final byte sex = sourceInfo.getSexe();
        final PokedexEntry child = sourceInfo.getEntry().getChild();
        resultInfo.setSexe(sex);
        resultInfo.setEntry(child.getForGender(sex));
        final SpeciesGene result = new SpeciesGene();
        result.setValue(resultInfo);
        return result;
    }

    @Override
    public ItemStack assemble(final PoweredCraftingInventory inv, Provider access)
    {
        return this.output;
    }

    @Override
    public int getEnergyCost(final IPoweredProgress tile)
    {
        return this.cost >= 0 ? this.cost : ENERGYCOST;
    }

    @Override
    public RecipeType<?> getType()
    {
        return RecipePokeAdv.CLONE_TYPE.get();
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return RecipePokeAdv.REVIVE.get();
    }

    /** Used to check if a recipe matches current crafting inventory */
    @Override
    public boolean matches(final PoweredCraftingInventory inv, final Level worldIn)
    {
        // Check if there is an egg
        if (inv.getItem(1).isEmpty()) return false;
        // Check if we specify any species
        if (!_genes.containsKey(GeneticsManager.SPECIESGENE))
        {
            IMobGenetics genes = ClonerHelper.getGenes(worldIn.registryAccess(), inv.getItem(0));
            if (genes == null) return false;
            if (genes.getAlleles(GeneticsManager.SPECIESGENE) == null) return false;
        }
        // Hardcode support size of inventory for now.
        if (inv.size() != 10) return false;
        if (!this.inputs.isEmpty())
        {
            List<Ingredient> test = new ArrayList<>(inputs);
            outer:
            for (int i = 2; i < 9; i++)
            {
                ItemStack n = inv.getItem(i);
                if (n.isEmpty()) continue;
                for (Ingredient ing : test)
                {
                    if (ing.test(n))
                    {
                        test.remove(ing);
                        continue outer;
                    }
                }
                return false;
            }
            return test.isEmpty();
        }
        return true;
    }

    private boolean shouldKeep(ItemStack item)
    {
        return this.nonConsumed.stream().anyMatch(i -> i.test(item));
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final PoweredCraftingInventory inv)
    {
        final NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.size(), ItemStack.EMPTY);
        if (!(inv.inventory instanceof ClonerTile)) return nonnulllist;
        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            final ItemStack item = inv.getItem(i);
            if (this.shouldKeep(item)) nonnulllist.set(i, item);
            else if (item.hasCraftingRemainingItem()) nonnulllist.set(i, item.getCraftingRemainingItem());
        }
        return nonnulllist;
    }
}
