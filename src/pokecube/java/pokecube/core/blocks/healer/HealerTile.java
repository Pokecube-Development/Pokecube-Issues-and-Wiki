package pokecube.core.blocks.healer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import thut.api.Tracker;
import thut.api.block.ITickTile;

public class HealerTile extends BlockEntity implements ITickTile
{
    public static long placementDelay = 30;// 1.5s default delay
    public boolean play = false;
    public long placeTime = -1;

    public HealerTile(final BlockPos pos, final BlockState state)
    {
        super(PokecubeItems.HEALER_TYPE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        tag.putLong("t", placeTime);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        placeTime = tag.getLong("t");
        super.loadAdditional(tag, registries);
    }

    @Override
    public void tick()
    {
        if (placeTime <= 0) placeTime = Tracker.instance().getTick() + placementDelay;
        if (!PokecubeCore.getConfig().pokeCenterMusic) return;
        if (!this.getLevel().isClientSide) return;
        final int power = this.getLevel().getBestNeighborSignal(this.getBlockPos());
        this.play = power > 0;
        PokecubeCore.proxy.pokecenterloop(this, this.play);
    }
}
