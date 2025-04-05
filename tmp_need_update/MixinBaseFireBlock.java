package pokecube.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.legends.blocks.normalblocks.InfectedFireBlock;
import pokecube.legends.init.BlockInit;

@Mixin(BaseFireBlock.class)
public abstract class MixinBaseFireBlock extends Block
{
    public MixinBaseFireBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
    }

    @Inject(method = "getState", at = @At("HEAD"), cancellable = true)
    private static void getInfectedFirePlacement(BlockGetter reader, BlockPos pos, CallbackInfoReturnable<BlockState> info)
    {
        if (InfectedFireBlock.canSurviveOnBlock(reader.getBlockState(pos.below())))
        {
            info.setReturnValue(BlockInit.INFECTED_FIRE.get().defaultBlockState());
        }
    }
}
