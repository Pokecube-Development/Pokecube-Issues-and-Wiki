package thut.core.client.render.model.parts.textures;

import java.io.IOException;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class MergedTexture extends BaseTexture
{
    ResourceLocation locB;
    float alpha;

    public MergedTexture(ResourceLocation locA, ResourceLocation locB, int alpha)
    {
        super(locA);
        this.locB = locB;
        this.alpha = alpha / 255f;
    }

    @Override
    public void load(ResourceManager manager) throws IOException
    {
        SimpleTexture.TextureImage imageA = SimpleTexture.TextureImage.load(manager, this.location);
        imageA.throwIfError();
        TextureMetadataSection texturemetadatasection = imageA.getTextureMetadata();
        boolean isBlur;
        boolean isClamp;
        if (texturemetadatasection != null)
        {
            isBlur = texturemetadatasection.isBlur();
            isClamp = texturemetadatasection.isClamp();
        }
        else
        {
            isBlur = false;
            isClamp = false;
        }
        NativeImage nA = imageA.getImage();
        SimpleTexture.TextureImage imageB = SimpleTexture.TextureImage.load(manager, this.locB);
        imageB.throwIfError();
        NativeImage nB = imageB.getImage();

        alpha = 0.75f;
        float sA = (1 - alpha);
        float sB = alpha;
        int xb, yb, xa, ya;

        // We do this rather than nA.blendPixel as we need to weight based
        // on our alpha, rather than the pixels themselves.
        for (int x = 0; x < Math.max(nA.getWidth(), nB.getWidth()); x++)
            for (int y = 0; y < Math.max(nA.getHeight(), nB.getHeight()); y++)
        {
            xb = x % nB.getWidth();
            yb = y % nB.getHeight();
            xa = x % nA.getWidth();
            ya = y % nA.getHeight();
            int rgbaA = nA.getPixelRGBA(xa, ya);
            int rA = NativeImage.getR(rgbaA);
            int gA = NativeImage.getG(rgbaA);
            int bA = NativeImage.getB(rgbaA);
            int aA = NativeImage.getA(rgbaA);

            if (aA == 0) continue;

            int rB = rA, gB = gA, bB = bA;

            int rgbaB = nB.getPixelRGBA(xb, yb);
            rB = NativeImage.getR(rgbaB);
            gB = NativeImage.getG(rgbaB);
            bB = NativeImage.getB(rgbaB);

            rA = (int) (sA * rA + sB * rB);
            gA = (int) (sA * gA + sB * gB);
            bA = (int) (sA * bA + sB * bB);

            nA.setPixelRGBA(xa, ya, NativeImage.combine(aA, bA, gA, rA));
        }

        if (!RenderSystem.isOnRenderThreadOrInit())
        {
            RenderSystem.recordRenderCall(() -> {
                this.doLoad(nA, isBlur, isClamp);
            });
        }
        else
        {
            this.doLoad(nA, isBlur, isClamp);
        }
    }

    private void doLoad(NativeImage image, boolean isBlur, boolean isClamp)
    {
        TextureUtil.prepareImage(this.getId(), 0, image.getWidth(), image.getHeight());
        image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), isBlur, isClamp, false, true);
    }
}
