package pokecube.core.moves.animations.presets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import pokecube.core.client.render.mobs.overlays.Utils;
import pokecube.core.moves.animations.AnimPreset;
import pokecube.core.moves.animations.MoveAnimationBase;

import java.util.ArrayList;
import java.util.List;

@AnimPreset(getPreset = "throw")
public class ThrowParticle extends MoveAnimationBase
{
    private static final List<Vector3f> SPHERECACHE = new ArrayList<>();

    static
    {
        float x, y, z;
        int n_phi = 6;
        int n_theta = 6;
        float dtheta = (float) (Math.PI / n_theta);
        float dphi = (float) (2 * Math.PI / n_phi);
        float r = 1;
        for (int p = 0; p < n_phi; p++)
            for (int t = -n_theta; t < n_theta; t++)
            {
                float theta = t * dtheta;
                float phi = p * dphi;
                float nextt = theta + dtheta;
                float nextp = phi + dphi;

                y = r * Mth.sin(theta);
                x = y * Mth.cos(phi);
                z = y * Mth.sin(phi);
                y = r * Mth.cos(theta);
                SPHERECACHE.add(new Vector3f(x, y, z));
                y = r * Mth.sin(theta);
                x = y * Mth.cos(nextp);
                z = y * Mth.sin(nextp);
                y = r * Mth.cos(theta);
                SPHERECACHE.add(new Vector3f(x, y, z));
                y = r * Mth.sin(nextt);
                x = y * Mth.cos(nextp);
                z = y * Mth.sin(nextp);
                y = r * Mth.cos(nextt);
                SPHERECACHE.add(new Vector3f(x, y, z));

                y = r * Mth.sin(nextt);
                x = y * Mth.cos(nextp);
                z = y * Mth.sin(nextp);
                y = r * Mth.cos(nextt);
                SPHERECACHE.add(new Vector3f(x, y, z));
                y = r * Mth.sin(nextt);
                x = y * Mth.cos(phi);
                z = y * Mth.sin(phi);
                y = r * Mth.cos(nextt);
                SPHERECACHE.add(new Vector3f(x, y, z));
                y = r * Mth.sin(theta);
                x = y * Mth.cos(phi);
                z = y * Mth.sin(phi);
                y = r * Mth.cos(theta);
                SPHERECACHE.add(new Vector3f(x, y, z));
            }
    }

    public ThrowParticle()
    {}

    @OnlyIn(Dist.CLIENT)
    @Override
    public void clientAnimation(final PoseStack mat, final MultiBufferSource buffer, final MovePacketInfo info,
            final float partialTick, int packedLightIn)
    {
        var buf = Utils.makeBuilder(ClientSide.RENDER_TYPE, buffer);

        mat.pushPose();
        GlStateManager._enableDepthTest();

        this.initColour(info.currentTick, info.move);
        final float alpha = (this.values.rgba >> 24 & 255) / 255f;
        final float red = (this.values.rgba >> 16 & 255) / 255f;
        final float green = (this.values.rgba >> 8 & 255) / 255f;
        final float blue = (this.values.rgba & 255) / 255f;

        float r = values.width * 0.2f;
        var pos = mat.last().pose();
        SPHERECACHE.forEach(v -> buf.addVertex(pos, v.x() * r, v.y() * r, v.z() * r).setColor(red, green, blue, alpha)
                .setLight(packedLightIn));
        GlStateManager._disableDepthTest();
        mat.popPose();
    }
}
