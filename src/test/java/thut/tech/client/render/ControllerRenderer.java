package thut.tech.client.render;

import java.awt.Color;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderStateShard.WriteMaskStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import thut.api.entity.blockentity.world.IBlockEntityWorld;
import thut.tech.common.TechCore;
import thut.tech.common.blocks.lift.ControllerTile;
import thut.tech.common.entity.EntityLift;

public class ControllerRenderer implements BlockEntityRenderer<ControllerTile>
{

    private static final ResourceLocation overlay   = new ResourceLocation("thuttech:textures/blocks/overlay.png");
    private static final ResourceLocation overlay_1 = new ResourceLocation("thuttech:textures/blocks/overlay_1.png");
    private static final ResourceLocation font      = new ResourceLocation("thuttech:textures/blocks/font.png");

    // Buttons for edit mode
    private static final ResourceLocation call   = new ResourceLocation("thuttech:textures/blocks/overlay_call.png");
    private static final ResourceLocation disp   = new ResourceLocation("thuttech:textures/blocks/overlay_display.png");
    private static final ResourceLocation exit   = new ResourceLocation("thuttech:textures/blocks/overlay_exit.png");
    private static final ResourceLocation unlink = new ResourceLocation("thuttech:textures/blocks/overlay_unlink.png");

    private static void render(final RenderType type, final PoseStack mat, final MultiBufferSource buff, final float x1,
            final float y1, final float x2, final float y2, final float r, final float g, final float b, final float a,
            final float[] uvs)
    {
        ControllerRenderer.render(type, mat, buff, x1, y1, x2, y2, r, g, b, a, uvs[0], uvs[1], uvs[2], uvs[3]);
    }

    private static void render(final RenderType type, final PoseStack mat, final MultiBufferSource buff, final float x1,
            final float y1, final float x2, final float y2, final float r, final float g, final float b, final float a,
            final float u1, final float u2, final float v1, final float v2)
    {
        final VertexConsumer buffer = buff.getBuffer(type);
        final Matrix4f o = mat.last().pose();
        buffer.vertex(o, x2, y2, 0).color(r, g, b, a).uv(u1, v1).endVertex();
        buffer.vertex(o, x2, y1, 0).color(r, g, b, a).uv(u1, v2).endVertex();
        buffer.vertex(o, x1, y1, 0).color(r, g, b, a).uv(u2, v2).endVertex();
        buffer.vertex(o, x1, y2, 0).color(r, g, b, a).uv(u2, v1).endVertex();
    }

    private static final TransparencyStateShard TRANSP = new RenderStateShard.TransparencyStateShard(
            "translucent_transparency", () ->
            {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }, () ->
            {
                RenderSystem.disableBlend();
            });

    private static final WriteMaskStateShard MASK = new RenderStateShard.WriteMaskStateShard(true, true);

    private static RenderType.CompositeState getState(final ResourceLocation texture)
    {
        return RenderType.CompositeState.builder().setShaderState(RenderStateShard.POSITION_COLOR_TEX_SHADER)
                .setTextureState(new TextureStateShard(texture, false, true)).setTransparencyState(
                        ControllerRenderer.TRANSP).setWriteMaskState(ControllerRenderer.MASK).createCompositeState(
                                false);
    }

    public static RenderType makeType(final ResourceLocation tex)
    {
        return RenderType.create(tex.toString(), DefaultVertexFormat.POSITION_COLOR_TEX, Mode.QUADS, 256, false, true,
                ControllerRenderer.getState(tex));
    }

    private static RenderType       NUMBERS   = ControllerRenderer.makeType(ControllerRenderer.font);
    private static final RenderType OVERLAY_1 = ControllerRenderer.makeType(ControllerRenderer.overlay_1);
    private static final RenderType OVERLAY   = ControllerRenderer.makeType(ControllerRenderer.overlay);

    // Edit mode buttons

    private static final RenderType CALL   = ControllerRenderer.makeType(ControllerRenderer.call);
    private static final RenderType DISP   = ControllerRenderer.makeType(ControllerRenderer.disp);
    private static final RenderType EXIT   = ControllerRenderer.makeType(ControllerRenderer.exit);
    private static final RenderType UNLINK = ControllerRenderer.makeType(ControllerRenderer.unlink);

    public ControllerRenderer(final BlockEntityRendererProvider.Context dispatcher)
    {
    }

    public void drawEditOverlay(final PoseStack mat, final MultiBufferSource buff, final ControllerTile monitor,
            final Direction side)
    {
        Color colour;
        // Call button toggle
        colour = monitor.callFaces[side.ordinal()] ? new Color(0, 255, 0, 255) : new Color(255, 0, 0, 255);
        this.drawOverLay(mat, buff, monitor, 1, colour, side, 0, 0, ControllerRenderer.CALL);

        // Floor Display toggle
        colour = monitor.floorDisplay[side.ordinal()] ? new Color(0, 255, 0, 255) : new Color(255, 0, 0, 255);
        this.drawOverLay(mat, buff, monitor, 2, colour, side, 0, 0, ControllerRenderer.DISP);

        // Clear Controller toggle
        colour = monitor.getLift() != null ? new Color(255, 255, 255, 255) : new Color(0, 0, 0, 0);
        this.drawOverLay(mat, buff, monitor, 13, colour, side, 0, 0, ControllerRenderer.UNLINK);

        // Exit edit mode
        colour = new Color(255, 255, 255, 255);
        this.drawOverLay(mat, buff, monitor, 16, colour, side, 0, 0, ControllerRenderer.EXIT);
    }

    public void drawFloorNumbers(final PoseStack mat, final MultiBufferSource buffer, final int page)
    {
        for (int floor = 1; floor <= 16; floor++)
            this.drawNumber(mat, buffer, floor + page * 16, floor);
    }

    private void drawNumber(final PoseStack mat, final MultiBufferSource buffer, final int number, final int floor)
    {
        this.drawNumber(mat, buffer, number, floor, false);
    }

    private void drawNumber(final PoseStack mat, final MultiBufferSource buffer, int number, int floor,
            final boolean wide)
    {
        mat.pushPose();
        floor--;
        final float dz = -0.006f;
        final boolean minus = number >= 64;
        if (minus) number -= 64;

        double x = (double) (3 - floor & 3) / (double) 4;
        final double y = ((double) 3 - (floor >> 2)) / 4;

        if (wide) x += -0.25;

        final int actFloor = number;
        float[] uvs = this.locationFromNumber(actFloor % 10);
        final float[] uvs1 = this.locationFromNumber(actFloor / 10);
        final float r = 0, g = 0, b = 0, a = 1f;

        if (actFloor > 8)
        {
            mat.translate(x + 0.01, y + 0.06, dz);
            float dx = minus ? -0.03f : 0;
            float dy = -0.0f;
            ControllerRenderer.render(ControllerRenderer.NUMBERS, mat, buffer, 0 + dx, 0, 0.15f + dx, 0.15f + dy, r, g,
                    b, a, uvs);
            ControllerRenderer.render(ControllerRenderer.NUMBERS, mat, buffer, 0.1f + dx, 0, 0.25f + dx, 0.15f + dy, r,
                    g, b, a, uvs1);
            if (minus)
            {
                uvs = this.locationFromNumber(-3);
                dx = 0.135f;
                dy = -0.0175f;
                ControllerRenderer.render(ControllerRenderer.NUMBERS, mat, buffer, 0 + dx, 0, 0.15f + dx, 0.15f + dy, r,
                        g, b, a, uvs);
            }
        }
        else
        {
            mat.translate(x + 0.05, y + 0.06, dz);
            ControllerRenderer.render(ControllerRenderer.NUMBERS, mat, buffer, 0, 0, 0.15f, 0.15f, r, g, b, a, uvs);

            if (minus)
            {
                uvs = this.locationFromNumber(-3);
                final float dx = 0.075f;
                ControllerRenderer.render(ControllerRenderer.NUMBERS, mat, buffer, 0 + dx, 0, 0.15f + dx, 0.15f, r, g,
                        b, a, uvs);
            }
        }
        mat.popPose();
    }

    public void drawOverLay(final PoseStack mat, final MultiBufferSource buffer, final ControllerTile monitor,
            int floor, final Color colour, final Direction side, final int order, final float shift,
            final RenderType type)
    {
        if (floor > 0 && floor < 17)
        {
            mat.pushPose();
            final float dz = -0.001f * (1 + order);
            floor -= 1;
            final double x = (double) (3 - floor & 3) / (double) 4, y = ((double) 3 - (floor >> 2)) / 4;
            final float r = colour.getRed() / 255f;
            final float g = colour.getGreen() / 255f;
            final float b = colour.getBlue() / 255f;
            final float a = colour.getAlpha() / 255f;
            mat.translate(x, y, dz);
            ControllerRenderer.render(type, mat, buffer, 0, 0, 0.25f + shift, 0.25f, r, g, b, a, 0, 1, 0, 1);
            mat.popPose();
        }
    }

    public void drawOverLay(final PoseStack mat, final MultiBufferSource buffer, final ControllerTile monitor,
            int floor, final Color colour, final Direction side, final boolean wide, final int order)
    {
        if (!wide) floor = floor - monitor.getSidePage(side) * 16;
        final RenderType type = wide ? ControllerRenderer.OVERLAY_1 : ControllerRenderer.OVERLAY;
        this.drawOverLay(mat, buffer, monitor, floor, colour, side, order, wide ? 0.25f : 0, type);
    }

    public float[] locationFromNumber(final int number)
    {
        final float[] ret = new float[4];

        final int index = 16 + number;
        int dx, dz;
        dx = index % 10;
        dz = index / 10;

        ret[0] = dx / 10f;
        ret[2] = dz / 10f;

        ret[1] = (1 + dx) / 10f;
        ret[3] = (1 + dz) / 10f;

        return ret;
    }

    @Override
    public void render(final ControllerTile tileentity, final float partialTicks, final PoseStack mat,
            final MultiBufferSource buff, final int combinedLightIn, final int combinedOverlayIn)
    {
        final ControllerTile monitor = tileentity;

        int calledFloor = 0;
        int currentFloor = 0;
        boolean hasLinker = Screen.hasShiftDown();
        hasLinker = hasLinker && (Minecraft.getInstance().player.getMainHandItem().getItem() == TechCore.LINKER.get()
                || Minecraft.getInstance().player.getOffhandItem().getItem() == TechCore.LINKER.get());

        final EntityLift lift = monitor.getLift();
        if (lift != null)
        {
            calledFloor = lift.getCalled() ? lift.getDestinationFloor() : -1;
            currentFloor = lift.getCurrentFloor();
        }

        final BlockState copied = monitor.copiedState;
        if (copied != null)
        {
            mat.pushPose();
            Level world = monitor.getLevel();
            final BlockPos pos = monitor.getBlockPos();
            BlockPos randPos = pos;
            if (world instanceof IBlockEntityWorld)
            {
                final IBlockEntityWorld w = (IBlockEntityWorld) world;
                world = w.getWorld();
                randPos = BlockPos.ZERO;
            }
            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            var model = dispatcher.getBlockModel(copied);
            for (var renderType : model.getRenderTypes(copied, RandomSource.create(copied.getSeed(randPos)),
                    net.minecraftforge.client.model.data.ModelData.EMPTY))
                dispatcher.getModelRenderer().tesselateBlock((BlockAndTintGetter) world, model, copied, pos, mat,
                        buff.getBuffer(renderType), false, RandomSource.create(), copied.getSeed(pos),
                        OverlayTexture.NO_OVERLAY, net.minecraftforge.client.model.data.ModelData.EMPTY, renderType);
            mat.popPose();
        }

        dirs:
        for (int i = 0; i < 6; i++)
        {
            final Direction dir = Direction.from3DDataValue(i);

            if (!monitor.isSideOn(dir)) continue;
            mat.pushPose();
            final float f = dir.toYRot();
            mat.translate(0.5D, 0.5D, 0.5D);
            mat.mulPose(Vector3f.YN.rotationDegrees(f + 180));
            mat.translate(-0.5D, -0.5D, -0.5D);

            int a = 64;
            if (monitor.isEditMode(dir)) this.drawEditOverlay(mat, buff, monitor, dir);
            else if (monitor.isFloorDisplay(dir))
            {
                // Draw the white background
                final Color colour = new Color(255, 255, 255, 255);
                mat.translate(-0.5, -0.095, 0);
                this.drawOverLay(mat, buff, monitor, 1, colour, dir, true, 0);

                mat.pushPose();
                mat.translate(0.4, 0.0, 0);
                this.drawNumber(mat, buff, currentFloor, 1, true);
                mat.popPose();
            }
            else if (monitor.isCallPanel(dir))
            {
                // Draw the white background
                Color colour = new Color(255, 255, 255, 255);

                mat.translate(-0.5, -0.095, 0);
                this.drawOverLay(mat, buff, monitor, 1, colour, dir, true, 0);

                // Draw highlight over the background.
                if (calledFloor == monitor.floor)
                {
                    colour = new Color(255, 255, 0, a);
                    this.drawOverLay(mat, buff, monitor, 1, colour, dir, true, 1);
                }
                else if (currentFloor == monitor.floor)
                {
                    colour = new Color(0, 128, 255, a);
                    this.drawOverLay(mat, buff, monitor, 1, colour, dir, true, 2);
                }

                mat.pushPose();
                mat.translate(0.4, 0.0, 0);
                this.drawNumber(mat, buff, monitor.floor, 1, true);
                mat.popPose();
            }
            else
            {
                final int page = monitor.getSidePage(dir);
                final int pageShift = page * 16;

                // Draw numbers on top
                if (lift == null)
                {
                    this.drawFloorNumbers(mat, buff, monitor.getSidePage(dir));
                    // Draw background slots
                    final Color colour = new Color(255, 255, 255, 255);
                    for (int j = pageShift + 1; j <= 16 + pageShift; j++)
                        this.drawOverLay(mat, buff, monitor, j, colour, dir, false, 0);
                    this.drawFloorNumbers(mat, buff, page);
                    mat.popPose();
                    continue dirs;
                }

                final Color mapped = new Color(255, 255, 255, 220);
                final Color unmapped = new Color(255, 255, 255, 64);
                for (int floor = 1; floor <= 16; floor++)
                {
                    final int realFloor = floor + pageShift;
                    final boolean hasFloor = lift.hasFloor(realFloor);
                    if (hasFloor)
                    {
                        this.drawNumber(mat, buff, realFloor, floor);
                        this.drawOverLay(mat, buff, monitor, realFloor, mapped, dir, false, 0);
                    }
                    else if (hasLinker)
                    {
                        this.drawNumber(mat, buff, realFloor, floor);
                        this.drawOverLay(mat, buff, monitor, realFloor, unmapped, dir, false, 0);
                    }
                }

                a = 128;
                Color colour = new Color(0, 255, 0, a);
                this.drawOverLay(mat, buff, monitor, monitor.floor, colour, dir, false, 0);
                colour = new Color(255, 255, 0, a);
                this.drawOverLay(mat, buff, monitor, monitor.getLift().getDestinationFloor(), colour, dir, false, 0);
                colour = new Color(0, 128, 255, a);

                this.drawOverLay(mat, buff, monitor, monitor.getLift().getCurrentFloor(), colour, dir, false, 0);

            }
            mat.popPose();
        }
    }
}
