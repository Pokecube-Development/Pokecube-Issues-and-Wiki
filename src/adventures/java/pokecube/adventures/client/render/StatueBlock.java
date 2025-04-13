package pokecube.adventures.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import pokecube.adventures.blocks.statue.StatueEntity;
import pokecube.core.client.render.mobs.overlays.Status.StatusTexturer;
import thut.api.attachments.CopyMob;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.core.client.render.animation.AnimationHelper;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.wrappers.ModelWrapper;

public class StatueBlock implements BlockEntityRenderer<StatueEntity>
{
    public StatueBlock(final BlockEntityRendererProvider.Context dispatcher)
    {}

    public static void renderStatue(CopyMob.CopyInfo info, final float partialTicks, final PoseStack mat,
            final MultiBufferSource bufferIn, final int combinedLightIn, final int combinedOverlayIn)
    {
        if (info.copy().getCopiedMob() == null)
        {
            return;
        }
        var mob = info.copy().getCopiedMob();

        final Minecraft mc = Minecraft.getInstance();
        CompoundTag tag = info.tag();
        if (tag.contains("statue:over_tex") && mc.getEntityRenderDispatcher()
                .getRenderer(mob) instanceof LivingEntityRenderer<?, ?> _renderer
                && _renderer.getModel() instanceof ModelWrapper<?> _wrap)
        {
            ResourceLocation tex;
            if (tag.contains("statue:tex_cache"))
            {
                tex = ResourceLocation.parse(tag.getString("statue:tex_cache"));
            }
            else
            {
                ResourceLocation inTag = ResourceLocation.parse(tag.getString("statue:over_tex"));
                boolean isBlock = BuiltInRegistries.BLOCK.containsKey(inTag);
                if (isBlock)
                {
                    Block b = BuiltInRegistries.BLOCK.get(inTag);
                    @SuppressWarnings("deprecation")
                    ResourceLocation tex_ = mc.getBlockRenderer().getBlockModel(b.defaultBlockState()).getParticleIcon()
                            .contents().name();
                    tex = ResourceLocation.fromNamespaceAndPath(tex_.getNamespace(),
                            "textures/" + tex_.getPath() + ".png");
                }
                else tex = inTag;
                tag.putString("statue:tex_cache", tex.toString());
            }
            int alpha = tag.contains("statue:over_tex_a") ? tag.getInt("statue:over_tex_a") : 200;

            StatusTexturer newTexer = new StatusTexturer(tex);
            newTexer.alpha = alpha;
            newTexer.animated = false;

            if (tag.contains("statue:anim"))
            {
                String anim = tag.getString("statue:anim");
                final IAnimationHolder anims = AnimationHelper.getHolder(mob);
                if (anims != null)
                {
                    anims.setFixed(true);
                    anims.overridePlaying(anim);
                }
            }

            @SuppressWarnings("unchecked")
            ModelWrapper<LivingEntity> wrap = (ModelWrapper<LivingEntity>) _wrap;
            IPartTexturer texer = wrap.renderer.getTexturer();
            newTexer.wrapped = texer;
            wrap.renderer.setTexturer(newTexer);
            newTexer.bindObject(mob);
            mc.getEntityRenderDispatcher().setRenderShadow(false);
            mc.getEntityRenderDispatcher().render(mob, 0.5f, 0, 0.5f, 0, 1, mat, bufferIn, combinedLightIn);
            wrap.renderer.setTexturer(texer);
        }
        else
        {
            mc.getEntityRenderDispatcher().setRenderShadow(false);
            mc.getEntityRenderDispatcher().render(mob, 0.5f, 0, 0.5f, 0, 1, mat, bufferIn, combinedLightIn);
        }
    }

    @Override
    public void render(final StatueEntity tile, final float partialTicks, final PoseStack matrixStackIn,
            final MultiBufferSource bufferIn, final int combinedLightIn, final int combinedOverlayIn)
    {
        var info = StatueEntity.unpackStatue(tile);
        tile.checkMob();
        if (info.copy().getCopiedMob() == null || tile.ticks++ < 10) return;
        renderStatue(info, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    @Override
    public boolean shouldRenderOffScreen(StatueEntity blockEntity)
    {
        var info = StatueEntity.unpackStatue(blockEntity);
        if (info == null || info.copy().getCopiedMob() == null)
        {
            return false;
        }
        return info.copy().getCopiedMob().getBbWidth() > 1;
    }

    @Override
    public AABB getRenderBoundingBox(StatueEntity blockEntity)
    {
        return AABB.INFINITE;
    }
}
