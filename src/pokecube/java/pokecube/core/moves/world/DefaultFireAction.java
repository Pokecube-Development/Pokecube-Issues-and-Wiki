package pokecube.core.moves.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import thut.api.maths.Vector3;

import java.util.List;

public class DefaultFireAction extends DefaultAction
{
    public static int FIRESTRONG = 100;

    public static boolean attemptSmelt(final IPokemob attacker, final Vector3 pos)
    {
        final Level world = attacker.getEntity().level();
        final List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, pos.getAABB().inflate(1));
        if (!items.isEmpty())
        {
            boolean smelt = false;
            var quickCheck = RecipeManager.createCheck(RecipeType.SMELTING);
            for (final ItemEntity item2 : items)
            {
                final ItemStack stack = item2.getItem();
                final int num = stack.getCount();
                var recipeHolder = quickCheck.getRecipeFor(new SingleRecipeInput(stack), world).orElse(null);
                if (recipeHolder == null) continue;
                var recipe = recipeHolder.value();
                ItemStack newstack = recipe.getResultItem(world.registryAccess());
                if (!newstack.isEmpty())
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
                    item2.setItem(newstack);
                    item2.lifespan += 6000;
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

    /**
     * This will have the following effects, for fire type moves: Ignite
     * flamable blocks Melt snow If strong, melt obsidian to lava If none of the
     * above, attempt to cook items nearby
     */
    @Override
    public boolean applyOutOfCombat(IPokemob user, Vector3 location, HitResult hit)
    {
        if (move.getPWR() <= 0 || !PokecubeCore.getConfig().defaultFireActions) return false;
        final boolean smelted = DefaultFireAction.attemptSmelt(user, location);
        // First try to smelt items
        if (smelted) return true;
        if (hit.getType() != HitResult.Type.BLOCK) return false;
        if(!(hit instanceof BlockHitResult blockHit)) return false;

        final Level world = user.getEntity().level();
        BlockState state = world.getBlockState(blockHit.getBlockPos());
        Block block = state.getBlock();
        Direction direction = getHitDirection(world, blockHit);
        final BlockPos hitPos = blockHit.getBlockPos().immutable();
        final BlockPos prevPos = hitPos.relative(direction);
        final boolean light = BaseFireBlock.canBePlacedAt(world, prevPos, direction);
        Block prevBlock = world.getBlockState(prevPos).getBlock();
        // Now try weak effects

        // Melt Snow
        if (block == Blocks.SNOW_BLOCK)
        {
            // Things below here all actually damage blocks, so check this.
            if (!MoveEventsHandler.canAffectBlock(user, new Vector3(hitPos), move.getName())) return false;
            world.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState());
            return true;
        }
        // Melt Snow
        else if (block == Blocks.SNOW)
        {
            // Things below here all actually damage blocks, so check this.
            if (!MoveEventsHandler.canAffectBlock(user, new Vector3(hitPos), move.getName())) return false;
            final int level = state.getValue(SnowLayerBlock.LAYERS);
            world.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, level));
            return true;
        }
        // Melt Ice
        else if (block == Blocks.ICE)
        {
            // Things below here all actually damage blocks, so check this.
            if (!MoveEventsHandler.canAffectBlock(user, new Vector3(hitPos), move.getName())) return false;
            world.setBlockAndUpdate(hitPos, Blocks.WATER.defaultBlockState());
            return true;
        }

        // Start fires
        if (light && move.getPWR() < FIRESTRONG)
        {
            // Things below here all actually damage blocks, so check this.
            if (!MoveEventsHandler.canAffectBlock(user, new Vector3(prevPos), move.getName())) return false;
            final BlockState fire = BaseFireBlock.getState(world, prevPos);
            world.setBlockAndUpdate(prevPos, fire);
            return true;
        }
        // Then see if we need to try strong effects
        if (move.getPWR() < FIRESTRONG) return false;

        // Melt obsidian
        if (block == Blocks.OBSIDIAN)
        {
            // Things below here all actually damage blocks, so check this.
            if (!MoveEventsHandler.canAffectBlock(user, new Vector3(hitPos), move.getName())) return false;
            world.setBlockAndUpdate(hitPos, Blocks.LAVA.defaultBlockState());
            return true;
        }
        // Evapourate water
        else if (prevBlock == Blocks.WATER)
        {
            // Things below here all actually damage blocks, so check this.
            if (!MoveEventsHandler.canAffectBlock(user, new Vector3(prevPos), move.getName())) return false;
            world.setBlockAndUpdate(prevPos, Blocks.AIR.defaultBlockState());
            return true;
        }
        // Start fires
        else if (light)
        {
            // Things below here all actually damage blocks, so check this.
            if (!MoveEventsHandler.canAffectBlock(user, new Vector3(prevPos), move.getName())) return false;
            final BlockState fire = BaseFireBlock.getState(world, prevPos);
            world.setBlockAndUpdate(prevPos, fire);
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
