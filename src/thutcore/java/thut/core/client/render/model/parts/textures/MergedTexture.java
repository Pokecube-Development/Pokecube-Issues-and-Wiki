package thut.core.client.render.model.parts.textures;

import java.io.IOException;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;

public class MergedTexture extends BaseTexture
{
    ResourceLocation locB;
    int ARGB_A;
    int ARGB_B;
    int ARGB_M;

    public MergedTexture(ResourceLocation locA, ResourceLocation locB, int ARGB_A, int ARGB_B)
    {
        super(locA);
        this.locB = locB;
        this.ARGB_A = ARGB_A;
        this.ARGB_B = ARGB_B;
        ARGB_M = FastColor.ARGB32.average(ARGB_A, ARGB_B);
    }

    public MergedTexture(ResourceLocation locA, ResourceLocation locB, int alpha)
    {
        super(locA);
        this.locB = locB;
        ARGB_A = FastColor.ARGB32.color(255 - alpha,255,255,255);
        ARGB_B = FastColor.ARGB32.color(alpha, 255,255,255);
        ARGB_M = FastColor.ARGB32.average(ARGB_A, ARGB_B);
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

        float sA = FastColor.ARGB32.alpha(ARGB_A) / 255f;
        float sB = FastColor.ARGB32.alpha(ARGB_B) / 255f;
        float scale = sA + sB;
        if(scale > 1)
        {
            sA /= scale;
            sB /= scale;
        }

        float mR = FastColor.ARGB32.red(ARGB_M) / 255f;
        float mG = FastColor.ARGB32.green(ARGB_M) / 255f;
        float mB = FastColor.ARGB32.blue(ARGB_M) / 255f;

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
            int rA = FastColor.ABGR32.red(rgbaA);
            int gA = FastColor.ABGR32.green(rgbaA);
            int bA = FastColor.ABGR32.blue(rgbaA);
            int aA = FastColor.ABGR32.alpha(rgbaA);

            if (aA == 0) continue;

            int rB, gB, bB;

            int rgbaB = nB.getPixelRGBA(xb, yb);
            rB = FastColor.ABGR32.red(rgbaB);
            gB = FastColor.ABGR32.green(rgbaB);
            bB = FastColor.ABGR32.blue(rgbaB);

            rA = (int) Math.min(255, (sA * rA + sB * rB) * mR);
            gA = (int) Math.min(255, (sA * gA + sB * gB) * mG);
            bA = (int) Math.min(255, (sA * bA + sB * bB) * mB);

            nA.setPixelRGBA(xa, ya, FastColor.ABGR32.color(aA, bA, gA, rA));
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
