package pokecube.core.client.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
import com.mojang.blaze3d.matrix.MatrixStack;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.MobEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.loading.FMLPaths;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.client.render.mobs.RenderPokemob;
import pokecube.core.client.render.mobs.RenderPokemob.Holder;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.EntityTools;
import thut.api.entity.IMobColourable;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.maths.vecmath.Vector3f;
import thut.core.common.ThutCore;
import thut.core.common.network.EntityUpdate;

public class AnimationGui extends Screen
{

    private static Map<PokedexEntry, IPokemob> renderMobs = Maps.newHashMap();

    private static Object2FloatOpenHashMap<PokedexEntry> sizes = new Object2FloatOpenHashMap<>();

    public static IPokemob getRenderMob(final PokedexEntry entry)
    {
        IPokemob ret = AnimationGui.renderMobs.get(entry);
        if (ret == null)
        {
            final MobEntity mob = PokecubeCore.createPokemob(entry, PokecubeCore.proxy.getWorld());
            ret = CapabilityPokemob.getPokemobFor(mob);
            AnimationGui.renderMobs.put(entry, ret);
        }
        return ret;
    }

    public static IPokemob getRenderMob(final IPokemob realMob)
    {
        final IPokemob ret = AnimationGui.getRenderMob(realMob.getPokedexEntry());
        if (ret == null) return realMob;
        if (ret != realMob)
        {
            EntityTools.copyEntityTransforms(ret.getEntity(), realMob.getEntity());
            final int id = ret.getEntity().getId();
            final int realId = realMob.getEntity().getId();
            if (id != realId)
            {
                // This is how we track if we need to update the mob again
                ret.getEntity().setId(realId);
                // Charm rendering cares about this, so sync that too
                ret.getEntity().setUUID(realMob.getEntity().getUUID());
                ret.read(realMob.write());
                ret.onGenesChanged();
                if (ret instanceof DefaultPokemob && realMob instanceof DefaultPokemob)
                {
                    final DefaultPokemob from = (DefaultPokemob) realMob;
                    final DefaultPokemob to = (DefaultPokemob) ret;
                    final INBT tag = GeneRegistry.GENETICS_CAP.getStorage().writeNBT(GeneRegistry.GENETICS_CAP,
                            from.genes, null);
                    GeneRegistry.GENETICS_CAP.getStorage().readNBT(GeneRegistry.GENETICS_CAP, to.genes, null, tag);
                }
                if (!realMob.getPokedexEntry().stock)
                {
                    final CompoundNBT tag = new CompoundNBT();
                    try
                    {
                        realMob.getEntity().saveWithoutId(tag);
                        EntityUpdate.readMob(ret.getEntity(), tag);
                    }
                    catch (final Exception e)
                    {
                        PokecubeCore.LOGGER.error("Error with syncing tag for " + realMob.getEntity().getType()
                                .getRegistryName());
                        e.printStackTrace();
                    }
                }

            }
        }
        return ret;
    }

    public static void printSizes()
    {
        final Map<String, Float> sizeMap = Maps.newHashMap();
        for (final PokedexEntry e : AnimationGui.sizes.keySet())
            sizeMap.put(e.getTrimmedName(), new Float(AnimationGui.sizes.getOrDefault(e, 0f)));

        try
        {
            final JsonObject main = new JsonObject();
            final List<String> entries = Lists.newArrayList(sizeMap.keySet());
            Collections.sort(entries);
            entries.forEach(e -> main.add(e, new JsonPrimitive(sizeMap.get(e))));
            final String json = PokedexEntryLoader.gson.toJson(main);
            final File dir = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("sizes.json").toFile();
            final FileWriter out = new FileWriter(dir);
            out.write(json);
            out.close();
        }
        catch (final IOException e1)
        {
            e1.printStackTrace();
        }
    }

    static String mob = "";

    public static PokedexEntry entry;

    TextFieldWidget anim;
    TextFieldWidget state_g;
    TextFieldWidget state_c;
    TextFieldWidget state_l;
    TextFieldWidget forme;
    TextFieldWidget forme_alt;
    TextFieldWidget dyeColour;
    TextFieldWidget rngValue;

    IPokemob    toRender;
    Holder      renderHolder;
    FormeHolder holder = null;

    List<PokedexEntry> entries = Lists.newArrayList();
    List<FormeHolder>  formes  = Lists.newArrayList();

    Set<PokedexEntry> doneEntries = Sets.newHashSet();

    Set<ResourceLocation> doneLocs = Sets.newHashSet();

    int entryIndex = 0;
    int formIndex  = 0;

    float xRenderAngle     = 0;
    float yRenderAngle     = 0;
    float yHeadRenderAngle = 0;
    float xHeadRenderAngle = 0;
    int   mouseRotateControl;
    int   prevX            = 0;
    int   prevY            = 0;
    float scale            = 1;
    long  transitTime      = 0;

    int[] shift = { 0, 0 };

    boolean ground = true;
    boolean bg     = false;
    byte    sexe   = IPokemob.NOSEXE;
    boolean shiny  = false;
    boolean cap    = false;
    boolean took   = false;

    boolean[] genders = { false, false };

    List<String> components;

    private static final Set<PokedexEntry>           borked         = Sets.newHashSet();
    private static final Map<PokedexEntry, Vector3f> original_sizes = Maps.newHashMap();
    private static int                               tries          = 0;

    public AnimationGui()
    {
        super(new TranslationTextComponent("pokecube.model_reloader"));
    }

    void onUpdated()
    {
        AnimationGui.entry = Database.getEntry(this.forme.getValue());
        if (AnimationGui.entry == null) AnimationGui.entry = Database.getEntry(AnimationGui.mob);
        AnimationGui.mob = AnimationGui.entry.getName();
        this.forme.setValue(AnimationGui.mob);
        this.holder = AnimationGui.entry.getModel(this.sexe);

        if (!this.forme_alt.getValue().isEmpty()) try
        {
            final ResourceLocation key = PokecubeItems.toPokecubeResource(this.forme_alt.getValue());
            this.holder = Database.formeHolders.get(key);
        }
        catch (final Exception e)
        {
            this.holder = AnimationGui.entry.getModel(this.sexe);
        }
        this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());

        this.toRender = AnimationGui.getRenderMob(AnimationGui.entry);
        this.toRender.setSexe(this.sexe);
        this.toRender.setShiny(this.shiny);
        this.toRender.getEntity().setOnGround(this.ground);
        this.toRender.setCustomHolder(this.holder);

        try
        {
            this.toRender.setRNGValue(Integer.parseInt(this.rngValue.getValue()));
        }
        catch (final NumberFormatException e2)
        {
            this.rngValue.setValue(this.toRender.getRNGValue() + "");
        }

        try
        {
            final int dye = Integer.parseInt(this.dyeColour.getValue());
            this.toRender.setDyeColour(dye);
        }
        catch (final NumberFormatException e1)
        {
        }

        this.toRender.onGenesChanged();
        this.dyeColour.setValue("" + this.toRender.getDyeColour());
        this.renderHolder = RenderPokemob.holders.get(AnimationGui.entry);
        if (this.holder != null) this.renderHolder = RenderPokemob.customs.getOrDefault(this.holder.key,
                this.renderHolder);
        this.renderHolder.init();
        PacketPokedex.updateWatchEntry(AnimationGui.entry);

        this.forme.moveCursorToStart();
        this.forme_alt.moveCursorToStart();

        Set<Object> states = Sets.newHashSet();
        String[] args = this.state_g.getValue().split(" ");
        for (final String s : args)
            try
            {
                states.add(GeneralStates.valueOf(s.toUpperCase(Locale.ENGLISH)));
            }
            catch (final Exception e)
            {

            }
        for (final GeneralStates state : GeneralStates.values())
        {
            final boolean value = states.contains(state);
            this.toRender.setGeneralState(state, value);
        }
        states = Sets.newHashSet();
        args = this.state_l.getValue().split(" ");
        for (final String s : args)
            try
            {
                states.add(LogicStates.valueOf(s.toUpperCase(Locale.ENGLISH)));
            }
            catch (final Exception e)
            {

            }
        for (final LogicStates state : LogicStates.values())
        {
            final boolean value = states.contains(state);
            this.toRender.setLogicState(state, value);
        }
        states = Sets.newHashSet();
        args = this.state_c.getValue().split(" ");
        for (final String s : args)
            try
            {
                states.add(CombatStates.valueOf(s.toUpperCase(Locale.ENGLISH)));
            }
            catch (final Exception e)
            {

            }
        for (final CombatStates state : CombatStates.values())
        {
            final boolean value = states.contains(state);
            this.toRender.setCombatState(state, value);
        }
    }

    private boolean capture(final boolean male, final boolean slowly)
    {
        final MainWindow window = Minecraft.getInstance().getWindow();
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

        ResourceLocation icon1 = AnimationGui.entry.getIcon(male, this.shiny);
        if (this.holder != null) icon1 = this.holder.getIcon(male, this.shiny, AnimationGui.entry);

        // Already captured for this icon.
        if (this.doneLocs.contains(icon1)) return true;

        final File outFile = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("img").resolve(icon1.getNamespace())
                .resolve(icon1.getPath()).toFile();
        outFile.getParentFile().mkdirs();
        File outFile2 = null;
        if (this.shiny && !AnimationGui.entry.hasShiny)
        {
            ResourceLocation icon = AnimationGui.entry.getIcon(male, false);
            if (this.holder != null) icon = this.holder.getIcon(male, false, AnimationGui.entry);
            outFile2 = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("img").resolve(icon.getNamespace()).resolve(
                    icon.getPath()).toFile();
        }

        GL11.glPixelStorei(3333, 1);
        GL11.glPixelStorei(3317, 1);
        final ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        int x0 = width, y0 = height, xf = 0, yf = 0;
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
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

        for (int i = x0; i < x0 + width; i++)
            for (int j = y0; j < y0 + height; j++)
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
        if (dx <= 0 || dy <= 0) System.out.println("Error with " + AnimationGui.entry);
        else
        {
            final float target = ow / 3f;
            final float big = 1.05f;
            final float sml = 0.95f;
            float s = width / target;
            final Vector3f dims = AnimationGui.entry.getModelSize();
            if (!AnimationGui.original_sizes.containsKey(AnimationGui.entry)) AnimationGui.original_sizes.put(
                    AnimationGui.entry, new Vector3f(dims));
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

    @Override
    public void removed()
    {
        super.removed();
    }

    @Override
    public void render(final MatrixStack mat, final int unk1, final int unk2, final float partialTicks)
    {
        if (this.bg)
        {
            mat.pushPose();
            mat.translate(0, 0, -900);
            AbstractGui.fill(mat, 0, 0, this.width, this.height, 0xFF121314);
            mat.popPose();
        }
        super.render(mat, unk1, unk2, partialTicks);

        final int yOffset = this.height / 2;
        this.font.draw(mat, "State-General", this.width - 101, yOffset - 42 - yOffset / 2, 0xFFFFFF);
        this.font.draw(mat, "State-Combat", this.width - 101, yOffset - 22 - yOffset / 2, 0xFFFFFF);
        this.font.draw(mat, "State-Logic", this.width - 101, yOffset - 02 - yOffset / 2, 0xFFFFFF);

        this.font.draw(mat, "Animation", this.width - 101, yOffset / 2 + 30, 0xFFFFFF);
        this.font.draw(mat, "              Info:", this.width - 101, yOffset / 2 + 30, 0xFFFFFF);
        this.font.draw(mat, "Forme", this.width - 101, yOffset / 2 + 60, 0xFFFFFF);

        if (this.toRender != null)
        {
            final MobEntity entity = this.toRender.getEntity();
            final IPokemob pokemob = this.toRender;
            pokemob.setSize(1);

            final float xSize = this.width / 2;
            final float dx = xSize / 3 + this.shift[0];
            final float dy = 00 + this.shift[1];

            final float yaw = 0;

            final IMobColourable colourable = pokemob.getEntity() instanceof IMobColourable ? (IMobColourable) pokemob
                    .getEntity() : pokemob instanceof IMobColourable ? (IMobColourable) pokemob : null;
            if (colourable != null) colourable.setRGBA(255, 255, 255, 255);
            // Reset some things that add special effects to rendered mobs.
            pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
            pokemob.setGeneralState(GeneralStates.EVOLVING, false);
            final int j = (int) ((this.width - xSize) / 2 + dx);
            final int k = (int) (this.height / 2 + dy);

            entity.yBodyRotO = yaw;
            entity.yBodyRot = yaw;
            entity.yRot = yaw;
            entity.yRotO = entity.yRot;
            entity.xRot = this.yHeadRenderAngle;
            entity.yHeadRot = -this.xHeadRenderAngle;
            entity.yHeadRotO = entity.yHeadRot;
            entity.xRotO = entity.xRot;
            entity.tickCount = Minecraft.getInstance().player.tickCount;
            entity.animationPosition += 0.0125;
            final float zoom = this.scale;
            if (this.renderHolder != null)
            {
                this.renderHolder.overrideAnim = true;
                this.renderHolder.anim = ThutCore.trim(this.anim.getValue());
            }
            final float l = AnimationGui.entry.getModelSize().lengthSquared();
            // Sometimes things go bad and this happens
            if (l <= 0.0001 || l > 1e10) AnimationGui.entry.getModelSize().set(1, 1, 1);
            GuiPokemobBase.autoScale = false;
            GuiPokemobBase.renderMob(entity, j, k, this.yRenderAngle, this.xRenderAngle, this.yHeadRenderAngle,
                    this.xHeadRenderAngle, zoom);
            GuiPokemobBase.autoScale = true;
            if (this.renderHolder != null) this.renderHolder.overrideAnim = false;
        }

        if (this.cap)
        {
            this.scale = 1;
            if (this.transitTime > System.currentTimeMillis()) return;
            if (this.took)
            {
                this.cylceUp();
                this.took = false;
                this.transitTime = System.currentTimeMillis() + 50;
            }
            else
            {
                try
                {
                    this.took = this.capture(this.sexe != IPokemob.FEMALE, AnimationGui.borked.contains(
                            AnimationGui.entry));
                    AnimationGui.tries = 0;
                }
                catch (final Exception e)
                {
                    final Vector3f dims = AnimationGui.entry.getModelSize();
                    if (AnimationGui.borked.add(AnimationGui.entry))
                    {
                        if (AnimationGui.original_sizes.containsKey(AnimationGui.entry)) dims.set(
                                AnimationGui.original_sizes.get(AnimationGui.entry));
                        else dims.set(0.1f, 0.1f, 0.1f);
                    }
                    else
                    {
                        dims.y *= 2;
                        dims.z = dims.y;
                        dims.x = dims.y;
                    }
                    PokecubeCore.LOGGER.error("borked: {}", AnimationGui.entry);
                    AnimationGui.tries++;
                    if (AnimationGui.tries > 20)
                    {
                        this.took = true;
                        PokecubeCore.LOGGER.error("Skipping image for {}", AnimationGui.entry);
                    }
                }
                this.transitTime = System.currentTimeMillis() + (this.took ? 0 : 10);
            }
        }
    }

    @Override
    protected void init()
    {
        super.init();
        final int yOffset = this.height / 2;
        final int xOffset = this.width / 2;
        this.sexe = IPokemob.MALE;
        this.bg = true;
        if (GuiPokedex.pokedexEntry != null) AnimationGui.mob = GuiPokedex.pokedexEntry.getName();
        AnimationGui.entry = Database.getEntry(AnimationGui.mob);
        if (AnimationGui.entry == null) AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
        if (AnimationGui.entry != null) AnimationGui.mob = AnimationGui.entry.getName();

        final ITextComponent blank = new StringTextComponent("");

        this.anim = new TextFieldWidget(this.font, this.width - 101, yOffset + 43 - yOffset / 2, 100, 10, blank);
        this.state_g = new TextFieldWidget(this.font, this.width - 101, yOffset - 33 - yOffset / 2, 100, 10, blank);
        this.state_c = new TextFieldWidget(this.font, this.width - 101, yOffset - 13 - yOffset / 2, 100, 10, blank);
        this.state_l = new TextFieldWidget(this.font, this.width - 101, yOffset + 07 - yOffset / 2, 100, 10, blank);
        this.forme = new TextFieldWidget(this.font, this.width - 101, yOffset + 73 - yOffset / 2, 100, 10, blank);
        this.forme_alt = new TextFieldWidget(this.font, this.width - 101, yOffset + 97 - yOffset / 2, 100, 10, blank);
        this.rngValue = new TextFieldWidget(this.font, this.width - 101, yOffset + 123 - yOffset / 2, 100, 10, blank);
        this.dyeColour = new TextFieldWidget(this.font, this.width - 21, yOffset + 28 - yOffset / 2, 20, 10, blank);
        this.forme.setValue(AnimationGui.mob);
        this.dyeColour.setValue(AnimationGui.entry.defaultSpecial + "");
        this.anim.setValue("idle");
        this.addButton(this.anim);
        this.addButton(this.state_g);
        this.addButton(this.state_c);
        this.addButton(this.state_l);
        this.addButton(this.forme);
        this.addButton(this.forme_alt);
        this.addButton(this.rngValue);
        this.addButton(this.dyeColour);

        final ITextComponent icons = new StringTextComponent("Icons");
        final ITextComponent up = new StringTextComponent("\u25bc");
        final ITextComponent down = new StringTextComponent("\u25b2");
        final ITextComponent right = new StringTextComponent("\u25b6");
        final ITextComponent left = new StringTextComponent("\u25c0");
        final ITextComponent next = new StringTextComponent("next");
        final ITextComponent prev = new StringTextComponent("prev");
        final ITextComponent plus = new StringTextComponent("+");
        final ITextComponent minus = new StringTextComponent("-");

        final ITextComponent reset = new StringTextComponent("reset");
        final ITextComponent f5 = new StringTextComponent("f5");
        final ITextComponent bg = new StringTextComponent("bg");

        int dy = -120;

        final Button iconBtn = this.addButton(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, icons, b ->
        {
            this.doneLocs.clear();
            this.entries.clear();
            this.entryIndex = 0;
            this.cap = !this.cap;
            b.setFGColor(this.cap ? 0xFF00FF00 : 0xFFFF0000);
        }));
        iconBtn.setFGColor(0xFFFF0000);
        dy += 20;
        this.addButton(new Button(this.width / 2 - xOffset + 20, yOffset + dy, 20, 20, up, b ->
        {
            this.shift[1] += Screen.hasShiftDown() ? 10 : 1;
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + dy, 20, 20, down, b ->
        {
            this.shift[1] -= Screen.hasShiftDown() ? 10 : 1;
        }));
        dy += 20;
        this.addButton(new Button(this.width / 2 - xOffset + 20, yOffset + dy, 20, 20, right, b ->
        {
            this.shift[0] += Screen.hasShiftDown() ? 10 : 1;
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + dy, 20, 20, left, b ->
        {
            this.shift[0] -= Screen.hasShiftDown() ? 10 : 1;
        }));
        dy += 20;
        this.addButton(new Button(this.width / 2 - xOffset + 20, yOffset + dy, 20, 20, plus, b ->
        {
            this.scale += Screen.hasShiftDown() ? 1 : 0.1;
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + dy, 20, 20, minus, b ->
        {
            this.scale -= Screen.hasShiftDown() ? 1 : 0.1;
        }));
        dy += 20;
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, prev, b ->
        {
            final PokedexEntry num = Pokedex.getInstance().getPrevious(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getLastEntry();
            AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
            this.forme.setValue(AnimationGui.mob);
            this.holder = AnimationGui.entry.getModel(this.sexe);
            this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
            this.onUpdated();
        }));
        dy += 20;
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, next, b ->
        {
            final PokedexEntry num = Pokedex.getInstance().getNext(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
            AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
            this.forme.setValue(AnimationGui.mob);
            this.holder = AnimationGui.entry.getModel(this.sexe);
            this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
            this.onUpdated();
        }));
        dy += 20;
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, reset, b ->
        {
            this.xRenderAngle = 0;
            this.yRenderAngle = 0;
            this.yHeadRenderAngle = 0;
            this.xHeadRenderAngle = 0;
            this.scale = 1;
            this.shift[0] = 0;
            this.shift[1] = 0;
        }));
        dy += 20;
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, new StringTextComponent("normal"),
                b ->
                {
                    this.shiny = !this.shiny;
                    b.setMessage(new StringTextComponent(this.shiny ? "shiny" : "normal"));
                    this.onUpdated();
                }));
        dy += 20;
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, new StringTextComponent("sexe:M"),
                b ->
                {
                    final String[] gender = b.getMessage().getString().split(":");
                    if (gender[1].equalsIgnoreCase("f"))
                    {
                        this.sexe = IPokemob.MALE;
                        b.setMessage(new StringTextComponent("sexe:M"));
                    }
                    else if (gender[1].equalsIgnoreCase("m"))
            {
                this.sexe = IPokemob.FEMALE;
                b.setMessage(new StringTextComponent("sexe:F"));
            }
                    this.holder = AnimationGui.entry.getModel(this.sexe);
                    this.forme_alt.setValue("");
                    this.onUpdated();
                }));
        dy += 20;
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, f5, b ->
        {
            AnimationGui.renderMobs.clear();
            RenderPokemob.reloadModel(AnimationGui.entry);
            this.onUpdated();
        }));
        dy += 20;
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, bg, b ->
        {
            this.bg = !this.bg;
        }));
        dy += 40;
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 10, new StringTextComponent("WRTSIZE"),
                b ->
                {
                    AnimationGui.printSizes();
                }));

        // Buttons from here down are on the right side of the screen

        this.addButton(new Button(this.width - 101 + 20, yOffset + 85 - yOffset / 2, 10, 10, right, b ->
        {
            AnimationGui.entry = Database.getEntry(AnimationGui.mob);
            if (AnimationGui.entry != null)
            {
                final List<PokedexEntry> formes = Lists.newArrayList(Database.getFormes(AnimationGui.entry));
                if (!formes.contains(AnimationGui.entry)) formes.add(AnimationGui.entry);
                Collections.sort(formes, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                for (int i = 0; i < formes.size(); i++)
                    if (formes.get(i) == AnimationGui.entry)
                    {
                        AnimationGui.entry = i + 1 < formes.size() ? formes.get(i + 1) : formes.get(0);
                        AnimationGui.mob = AnimationGui.entry.getName();
                        this.holder = AnimationGui.entry.getModel(this.sexe);
                        this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());
                        this.forme.setValue(AnimationGui.mob);
                        break;
                    }
            }
            this.onUpdated();
        }));
        this.addButton(new Button(this.width - 101, yOffset + 85 - yOffset / 2, 10, 10, left, b ->
        {
            AnimationGui.entry = Database.getEntry(AnimationGui.mob);
            if (AnimationGui.entry != null)
            {
                final List<PokedexEntry> formes = Lists.newArrayList(Database.getFormes(AnimationGui.entry));
                if (!formes.contains(AnimationGui.entry)) formes.add(AnimationGui.entry);
                Collections.sort(formes, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                for (int i = 0; i < formes.size(); i++)
                    if (formes.get(i) == AnimationGui.entry)
                    {
                        AnimationGui.entry = i - 1 >= 0 ? formes.get(i - 1) : formes.get(formes.size() - 1);
                        AnimationGui.mob = AnimationGui.entry.getName();
                        this.holder = AnimationGui.entry.getModel(this.sexe);
                        this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());
                        this.forme.setValue(AnimationGui.mob);
                        break;
                    }
            }
            this.onUpdated();
        }));
        this.addButton(new Button(this.width - 101 + 20, yOffset + 108 - yOffset / 2, 10, 10, right, b ->
        {
            AnimationGui.entry = Database.getEntry(AnimationGui.mob);
            if (AnimationGui.entry != null)
            {
                final List<FormeHolder> holders = Database.customModels.get(AnimationGui.entry);
                if (holders != null) try
                {
                    final ResourceLocation key = this.forme_alt.getValue().isEmpty() ? null
                            : PokecubeItems.toPokecubeResource(this.forme_alt.getValue());
                    for (int i = 0; i < holders.size(); i++)
                        if (key == null || holders.get(i).key.equals(key))
                        {
                            final FormeHolder holder = i + 1 < holders.size() ? holders.get(i + 1) : holders.get(0);
                            this.forme_alt.setValue(holder.key.toString());
                            break;
                        }
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error cycling forme holder!");
                    this.forme_alt.setValue("");
                }
                else this.forme_alt.setValue("");
            }
            this.onUpdated();
        }));
        this.addButton(new Button(this.width - 101, yOffset + 108 - yOffset / 2, 10, 10, left, b ->
        {
            AnimationGui.entry = Database.getEntry(AnimationGui.mob);
            if (AnimationGui.entry != null)
            {
                final List<FormeHolder> holders = Database.customModels.get(AnimationGui.entry);
                if (holders != null) try
                {
                    final ResourceLocation key = this.forme_alt.getValue().isEmpty() ? null
                            : PokecubeItems.toPokecubeResource(this.forme_alt.getValue());
                    for (int i = 0; i < holders.size(); i++)
                        if (key == null || holders.get(i).key.equals(key))
                        {
                            final FormeHolder holder = i - 1 >= 0 ? holders.get(i - 1)
                                    : holders.get(holders.size() - 1);
                            this.forme_alt.setValue(holder.key.toString());
                            break;
                        }
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error cycling forme holder!");
                    this.forme_alt.setValue("");
                }
                else this.forme_alt.setValue("");
            }
            this.onUpdated();
        }));

        this.onUpdated();
    }

    @Override
    public boolean keyPressed(final int code, final int unk1, final int unk2)
    {
        if (code == GLFW.GLFW_KEY_ENTER) this.onUpdated();
        if (code == GLFW.GLFW_KEY_RIGHT) if (!Screen.hasShiftDown()) this.cylceUp();
        else
        {
            final PokedexEntry num = Pokedex.getInstance().getNext(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
            AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
            this.forme.setValue(AnimationGui.mob);
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
            this.holder = AnimationGui.entry.getModel(this.sexe);
            this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());
            this.onUpdated();
        }
        if (code == GLFW.GLFW_KEY_LEFT)
        {
            final PokedexEntry num = Pokedex.getInstance().getPrevious(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getLastEntry();
            AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
            this.forme.setValue(AnimationGui.mob);
            this.holder = AnimationGui.entry.getModel(this.sexe);
            this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());
            this.onUpdated();
        }
        return super.keyPressed(code, unk1, unk2);
    }

    private void cylceUp()
    {
        boolean next = false;
        if (this.genders[0] && this.genders[1])
        {
            this.genders[0] = false;
            this.genders[1] = false;
            next = true;
        }
        if (!next)
        {
            final boolean didMale = this.genders[0];
            final boolean didFemale = this.genders[1];
            if (!didMale)
            {
                this.sexe = IPokemob.MALE;
                this.genders[0] = true;
            }
            else if (!didFemale)
            {
                this.sexe = IPokemob.FEMALE;
                this.genders[1] = true;
            }
            this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());
            this.holder = AnimationGui.entry.getModel(this.sexe);
            AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
            this.forme.setValue(AnimationGui.mob);
            this.onUpdated();
            return;
        }

        this.formes = Database.customModels.getOrDefault(AnimationGui.entry, Collections.emptyList());
        this.entries = Lists.newArrayList(Database.getFormes(AnimationGui.entry));
        if (AnimationGui.entry.getBaseForme() != null && !this.entries.contains(AnimationGui.entry.getBaseForme()))
        {
            this.entries.add(AnimationGui.entry.getBaseForme());
            Collections.sort(this.entries, Database.COMPARATOR);
        }
        if (this.entryIndex >= this.entries.size())
        {
            this.entryIndex = 0;
            this.formIndex = -1;
            final PokedexEntry num = Pokedex.getInstance().getNext(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else
            {
                AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
                this.shiny = !this.shiny;
            }
            this.holder = AnimationGui.entry.getModel(this.sexe);
        }
        else if (!this.formes.isEmpty() && this.formIndex++ < this.formes.size() - 1)
        {
            this.holder = this.formes.get(this.formIndex);
            ResourceLocation icon1 = AnimationGui.entry.getIcon(this.sexe == IPokemob.MALE, this.shiny);
            if (this.holder != null) icon1 = this.holder.getIcon(this.sexe == IPokemob.MALE, this.shiny,
                    AnimationGui.entry);
            while (this.doneLocs.contains(icon1) && this.formIndex++ < this.formes.size() - 1)
            {
                this.holder = this.formes.get(this.formIndex);
                icon1 = AnimationGui.entry.getIcon(this.sexe == IPokemob.MALE, this.shiny);
                if (this.holder != null) icon1 = this.holder.getIcon(this.sexe == IPokemob.MALE, this.shiny,
                        AnimationGui.entry);
            }
        }
        else if (this.entries.size() > 0)
        {
            this.formIndex = -1;
            AnimationGui.entry = this.entries.get(this.entryIndex++ % this.entries.size());
            this.holder = AnimationGui.entry.getModel(this.sexe);
            ResourceLocation icon1 = AnimationGui.entry.getIcon(this.sexe == IPokemob.MALE, this.shiny);
            if (this.holder != null) icon1 = this.holder.getIcon(this.sexe == IPokemob.MALE, this.shiny,
                    AnimationGui.entry);
            // Already captured for this icon.
            while (this.doneLocs.contains(icon1) && this.entryIndex < this.entries.size())
            {
                AnimationGui.entry = this.entries.get(this.entryIndex++ % this.entries.size());
                this.holder = AnimationGui.entry.getModel(this.sexe);
                icon1 = AnimationGui.entry.getIcon(this.sexe == IPokemob.MALE, this.shiny);
                if (this.holder != null) icon1 = this.holder.getIcon(this.sexe == IPokemob.MALE, this.shiny,
                        AnimationGui.entry);
            }
        }

        this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());
        AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
        this.forme.setValue(AnimationGui.mob);
        this.onUpdated();
    }

    @Override
    public boolean mouseDragged(final double x, final double y, final int m, final double dx, final double dy)
    {
        // left click
        if (m == 0)
        {
            this.xRenderAngle -= dx;
            this.yRenderAngle += dy;
        }
        // right click
        if (m == 1)
        {
            this.xHeadRenderAngle -= dx;
            this.yHeadRenderAngle += dy;
        }
        return super.mouseDragged(x, y, m, dx, dy);
    }

    @Override
    public boolean isPauseScreen()
    {
        return this.cap;
    }

}
