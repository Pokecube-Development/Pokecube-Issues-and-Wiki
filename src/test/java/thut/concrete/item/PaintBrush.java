package thut.concrete.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import thut.api.block.IDyedBlock;
import thut.api.block.flowing.IFlowingBlock;
import thut.concrete.Concrete;
import thut.concrete.block.ConcreteBlock;

public class PaintBrush extends Item
{
    private final DyeColor colour;

    public PaintBrush(Properties properties, DyeColor colour)
    {
        super(properties);
        this.colour = colour;
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        if (colour == null) return super.useOn(context);
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

//        if (level instanceof ServerLevel)
//        {
//            ExplosionCustom boom = new ExplosionCustom(level, null, Vector3.getNewVector().set(pos), 200);
//            boom.breaker = new ChamberBoom(4);
//            boom.doExplosion();
//        }

        if (state.getBlock() instanceof IDyedBlock b)
        {
            if (colour != b.getColour() && b.getFor(colour) != null)
            {
                BlockState painted = IFlowingBlock.copyValidTo(state, b.getFor(colour).defaultBlockState());
                level.setBlock(pos, painted, 3);
                ItemStack stack = context.getItemInHand();
                if (context.getPlayer() instanceof ServerPlayer player)
                {
                    boolean broke = stack.hurt(1, player.getRandom(), player);
                    if (broke) stack = new ItemStack(Concrete.BRUSHES[16].get());
                    player.setItemInHand(context.getHand(), stack);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        else if (ConcreteBlock.VANILLAREV.containsKey(state.getBlock()))
        {
            DyeColor old = ConcreteBlock.VANILLAREV.get(state.getBlock());
            if (old != this.colour)
            {
                ItemStack stack = context.getItemInHand();
                if (context.getPlayer() instanceof ServerPlayer player)
                {
                    boolean broke = stack.hurt(1, player.getRandom(), player);
                    if (broke) stack = new ItemStack(Concrete.BRUSHES[16].get());
                    player.setItemInHand(context.getHand(), stack);
                }
                Block newBlock = ConcreteBlock.VANILLA.get(colour);
                level.setBlock(pos, newBlock.defaultBlockState(), 3);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return super.useOn(context);
    }

}
