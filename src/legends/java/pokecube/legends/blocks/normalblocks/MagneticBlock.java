package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.legends.blocks.BlockBase;

public class MagneticBlock extends BlockBase
{

    public MagneticBlock(final MapColor color, final SoundType sound, final NoteBlockInstrument instrument,
                         final boolean requiresCorrectToolForDrops, final float destroyTime, final float blastResistance)
    {
        super(color, sound, instrument, requiresCorrectToolForDrops, destroyTime, blastResistance);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult)
    {
        if (player instanceof ServerPlayer)
        {
            if (!level.isClientSide) level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 3, Level.ExplosionInteraction.TNT);

            if (level instanceof ServerLevel)
            {
                //                 ((ServerWorld) world).addEntity(new LightningBoltEntity(null,
                //                 world));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
