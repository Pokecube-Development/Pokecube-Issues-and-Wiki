package pokecube.core.client.render.mobs.overlays;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class Utils
{
    public static VertexConsumer makeBuilder(final RenderType type, final MultiBufferSource buffer)
    {
        return buffer.getBuffer(type);
    }
}
