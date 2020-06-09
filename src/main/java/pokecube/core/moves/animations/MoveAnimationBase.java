package pokecube.core.moves.animations;

import java.util.Random;

import net.minecraft.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.interfaces.IMoveAnimation;
import pokecube.core.interfaces.Move_Base;

public abstract class MoveAnimationBase implements IMoveAnimation
{
    protected String particle;

    protected int rgba         = 0xFFFFFFFF;
    protected int duration     = 5;
    protected int particleLife = 5;

    protected boolean customColour = false;
    protected boolean flat         = false;
    protected boolean reverse      = false;

    protected float density = 1;
    protected float width   = 1;
    protected float angle   = 0;

    protected String rgbaVal = null;

    @Override
    public int getApplicationTick()
    {
        return this.duration;
    }

    public int getColourFromMove(final Move_Base move, int alpha)
    {
        alpha = Math.min(255, alpha);
        final int colour = move.getType(null).colour + 0x01000000 * alpha;
        return colour;
    }

    @Override
    public int getDuration()
    {
        return this.duration;
    }

    public IMoveAnimation init(final String preset)
    {
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public void initColour(final long time, final float partialTicks, final Move_Base move)
    {
        this.reallyInitRGBA();
        if (this.customColour) return;
        if (this.particle == null)
        {
            this.rgba = this.getColourFromMove(move, 255);
            return;
        }
        if (this.particle.equals("airbubble")) this.rgba = 0x78000000 + DyeColor.CYAN.getTextColor();
        else if (this.particle.equals("aurora"))
        {
            final DyeColor colour = DyeColor.values()[new Random(time / 10).nextInt(DyeColor.values().length)];
            final int rand = colour.getTextColor();
            this.rgba = 0x61000000 + rand;
        }
        else if (this.particle.equals("iceshard")) this.rgba = 0x78000000 + DyeColor.CYAN.getTextColor();
        else if (this.particle.equals("spark")) this.rgba = 0x78000000 + DyeColor.YELLOW.getTextColor();
        else this.rgba = this.getColourFromMove(move, 255);
    }

    protected void initRGBA(final String val)
    {
        this.rgbaVal = val;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void reallyInitRGBA()
    {
        if (this.rgbaVal == null) return;
        final String val = this.rgbaVal;
        this.rgbaVal = null;
        final int alpha = 255;
        DyeColor colour = null;
        try
        {
            colour = DyeColor.byId(Integer.parseInt(val));
        }
        catch (final NumberFormatException e)
        {
            try
            {
                colour = DyeColor.valueOf(val);
            }
            catch (final Exception e1)
            {
                for (final DyeColor col : DyeColor.values())
                    if (col.getName().equals(val))
                    {
                        colour = col;
                        break;
                    }
            }
        }
        if (colour == null)
        {
            try
            {
                this.rgba = Integer.parseInt(val);
            }
            catch (final NumberFormatException e)
            {

            }
            return;
        }
        this.rgba = colour.getTextColor() + 0x01000000 * alpha;
        this.customColour = true;
    }

    @Override
    public void setDuration(final int duration)
    {
        this.duration = duration;
    }
}
