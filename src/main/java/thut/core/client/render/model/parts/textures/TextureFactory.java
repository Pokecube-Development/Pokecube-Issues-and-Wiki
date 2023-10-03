package thut.core.client.render.model.parts.textures;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import thut.lib.ResourceHelper;

public class TextureFactory
{

    public static BaseTexture create(TextureManager texturemanager, ResourceLocation tex, float expectedH,
            float expectedW)
    {
        // First check if a mcmeta version of the file exists.
        ResourceLocation mcmeta = new ResourceLocation(tex.getNamespace(), tex.getPath() + ".mcmeta");
        BaseTexture texture = null;
        // default this negative so not more than expected if not found.
        NativeImage img = null;
        if (ResourceHelper.exists(mcmeta, Minecraft.getInstance().getResourceManager()))
        {
            // If has mcmeta file, read info from that, and hand in -1 for
            // img_h, as it will determine it itself.
            texture = new AnimatedTexture(tex, img, expectedH, expectedW, true);
        }
        else
        {
            // Otherwise make a BaseTexture, and then have that check image
            // size.
            texture = new BaseTexture(tex);
            img = texture.getImage();
        }

        double expectedAspectRatio = expectedH / expectedW;
        double imgAspectRatio = expectedAspectRatio;

        if (img != null)
        {
            imgAspectRatio = img.getHeight() / ((double) img.getWidth());
        }

        // If we were higher than expected, then it means it is probably a strip
        // texture, but if we got here, no mcmeta file.
        if (Math.abs(imgAspectRatio - expectedAspectRatio) > 1e-3)
            texture = new AnimatedTexture(tex, img, expectedH, expectedW, false);

        // Now register the texture and return. For animated ones, this
        // registers the time ticker for animations.
        texturemanager.register(tex, texture);
        return texture;
    }

}
