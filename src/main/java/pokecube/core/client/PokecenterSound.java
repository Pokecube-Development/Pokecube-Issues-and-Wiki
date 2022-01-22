package pokecube.core.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import pokecube.core.blocks.healer.HealerTile;
import thut.api.maths.Vector3;

public class PokecenterSound extends AbstractTickableSoundInstance
{
    private final HealerTile tile;

    public boolean stopped = false;

    public PokecenterSound(final HealerTile tileIn)
    {
        super(HealerTile.MUSICLOOP, SoundSource.RECORDS);
        this.tile = tileIn;
        this.looping = true;
        this.delay = 1;
        this.volume = 3F;
        final Vector3 pos1 = new Vector3();
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
            this.looping = true;
            return;
        }
        if (!this.tile.play) this.volume = 0;
    }

    @Override
    public boolean canStartSilent()
    {
        return true;
    }
}
