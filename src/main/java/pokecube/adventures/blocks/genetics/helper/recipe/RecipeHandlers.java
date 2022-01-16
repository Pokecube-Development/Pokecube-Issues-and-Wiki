package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.BookCloningRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.genetics.cloner.ClonerTile;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper.DNAPack;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone.AnyMatcher;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone.ReviveMatcher;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.SelectorValue;
import pokecube.adventures.events.CloneEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.recipes.IRecipeParser;
import pokecube.core.database.recipes.XMLRecipeHandler;
import pokecube.core.database.recipes.XMLRecipeHandler.XMLRecipe;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.ItemFossil;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.IMobGenetics;

public class RecipeHandlers
{
    private static final String ENERGY = "cost";
    private static final String PRIORITY = "priority";
    private static final String POKEMOB = "pokemon";
    private static final String TAME = "tame";
    private static final String LEVEL = "lvl";
    private static final String REMAIN = "remain";
    private static final String CHANCE = "chance";
    private static final String POKEMOBA = "pokemonA";
    private static final String POKEMOBB = "pokemonB";
    private static final String POKEMOBE = "pokemonE";
    private static final String DNADESTRUCT = "dna";
    private static final String SELECTORDESTRUCT = "selector";

    public static class ClonerRecipeParser implements IRecipeParser
    {
        public static class RecipeMatcher implements ReviveMatcher
        {
            final PokedexEntry entry;
            final List<Ingredient> stacks = Lists.newArrayList();
            final List<Integer> remains = Lists.newArrayList();

            boolean tame = false;

            int level = AnyMatcher.level;
            int priority = 100;
            int energy = RecipeClone.ENERGYCOST;

            public RecipeMatcher(final PokedexEntry entry)
            {
                this.entry = entry;
            }

            @Override
            public PokedexEntry getDefault()
            {
                return this.entry;
            }

            @Override
            public List<Ingredient> getInputs()
            {
                return this.stacks;
            }

            @Override
            public boolean complete(final IPoweredProgress tile)
            {
                final Level world = ((BlockEntity) tile).getLevel();
                final BlockPos pos = ((BlockEntity) tile).getBlockPos();
                final PokedexEntry entry = RecipeClone.getEntry(this, tile);
                if (entry == Database.missingno) return false;
                final boolean tame = !entry.isLegendary() && this.tame;
                Mob entity = PokecubeCore.createPokemob(entry, world);
                if (entity != null)
                {
                    // First move the mob to correct spot, so later checks for
                    // evolution/etc can apply properly
                    final Direction dir = world.getBlockState(pos).getValue(HorizontalDirectionalBlock.FACING);
                    entity.moveTo(pos.getX() + 0.5 + dir.getStepX(), pos.getY() + 1, pos.getZ() + 0.5 + dir.getStepZ(),
                            world.random.nextFloat() * 360F, 0.0F);
                    
                    // Mark as cloned for preventing drops, etc
                    entity.getPersistentData().putBoolean("cloned", true);

                    ItemStack dnaSource = tile.getItem(0);
                    if (!dnaSource.isEmpty()) dnaSource = dnaSource.copy();
                    IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
                    entity.setHealth(entity.getMaxHealth());
                    // to avoid the death on spawn
                    final int exp = Tools.levelToXp(entry.getEvolutionMode(), this.level);
                    // that will make your pokemob around level 3-5.
                    // You can give him more XP if you want
                    entity = (pokemob = pokemob.setForSpawn(exp)).getEntity();
                    if (tile.getUser() != null && tame) pokemob.setOwner(tile.getUser().getUUID());

                    final CloneEvent.Spawn event = new CloneEvent.Spawn((ClonerTile) tile, pokemob);
                    if (PokecubeCore.POKEMOB_BUS.post(event)) return false;
                    pokemob = event.getPokemob();
                    entity = pokemob.getEntity();
                    world.addFreshEntity(entity);
                    final IMobGenetics genes = ClonerHelper.getGenes(dnaSource);
                    if (genes != null) GeneticsManager.initFromGenes(genes, pokemob);
                    entity.playAmbientSound();
                }
                return true;
            }

            @Override
            public PokedexEntry getEntry(final CraftingContainer inventory)
            {
                if (inventory.getItem(1).isEmpty()) return Database.missingno;
                boolean valid = false;
                if (!this.stacks.isEmpty())
                {
                    final List<Ingredient> temp = Lists.newArrayList();
                    temp.addAll(this.stacks);
                    outer:
                    // 0 and 1 are the egg and normal dna slots.
                    for (int i = 2; i < inventory.getContainerSize(); i++)
                    {
                        final ItemStack stack = inventory.getItem(i);
                        if (stack.isEmpty()) continue;
                        for (final Ingredient ing : temp) if (ing.test(stack))
                        {
                            boolean hasTag = false;
                            ItemStack test = ItemStack.EMPTY;
                            for (final ItemStack s : ing.getItems())
                            {
                                hasTag = s.hasTag();
                                test = s;
                                if (hasTag) break;
                            }
                            if (hasTag && !stack.hasTag()) continue;
                            if (hasTag && !ItemStack.tagMatches(stack, test)) continue;
                            temp.remove(ing);
                            continue outer;
                        }
                        // Invalid, lets just return here.
                        return Database.missingno;
                    }
                    valid = temp.isEmpty();
                }
                return valid ? this.entry : Database.missingno;
            }

            @Override
            public boolean shouldKeep(final ItemStack stack, int slot)
            {
                for (final Integer test : this.remains)
                {
                    final Ingredient i = this.stacks.get(test);
                    if (i.test(stack)) return true;
                }
                return false;
            }

            @Override
            public int priority()
            {
                return this.priority;
            }

            @Override
            public int getEnergyCost()
            {
                return this.energy;
            }

        }

        @Override
        public void manageRecipe(final JsonObject json) throws NullPointerException
        {
            final XMLRecipe recipe = this.fromJson(json);
            final NonNullList<Ingredient> recipeItemsIn = XMLRecipeHandler.getInputItems(json);

            final PokedexEntry entry = Database.getEntry(recipe.values.get(RecipeHandlers.POKEMOB));
            if (entry == null)
                throw new NullPointerException("No Entry for " + recipe.values.get(RecipeHandlers.POKEMOB));
            final int energy = Integer.parseInt(recipe.values.get(RecipeHandlers.ENERGY));
            final int level = Integer.parseInt(recipe.values.get(RecipeHandlers.LEVEL));
            int priority = 0;
            boolean tame = false;
            if (recipe.values.containsKey(RecipeHandlers.PRIORITY))
                priority = Integer.parseInt(recipe.values.get(RecipeHandlers.PRIORITY));
            if (recipe.values.containsKey(RecipeHandlers.TAME))
                tame = Boolean.parseBoolean(recipe.values.get(RecipeHandlers.TAME));

            final RecipeMatcher matcher = new RecipeMatcher(entry);
            matcher.stacks.addAll(recipeItemsIn);
            matcher.level = level;
            matcher.tame = tame;
            matcher.priority = priority;
            matcher.energy = energy;
            if (recipe.values.containsKey(RecipeHandlers.REMAIN))
            {
                final String[] remain = recipe.values.get(RecipeHandlers.REMAIN).split(",");
                for (final String s : remain) matcher.remains.add(Integer.parseInt(s));
            }
            RecipeClone.MATCHERS.add(matcher);
            RecipeClone.MATCHERS.sort((o1, o2) -> o1.priority() - o2.priority());
        }

        @Override
        public void init()
        {
            RecipeClone.MATCHERS.clear();
        }
    }

    public static class SelectorRecipeParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(final JsonObject json) throws NullPointerException
        {
            final XMLRecipe recipe = this.fromJson(json);
            final List<Ingredient> inputs = XMLRecipeHandler.getInputItems(json);
            if (inputs.size() != 1) throw new NullPointerException("Wrong number of stacks for " + recipe);
            final Ingredient stack = inputs.get(0);
            if (stack.isEmpty()) throw new NullPointerException("Invalid stack for " + recipe);
            final float dna = Float.parseFloat(recipe.values.get(RecipeHandlers.DNADESTRUCT));
            final float select = Float.parseFloat(recipe.values.get(RecipeHandlers.SELECTORDESTRUCT));
            final SelectorValue value = new SelectorValue(select, dna);
            RecipeSelector.addSelector(stack, value);
        }

        @Override
        public void init()
        {
            RecipeSelector.clear();
        }
    }

    public static class DNARecipeParser implements IRecipeParser
    {
        @Override
        public void manageRecipe(final JsonObject json) throws NullPointerException
        {
            final XMLRecipe recipe = this.fromJson(json);

            final List<Ingredient> inputs = XMLRecipeHandler.getInputItems(json);
            if (inputs.size() != 1) throw new NullPointerException("Wrong number of stacks for " + recipe);
            final Ingredient stack = inputs.get(0);
            if (stack.isEmpty()) throw new NullPointerException("Invalid stack for " + recipe);
            PokedexEntry entry = Database.getEntry(recipe.values.get(RecipeHandlers.POKEMOB));
            PokedexEntry entryA = Database.getEntry(recipe.values.get(RecipeHandlers.POKEMOBA));
            PokedexEntry entryB = Database.getEntry(recipe.values.get(RecipeHandlers.POKEMOBB));
            final PokedexEntry entryE = Database.getEntry(recipe.values.get(RecipeHandlers.POKEMOBE));

            if (entry == null && entryA == null && entryB == null && entryE == null)
                throw new NullPointerException("No Entry for " + recipe.values.get(RecipeHandlers.POKEMOB));

            if (entry == null) entry = entryA == null ? entryB == null ? entryE : entryB : entryA;
            if (entryA == null) entryA = entry;
            if (entryB == null) entryB = entry;

            final SpeciesGene geneA = new SpeciesGene();
            SpeciesInfo info = geneA.getValue();
            info.entry = entryA;
            final SpeciesGene geneB = new SpeciesGene();
            info = geneB.getValue();
            info.entry = entryB;
            final Alleles<?, ?> alleles = new Alleles<>(geneA, geneB);
            if (entryE != null)
            {
                final SpeciesGene geneE = new SpeciesGene();
                info = geneE.getValue();
                info.entry = entryE;
            }
            float chance = 1;
            if (recipe.values.containsKey(RecipeHandlers.CHANCE))
                chance = Float.parseFloat(recipe.values.get(RecipeHandlers.CHANCE));
            final String key = stack.toJson().toString();
            final DNAPack pack = new DNAPack(key, alleles, chance);
            ClonerHelper.registerDNA(pack, stack);
        }

        @Override
        public void init()
        {
            ClonerHelper.DNAITEMS.clear();

            if (PokecubeAdv.config.autoAddFossilDNA)
                for (final Entry<String, ItemFossil> fossil : ItemGenerator.fossils.entrySet())
            {
                final String name = fossil.getKey();
                final Ingredient stack = Ingredient.of(fossil.getValue());
                final SpeciesGene gene = new SpeciesGene();
                final SpeciesInfo info = gene.getValue();
                info.entry = Database.getEntry(name);
                final Alleles<?, ?> genes = new Alleles<>(gene, gene);
                ClonerHelper.registerDNA(new DNAPack(name, genes, 1), stack);
            }
        }
    }

    public static void init()
    {
        XMLRecipeHandler.recipeParsers.put("cloner", new ClonerRecipeParser());
        XMLRecipeHandler.recipeParsers.put("selector", new SelectorRecipeParser());
        XMLRecipeHandler.recipeParsers.put("dna", new DNARecipeParser());
        MinecraftForge.EVENT_BUS.addListener(RecipeHandlers::onCrafted);
    }

    private static void onCrafted(final ItemCraftedEvent event)
    {
        if (!(event.getInventory() instanceof CraftingContainer)) return;
        final CraftingContainer inv = (CraftingContainer) event.getInventory();
        final BookCloningRecipe test = new BookCloningRecipe(new ResourceLocation("dummy"));

        if (!test.matches(inv, event.getEntity().getLevel())) return;
        final SelectorValue value = ClonerHelper.getSelectorValue(event.getCrafting());
        if (value == RecipeSelector.defaultSelector) return;
        event.getCrafting().getTag().remove(ClonerHelper.SELECTORTAG);
    }
}
