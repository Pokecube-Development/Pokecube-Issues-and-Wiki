package pokecube.core.blocks.healer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import thut.api.block.ITickTile;

public class HealerTile extends BlockEntity implements ITickTile
{
    public boolean play = false;

    public HealerTile(final BlockPos pos, final BlockState state)
    {
        super(PokecubeItems.HEALER_TYPE.get(), pos, state);
    }

    @Override
    public void tick()
    {
        if (!PokecubeCore.getConfig().pokeCenterMusic) return;
        if (!this.getLevel().isClientSide) return;
        final int power = this.getLevel().getBestNeighborSignal(this.getBlockPos());
        this.play = power > 0;
        PokecubeCore.proxy.pokecenterloop(this, this.play);
    }
}
