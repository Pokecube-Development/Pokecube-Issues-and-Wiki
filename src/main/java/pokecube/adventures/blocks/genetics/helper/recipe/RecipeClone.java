package pokecube.adventures.blocks.genetics.helper.recipe;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pokecube.adventures.blocks.genetics.cloner.ClonerTile;
import pokecube.adventures.blocks.genetics.helper.ClonerHelper;
import pokecube.adventures.blocks.genetics.helper.crafting.PoweredCraftingInventory;
import pokecube.adventures.events.CloneEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.IMobGenetics;

public class RecipeClone extends PoweredRecipe
{

    public static class AnyMatcher implements ReviveMatcher
    {
        public static int level = 20;

        @Override
        public boolean complete(final IPoweredProgress tile)
        {
            final World world = ((TileEntity) tile).getWorld();
            final BlockPos pos = ((TileEntity) tile).getPos();
            final PokedexEntry entry = RecipeClone.getEntry(this, tile);
            if (entry == Database.missingno) return false;
            final boolean tame = !entry.isLegendary();
            MobEntity entity = PokecubeCore.createPokemob(entry, world);
            if (entity != null)
            {
                ItemStack dnaSource = tile.getStackInSlot(0);
                if (!dnaSource.isEmpty()) dnaSource = dnaSource.copy();
                IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
                entity.setHealth(entity.getMaxHealth());
                // to avoid the death on spawn
                final int exp = Tools.levelToXp(entry.getEvolutionMode(), AnyMatcher.level);

                final IMobGenetics genes = ClonerHelper.getGenes(dnaSource);
                if (genes != null) GeneticsManager.initFromGenes(genes, pokemob);
                pokemob.getEntity().getPersistentData().putInt("spawnExp", exp);
                pokemob = pokemob.spawnInit();

                if (tile.getUser() != null && tame) pokemob.setOwner(tile.getUser().getUniqueID());

                final CloneEvent.Spawn event = new CloneEvent.Spawn((ClonerTile) tile, pokemob);
                if (PokecubeCore.POKEMOB_BUS.post(event)) return false;

                pokemob = event.getPokemob();
                entity = pokemob.getEntity();
                final Direction dir = world.getBlockState(pos).get(HorizontalBlock.HORIZONTAL_FACING);
                entity.setLocationAndAngles(pos.getX() + 0.5 + dir.getXOffset(), pos.getY() + 1, pos.getZ() + 0.5 + dir
                        .getZOffset(), world.rand.nextFloat() * 360F, 0.0F);
                entity.getPersistentData().putBoolean("cloned", true);
                world.addEntity(entity);
                entity.playAmbientSound();
            }
            return true;
        }

        @Override
        public PokedexEntry getEntry(final CraftingInventory inventory)
        {
            final ItemStack dnaSource = inventory.getStackInSlot(0);
            if (dnaSource.isEmpty()) return Database.missingno;
            final ItemStack material = inventory.getStackInSlot(1);
            if (material.isEmpty()) return Database.missingno;
            final PokedexEntry entry = ClonerHelper.getFromGenes(dnaSource);
            if (entry == null) return Database.missingno;
            return entry;
        }

    }

    public static interface ReviveMatcher
    {
        boolean complete(final IPoweredProgress tile);

        @Nonnull
        /**
         * This method should return Database.missingno if not valid!
         *
         * @param inventory
         * @return
         */
        PokedexEntry getEntry(CraftingInventory inventory);

        default PokedexEntry getEntry(final CraftingInventory inventory, final World world)
        {
            return this.getEntry(inventory);
        }

        default int priority()
        {
            return 100;
        }

        default int getEnergyCost()
        {
            return RecipeClone.ENERGYCOST;
        }

        default boolean shouldKeep(final ItemStack stack)
        {
            return false;
        }
    }

    public static int                                  ENERGYCOST = 10000;
    public static final IRecipeSerializer<RecipeClone> SERIALIZER = IRecipeSerializer.register(
            "pokecube_adventures:reviving", new SpecialRecipeSerializer<>(RecipeClone::new));

    public static Function<ItemStack, Integer> ENERGYNEED = (s) -> RecipeClone.ENERGYCOST;
    private static List<RecipeClone>           recipeList = Lists.newArrayList();

    private static HashMap<PokedexEntry, RecipeClone> entryMap = Maps.newHashMap();

    public static ReviveMatcher             ANYMATCHER = new AnyMatcher();
    public static final List<ReviveMatcher> MATCHERS   = Lists.newArrayList();

    public static PokedexEntry getEntry(final ReviveMatcher matcher, final IPoweredProgress tile)
    {
        if (!(tile instanceof ClonerTile)) return Database.missingno;
        final ClonerTile cloner = (ClonerTile) tile;
        PokedexEntry entry = matcher.getEntry(tile.getCraftMatrix(), cloner.getWorld());
        final CloneEvent.Pick pick = new CloneEvent.Pick(cloner, entry);
        if (PokecubeCore.POKEMOB_BUS.post(pick)) entry = Database.missingno;
        entry = pick.getEntry();
        return entry;
    }

    public static RecipeClone getRecipe(final PokedexEntry entry)
    {
        return RecipeClone.entryMap.get(entry);
    }

    public static List<RecipeClone> getRecipeList()
    {
        return Lists.newArrayList(RecipeClone.recipeList);
    }

    public RecipeClone(final ResourceLocation loc)
    {
        super(loc);
    }

    @Override
    public boolean canFit(final int width, final int height)
    {
        return width * height > 2;
    }

    @Override
    public boolean complete(final IPoweredProgress tile)
    {
        boolean completed = false;
        for (final ReviveMatcher matcher : RecipeClone.MATCHERS)
            if (completed = matcher.complete(tile)) break;
        if (!completed) completed = RecipeClone.ANYMATCHER.complete(tile);
        if (completed)
        {
            final List<ItemStack> remaining = Lists.newArrayList(this.getRemainingItems(tile.getCraftMatrix()));
            tile.setInventorySlotContents(tile.getOutputSlot(), this.getCraftingResult(tile.getCraftMatrix()));
            for (int i = 0; i < remaining.size(); i++)
            {
                final ItemStack stack = remaining.get(i);
                if (!stack.isEmpty()) tile.setInventorySlotContents(i, stack);
                else
                {
                    final ItemStack old = tile.getStackInSlot(i);
                    if (PokecubeManager.isFilled(old)) PlayerPokemobCache.UpdateCache(old, false, true);
                    tile.decrStackSize(i, 1);
                }
            }
            if (tile.getCraftMatrix().eventHandler != null) tile.getCraftMatrix().eventHandler.detectAndSendChanges();
        }
        return completed;
    }

    @Override
    public Function<ItemStack, Integer> getCostFunction()
    {
        return RecipeClone.ENERGYNEED;
    }

    @Override
    public ItemStack getCraftingResult(final CraftingInventory inv)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public int getEnergyCost(final IPoweredProgress tile)
    {
        for (final ReviveMatcher matcher : RecipeClone.MATCHERS)
            if (RecipeClone.getEntry(matcher, tile) != Database.missingno) return matcher.getEnergyCost();
        return RecipeClone.ANYMATCHER.getEnergyCost();
    }

    public PokedexEntry getPokedexEntry(final IPoweredProgress tile)
    {
        PokedexEntry entry = Database.missingno;
        for (final ReviveMatcher matcher : RecipeClone.MATCHERS)
            if ((entry = RecipeClone.getEntry(matcher, tile)) != Database.missingno) return entry;
        return RecipeClone.getEntry(RecipeClone.ANYMATCHER, tile);
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return RecipeClone.SERIALIZER;
    }

    /** Used to check if a recipe matches current crafting inventory */
    @Override
    public boolean matches(final CraftingInventory inv, final World worldIn)
    {
        for (final ReviveMatcher matcher : RecipeClone.MATCHERS)
            if (matcher.getEntry(inv, worldIn) != Database.missingno) return true;
        return RecipeClone.ANYMATCHER.getEntry(inv, worldIn) != Database.missingno;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(final CraftingInventory inv)
    {
        final NonNullList<ItemStack> nonnulllist = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
        if (!(inv instanceof PoweredCraftingInventory)) return nonnulllist;
        final PoweredCraftingInventory inv_p = (PoweredCraftingInventory) inv;
        if (!(inv_p.inventory instanceof ClonerTile)) return nonnulllist;
        final ClonerTile tile = (ClonerTile) inv_p.inventory;
        ReviveMatcher matcher = RecipeClone.ANYMATCHER;
        for (final ReviveMatcher matcher2 : RecipeClone.MATCHERS)
            if (matcher2.getEntry(inv, tile.getWorld()) != Database.missingno)
            {
                matcher = matcher2;
                break;
            }
        for (int i = 0; i < nonnulllist.size(); ++i)
        {
            final ItemStack item = inv.getStackInSlot(i);
            if (matcher.shouldKeep(item)) nonnulllist.set(i, item);
            else if (item.hasContainerItem()) nonnulllist.set(i, item.getContainerItem());
        }
        return nonnulllist;
    }
}
