package thut.core.client.render.model.parts.textures;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;

public class BaseTexture extends SimpleTexture
{
    private static final float[] DEFAULT_SCALE =
    { 1, 1 };
    private static final float[] DEFAULT_OFFSET =
    { 0, 0 };

    public BaseTexture(ResourceLocation location)
    {
        super(location);
    }

    public float[] getTexScale()
    {
        return DEFAULT_SCALE;
    }

    public float[] getTexOffset()
    {
        return DEFAULT_OFFSET;
    }
}
