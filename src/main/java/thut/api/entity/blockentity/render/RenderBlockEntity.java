package thut.api.entity.blockentity.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.IBlockEntity;

@OnlyIn(Dist.CLIENT)
public class RenderBlockEntity<T extends BlockEntityBase> extends EntityRenderer<T>
{
    private static IBakedModel crate_model;

    static final Tessellator   t     = new Tessellator(2097152);

    float                      pitch = 0.0f;
    float                      yaw   = 0.0f;
    long                       time  = 0;
    boolean                    up    = true;
    BufferBuilder              b     = RenderBlockEntity.t.getBuffer();

    ResourceLocation           texture;

    public RenderBlockEntity(final EntityRendererManager manager)
    {
        super(manager);
    }

    @Override
    public void render(final T entity, final float entityYaw, final float partialTicks, final MatrixStack mat,
            final IRenderTypeBuffer bufferIn, final int packedLightIn)
    {
        // Incase some other mod tries to render as us.
        if (!(entity instanceof IBlockEntity)) return;
        try
        {
            mat.push();
            final IBlockEntity blockEntity = entity;
            if (entity instanceof IMultiplePassengerEntity)
            {
                final IMultiplePassengerEntity multi = (IMultiplePassengerEntity) entity;
                final float yaw = -(multi.getPrevYaw() + (multi.getYaw() - multi.getPrevYaw()) * partialTicks);
                final float pitch = -(multi.getPrevPitch() + (multi.getPitch() - multi.getPrevPitch()) * partialTicks);
                mat.rotate(new Quaternion(0, yaw, pitch, true));
            }
            final BlockPos.Mutable pos = new BlockPos.Mutable();

            final int xMin = MathHelper.floor(blockEntity.getMin().getX());
            final int xMax = MathHelper.floor(blockEntity.getMax().getX());
            final int zMin = MathHelper.floor(blockEntity.getMin().getZ());
            final int zMax = MathHelper.floor(blockEntity.getMax().getZ());
            final int yMin = MathHelper.floor(blockEntity.getMin().getY());
            final int yMax = MathHelper.floor(blockEntity.getMax().getY());

            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        pos.setPos(i - xMin, j - yMin, k - zMin);
                        if (!blockEntity.shouldHide(pos))
                            this.drawBlockAt(pos, blockEntity, mat, bufferIn, packedLightIn);
                        else this.drawCrateAt(pos, blockEntity, mat, bufferIn, packedLightIn);
                    }

            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        pos.setPos(i, j, k);
                        if (!blockEntity.shouldHide(pos))
                            this.drawTileAt(pos, blockEntity, partialTicks, mat, bufferIn, packedLightIn);
                    }
            mat.pop();

        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private void drawBlockAt(BlockPos pos, final IBlockEntity entity, final MatrixStack mat,
            final IRenderTypeBuffer bufferIn, final int packedLightIn)
    {
        if (entity.getBlocks() == null) return;
        BlockState BlockState = entity.getBlocks()[pos.getX()][pos.getY()][pos.getZ()];
        final BlockPos mobPos = entity.getMin();
        pos = pos.add(mobPos);
        if (BlockState == null) BlockState = Blocks.AIR.getDefaultState();
        World world = ((Entity) entity).getEntityWorld();
        if (BlockState.getMaterial() != Material.AIR)
        {
            final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance()
                    .getBlockRendererDispatcher();
            final BlockState actualstate = BlockState.getBlock().getExtendedState(BlockState, entity.getFakeWorld(),
                    pos);
            if (actualstate.getRenderType() == BlockRenderType.MODEL)
            {
                mat.push();
                mat.translate(0.5, 0, 0.5);
                final IBakedModel model = blockrendererdispatcher.getModelForState(actualstate);

                this.renderBakedBlockModel(entity, model, actualstate, world, pos, mat, bufferIn, packedLightIn);
                mat.pop();
            }
        }
    }

    private void drawCrateAt(final BlockPos.Mutable pos, final IBlockEntity blockEntity, final MatrixStack mat,
            final IRenderTypeBuffer bufferIn, final int packedLightIn)
    {
        mat.push();
        mat.rotate(new Quaternion(-180, 90, 0, true));
        mat.translate(0.5F, 0.5F, 0.5F);
        RenderHelper.disableStandardItemLighting();
        final float f7 = 1.0F;
        mat.scale(-f7, -f7, f7);
        this.getRenderManager().textureManager.bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
        this.getCrateModel();
        RenderHelper.enableStandardItemLighting();
        mat.pop();
    }

    private void drawTileAt(final BlockPos pos, final IBlockEntity entity, final float partialTicks,
            final MatrixStack mat, final IRenderTypeBuffer bufferIn, final int packedLightIn)
    {
        final TileEntity tile = entity.getTiles()[pos.getX()][pos.getY()][pos.getZ()];
        if (tile != null)
        {
            mat.push();
            mat.rotate(new Quaternion(0, 90.0F, 0.0F, true));
            mat.push();
            mat.rotate(new Quaternion(-180, 0.0F, 0.0F, true));
            mat.translate(0.5F, 0.5F, 0.5F);
            mat.rotate(new Quaternion(0, -90.0F, 0.0F, true));
            final float f7 = 1.0F;
            mat.scale(-f7, -f7, f7);
            final boolean fast = tile.hasFastRenderer();
            if (fast)
            {
                // TODO how this works now?
                // TileEntityRendererDispatcher.instance.preDrawBatch();
                // TileEntityRendererDispatcher.instance.render(tile, 0, 0, 0,
                // partialTicks);
                // TileEntityRendererDispatcher.instance.drawBatch();
            }
            // else TileEntityRendererDispatcher.instance.render(tile, 0, 0, 0,
            // partialTicks);
            mat.pop();
            mat.pop();
        }
    }

    private IBakedModel getCrateModel()
    {
        if (RenderBlockEntity.crate_model == null)
        {
            // IModel<?> model = ModelLoaderRegistry
            // .getModelOrLogError(new ResourceLocation(ThutCore.MODID,
            // "block/craft_crate"), "derp?");
            // crate_model = model.bake(model.getDefaultState(),
            // DefaultVertexFormats.BLOCK,
            // ModelLoader.defaultTextureGetter());
        }
        return RenderBlockEntity.crate_model;
    }

    @Override
    public ResourceLocation getEntityTexture(final T entity)
    {
        return PlayerContainer.LOCATION_BLOCKS_TEXTURE;
    }

    private void renderBakedBlockModel(final IBlockEntity entity, final IBakedModel model, final BlockState state,
            final IBlockReader world, BlockPos pos, final MatrixStack mat, final IRenderTypeBuffer bufferIn,
            final int packedLightIn)
    {
        mat.translate(pos.getX() - 1, pos.getY(), pos.getZ() - 1);
        mat.rotate(Vector3f.YN.rotationDegrees(180.0F));
        mat.rotate(Vector3f.ZP.rotationDegrees(180.0F));
        mat.rotate(Vector3f.XP.rotationDegrees(180.0F));

        Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(state, mat, bufferIn, packedLightIn,
                OverlayTexture.DEFAULT_LIGHT, EmptyModelData.INSTANCE);
        mat.translate(pos.getX(), pos.getY(), pos.getZ());
        return;
    }
}
