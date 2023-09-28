package pokecube.core.client.gui.watch;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.util.WatchPage;
import thut.api.maths.Vector3;
import thut.lib.AxisAngles;
import thut.lib.TComponent;

public class SecretBaseRadarPage extends WatchPage
{
    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_meteor");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_meteor_nm");

    public static Map<RadarMode, Set<BlockPos>> radar_hits = Maps.newHashMap();

    public static enum RadarMode
    {
        SECRET_BASE("base"), METEOR("meteor", 10), SPAWN_INHIBITORS("repels");

        RadarMode(final String string)
        {
            this(string, 1);
        }

        RadarMode(final String string, final float scale)
        {
            this.key = TComponent.translatable("pokewatch.title." + string + "radar");
            this.rangeScale = scale;
            SecretBaseRadarPage.radar_hits.put(this, Sets.newHashSet());
        }

        final MutableComponent key;

        final float rangeScale;
    }

    public static void updateRadar(final CompoundTag data)
    {
        if (data.contains("_meteors_") && data.get("_meteors_") instanceof ListTag)
        {
            final ListTag list = (ListTag) data.get("_meteors_");
            pokecube.core.client.gui.watch.SecretBaseRadarPage.radar_hits.get(RadarMode.METEOR).clear();
            for (int i = 0; i < list.size(); i++)
            {
                final CompoundTag tag = list.getCompound(i);
                pokecube.core.client.gui.watch.SecretBaseRadarPage.radar_hits.get(RadarMode.METEOR)
                        .add(NbtUtils.readBlockPos(tag));
            }
        }
        if (data.contains("_bases_") && data.get("_bases_") instanceof ListTag)
        {
            final ListTag list = (ListTag) data.get("_bases_");
            pokecube.core.client.gui.watch.SecretBaseRadarPage.radar_hits.get(RadarMode.SECRET_BASE).clear();
            for (int i = 0; i < list.size(); i++)
            {
                final CompoundTag tag = list.getCompound(i);
                pokecube.core.client.gui.watch.SecretBaseRadarPage.radar_hits.get(RadarMode.SECRET_BASE)
                        .add(NbtUtils.readBlockPos(tag));
            }
        }
        if (data.contains("_repels_") && data.get("_repels_") instanceof ListTag)
        {
            final ListTag list = (ListTag) data.get("_repels_");
            pokecube.core.client.gui.watch.SecretBaseRadarPage.radar_hits.get(RadarMode.SPAWN_INHIBITORS).clear();
            for (int i = 0; i < list.size(); i++)
            {
                final CompoundTag tag = list.getCompound(i);
                pokecube.core.client.gui.watch.SecretBaseRadarPage.radar_hits.get(RadarMode.SPAWN_INHIBITORS)
                        .add(NbtUtils.readBlockPos(tag));
            }
        }
        pokecube.core.client.gui.watch.SecretBaseRadarPage.baseRange = data.getInt("R");
    }

    public static float baseRange = 64;

    private static RadarMode mode = RadarMode.SECRET_BASE;

    public SecretBaseRadarPage(final GuiPokeWatch watch)
    {
        super(TComponent.translatable(""), watch, SecretBaseRadarPage.TEX_DM, SecretBaseRadarPage.TEX_NM);
    }

    @Override
    public Component getTitle()
    {
        return SecretBaseRadarPage.mode.key;
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;
        this.addRenderableWidget(new TexButton(x + 95, y - 70, 12, 12, TComponent.literal(""),
                b -> SecretBaseRadarPage.mode = RadarMode.values()[(SecretBaseRadarPage.mode.ordinal() + 1)
                        % RadarMode.values().length]).setTex(GuiPokeWatch.getWidgetTex())
                                .setRender(new UVImgRender(200, 0, 12, 12)));
    }

    @Override
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        mat.pushPose();
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;

        mat.translate(x + 126, y + 72, 0);
        float xCoord = 0;
        float yCoord = 0;
        final float zCoord = this.getBlitOffset();
        final float maxU = 1;
        final float maxV = 1;
        final float minU = -1;
        final float minV = -1;
        float r = 0;
        float g = 1;
        final float b = 0;
        float a = 1;
        RenderSystem.disableTexture();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexbuffer = tessellator.getBuilder();
        r = 1;
        g = 0;
        final Vector3 here = new Vector3().set(this.watch.player);
        final float angle = -this.watch.player.yRot % 360 + 180;
        // GL11.glRotated(angle, 0, 0, 1);
        mat.mulPose(AxisAngles.ZP.rotationDegrees(angle));

        final Set<BlockPos> coords = SecretBaseRadarPage.radar_hits.get(SecretBaseRadarPage.mode);
        final float scale = SecretBaseRadarPage.mode.rangeScale;
        final float range = SecretBaseRadarPage.baseRange * scale;

        vertexbuffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (final BlockPos c : coords)
        {
            final Vector3 loc = new Vector3().set(c);
            mat.pushPose();
            Matrix4f matrix = mat.last().pose();
            final Vector3 v = loc.subtract(here);
            final float max = 55;
            final float hDistSq = (float) (v.x * v.x + v.z * v.z);
            final float vDist = (float) Math.abs(v.y) / scale;
            v.y = 0;
            v.norm();
            a = (64 - vDist) / 64;
            a = Math.min(a, 1);
            a = Math.max(a, 0.125f);

            float dist = (float) (max * Math.sqrt(hDistSq) / range);
            dist = Math.min(dist, max);
            v.scalarMultBy(dist);

            xCoord = (float) v.x;
            yCoord = (float) v.z;

            vertexbuffer.vertex(matrix, xCoord + minU, yCoord + maxV, zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.vertex(matrix, xCoord + maxU, yCoord + maxV, zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.vertex(matrix, xCoord + maxU, yCoord + minV, zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.vertex(matrix, xCoord + minU, yCoord + minV, zCoord).color(r, g, b, a).endVertex();
            mat.popPose();
        }
        tessellator.end();
        mat.popPose();
        GuiComponent.drawCenteredString(mat, this.font, this.getTitle().getString(), x + 128, y + 8, 0x78C850);

        super.render(mat, mouseX, mouseY, partialTicks);
    }
}
