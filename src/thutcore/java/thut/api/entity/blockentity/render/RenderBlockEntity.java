package thut.api.entity.blockentity.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.IBlockEntity;

@OnlyIn(Dist.CLIENT)
public class RenderBlockEntity<T extends BlockEntityBase> extends EntityRenderer<T>
{
    private static BakedModel crate_model;

    public RenderBlockEntity(final Context manager)
    {
        super(manager);
    }

    @Override
    public void render(final T entity, final float entityYaw, final float partialTicks, final PoseStack mat,
            final MultiBufferSource bufferIn, final int packedLightIn)
    {
        if (entity.getBlocks() == null || entity.getTiles() == null) return;
        try
        {
            mat.pushPose();

            final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            var v = entity.getV();
            if (v.y() > 0) mat.translate(v.x(), v.y(), v.z());

            final int xMin = Mth.floor(entity.getMin().getX());
            final int xMax = Mth.floor(entity.getMax().getX());
            final int zMin = Mth.floor(entity.getMin().getZ());
            final int zMax = Mth.floor(entity.getMax().getZ());
            final int yMin = Mth.floor(entity.getMin().getY());
            final int yMax = Mth.floor(entity.getMax().getY());

            mat.translate(xMin, 0, zMin);

            mat.mulPose(Axis.YN.rotationDegrees(180.0F));
            mat.mulPose(Axis.ZP.rotationDegrees(180.0F));
            mat.mulPose(Axis.XP.rotationDegrees(180.0F));
            if (entity instanceof IMultiplePassengerEntity)
            {
//                final float yaw = -(multi.getPrevYaw() + (multi.getYaw() - multi.getPrevYaw()) * partialTicks);
//                final float pitch = -(multi.getPrevPitch() + (multi.getPitch() - multi.getPrevPitch()) * partialTicks);
                // TODO: Fix this
                // mat.mulPose(new Quaternionf(0, yaw, pitch, true));
            }
            VertexConsumer fakeBuilder = null;
            if (!entity.isReal())
            {
                fakeBuilder = bufferIn.getBuffer(TRANSLUCENT);
            }
            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        pos.set(i - xMin, j - yMin, k - zMin);
                        if (!entity.shouldHide(pos))
                        {
                            mat.pushPose();
                            mat.translate(pos.getX(), pos.getY(), pos.getZ());
                            this.drawTileAt(pos, entity, partialTicks, mat, bufferIn, packedLightIn);
                            this.drawBlockAt(pos, entity, mat, bufferIn, fakeBuilder);
                            mat.popPose();
                        }
                        else this.drawCrateAt(pos, entity, mat, bufferIn, packedLightIn);
                    }
            mat.popPose();

        }
        catch (final Exception ignored)
        {
            // NO-OP as otherwise lags render thread
        }
    }

    private void drawBlockAt(final BlockPos pos, final T entity, final PoseStack mat,
            final MultiBufferSource bufferIn, VertexConsumer fakeBuilder)
    {
        BlockState state = entity.getBlocks()[pos.getX()][pos.getY()][pos.getZ()];
        final BlockPos mobPos = entity.getMin();
        final BlockPos realpos = pos.offset(mobPos).offset(entity.blockPosition());
        if (state == null) state = Blocks.AIR.defaultBlockState();
        if (!state.is(Blocks.AIR) || !state.is(Blocks.CAVE_AIR))
        {
            this.renderBakedBlockModel(entity, state, realpos, mat, bufferIn, fakeBuilder);
        }
    }

    private void drawCrateAt(final BlockPos.MutableBlockPos pos, final IBlockEntity blockEntity, final PoseStack mat,
            final MultiBufferSource bufferIn, final int packedLightIn)
    {
        mat.pushPose();
        // TODO: Fix this
        // mat.mulPose(new Quaternionf(-180, 90, 0, true));
        mat.translate(0.5F, 0.5F, 0.5F);
        final float f7 = 1.0F;
        mat.scale(-f7, -f7, f7);

        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        this.getCrateModel();
        mat.popPose();
    }

    private void drawTileAt(final BlockPos pos, final IBlockEntity entity, final float partialTicks,
            final PoseStack mat, final MultiBufferSource bufferIn, final int packedLightIn)
    {
        final BlockEntity tile = entity.getTiles()[pos.getX()][pos.getY()][pos.getZ()];
        if (tile != null)
            Minecraft.getInstance().getBlockEntityRenderDispatcher().render(tile, packedLightIn, mat, bufferIn);
    }

    private BakedModel getCrateModel()
    {
        if (RenderBlockEntity.crate_model == null)
        {
            // TODO: FIXME actually load a real model here!
            final ModelResourceLocation loc = new ModelResourceLocation(ResourceLocation.parse("thutcore:craft_crate"), "thutcore:craft_crate");
            RenderBlockEntity.crate_model = Minecraft.getInstance().getModelManager().getModel(loc);
        }
        return RenderBlockEntity.crate_model;
    }

    @Override
    public ResourceLocation getTextureLocation(final T entity)
    {
        return InventoryMenu.BLOCK_ATLAS;
    }

    public static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
            "fake_block_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                        GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                );
                RenderSystem.setShaderColor(1f, 1f, 1f, 0.5f);
            },
            () -> {
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            }
    );

    public static final RenderType TRANSLUCENT = RenderType.create("fake_entity", DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, 786432, true, true,
            RenderType.CompositeState.builder()
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setShaderState(RenderType.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(RenderType.BLOCK_SHEET_MIPPED)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderType.TRANSLUCENT_TARGET)
                    .createCompositeState(true));

    private void renderBakedBlockModel(T entity, final BlockState state,
            final BlockPos real_pos, final PoseStack mat, final MultiBufferSource bufferIn, final VertexConsumer fakeBuilder)
    {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        var model = dispatcher.getBlockModel(state);
        if (!entity.isReal())
        {
            dispatcher.getModelRenderer().tesselateBlock(entity.getFakeWorld(), model, state, real_pos, mat,
                    fakeBuilder, true, RandomSource.create(), state.getSeed(real_pos),
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, TRANSLUCENT);
            return;
        }

        for (var renderType : model.getRenderTypes(state, RandomSource.create(state.getSeed(real_pos)), ModelData.EMPTY))
        {
            dispatcher.getModelRenderer().tesselateBlock(entity.getFakeWorld(), model, state, real_pos, mat,
                    bufferIn.getBuffer(renderType), true, RandomSource.create(), state.getSeed(real_pos),
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
        }
    }
}
