package pokecube.core.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;
import thut.api.boom.ExplosionCustom;
import thut.api.terrain.BiomeType;

public class MeteorEvent extends Event
{
    private final BlockState original;
    private BlockState turnTo;
    private final BlockPos pos;
    private final float power;
    private final ExplosionCustom boom;
    private BiomeType subbiome = BiomeType.METEOR;

    public MeteorEvent(final BlockState original, final BlockState turnTo, final BlockPos pos, final float power, final ExplosionCustom boom)
    {
        this.original = original;
        this.setTurnTo(turnTo);
        this.pos = pos;
        this.power = power;
        this.boom = boom;
    }

    public BlockState getOriginal()
    {
        return this.original;
    }

    public BlockState getTurnTo()
    {
        return this.turnTo;
    }

    public void setTurnTo(final BlockState turnTo)
    {
        this.turnTo = turnTo;
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    public float getPower()
    {
        return this.power;
    }

    public ExplosionCustom getBoom()
    {
        return this.boom;
    }

    public BiomeType getSubbiome()
    {
        return this.subbiome;
    }

    public void setSubbiome(final BiomeType subbiome)
    {
        this.subbiome = subbiome;
    }

}
