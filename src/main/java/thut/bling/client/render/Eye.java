package thut.bling.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import thut.bling.ThutBling;
import thut.core.client.render.model.IModel;

public class Eye
{
    private static final RenderType TYPE = RenderType.create("thuttech:font", DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder()
                    .setTextureState(new TextureStateShard(
                            new ResourceLocation(ThutBling.MODID, "textures/items/eye.png"), false, false))
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () ->
                    {
                        RenderSystem.enableBlend();
                    }, () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    })).setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER).createCompositeState(false));

    public static void renderEye(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final ResourceLocation[] textures, final int brightness,
            final int overlay)
    {
        // TODO eye by model instead of texture.
        mat.pushPose();
        mat.translate(-0.26, -0.175, -0.251);

        final float height = 0.5f;
        final float width = 0.5f;
        final VertexConsumer vertexbuffer = buff.getBuffer(Eye.TYPE);

        vertexbuffer.vertex(mat.last().pose(), 0.0f, height, 0.0f).color(255, 255, 255, 255).uv(0, 1).endVertex();
        vertexbuffer.vertex(mat.last().pose(), width, height, 0.0f).color(255, 255, 255, 255).uv(1, 1).endVertex();
        vertexbuffer.vertex(mat.last().pose(), width, 0.0f, 0.0f).color(255, 255, 255, 255).uv(1, 0).endVertex();
        vertexbuffer.vertex(mat.last().pose(), 0.0f, 0.0f, 0.0f).color(255, 255, 255, 255).uv(0, 0).endVertex();
        mat.popPose();
    }
}
