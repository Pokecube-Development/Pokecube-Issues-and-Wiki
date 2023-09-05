package pokecube.core.client.gui.animation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.client.gui.AnimationGui;
import pokecube.core.database.Database;
import pokecube.core.network.packets.PacketPokedex;
import thut.api.maths.vecmath.Vec3f;
import thut.api.util.JsonUtil;
import thut.lib.TComponent;

public class IconModule extends AnimModule
{
    private static final Set<PokedexEntry> borked = Sets.newHashSet();
    private static final Map<PokedexEntry, Vec3f> original_sizes = Maps.newHashMap();
    private static int tries = 0;

    boolean cap = false;
    boolean took = false;
    long transitTime = 0;

    Set<PokedexEntry> doneEntries = Sets.newHashSet();
    Set<ResourceLocation> doneLocs = Sets.newHashSet();

    public static void printSizes()
    {
        final Map<String, Float> sizeMap = Maps.newHashMap();
        for (final PokedexEntry e : AnimationGui.sizes.keySet())
            sizeMap.put(e.getTrimmedName(), Float.valueOf(AnimationGui.sizes.getOrDefault(e, 0f)));

        try
        {
            final JsonObject main = new JsonObject();
            final List<String> entries = Lists.newArrayList(sizeMap.keySet());
            Collections.sort(entries);
            entries.forEach(e -> main.add(e, new JsonPrimitive(sizeMap.get(e))));
            final String json = JsonUtil.gson.toJson(main);
            final File dir = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("sizes.json").toFile();
            FileOutputStream outS = new FileOutputStream(dir);
            outS.write(json.getBytes());
            outS.close();
        }
        catch (final IOException e1)
        {
            e1.printStackTrace();
        }
    }

    public IconModule(AnimationGui parent)
    {
        super(parent);
    }

    @Override
    public void init()
    {
        final int yOffset = parent.height / 2;
        int dy = -120;
        final Component icons = TComponent.literal("Icons");

        final Button iconBtn = this.addRenderableWidget(new Button.Builder(icons, (b) -> {
            this.doneLocs.clear();
            parent.entries.clear();
            parent.entryIndex = 0;
            this.cap = !this.cap;
            b.setFGColor(this.cap ? 0xFF00FF00 : 0xFFFF0000);
        }).bounds(0, yOffset + dy, 40, 20).build());

        iconBtn.setFGColor(0xFFFF0000);

        dy += 230;
        this.addRenderableWidget(new Button.Builder(TComponent.literal("WRTSIZE"), (b) -> {
            IconModule.printSizes();
        }).bounds(0, yOffset + dy, 40, 10).build());

        this.setEnabled(false);
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    @Override
    public void postRender()
    {
        parent.xRenderAngle = 35;
        parent.yRenderAngle = 5;
        parent.yHeadRenderAngle = -5;
        parent.xHeadRenderAngle = -15;
        
        boolean debug = false;
        if (this.cap)
        {
            parent.scale = 1;
            if (this.transitTime > System.currentTimeMillis()) return;
            if (this.took)
            {
                this.cylceUp();
                this.took = false;
                int time = 50;
                if (debug) time = 5;
                this.transitTime = System.currentTimeMillis() + time;
            }
            else
            {
                try
                {
                    this.took = debug || this.capture(parent.sexe != IPokemob.FEMALE,
                            IconModule.borked.contains(AnimationGui.entry));
                    IconModule.tries = 0;
                }
                catch (final Exception e)
                {
                    final Vec3f dims = AnimationGui.entry.getModelSize();
                    if (IconModule.borked.add(AnimationGui.entry))
                    {
                        if (IconModule.original_sizes.containsKey(AnimationGui.entry))
                            dims.set(IconModule.original_sizes.get(AnimationGui.entry));
                        else dims.set(0.1f, 0.1f, 0.1f);
                    }
                    else
                    {
                        dims.y *= 2;
                        dims.z = dims.y;
                        dims.x = dims.y;
                    }
                    PokecubeAPI.LOGGER.error("borked: {}", AnimationGui.entry);
                    IconModule.tries++;
                    if (IconModule.tries > 20)
                    {
                        this.took = true;
                        PokecubeAPI.LOGGER.error("Skipping image for {}", AnimationGui.entry);
                    }
                }
                this.transitTime = System.currentTimeMillis() + (this.took ? 0 : debug ? 1 : 10);
            }
        }
    }

    public boolean updateOnButtonPress(int code)
    {
        if (code == GLFW.GLFW_KEY_RIGHT) if (!Screen.hasShiftDown()) this.cylceUp();
        else
        {
            final PokedexEntry num = Pokedex.getInstance().getNext(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
            AnimationGui.mob = AnimationGui.entry.getForGender(parent.sexe).getName();
            parent.forme.setValue(AnimationGui.mob);
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
            parent.holder = AnimationGui.entry.getModel(parent.sexe);
            parent.forme_alt.setValue(parent.holder == null ? "" : parent.holder.key.toString());
            return true;
        }
        if (code == GLFW.GLFW_KEY_LEFT)
        {
            final PokedexEntry num = Pokedex.getInstance().getPrevious(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getLastEntry();
            AnimationGui.mob = AnimationGui.entry.getForGender(parent.sexe).getName();
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
            parent.forme.setValue(AnimationGui.mob);
            parent.holder = AnimationGui.entry.getModel(parent.sexe);
            parent.forme_alt.setValue(parent.holder == null ? "" : parent.holder.key.toString());
            return true;
        }
        return false;
    }

    private boolean capture(final boolean male, final boolean slowly)
    {
        final Window window = Minecraft.getInstance().getWindow();
        final int h = window.getScreenHeight();
        final int w = window.getScreenWidth();

        final double scale = window.getGuiScale();
        int x;
        int y;

        // The 140 is for 40 pixels for buttons, and 100 pixels for text boxes
        // then -10 for some padding, related to the 5 + for x and y below
        int width = (int) (w - scale * 140);
        int height = width;
        if (height > h) height = width = h;

        x = w / 2 - width / 2;
        y = h / 2 - height / 2;

        ResourceLocation icon1 = AnimationGui.entry.getIcon(male, parent.shiny);
        if (parent.holder != null) icon1 = parent.holder.getIcon(male, parent.shiny, AnimationGui.entry);

        // Already captured for this icon.
        if (this.doneLocs.contains(icon1)) return true;

        final File outFile = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("img")
                .resolve(parent.shiny ? "shiny" : "normal").resolve(icon1.getNamespace()).resolve(icon1.getPath())
                .toFile();
        outFile.getParentFile().mkdirs();
        File outFile2 = null;
        if (parent.shiny && !AnimationGui.entry.hasShiny)
        {
            ResourceLocation icon = AnimationGui.entry.getIcon(male, false);
            if (parent.holder != null) icon = parent.holder.getIcon(male, false, AnimationGui.entry);
            outFile2 = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("img")
                    .resolve(parent.shiny ? "shiny" : "normal").resolve(icon.getNamespace()).resolve(icon.getPath())
                    .toFile();
        }

        GL11.glPixelStorei(3333, 1);
        GL11.glPixelStorei(3317, 1);
        final ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        int x0 = width, y0 = height, xf = 0, yf = 0;
        for (int i = 0; i < width; i++) for (int j = 0; j < height; j++)
        {
            final int k = (i + width * j) * 4;
            final int r = buffer.get(k) & 0xFF;
            final int g = buffer.get(k + 1) & 0xFF;
            final int b = buffer.get(k + 2) & 0xFF;
            if (!(r == 18 && g == 19 && b == 20))
            {
                x0 = Math.min(i, x0);
                xf = Math.max(i, xf);
                y0 = Math.min(j, y0);
                yf = Math.max(j, yf);
            }
        }
        int dy = yf - y0;
        int dx = xf - x0;
        final int dr = Math.max(dx, dy);
        if (dx > dy) y0 -= (dx - dy) / 2;
        if (dx < dy) x0 -= (dy - dx) / 2;
        dx = dr;
        dy = dr;
        final int ow = width;
        width = dx + 1;
        height = dy + 1;
        if (width < 0 || height < 0) return true;

        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int maxX = 0, minX = width, maxY = 0, minY = height;

        for (int i = x0; i < x0 + width; i++) for (int j = y0; j < y0 + height; j++)
        {
            final int k = (i + ow * j) * 4;
            final int r = buffer.get(k) & 0xFF;
            final int g = buffer.get(k + 1) & 0xFF;
            final int b = buffer.get(k + 2) & 0xFF;
            int a = 0xFF;
            if (r == 18 && g == 19 && b == 20) a = 0;

            if (a != 0)
            {
                minX = Math.min(minX, i);
                maxX = Math.max(maxX, i);
                minY = Math.min(minY, j);
                maxY = Math.max(maxY, j);
            }
            x = i - x0;
            y = height - (j - y0 + 1);
            image.setRGB(x, y, a << 24 | r << 16 | g << 8 | b);
        }

        dx = maxX - minX;
        dy = maxY - minY;

        boolean scaled = false;
        if (dx <= 0 || dy <= 0) PokecubeAPI.LOGGER.error("Error with " + AnimationGui.entry);
        else
        {
            final float target = ow / 3f;
            final float big = 1.05f;
            final float sml = 0.95f;
            float s = width / target;
            final Vec3f dims = AnimationGui.entry.getModelSize();
            if (!IconModule.original_sizes.containsKey(AnimationGui.entry))
                IconModule.original_sizes.put(AnimationGui.entry, new Vec3f(dims));
            if (s > big)
            {
                if (slowly) s = 1.005f;
                dims.y *= s;
                dims.z = dims.y;
                dims.x = dims.y;
                scaled = true;
            }
            else if (s < sml)
            {
                if (slowly) s = 0.995f;
                dims.y *= s;
                dims.z = dims.y;
                dims.x = dims.y;
                scaled = true;
            }
            AnimationGui.sizes.put(AnimationGui.entry, dims.y);
        }

        try
        {
            if (!scaled)
            {
                ImageIO.write(image, "png", outFile);
                if (outFile2 != null) ImageIO.write(image, "png", outFile2);
                this.doneLocs.add(icon1);
            }
            return !scaled;
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return false;

    }

    public void cylceUp()
    {
        boolean next = false;

        if (parent.genders[0] && parent.genders[1])
        {
            parent.genders[0] = false;
            parent.genders[1] = false;
            next = true;
        }
        if (!next)
        {
            final boolean didMale = parent.genders[0];
            final boolean didFemale = parent.genders[1];
            if (!didMale)
            {
                parent.sexe = IPokemob.MALE;
                parent.genders[0] = true;
            }
            else if (!didFemale)
            {
                parent.sexe = IPokemob.FEMALE;
                parent.genders[1] = true;
            }
            parent.forme_alt.setValue(parent.holder == null ? "" : parent.holder.key.toString());
            parent.holder = AnimationGui.entry.getModel(parent.sexe);
            AnimationGui.mob = AnimationGui.entry.getForGender(parent.sexe).getName();
            parent.forme.setValue(AnimationGui.mob);
            parent.onUpdated();
            return;
        }

        parent.formes = Database.customModels.getOrDefault(AnimationGui.entry, Collections.emptyList());
        parent.entries = Lists.newArrayList(Database.getFormes(AnimationGui.entry));
        if (AnimationGui.entry.getBaseForme() != null && !parent.entries.contains(AnimationGui.entry.getBaseForme()))
        {
            parent.entries.add(AnimationGui.entry.getBaseForme());
            Collections.sort(parent.entries, Database.COMPARATOR);
        }
        if (parent.entryIndex >= parent.entries.size())
        {
            parent.entryIndex = 0;
            parent.formIndex = -1;
            final PokedexEntry num = Pokedex.getInstance().getNext(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else
            {
                AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
                parent.shiny = !parent.shiny;
            }
            parent.holder = AnimationGui.entry.getModel(parent.sexe);
        }
        else if (!parent.formes.isEmpty() && parent.formIndex++ < parent.formes.size() - 1)
        {
            parent.holder = parent.formes.get(parent.formIndex);
            ResourceLocation icon1 = AnimationGui.entry.getIcon(parent.sexe == IPokemob.MALE, parent.shiny);
            if (parent.holder != null)
                icon1 = parent.holder.getIcon(parent.sexe == IPokemob.MALE, parent.shiny, AnimationGui.entry);
            while (this.doneLocs.contains(icon1) && parent.formIndex++ < parent.formes.size() - 1)
            {
                parent.holder = parent.formes.get(parent.formIndex);
                icon1 = AnimationGui.entry.getIcon(parent.sexe == IPokemob.MALE, parent.shiny);
                if (parent.holder != null)
                    icon1 = parent.holder.getIcon(parent.sexe == IPokemob.MALE, parent.shiny, AnimationGui.entry);
            }
        }
        else if (parent.entries.size() > 0)
        {
            parent.formIndex = -1;
            AnimationGui.entry = parent.entries.get(parent.entryIndex++ % parent.entries.size());
            parent.holder = AnimationGui.entry.getModel(parent.sexe);
            ResourceLocation icon1 = AnimationGui.entry.getIcon(parent.sexe == IPokemob.MALE, parent.shiny);
            if (parent.holder != null)
                icon1 = parent.holder.getIcon(parent.sexe == IPokemob.MALE, parent.shiny, AnimationGui.entry);
            // Already captured for this icon.
            while (this.doneLocs.contains(icon1) && parent.entryIndex < parent.entries.size())
            {
                AnimationGui.entry = parent.entries.get(parent.entryIndex++ % parent.entries.size());
                parent.holder = AnimationGui.entry.getModel(parent.sexe);
                icon1 = AnimationGui.entry.getIcon(parent.sexe == IPokemob.MALE, parent.shiny);
                if (parent.holder != null)
                    icon1 = parent.holder.getIcon(parent.sexe == IPokemob.MALE, parent.shiny, AnimationGui.entry);
            }
        }

        parent.forme_alt.setValue(parent.holder == null ? "" : parent.holder.key.toString());
        AnimationGui.mob = AnimationGui.entry.getForGender(parent.sexe).getName();
        parent.forme.setValue(AnimationGui.mob);
        parent.onUpdated();
    }
}
