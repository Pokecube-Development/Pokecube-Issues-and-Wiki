package pokecube.core.client.gui.watch;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.util.WatchPage;
import thut.api.maths.Vector3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SecretBaseRadarPage extends WatchPage
{
    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_meteor");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_meteor_nm");

    public static Map<String, Set<BlockPos>> radar_hits = Maps.newHashMap();
    public static Map<String, RadarMode> RADAR_MODES = new ConcurrentHashMap<>();

    public static RadarMode DEFAULT;
    static
    {
        RADAR_MODES.put("_repels_", DEFAULT = new RadarMode("repels", "_repels_",1));
        RADAR_MODES.put("_meteors_", new RadarMode("meteor", "_meteors_", 10));
    }

    public static class RadarMode // TODO make this registerable, move bases to register from the gimmick
    {
        public RadarMode(final String name, final String key, final float scale)
        {
            this.name = Component.translatable("pokewatch.title." + name + "radar");
            this.key = key;
            this.rangeScale = scale;
        }

        final MutableComponent name;
        final String key;
        final float rangeScale;

        public void unpack(CompoundTag data)
        {
            var data_list = SecretBaseRadarPage.radar_hits.computeIfAbsent(key, k->new HashSet<>());
            data_list.clear();
            if (data.contains(key) && data.get(key) instanceof ListTag list)
            {
                for (int i = 0; i < list.size(); i++)
                {
                    final CompoundTag tag = list.getCompound(i);
                    SecretBaseRadarPage.radar_hits.get(key).add(NbtUtils.readBlockPos(tag, "V").get());
                }
            }
        }
    }

    public static void updateRadar(final CompoundTag data)
    {
        RADAR_MODES.forEach((key, mode)-> mode.unpack(data));
        SecretBaseRadarPage.baseRange = data.getInt("R");
    }

    public static float baseRange = 64;

    private static RadarMode mode = DEFAULT;

    public SecretBaseRadarPage(final GuiPokeWatch watch)
    {
        super(Component.literal(""), watch, SecretBaseRadarPage.TEX_DM, SecretBaseRadarPage.TEX_NM);
    }

    @Override
    public Component getTitle()
    {
        return SecretBaseRadarPage.mode.name;
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;
        this.addRenderableWidget(new TexButton.Builder(Component.literal(""),
                b -> {
                    List<String> opts = new ArrayList<>(RADAR_MODES.keySet());
                    opts.sort(null);
                    int i = opts.indexOf(mode.key);
                    if(i==-1) i = 0;
                    i = (i + 1) % opts.size();
                    SecretBaseRadarPage.mode = RADAR_MODES.get(opts.get(i));
                }).bounds(x + 136, y + 90, 17, 17)
                .setTexture(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(212, 123, 17, 17))
                .tooltip(Tooltip.create(Component.translatable("button.pokecube.pokewatch.radar.tooltip")))
                .createNarration(supplier -> Component.translatable("button.pokecube.pokewatch.radar.narrate"))
                .build());
    }

    @Override
    public void renderPage(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        graphics.pose().pushPose();
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;

        graphics.pose().translate(x + 126, y + 85, 0);
        float r, g, b = 0, a;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        final Tesselator tessellator = Tesselator.getInstance();
        final BufferBuilder vertexbuffer = tessellator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        r = 1;
        g = 0;
        final Vector3 here = new Vector3().set(this.watch.player);
        final float angle = this.watch.player.getYRot() % 360 + 180;

        final Set<BlockPos> coords = SecretBaseRadarPage.radar_hits.get(SecretBaseRadarPage.mode.key);
        final float scale = SecretBaseRadarPage.mode.rangeScale;
        final float range = SecretBaseRadarPage.baseRange * scale;

        for (final BlockPos c : coords)
        {
            final Vector3 loc = new Vector3().set(c);
            final Vector3 v = loc.subtract(here);
            v.rotateAboutLine(Vector3.secondAxis, Mth.DEG_TO_RAD * angle, loc);
            v.set(loc);
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

            int xCoord = (int) v.x;
            int yCoord = (int) v.z;
            graphics.fill(xCoord - 1, yCoord - 1, xCoord + 1, yCoord + 1, FastColor.ARGB32.colorFromFloat(a, r, g, b));
        }
        graphics.pose().popPose();
        graphics.drawCenteredString(this.font, this.getTitle().getString(), x + 128, y + 18, 0x78C850);
    }
}
