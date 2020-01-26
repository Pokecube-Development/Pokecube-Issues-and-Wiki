package pokecube.core.blocks.healer;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import pokecube.core.PokecubeCore;

public class HealerTile extends TileEntity implements ITickableTileEntity
{
    public static TileEntityType<? extends TileEntity> TYPE;

    public static SoundEvent MUSICLOOP;

    long startTick    = -1;
    int  tickDuration = -1;
    int  tick         = 0;

    public HealerTile()
    {
        super(HealerTile.TYPE);
        this.tickDuration = PokecubeCore.getConfig().pokeCenterLoopDir;
    }

    @Override
    public void tick()
    {
        if (!PokecubeCore.getConfig().pokeCenterMusic) return;
        if (!this.getWorld().isRemote || HealerTile.MUSICLOOP == null) return;
        final int power = this.getWorld().getStrongPower(this.getPos());
        final boolean play = power > 0;
        boolean sound = PokecubeCore.proxy.hasSound(this.getPos());

        if (!play)
        {
            this.tick = 0;
            if (sound) PokecubeCore.proxy.toggleSound(HealerTile.MUSICLOOP, this.getPos(), play, false,
                    SoundCategory.RECORDS);
            return;
        }

        // No sound, so we need to start timer.
        if (!sound)
        {
            if (this.tick > 2) this.tickDuration = this.tick - 3;
        }
        else this.tick++;

        if (sound && this.tick > this.tickDuration) sound = false;

        // Start playing the sound
        if (!sound)
        {
            System.out.println("Starting: " + this.tick + " " + this.tickDuration + " " + this.startTick);
            this.tick = 0;
            PokecubeCore.proxy.toggleSound(HealerTile.MUSICLOOP, this.getPos(), play, false, SoundCategory.RECORDS);
        }
    }
}
