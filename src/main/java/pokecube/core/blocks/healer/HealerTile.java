package pokecube.core.blocks.healer;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;

public class HealerTile extends TileEntity implements ITickableTileEntity
{
    public static SoundEvent MUSICLOOP;

    public boolean play = false;

    public HealerTile()
    {
        super(PokecubeItems.HEALER_TYPE.get());
    }

    @Override
    public void tick()
    {
        if (!PokecubeCore.getConfig().pokeCenterMusic) return;
        if (!this.getLevel().isClientSide || HealerTile.MUSICLOOP == null) return;
        final int power = this.getLevel().getBestNeighborSignal(this.getBlockPos());
        this.play = power > 0;
        PokecubeCore.proxy.pokecenterloop(this, this.play);
    }
}
