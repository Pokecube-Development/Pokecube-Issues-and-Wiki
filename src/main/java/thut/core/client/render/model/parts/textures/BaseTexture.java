package thut.core.client.render.model.parts.textures;

import java.io.IOException;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
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

    protected NativeImage getImage()
    {
        try
        {
            var manager = Minecraft.getInstance().getResourceManager();
            var img = this.getTextureImage(manager).getImage();
            return img;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
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
