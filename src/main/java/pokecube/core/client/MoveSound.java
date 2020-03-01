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

    public MoveSound(final SoundEvent sound, final Vector3 pos)
    {
        super(sound, SoundCategory.BLOCKS);
        this.volume = 1F;
        this.pos1.set(pos);
        this.player = Minecraft.getInstance().player;
        this.x = (float) this.pos1.x;
        this.y = (float) this.pos1.y;
        this.z = (float) this.pos1.z;
    }

    @Override
    public void tick()
    {
        this.pos2.set(this.player);
        final double dist = this.pos2.distanceTo(this.pos1);
        this.volume = (float) (5.0f / dist);
        this.volume = Math.min(1, this.volume);
        if (this.time++ > 100 || dist > 20) this.donePlaying = true;
    }

    @Override
    public boolean canBeSilent()
    {
        return true;
    }
}
