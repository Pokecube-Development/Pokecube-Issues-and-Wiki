package thut.api.entity.blockentity.render;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import thut.api.entity.IMultiplePassengerEntity;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.IBlockEntity;

@OnlyIn(Dist.CLIENT)
public class RenderBlockEntity<T extends BlockEntityBase> extends EntityRenderer<T>
{
    private static IBakedModel crate_model;

    static final Tessellator t = new Tessellator(2097152);

    float         pitch = 0.0f;
    float         yaw   = 0.0f;
    long          time  = 0;
    boolean       up    = true;
    BufferBuilder b     = RenderBlockEntity.t.getBuffer();

    ResourceLocation texture;

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

            final BlockPos.Mutable pos = new BlockPos.Mutable();
            final IBlockEntity blockEntity = entity;

            final int xMin = MathHelper.floor(blockEntity.getMin().getX());
            final int xMax = MathHelper.floor(blockEntity.getMax().getX());
            final int zMin = MathHelper.floor(blockEntity.getMin().getZ());
            final int zMax = MathHelper.floor(blockEntity.getMax().getZ());
            final int yMin = MathHelper.floor(blockEntity.getMin().getY());
            final int yMax = MathHelper.floor(blockEntity.getMax().getY());

            final double dx = (xMax - xMin) / 2 + 0.5;
            final double dz = (zMax - zMin) / 2 + 0.5;

            mat.translate(-dx, 0, -dz);

            mat.rotate(Vector3f.YN.rotationDegrees(180.0F));
            mat.rotate(Vector3f.ZP.rotationDegrees(180.0F));
            mat.rotate(Vector3f.XP.rotationDegrees(180.0F));
            if (entity instanceof IMultiplePassengerEntity)
            {
                final IMultiplePassengerEntity multi = (IMultiplePassengerEntity) entity;
                final float yaw = -(multi.getPrevYaw() + (multi.getYaw() - multi.getPrevYaw()) * partialTicks);
                final float pitch = -(multi.getPrevPitch() + (multi.getPitch() - multi.getPrevPitch()) * partialTicks);
                mat.rotate(new Quaternion(0, yaw, pitch, true));
            }

            for (int i = xMin; i <= xMax; i++)
                for (int j = yMin; j <= yMax; j++)
                    for (int k = zMin; k <= zMax; k++)
                    {
                        pos.setPos(i - xMin, j - yMin, k - zMin);
                        if (!blockEntity.shouldHide(pos))
                        {
                            mat.push();
                            mat.translate(pos.getX(), pos.getY(), pos.getZ());
                            this.drawTileAt(pos, blockEntity, partialTicks, mat, bufferIn, packedLightIn);
                            this.drawBlockAt(pos, blockEntity, mat, bufferIn, packedLightIn);
                            mat.pop();
                        }
                        else this.drawCrateAt(pos, blockEntity, mat, bufferIn, packedLightIn);
                    }
            mat.pop();

        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    private void drawBlockAt(final BlockPos pos, final IBlockEntity entity, final MatrixStack mat,
            final IRenderTypeBuffer bufferIn, final int packedLightIn)
    {
        if (entity.getBlocks() == null) return;
        BlockState state = entity.getBlocks()[pos.getX()][pos.getY()][pos.getZ()];
        final BlockPos mobPos = entity.getMin();
        final BlockPos realpos = pos.add(mobPos).add(((Entity) entity).getPosition());
        if (state == null) state = Blocks.AIR.getDefaultState();
        World world = ((Entity) entity).getEntityWorld();
        if (state.getMaterial() != Material.AIR)
        {
            world = (World) entity.getFakeWorld();
            final BlockState actualstate = state.getBlock().getExtendedState(state, entity.getFakeWorld(), pos);
            this.renderBakedBlockModel(entity, actualstate, world, realpos, pos, mat, bufferIn, packedLightIn);
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
        if (tile != null) TileEntityRendererDispatcher.instance.renderTileEntity(tile, partialTicks, mat, bufferIn);
    }

    private IBakedModel getCrateModel()
    {
        if (RenderBlockEntity.crate_model == null)
        {
            // FIXME actually load a real model here!
            final ResourceLocation loc = new ModelResourceLocation("thutcore:craft_crate");
            RenderBlockEntity.crate_model = Minecraft.getInstance().getModelManager().getModel(loc);
        }
        return RenderBlockEntity.crate_model;
    }

    @Override
    public ResourceLocation getEntityTexture(final T entity)
    {
        return PlayerContainer.LOCATION_BLOCKS_TEXTURE;
    }

    private void renderBakedBlockModel(final IBlockEntity entity, final BlockState state, final IBlockReader world,
            final BlockPos real_pos, final BlockPos relPos, final MatrixStack mat, final IRenderTypeBuffer bufferIn,
            final int packedLightIn)
    {
        final IModelData data = Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(state)
                .getModelData((ILightReader) world, real_pos, state, EmptyModelData.INSTANCE);
        final BlockPos rpos = relPos.add(entity.getOriginalPos());
        for (final RenderType type : RenderType.getBlockRenderTypes())
            if (RenderTypeLookup.canRenderInLayer(state, type))
            {
                final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
                final IBakedModel model = blockRenderer.getModelForState(state);
                blockRenderer.getBlockModelRenderer().renderModel((ILightReader) world, model, state, real_pos, mat,
                        bufferIn.getBuffer(type), false, new Random(), state.getPositionRandom(rpos), packedLightIn,
                        data);
            }
    }
}
