package pokecube.core.blocks.healer;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import pokecube.core.PokecubeCore;

public class HealerTile extends TileEntity implements ITickableTileEntity
{
    public static TileEntityType<? extends TileEntity> TYPE;

    public static SoundEvent MUSICLOOP;

    public boolean play = false;

    public HealerTile()
    {
        super(HealerTile.TYPE);
    }

    @Override
    public void tick()
    {
        if (!PokecubeCore.getConfig().pokeCenterMusic) return;
        if (!this.getWorld().isRemote || HealerTile.MUSICLOOP == null) return;
        final int power = this.getWorld().getRedstonePowerFromNeighbors(this.getPos());
        this.play = power > 0;
        PokecubeCore.proxy.pokecenterloop(this, this.play);
    }
}
