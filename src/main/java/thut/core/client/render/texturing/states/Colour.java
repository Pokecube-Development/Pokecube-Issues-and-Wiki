package thut.core.client.render.texturing.states;

import thut.api.entity.IMobTexturable;

public class Colour
{
    public float   red   = 1;
    public float   blue  = 1;
    public float   green = 1;
    public float   alpha = 1;
    public String  forme = "";
    public boolean mul   = true;

    public Colour()
    {
    }

    public void apply(final int[] rgbaIn, final IMobTexturable mob)
    {
        if (!this.forme.isEmpty() && !this.forme.equals(mob.getForm())) return;

        if (this.mul)
        {
            final float r = this.red * rgbaIn[0] / 255f;
            final float g = this.green * rgbaIn[1] / 255f;
            final float b = this.blue * rgbaIn[2] / 255f;
            final float a = this.alpha * rgbaIn[3] / 255f;
            rgbaIn[0] = (int) (r * 255);
            rgbaIn[1] = (int) (g * 255);
            rgbaIn[2] = (int) (b * 255);
            rgbaIn[3] = (int) (a * 255);
        }
    }
}
