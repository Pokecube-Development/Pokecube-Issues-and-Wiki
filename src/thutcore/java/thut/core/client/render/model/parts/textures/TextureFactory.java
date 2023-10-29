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
        if (tex.getPath().contains("--sep--"))
        {
            var paths = tex.getPath().split("--sep--");
            if (paths.length >= 4)
            {
                ResourceLocation locA = new ResourceLocation(tex.getNamespace(), paths[0]);
                ResourceLocation locB = new ResourceLocation(paths[1], paths[2]);
                texture = new MergedTexture(locA, locB, Integer.parseInt(paths[3]));
                texturemanager.register(tex, texture);
                return texture;
            }
        }
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

        if (img != null && expectedH > 0 && expectedW > 0)
        {
            imgAspectRatio = img.getHeight() / ((double) img.getWidth());
        }

        // If we were higher than expected, then it means it is probably a strip
        // texture, but if we got here, no mcmeta file. Rounded to 1 decimal
        // place to account for strange aspect ratios which are not animations.
        if (Math.round(10 * imgAspectRatio / expectedAspectRatio) >= 20)
        {
            texture = new AnimatedTexture(tex, img, expectedH, expectedW, false);
        }

        // Now register the texture and return. For animated ones, this
        // registers the time ticker for animations.
        texturemanager.register(tex, texture);
        return texture;
    }

}
