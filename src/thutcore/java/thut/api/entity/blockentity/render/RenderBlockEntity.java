package thut.api.entity.blockentity.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.IBlockEntity;

@OnlyIn(Dist.CLIENT)
public class RenderBlockEntity<T extends BlockEntityBase> extends EntityRenderer<T>
{
    private static BakedModel crate_model;

    static final Tesselator t = new Tesselator(2097152);

    float pitch = 0.0f;
    float yaw = 0.0f;
    long time = 0;
    boolean up = true;
    BufferBuilder b = RenderBlockEntity.t.getBuilder();

    ResourceLocation texture;

    public RenderBlockEntity(final Context manager)
    {
        super(manager);
    }

    @Override
    public void render(final T entity, final float entityYaw, final float partialTicks, final PoseStack mat,
            final MultiBufferSource bufferIn, final int packedLightIn)
    {
        try
        {
            mat.pushPose();

            final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            final IBlockEntity blockEntity = entity;

            var v = entity.getV();
            if (v.y() > 0) mat.translate(v.x(), v.y(), v.z());

            final int xMin = Mth.floor(blockEntity.getMin().getX());
            final int xMax = Mth.floor(blockEntity.getMax().getX());
            final int zMin = Mth.floor(blockEntity.getMin().getZ());
            final int zMax = Mth.floor(blockEntity.getMax().getZ());
            final int yMin = Mth.floor(blockEntity.getMin().getY());
            final int yMax = Mth.floor(blockEntity.getMax().getY());

            mat.translate(xMin, 0, zMin);

            mat.mulPose(Axis.YN.rotationDegrees(180.0F));
            mat.mulPose(Axis.ZP.rotationDegrees(180.0F));
            mat.mulPose(Axis.XP.rotationDegrees(180.0F));
            if (entity instanceof IMultiplePassengerEntity multi)
            {
//                final float yaw = -(multi.getPrevYaw() + (multi.getYaw() - multi.getPrevYaw()) * partialTicks);
//                final float pitch = -(multi.getPrevPitch() + (multi.getPitch() - multi.getPrevPitch()) * partialTicks);
                // TODO: Fix this
                // mat.mulPose(new Quaternionf(0, yaw, pitch, true));
            }

            for (int i = xMin; i <= xMax; i++) for (int j = yMin; j <= yMax; j++) for (int k = zMin; k <= zMax; k++)
            {
                pos.set(i - xMin, j - yMin, k - zMin);
                if (!blockEntity.shouldHide(pos))
                {
                    mat.pushPose();
                    mat.translate(pos.getX(), pos.getY(), pos.getZ());
                    this.drawTileAt(pos, blockEntity, partialTicks, mat, bufferIn, packedLightIn);
                    this.drawBlockAt(pos, blockEntity, mat, bufferIn, packedLightIn);
                    mat.popPose();
                }
                else this.drawCrateAt(pos, blockEntity, mat, bufferIn, packedLightIn);
            }
            mat.popPose();

        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private void drawBlockAt(final BlockPos pos, final IBlockEntity entity, final PoseStack mat,
            final MultiBufferSource bufferIn, final int packedLightIn)
    {
        if (entity.getBlocks() == null) return;
        BlockState state = entity.getBlocks()[pos.getX()][pos.getY()][pos.getZ()];
        final BlockPos mobPos = entity.getMin();
        final BlockPos realpos = pos.offset(mobPos).offset(((Entity) entity).blockPosition());
        if (state == null) state = Blocks.AIR.defaultBlockState();
        if (!state.is(Blocks.AIR) || !state.is(Blocks.CAVE_AIR))
        {
            final BlockState actualstate = state;// .getBlock().getStateAtViewpoint(state,
                                                 // entity.getFakeWorld(), pos);
            this.renderBakedBlockModel(entity, actualstate, entity.getFakeWorld(), realpos, pos, mat, bufferIn,
                    packedLightIn);
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
            final ResourceLocation loc = new ModelResourceLocation(new ResourceLocation("thutcore:craft_crate"),
                    "thutcore:craft_crate");
            RenderBlockEntity.crate_model = Minecraft.getInstance().getModelManager().getModel(loc);
        }
        return RenderBlockEntity.crate_model;
    }

    @Override
    public ResourceLocation getTextureLocation(final T entity)
    {
        return InventoryMenu.BLOCK_ATLAS;
    }

    private void renderBakedBlockModel(final IBlockEntity entity, final BlockState state, final BlockGetter world,
            final BlockPos real_pos, final BlockPos relPos, final PoseStack mat, final MultiBufferSource bufferIn,
            final int packedLightIn)
    {
        BlockPos rpos = real_pos;
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        var model = dispatcher.getBlockModel(state);
        for (var renderType : model.getRenderTypes(state, RandomSource.create(state.getSeed(rpos)), ModelData.EMPTY))
            dispatcher.getModelRenderer().tesselateBlock((BlockAndTintGetter) world, model, state, rpos, mat,
                    bufferIn.getBuffer(renderType), true, RandomSource.create(), state.getSeed(rpos),
                    OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
    }
}
