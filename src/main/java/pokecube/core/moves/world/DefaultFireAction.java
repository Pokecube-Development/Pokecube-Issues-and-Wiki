package pokecube.core.moves.world;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.eventhandlers.MoveEventsHandler.UseContext;
import thut.api.maths.Vector3;

public class DefaultFireAction extends DefaultAction
{
    public static int FIRESTRONG = 100;

    public static boolean attemptSmelt(final IPokemob attacker, final Vector3 pos)
    {
        final Level world = attacker.getEntity().getLevel();
        final List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, pos.getAABB().inflate(1));
        if (!items.isEmpty())
        {
            boolean smelt = false;
            final AbstractFurnaceBlockEntity tile = new FurnaceBlockEntity(pos.getPos(), pos.getBlockState(world));
            tile.setLevel(world);
            for (final ItemEntity item2 : items)
            {
                final ItemEntity item = item2;
                final ItemStack stack = item.getItem();
                final int num = stack.getCount();
                tile.setItem(0, stack);
                tile.setItem(1, stack);
                var recipe = world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, tile, world).orElse(null);
                if (recipe == null) continue;
                ItemStack newstack = recipe.getResultItem();
                if (newstack != null)
                {
                    newstack = newstack.copy();
                    newstack.setCount(num);
                    int i1 = num;
                    float f = recipe.getExperience();
                    if (f == 0.0F) i1 = 0;
                    else if (f < 1.0F)
                    {
                        int j = Mth.floor(i1 * f);
                        if (j < Mth.ceil(i1 * f) && Math.random() < i1 * f - j) ++j;

                        i1 = j;
                    }
                    f = i1;
                    while (i1 > 0)
                    {
                        final int k = ExperienceOrb.getExperienceValue(i1);
                        i1 -= k;
                        world.addFreshEntity(new ExperienceOrb(world, pos.x, pos.y + 1.5D, pos.z + 0.5D, k));
                    }
                    int hunger = PokecubeCore.getConfig().baseSmeltingHunger * num;
                    hunger = (int) Math.max(1, hunger / (float) attacker.getLevel());
                    if (f > 0) hunger *= f;
                    attacker.applyHunger(hunger);
                    item.setItem(newstack);
                    item.lifespan += 6000;
                    smelt = true;
                }
            }
            return smelt;
        }
        return false;
    }

    public DefaultFireAction(MoveEntry move)
    {
        super(move);
    }

    @Override
    /**
     * This will have the following effects, for fire type moves: Ignite
     * flamable blocks Melt snow If strong, melt obsidian to lava If none of the
     * above, attempt to cook items nearby
     */
    public boolean applyOutOfCombat(IPokemob user, Vector3 location)
    {
        if (move.getPWR() <= 0 || !PokecubeCore.getConfig().defaultFireActions) return false;
        final Level world = user.getEntity().getLevel();
        final UseContext context = MoveEventsHandler.getContext(world, user, Blocks.LAVA.defaultBlockState(), location);
        final BlockState state = context.getHitState();
        Block block = state.getBlock();
        final BlockPos hitPos = context.getHitPos();
        final BlockPos prevPos = context.getClickedPos();
        final BlockPos placePos = prevPos;
        final boolean light = BaseFireBlock.canBePlacedAt(world, placePos, context.getHorizontalDirection());
        final BlockState prev = world.getBlockState(prevPos);

        final boolean smelted = DefaultFireAction.attemptSmelt(user, location);
        // First try to smelt items
        if (smelted) return true;

        // Things below here all actually damage blocks, so check this.
        if (!MoveEventsHandler.canAffectBlock(user, location, move.getName())) return false;

        // Melt Snow
        if (block == Blocks.SNOW_BLOCK)
        {
            world.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState());
            return true;
        }
        // Melt Snow
        else if (block == Blocks.SNOW)
        {
            final int level = state.getValue(SnowLayerBlock.LAYERS);
            world.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, level));
            return true;
        }
        // Melt Ice
        else if (block == Blocks.ICE)
        {
            world.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState());
            return true;
        }
        block = prev.getBlock();

        // Melt Snow
        if (block == Blocks.SNOW_BLOCK)
        {
            world.setBlockAndUpdate(prevPos, Blocks.WATER.defaultBlockState());
            return true;
        }
        // Melt Snow
        else if (block == Blocks.SNOW)
        {
            final int level = prev.getValue(SnowLayerBlock.LAYERS);
            world.setBlockAndUpdate(prevPos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, level));
            return true;
        }
        // Melt Ice
        else if (block == Blocks.ICE)
        {
            world.setBlockAndUpdate(prevPos, Blocks.WATER.defaultBlockState());
            return true;
        }

        // Start fires
        if (light && move.getPWR() < FIRESTRONG)
        {
            final BlockState fire = BaseFireBlock.getState(world, placePos);
            world.setBlockAndUpdate(placePos, fire);
            return true;
        }
        if (move.getPWR() < FIRESTRONG) return false;

        block = state.getBlock();
        // Melt obsidian
        if (block == Blocks.OBSIDIAN)
        {
            world.setBlockAndUpdate(hitPos, Blocks.LAVA.defaultBlockState());
            return true;
        }
        // Evapourate water
        else if (block == Blocks.WATER)
        {
            world.setBlockAndUpdate(hitPos, Blocks.AIR.defaultBlockState());
            return true;
        }
        block = prev.getBlock();
        if (block == Blocks.OBSIDIAN)
        {
            world.setBlockAndUpdate(hitPos, Blocks.LAVA.defaultBlockState());
            return true;
        }
        // Evapourate water
        else if (block == Blocks.WATER)
        {
            world.setBlockAndUpdate(hitPos, Blocks.AIR.defaultBlockState());
            return true;
        }
        // Start fires
        else if (light)
        {
            final BlockState fire = BaseFireBlock.getState(world, placePos);
            world.setBlockAndUpdate(placePos, fire);
            return true;
        }
        return false;
    }

    @Override
    public boolean isValid()
    {
        return move.getType(null) == PokeType.getType("fire");
    }

}
