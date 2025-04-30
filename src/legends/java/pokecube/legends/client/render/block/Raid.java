package pokecube.legends.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import pokecube.legends.tileentity.RaidSpawn;

import java.util.List;

public class Raid implements BlockEntityRenderer<RaidSpawn>
{
    public Raid(final BlockEntityRendererProvider.Context ignored)
    {}

    @Override
    public void render(final RaidSpawn blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {
        long i = blockEntity.getLevel().getGameTime();
        List<BeaconBlockEntity.BeaconBeamSection> list = blockEntity.getBeamSections();
        int j = 0;

        for (int k = 0; k < list.size(); k++)
        {
            BeaconBlockEntity.BeaconBeamSection beaconblockentity$beaconbeamsection = list.get(k);
            renderBeaconBeam(poseStack, bufferSource, partialTick, i, j,
                    k == list.size() - 1 ? 1024 : beaconblockentity$beaconbeamsection.getHeight(),
                    beaconblockentity$beaconbeamsection.getColor());
            j += beaconblockentity$beaconbeamsection.getHeight();
        }
    }

    private static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick,
            long gameTime, int yOffset, int height, int color)
    {
        BeaconRenderer.renderBeaconBeam(poseStack, bufferSource, BeaconRenderer.BEAM_LOCATION, partialTick, 1.0F,
                gameTime, yOffset, height, color, 0.2F, 0.25F);
    }

    @Override
    public boolean shouldRenderOffScreen(final RaidSpawn te)
    {
        return true;
    }

    @Override
    public int getViewDistance()
    {
        return 256;
    }

    @Override
    public boolean shouldRender(RaidSpawn blockEntity, Vec3 cameraPos)
    {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).multiply(1.0, 0.0, 1.0)
                .closerThan(cameraPos.multiply(1.0, 0.0, 1.0), this.getViewDistance());
    }

    @Override
    public AABB getRenderBoundingBox(RaidSpawn blockEntity)
    {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, BeaconRenderer.MAX_RENDER_Y,
                pos.getZ() + 1.0);
    }
}
