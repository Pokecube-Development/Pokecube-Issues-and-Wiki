package thut.concrete.item;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RebarBlockItem extends BlockItem
{

    public RebarBlockItem(Block block, Properties props)
    {
        super(block, props);
        // TODO Auto-generated constructor stub
    }

    @Override
    public InteractionResult useOn(UseOnContext p_40581_)
    {
        // TODO Auto-generated method stub
        return super.useOn(p_40581_);
    }

    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context)
    {
        BlockPos blockpos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState blockstate = level.getBlockState(blockpos);
        Block block = this.getBlock();
        BlockState usedOn = level.getBlockState(blockpos = new BlockPos(context.getClickLocation()));

        if (!usedOn.is(block))
        {
            return context;
        }
        else
        {
            double dy = context.getClickLocation().y - blockpos.getY();
            Direction direction;
            boolean rev = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();

            if (dy > 0.8) direction = Direction.UP;
            else if (dy < 0.2) direction = Direction.DOWN;
            else direction = rev ? context.getClickedFace().getOpposite() : context.getClickedFace();

            int i = 0;
            BlockPos.MutableBlockPos blockpos$mutableblockpos = blockpos.mutable().move(direction);

            while (i < 7)
            {
                if (!level.isClientSide && !level.isInWorldBounds(blockpos$mutableblockpos))
                {
                    Player player = context.getPlayer();
                    int j = level.getMaxBuildHeight();
                    if (player instanceof ServerPlayer && blockpos$mutableblockpos.getY() >= j)
                    {
                        ((ServerPlayer) player).sendMessage(
                                (new TranslatableComponent("build.tooHigh", j - 1)).withStyle(ChatFormatting.RED),
                                ChatType.GAME_INFO, Util.NIL_UUID);
                    }
                    break;
                }

                blockstate = level.getBlockState(blockpos$mutableblockpos);
                if (!blockstate.is(this.getBlock()))
                {
                    if (blockstate.canBeReplaced(context))
                    {
                        return BlockPlaceContext.at(context, blockpos$mutableblockpos, direction.getOpposite());
                    }
                    break;
                }

                blockpos$mutableblockpos.move(direction);
                ++i;
            }
            return null;
        }
    }

    protected boolean mustSurvive()
    {
        return false;
    }
}
