package pokecube.core.moves.animations;

import java.util.Random;

import com.google.gson.JsonObject;

import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveAnimation;
import thut.api.util.JsonUtil;

public abstract class MoveAnimationBase implements IMoveAnimation
{

    public static class Values
    {
        public String particle = "misc";

        public int rgba = 0xFFFFFFFF;
        public int duration = 5;
        public int applyAfter = 0;
        public int lifetime = 5;

        public boolean customColour = false;
        public boolean flat = false;
        public boolean reverse = false;
        public boolean absolute = false;

        public float density = 1;
        public float width = 1;
        public float angle = 0;

        public String rgba_string = null;

        public String f_radial;
        public String f_phi;
        public String f_theta;

        public String f_x;
        public String f_y;
        public String f_z;
    }

    protected Values values = new Values();

    @Override
    public int getApplicationTick()
    {
        return this.values.applyAfter;
    }

    public int getColourFromMove(final MoveEntry move, int alpha)
    {
        alpha = Math.min(255, alpha);
        final int colour = move.getType(null).colour + 0x01000000 * alpha;
        return colour;
    }

    @Override
    public int getDuration()
    {
        return this.values.duration;
    }

    public IMoveAnimation init(JsonObject preset)
    {
        if (preset != null)
        {
            try
            {
                this.values = JsonUtil.gson.fromJson(preset, Values.class);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                values = new Values();
            }
        }
        else values = new Values();
        return this;
    }

    @OnlyIn(Dist.CLIENT)
    public void initColour(final long time, final float partialTicks, final MoveEntry move)
    {
        this.reallyInitRGBA();
        if (this.values.customColour) return;
        if (this.values.particle == null)
        {
            this.values.rgba = this.getColourFromMove(move, 255);
            return;
        }
        if (this.values.particle.equals("airbubble")) this.values.rgba = 0x78000000 + DyeColor.CYAN.getTextColor();
        else if (this.values.particle.equals("aurora"))
        {
            final DyeColor colour = DyeColor.values()[new Random(time / 10).nextInt(DyeColor.values().length)];
            final int rand = colour.getTextColor();
            this.values.rgba = 0x61000000 + rand;
        }
        else if (this.values.particle.equals("iceshard")) this.values.rgba = 0x78000000 + DyeColor.CYAN.getTextColor();
        else if (this.values.particle.equals("spark")) this.values.rgba = 0x78000000 + DyeColor.YELLOW.getTextColor();
        else this.values.rgba = this.getColourFromMove(move, 255);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void reallyInitRGBA()
    {
        if (this.values.rgba_string == null) return;
        final String val = this.values.rgba_string;
        this.values.rgba_string = null;
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
                for (final DyeColor col : DyeColor.values()) if (col.getSerializedName().equals(val))
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
                this.values.rgba = Integer.parseInt(val);
            }
            catch (final NumberFormatException e)
            {

            }
            return;
        }
        this.values.rgba = colour.getTextColor() + 0x01000000 * alpha;
        this.values.customColour = true;
    }

    @Override
    public void setDuration(final int duration)
    {
        this.values.duration = duration;
    }
}
