package pokecube.core.client.gui;

import java.awt.image.BufferedImage;
import java.io.File;
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
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.loading.FMLPaths;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.client.render.mobs.RenderMobOverlays;
import pokecube.core.client.render.mobs.RenderPokemob;
import pokecube.core.client.render.mobs.RenderPokemob.Holder;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.network.packets.PacketPokedex;
import thut.api.entity.IMobColourable;
import thut.api.maths.vecmath.Vector3f;
import thut.core.common.ThutCore;
import thut.core.common.handlers.PlayerDataHandler;

public class AnimationGui extends Screen
{
    private static Map<PokedexEntry, IPokemob> renderMobs = Maps.newHashMap();

    public static IPokemob getRenderMob(final PokedexEntry entry)
    {
        IPokemob ret = AnimationGui.renderMobs.get(entry);
        if (ret == null)
        {
            final MobEntity mob = PokecubeCore.createPokemob(entry, PokecubeCore.proxy.getWorld());
            ret = CapabilityPokemob.getPokemobFor(mob);
        }
        return ret;
    }

    public static IPokemob getRenderMob(final IPokemob realMob)
    {
        final IPokemob ret = AnimationGui.getRenderMob(realMob.getPokedexEntry());
        ret.read(realMob.write());
        ret.onGenesChanged();
        return ret;
    }

    static String mob = "";

    public static PokedexEntry entry;
    TextFieldWidget            anim;
    TextFieldWidget            state_g;
    TextFieldWidget            state_c;
    TextFieldWidget            state_l;
    TextFieldWidget            forme;
    TextFieldWidget            forme_alt;
    TextFieldWidget            dyeColour;

    IPokemob    toRender;
    Holder      renderHolder;
    FormeHolder holder = null;

    List<PokedexEntry> entries = Lists.newArrayList();
    List<FormeHolder>  formes  = Lists.newArrayList();

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
        AnimationGui.entry = Database.getEntry(this.forme.getText());
        if (AnimationGui.entry == null) AnimationGui.entry = Database.getEntry(AnimationGui.mob);
        AnimationGui.mob = AnimationGui.entry.getName();
        this.forme.setText(AnimationGui.mob);
        this.holder = AnimationGui.entry.getModel(this.sexe);

        if (!this.forme_alt.getText().isEmpty()) try
        {
            final ResourceLocation key = PokecubeItems.toPokecubeResource(this.forme_alt.getText());
            this.holder = Database.formeHolders.get(key);
        }
        catch (final Exception e)
        {
            this.holder = AnimationGui.entry.getModel(this.sexe);
        }
        this.forme_alt.setText(this.holder == null ? "" : this.holder.key.toString());

        this.toRender = AnimationGui.getRenderMob(AnimationGui.entry);
        this.toRender.setSexe(this.sexe);
        this.toRender.setShiny(this.shiny);
        this.toRender.getEntity().onGround = this.ground;
        this.toRender.setCustomHolder(this.holder);

        try
        {
            final int dye = Integer.parseInt(this.dyeColour.getText());
            this.toRender.setDyeColour(dye);
        }
        catch (final NumberFormatException e1)
        {
        }

        this.toRender.onGenesChanged();
        this.dyeColour.setText("" + this.toRender.getDyeColour());
        if (this.renderHolder != null) this.renderHolder.overrideAnim = false;

        this.renderHolder = RenderPokemob.holders.get(AnimationGui.entry);
        if (this.holder != null) this.renderHolder = RenderPokemob.customs.getOrDefault(this.holder.key,
                this.renderHolder);

        this.renderHolder.overrideAnim = true;
        this.renderHolder.anim = ThutCore.trim(this.anim.getText());
        PacketPokedex.updateWatchEntry(AnimationGui.entry);

        this.forme.setCursorPositionZero();
        this.forme_alt.setCursorPositionZero();

        Set<Object> states = Sets.newHashSet();
        String[] args = this.state_g.getText().split(" ");
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
        args = this.state_l.getText().split(" ");
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
        args = this.state_c.getText().split(" ");
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

    private boolean capture(final boolean slowly)
    {
        final MainWindow window = Minecraft.getInstance().mainWindow;
        final int h = window.getHeight();
        final int w = window.getWidth();

        final double scale = window.getGuiScaleFactor();
        int x;
        int y;

        // The 140 is for 40 pixels for buttons, and 100 pixels for text boxes
        // then -10 for some padding, related to the 5 + for x and y below
        int width = (int) (w - scale * 140);
        int height = width;
        if (height > h) height = width = h;

        x = w / 2 - width / 2;
        y = h / 2 - height / 2;

        final File dir = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("img").toFile();
        dir.mkdirs();
        // TODO instead go based on the mob's assumed texture?

        String name = this.holder == null ? AnimationGui.entry.getTrimmedName() : this.holder.key.getPath();

        final boolean genderDiff = AnimationGui.entry.textureDetails[1] != null || AnimationGui.entry.getModel(
                (byte) 0) != AnimationGui.entry.getModel((byte) 1);
        if (genderDiff && this.holder == null) name = name + "_" + (this.sexe == IPokemob.FEMALE ? "female" : "male");

        final File outfile = new File(dir, name + (this.shiny && AnimationGui.entry.hasShiny ? "s" : "") + ".png");

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
        }

        try
        {
            if (!scaled) ImageIO.write(image, "png", outfile);
            return !scaled;
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }
        return false;

    }

    @Override
    public void onClose()
    {
        if (this.renderHolder != null) this.renderHolder.overrideAnim = false;
        super.onClose();
    }

    @Override
    public void render(final int unk1, final int unk2, final float unk3)
    {
        if (this.bg)
        {
            GlStateManager.pushMatrix();
            GlStateManager.translated(0, 0, -900);
            AbstractGui.fill(0, 0, this.width, this.height, 0xFF121314);
            GlStateManager.popMatrix();
        }

        final int yOffset = this.height / 2;
        this.font.drawString("State-General", this.width - 101, yOffset - 42 - yOffset / 2, 0xFFFFFF);
        this.font.drawString("State-Combat", this.width - 101, yOffset - 22 - yOffset / 2, 0xFFFFFF);
        this.font.drawString("State-Logic", this.width - 101, yOffset - 02 - yOffset / 2, 0xFFFFFF);

        this.font.drawString("Animation", this.width - 101, yOffset + 30 - yOffset / 2, 0xFFFFFF);
        this.font.drawString("              Info:", this.width - 101, yOffset + 30 - yOffset / 2, 0xFFFFFF);
        this.font.drawString("Forme", this.width - 101, yOffset + 60 - yOffset / 2, 0xFFFFFF);

        super.render(unk1, unk2, unk3);

        if (this.toRender != null)
        {
            final MobEntity entity = this.toRender.getEntity();
            final Minecraft mc = this.getMinecraft();
            final IPokemob pokemob = this.toRender;
            pokemob.setSize(1);

            final float xSize = this.width / 2;
            final float ySize = this.height / 2;
            final float dx = xSize / 3 + this.shift[0];
            final float dy = 00 + this.shift[1];

            final float yaw = 0;

            final PokedexEntry pokedexEntry = pokemob.getPokedexEntry();
            final PokecubePlayerStats stats = PlayerDataHandler.getInstance().getPlayerData(Minecraft
                    .getInstance().player).getData(PokecubePlayerStats.class);
            final IMobColourable colourable = pokemob.getEntity() instanceof IMobColourable ? (IMobColourable) pokemob
                    .getEntity() : pokemob instanceof IMobColourable ? (IMobColourable) pokemob : null;
            if (colourable != null)
            {
                boolean fullColour = StatsCollector.getCaptured(pokedexEntry, Minecraft.getInstance().player) > 0
                        || StatsCollector.getHatched(pokedexEntry, Minecraft.getInstance().player) > 0
                        || mc.player.abilities.isCreativeMode;

                // Megas Inherit colouring from the base form.
                if (!fullColour && pokedexEntry.isMega) fullColour = StatsCollector.getCaptured(pokedexEntry
                        .getBaseForme(), Minecraft.getInstance().player) > 0 || StatsCollector.getHatched(pokedexEntry
                                .getBaseForme(), Minecraft.getInstance().player) > 0;

                // Set colouring accordingly.
                if (fullColour) colourable.setRGBA(255, 255, 255, 255);
                else if (stats.hasInspected(pokedexEntry)) colourable.setRGBA(127, 127, 127, 255);
                else colourable.setRGBA(15, 15, 15, 255);
            }
            // Reset some things that add special effects to rendered mobs.
            pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
            pokemob.setGeneralState(GeneralStates.EVOLVING, false);

            final Vector3f dims = pokemob.getPokedexEntry().getModelSize();
            final float size = Math.max(dims.z, Math.max(dims.y, dims.x));
            final float j = (this.width - xSize) / 2 + dx;
            final float k = (this.height - ySize) / 2 + dy;

            GL11.glPushMatrix();
            GL11.glTranslatef(j + 60, k + 100, 50F);
            final float zoom = 45F / size * this.scale;
            GL11.glScalef(zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            final float f5 = k + 75 - 50 - ySize;
            GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);

            GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-(float) Math.atan(f5 / 40F) * 20F, 1.0F, 0.0F, 0.0F);

            GL11.glRotatef(this.yRenderAngle, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-this.xRenderAngle, 0.0F, 1.0F, 0.0F);
            entity.prevRenderYawOffset = yaw;
            entity.renderYawOffset = yaw;
            entity.rotationYaw = yaw;
            entity.prevRotationYaw = entity.rotationYaw;
            entity.rotationPitch = this.yHeadRenderAngle;
            entity.rotationYawHead = this.xHeadRenderAngle;
            entity.prevRotationYawHead = entity.rotationYawHead;
            entity.prevRotationPitch = entity.rotationPitch;
            entity.ticksExisted = Minecraft.getInstance().player.ticksExisted;
            entity.limbSwing += 0.125;

            GlStateManager.enableColorMaterial();
            RenderHelper.enableStandardItemLighting();
            RenderMobOverlays.enabled = false;
            final EntityRendererManager entityrenderermanager = Minecraft.getInstance().getRenderManager();
            entityrenderermanager.setPlayerViewY(180.0F);
            entityrenderermanager.setRenderShadow(false);
            entityrenderermanager.renderEntity(entity, 0.0D, 0.0D, 0.0D, yaw, unk3, false);
            entityrenderermanager.setRenderShadow(true);
            RenderMobOverlays.enabled = true;
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.activeTexture(GLX.GL_TEXTURE1);
            GlStateManager.disableTexture();
            GlStateManager.activeTexture(GLX.GL_TEXTURE0);
            if (entity instanceof IMobColourable) ((IMobColourable) entity).setRGBA(255, 255, 255, 255);

            GL11.glPopMatrix();
        }

        if (this.cap)
        {
            this.scale = 1;
            if (this.took && this.transitTime < System.currentTimeMillis())
            {
                this.cylceUp();
                this.took = false;
                this.transitTime = System.currentTimeMillis() + 2;
            }
            else if (this.transitTime < System.currentTimeMillis())
            {
                try
                {
                    this.took = this.capture(AnimationGui.borked.contains(AnimationGui.entry));
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
                this.transitTime = System.currentTimeMillis() + (this.took ? 0 : 2);
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
        if (GuiPokedex.pokedexEntry != null) AnimationGui.mob = GuiPokedex.pokedexEntry.getName();
        AnimationGui.entry = Database.getEntry(AnimationGui.mob);
        if (AnimationGui.entry == null) AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
        if (AnimationGui.entry != null) AnimationGui.mob = AnimationGui.entry.getName();

        this.anim = new TextFieldWidget(this.font, this.width - 101, yOffset + 43 - yOffset / 2, 100, 10, "");
        this.state_g = new TextFieldWidget(this.font, this.width - 101, yOffset - 33 - yOffset / 2, 100, 10, "");
        this.state_c = new TextFieldWidget(this.font, this.width - 101, yOffset - 13 - yOffset / 2, 100, 10, "");
        this.state_l = new TextFieldWidget(this.font, this.width - 101, yOffset + 07 - yOffset / 2, 100, 10, "");
        this.forme = new TextFieldWidget(this.font, this.width - 101, yOffset + 73 - yOffset / 2, 100, 10, "");
        this.forme_alt = new TextFieldWidget(this.font, this.width - 101, yOffset + 97 - yOffset / 2, 100, 10, "");
        this.dyeColour = new TextFieldWidget(this.font, this.width - 21, yOffset + 28 - yOffset / 2, 20, 10, "");
        this.forme.setText(AnimationGui.mob);
        this.dyeColour.setText(AnimationGui.entry.defaultSpecial + "");
        this.anim.setText("idle");
        this.addButton(this.anim);
        this.addButton(this.state_g);
        this.addButton(this.state_c);
        this.addButton(this.state_l);
        this.addButton(this.forme);
        this.addButton(this.forme_alt);
        this.addButton(this.dyeColour);

        this.addButton(new Button(this.width / 2 - xOffset, yOffset, 40, 20, "next", b ->
        {
            final PokedexEntry num = Pokedex.getInstance().getNext(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
            AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
            this.forme.setText(AnimationGui.mob);
            this.holder = AnimationGui.entry.getModel(this.sexe);
            this.forme_alt.setText(this.holder == null ? "" : this.holder.key.toString());
            System.out.println(AnimationGui.entry + " " + this.holder + " " + this.sexe);
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
            this.onUpdated();
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset - 20, 40, 20, "prev", b ->
        {
            final PokedexEntry num = Pokedex.getInstance().getPrevious(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getLastEntry();
            AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
            this.forme.setText(AnimationGui.mob);
            this.holder = AnimationGui.entry.getModel(this.sexe);
            this.forme_alt.setText(this.holder == null ? "" : this.holder.key.toString());
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
            this.onUpdated();
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset - 40, 40, 20, "ground", b ->
        {
            this.ground = !this.ground;
            b.setMessage(this.ground ? "ground" : "float");
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + 80, 40, 20, "F5", b ->
        {
            AnimationGui.renderMobs.clear();
            RenderPokemob.reloadModel(AnimationGui.entry);
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + 100, 40, 20, "BG", b ->
        {
            this.bg = !this.bg;
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + 20, 40, 20, "Reset", b ->
        {
            this.xRenderAngle = 0;
            this.yRenderAngle = 0;
            this.yHeadRenderAngle = 0;
            this.xHeadRenderAngle = 0;
            this.scale = 1;
            this.shift[0] = 0;
            this.shift[1] = 0;
        }));
        this.addButton(new Button(this.width / 2 - xOffset + 20, yOffset - 60, 20, 20, "+", b ->
        {
            this.scale += Screen.hasShiftDown() ? 1 : 0.1;
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset - 60, 20, 20, "-", b ->
        {
            this.scale -= Screen.hasShiftDown() ? 1 : 0.1;
        }));
        this.addButton(new Button(this.width / 2 - xOffset + 20, yOffset - 80, 20, 20, "\u25b6", b ->
        {
            this.shift[0] += Screen.hasShiftDown() ? 10 : 1;
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset - 80, 20, 20, "\u25c0", b ->
        {
            this.shift[0] -= Screen.hasShiftDown() ? 10 : 1;
        }));
        this.addButton(new Button(this.width / 2 - xOffset + 20, yOffset - 100, 20, 20, "\u25bc", b ->
        {
            this.shift[1] += Screen.hasShiftDown() ? 10 : 1;
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset - 100, 20, 20, "\u25b2", b ->
        {
            this.shift[1] -= Screen.hasShiftDown() ? 10 : 1;
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset - 120, 40, 20, "Icons", b ->
        {
            this.cap = !this.cap;
            b.setFGColor(this.cap ? 0xFF00FF00 : 0xFFFF0000);
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + 40, 40, 20, "normal", b ->
        {
            this.shiny = !this.shiny;
            b.setMessage(this.shiny ? "shiny" : "normal");
            this.onUpdated();
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + 60, 40, 20, "sexe:M", b ->
        {
            final String[] gender = b.getMessage().split(":");
            if (gender[1].equalsIgnoreCase("f"))
            {
                this.sexe = IPokemob.MALE;
                b.setMessage("sexe:M");
            }
            else if (gender[1].equalsIgnoreCase("m"))
            {
                this.sexe = IPokemob.FEMALE;
                b.setMessage("sexe:F");
            }
            this.holder = AnimationGui.entry.getModel(this.sexe);
            this.forme_alt.setText("");
            this.onUpdated();
        }));
        this.addButton(new Button(this.width - 101 + 20, yOffset + 85 - yOffset / 2, 10, 10, "\u25b6", b ->
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
                        this.forme_alt.setText(this.holder == null ? "" : this.holder.key.toString());
                        this.forme.setText(AnimationGui.mob);
                        break;
                    }
            }
            this.onUpdated();
        }));
        this.addButton(new Button(this.width - 101, yOffset + 85 - yOffset / 2, 10, 10, "\u25c0", b ->
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
                        this.forme_alt.setText(this.holder == null ? "" : this.holder.key.toString());
                        this.forme.setText(AnimationGui.mob);
                        break;
                    }
            }
            this.onUpdated();
        }));
        this.addButton(new Button(this.width - 101 + 20, yOffset + 108 - yOffset / 2, 10, 10, "\u25b6", b ->
        {
            AnimationGui.entry = Database.getEntry(AnimationGui.mob);
            if (AnimationGui.entry != null)
            {
                final List<FormeHolder> holders = Database.customModels.get(AnimationGui.entry);
                if (holders != null) try
                {
                    final ResourceLocation key = this.forme_alt.getText().isEmpty() ? null
                            : PokecubeItems.toPokecubeResource(this.forme_alt.getText());
                    for (int i = 0; i < holders.size(); i++)
                        if (holders.get(i).key.equals(key))
                        {
                            final FormeHolder holder = i + 1 < holders.size() ? holders.get(i + 1) : holders.get(0);
                            this.forme_alt.setText(holder.key.toString());
                            break;
                        }
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error cycling forme holder!");
                    this.forme_alt.setText("");
                }
                else this.forme_alt.setText("");
            }
            this.onUpdated();
        }));
        this.addButton(new Button(this.width - 101, yOffset + 108 - yOffset / 2, 10, 10, "\u25c0", b ->
        {
            AnimationGui.entry = Database.getEntry(AnimationGui.mob);
            if (AnimationGui.entry != null)
            {
                final List<FormeHolder> holders = Database.customModels.get(AnimationGui.entry);
                if (holders != null) try
                {
                    final ResourceLocation key = this.forme_alt.getText().isEmpty() ? null
                            : PokecubeItems.toPokecubeResource(this.forme_alt.getText());
                    for (int i = 0; i < holders.size(); i++)
                        if (holders.get(i).key.equals(key))
                        {
                            final FormeHolder holder = i - 1 >= 0 ? holders.get(i - 1)
                                    : holders.get(holders.size() - 1);
                            this.forme_alt.setText(holder.key.toString());
                            break;
                        }
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error cycling forme holder!");
                    this.forme_alt.setText("");
                }
                else this.forme_alt.setText("");
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
            this.forme.setText(AnimationGui.mob);
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
            this.holder = AnimationGui.entry.getModel(this.sexe);
            this.forme_alt.setText(this.holder == null ? "" : this.holder.key.toString());
            this.onUpdated();
        }
        if (code == GLFW.GLFW_KEY_LEFT)
        {
            final PokedexEntry num = Pokedex.getInstance().getPrevious(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getLastEntry();
            AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
            this.forme.setText(AnimationGui.mob);
            this.holder = AnimationGui.entry.getModel(this.sexe);
            this.forme_alt.setText(this.holder == null ? "" : this.holder.key.toString());
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
        if (!next) if (!this.genders[0])
        {
            this.sexe = IPokemob.MALE;
            this.genders[0] = true;
        }
        else if (!this.genders[1])
        {
            this.sexe = IPokemob.FEMALE;
            this.genders[1] = true;
        }

        this.formes = Database.customModels.getOrDefault(AnimationGui.entry, Collections.emptyList());
        this.entries = Lists.newArrayList(Database.getFormes(AnimationGui.entry));
        if (AnimationGui.entry.getBaseForme() != null && !this.entries.contains(AnimationGui.entry.getBaseForme()))
        {
            this.entries.add(AnimationGui.entry.getBaseForme());
            Collections.sort(this.entries, Database.COMPARATOR);
        }
        if (!next)
        {

        }
        if (this.entryIndex >= this.entries.size())
        {
            this.entryIndex = 0;
            this.formIndex = -1;
            final PokedexEntry num = Pokedex.getInstance().getNext(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
            this.holder = AnimationGui.entry.getModel(this.sexe);
        }
        else if (!this.formes.isEmpty() && this.formIndex++ < this.formes.size() - 1) this.holder = this.formes.get(
                this.formIndex);
        else if (this.entries.size() > 0)
        {
            this.formIndex = -1;
            AnimationGui.entry = this.entries.get(this.entryIndex++ % this.entries.size());
            this.holder = AnimationGui.entry.getModel(this.sexe);
        }

        this.forme_alt.setText(this.holder == null ? "" : this.holder.key.toString());
        AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
        this.forme.setText(AnimationGui.mob);
        this.onUpdated();
    }

    @Override
    public boolean mouseDragged(final double x, final double y, final int m, final double dx, final double dy)
    {
        // left click
        if (m == 0)
        {
            this.xRenderAngle += dx;
            this.yRenderAngle += dy;
        }
        // right click
        if (m == 1)
        {
            this.xHeadRenderAngle += dx;
            this.yHeadRenderAngle += dy;
        }
        return super.mouseDragged(x, y, m, dx, dy);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

}
