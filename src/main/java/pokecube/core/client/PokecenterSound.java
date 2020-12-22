package pokecube.core.client;

import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;
import pokecube.core.blocks.healer.HealerTile;
import thut.api.maths.Vector3;

public class PokecenterSound extends TickableSound
{
    private final HealerTile tile;

    public boolean stopped = false;

    public PokecenterSound(final HealerTile tileIn)
    {
        super(HealerTile.MUSICLOOP, SoundCategory.BLOCKS);
        this.tile = tileIn;
        this.repeat = true;
        this.repeatDelay = 1;
        this.volume = 3F;
        final Vector3 pos1 = Vector3.getNewVector();
        pos1.set(tileIn).addTo(0.5, 0.5, 0.5);
        this.x = (float) pos1.x;
        this.y = (float) pos1.y;
        this.z = (float) pos1.z;
    }

    @Override
    public void tick()
    {
        if (this.stopped)
        {
            this.volume = 0;
            this.repeat = true;
            return;
        }
        if (!this.tile.play) this.volume = 0;
    }

    @Override
    public boolean canBeSilent()
    {
        return true;
    }
}
