package thut.core.client.render.model.parts;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public interface RenderTypeProvider
{
    RenderType makeRenderType(final Material material, final ResourceLocation tex, Mode mode);

    public static RenderTypeProvider NORMAL = new RenderTypeProvider()
    {
        @Override
        public RenderType makeRenderType(Material material, ResourceLocation tex, Mode mode)
        {
            material.tex = tex;
            if (material.types.containsKey(tex)) return material.types.get(tex);
            if (material.render_name.contains("water_mask_"))
            {
                material.cull = false;
                material.types.put(tex, Material.WATER_MASK);
                return Material.WATER_MASK;
            }

            RenderType type = null;
            final String id = material.render_name + tex;
            final RenderType.CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder();
            // No blur, No MipMap
            builder.setTextureState(new RenderStateShard.TextureStateShard(tex, false, false));

            builder.setTransparencyState(Material.DEFAULTTRANSP);

            RenderStateShard.ShaderStateShard shard = Material.SHADERS.getOrDefault(material.shader,
                    RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER);

            builder.setShaderState(shard);

            // These are needed in general for world lighting
            builder.setLightmapState(RenderStateShard.LIGHTMAP);
            builder.setOverlayState(RenderStateShard.OVERLAY);

            final boolean transp = material.alpha < 1 || material.transluscent;
            if (transp)
            {
                // These act like masking
                builder.setWriteMaskState(RenderStateShard.COLOR_WRITE);
                builder.setDepthTestState(Material.LESSTHAN);
            }
            // Otheerwise disable culling entirely
            else builder.setCullState(RenderStateShard.NO_CULL);

            final RenderType.CompositeState rendertype$state = builder.createCompositeState(true);
            type = RenderType.create(id, DefaultVertexFormat.NEW_ENTITY, mode, 256, true, false, rendertype$state);

            material.types.put(tex, type);
            return type;
        }
    };

    public static RenderTypeProvider GLOWING = new RenderTypeProvider()
    {
        @Override
        public RenderType makeRenderType(Material material, ResourceLocation tex, Mode mode)
        {
            // TODO fix Pokecube-Development/Pokecube-Issues-and-Wiki #484 by
            // making this make some form of render type like the one used for
            // glowing mobs.
            return null;
        }
    };

}
