package pokecube.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundCategory;
import pokecube.core.blocks.healer.HealerTile;
import thut.api.maths.Vector3;

public class PokecenterSound extends TickableSound
{
    private final HealerTile         tile;
    private final ClientPlayerEntity player;
    private final Vector3            pos1 = Vector3.getNewVector();
    private final Vector3            pos2 = Vector3.getNewVector();

    public PokecenterSound(final HealerTile tileIn)
    {
        super(HealerTile.MUSICLOOP, SoundCategory.BLOCKS);
        this.tile = tileIn;
        this.repeat = true;
        this.repeatDelay = 1;
        this.volume = 0.1F;
        this.player = Minecraft.getInstance().player;
        this.pos1.set(tileIn).addTo(0.5, 0.5, 0.5);
        this.x = (float) this.pos1.x;
        this.y = (float) this.pos1.y;
        this.z = (float) this.pos1.z;
    }

    @Override
    public void tick()
    {
        this.pos2.set(this.player);
        final double distSq = this.pos2.distToSq(this.pos1);
        this.volume = (float) (50.0f / distSq);
        this.volume = Math.min(1, this.volume);
        if (!this.tile.play) this.volume = 0;
    }

    @Override
    public boolean canBeSilent()
    {
        return true;
    }
}
