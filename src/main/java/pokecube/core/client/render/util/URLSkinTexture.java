package pokecube.core.client.render.util;

import java.io.File;

import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/** This class is directly based on the URL Skin texture used by CustomNPCs. */
@OnlyIn(Dist.CLIENT)
public class URLSkinTexture extends HttpTexture
{

    public URLSkinTexture(final File file, final String url, final ResourceLocation resource)
    {
        super(file, url, resource, false, (Runnable) null);
    }

}