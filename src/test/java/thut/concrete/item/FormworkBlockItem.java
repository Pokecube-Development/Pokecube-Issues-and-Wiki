package thut.concrete.item;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import thut.concrete.block.FormworkBlock;

public class FormworkBlockItem extends BlockItem
{

    public FormworkBlockItem(Block block, Properties props)
    {
        super(block, props);
    }

    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context)
    {
        BlockPos blockpos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState blockstate = level.getBlockState(blockpos);
        Block block = this.getBlock();
        if (!blockstate.is(block))
        {
            return FormworkBlock.getDistance(level, blockpos) == 7 ? null : context;
        }
        else
        {
            Direction direction;
            if (context.isSecondaryUseActive())
            {
                direction = context.isInside() ? context.getClickedFace().getOpposite() : context.getClickedFace();
            }
            else
            {
                direction = context.getClickedFace() == Direction.UP ? context.getHorizontalDirection()
                        : Direction.UP;
            }

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
                        return BlockPlaceContext.at(context, blockpos$mutableblockpos, direction);
                    }
                    break;
                }

                blockpos$mutableblockpos.move(direction);
                if (direction.getAxis().isHorizontal())
                {
                    ++i;
                }
            }

            return null;
        }
    }

    protected boolean mustSurvive()
    {
        return false;
    }
}
