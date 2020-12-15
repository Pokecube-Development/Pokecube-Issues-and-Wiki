package pokecube.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import thut.api.maths.Vector3;

public class MoveSound extends TickableSound
{
    private final ClientPlayerEntity player;
    private final Vector3            pos1 = Vector3.getNewVector();
    private final Vector3            pos2 = Vector3.getNewVector();
    private int                      time = 0;

    public static float getVolume(final Vector3 pos1, final Vector3 pos2, final float volumeScale)
    {
        final double dist = pos2.distanceTo(pos1);
        if (dist > 32) return 0;
        float volume = (float) (1f / dist) * volumeScale;
        volume = Math.min(1, volume);
        return volume;
    }

    private float volumeScale = 1;

    public MoveSound(final SoundEvent sound, final Vector3 pos, final float volumeScale)
    {
        super(sound, SoundCategory.BLOCKS);
        this.volumeScale = volumeScale;
        this.pos1.set(pos);
        this.player = Minecraft.getInstance().player;
        this.x = (float) this.pos1.x;
        this.y = (float) this.pos1.y;
        this.z = (float) this.pos1.z;
        this.volume = MoveSound.getVolume(this.pos1, this.pos2, this.volumeScale);
    }

    @Override
    public void tick()
    {
        this.pos2.set(this.player);
        this.volume = MoveSound.getVolume(this.pos1, this.pos2, this.volumeScale);
        if (this.time++ > 100 || this.volume == 0) this.repeat = true;
    }

    @Override
    public boolean canBeSilent()
    {
        return true;
    }
}
