package pokecube.core.blocks.healer;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;

public class HealerTile extends TileEntity implements ITickableTileEntity
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(HealerTile::new,
            PokecubeItems.HEALER).build(null);

    public static SoundEvent MUSICLOOP;

    long startTick    = -1;
    int  tickDuration = -1;
    int  tick         = 0;

    public HealerTile()
    {
        this(HealerTile.TYPE);
    }

    public HealerTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        super.read(compound);
    }

    @Override
    public void tick()
    {
        if (!this.getWorld().isRemote || HealerTile.MUSICLOOP == null) return;
        final int power = this.getWorld().getStrongPower(this.getPos());
        final boolean play = power > 0;
        boolean sound = PokecubeCore.proxy.hasSound(this.getPos());

        if (!play)
        {
            this.tick = 0;
            if (sound) PokecubeCore.proxy.toggleSound(HealerTile.MUSICLOOP, this.getPos(), power > 0, false,
                    SoundCategory.RECORDS);
            return;
        }

        // Conclude timer for tick length
        if (!sound && this.startTick != -1)
        {
            if (this.tickDuration == -1) this.tickDuration = (int) (this.getWorld().getGameTime() - this.startTick) - 1;
            this.startTick = -1;
        }
        // Start timer for tick length
        if (!sound && this.startTick == -1) this.startTick = this.getWorld().getGameTime();

        // Stop the sound, as we are right at the end.
        if (this.tickDuration != -1 && this.tickDuration < this.tick++ && sound)
        {
            sound = false;
            PokecubeCore.proxy.toggleSound(HealerTile.MUSICLOOP, this.getPos(), power > 0, true, SoundCategory.RECORDS);
            this.tick = 0;
        }

        // Start playing the sound
        if (!sound)
        {
            this.tick = 0;
            PokecubeCore.proxy.toggleSound(HealerTile.MUSICLOOP, this.getPos(), power > 0, false,
                    SoundCategory.RECORDS);
        }
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound)
    {
        return super.write(compound);
    }

}
