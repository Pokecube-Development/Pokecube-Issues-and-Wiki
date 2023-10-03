package thut.core.client.render.model.parts.textures;

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
        ResourceLocation mcmeta = new ResourceLocation(tex.getNamespace(), tex.getPath());
        BaseTexture texture = null;
        if (ResourceHelper.exists(mcmeta, Minecraft.getInstance().getResourceManager()))
            texture = new AnimatedTexture(tex, expectedH, expectedW);
        else texture = new BaseTexture(tex);
        texturemanager.register(tex, texture);
        return texture;
    }

}
