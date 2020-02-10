package pokecube.core.client.gui;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.mobs.RenderMobOverlays;
import pokecube.core.client.render.mobs.RenderPokemob;
import pokecube.core.client.render.mobs.RenderPokemob.Holder;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
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
    TextFieldWidget            dyeColour;

    IPokemob toRender;
    Holder   renderHolder;

    float xRenderAngle     = 0;
    float yRenderAngle     = 0;
    float yHeadRenderAngle = 0;
    float xHeadRenderAngle = 0;
    int   mouseRotateControl;
    int   prevX            = 0;
    int   prevY            = 0;
    float scale            = 1;

    int[] shift = { 0, 0 };

    boolean ground = true;
    byte    sexe   = IPokemob.NOSEXE;
    boolean shiny  = false;

    List<String> components;

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

        this.toRender = AnimationGui.getRenderMob(AnimationGui.entry);
        this.toRender.setSexe(this.sexe);
        this.toRender.setShiny(this.shiny);
        this.toRender.getEntity().onGround = this.ground;
        this.toRender.onGenesChanged();
        this.dyeColour.setText("" + this.toRender.getDyeColour());
        if (this.renderHolder != null) this.renderHolder.overrideAnim = false;
        this.renderHolder = RenderPokemob.holders.get(AnimationGui.entry);
        this.renderHolder.overrideAnim = true;
        this.renderHolder.anim = ThutCore.trim(this.anim.getText());
        PacketPokedex.updateWatchEntry(AnimationGui.entry);

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

    @Override
    public void onClose()
    {
        if (this.renderHolder != null) this.renderHolder.overrideAnim = false;
        super.onClose();
    }

    @Override
    public void render(final int unk1, final int unk2, final float unk3)
    {
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

            final float dx = 100 + this.shift[0];
            final float dy = 00 + this.shift[1];
            final float xSize = this.width / 2;
            final float ySize = this.height / 2;

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

            final float mobScale = pokemob.getSize();
            final Vector3f dims = pokemob.getPokedexEntry().getModelSize();
            final float size = Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
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
    }

    @Override
    protected void init()
    {
        super.init();
        final int yOffset = this.height / 2;
        final int xOffset = this.width / 2;

        if (GuiPokedex.pokedexEntry != null) AnimationGui.mob = GuiPokedex.pokedexEntry.getName();
        AnimationGui.entry = Database.getEntry(AnimationGui.mob);
        if (AnimationGui.entry == null) AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
        if (AnimationGui.entry != null) AnimationGui.mob = AnimationGui.entry.getName();

        this.anim = new TextFieldWidget(this.font, this.width - 101, yOffset + 43 - yOffset / 2, 100, 10, "");
        this.state_g = new TextFieldWidget(this.font, this.width - 101, yOffset - 33 - yOffset / 2, 100, 10, "");
        this.state_c = new TextFieldWidget(this.font, this.width - 101, yOffset - 13 - yOffset / 2, 100, 10, "");
        this.state_l = new TextFieldWidget(this.font, this.width - 101, yOffset + 07 - yOffset / 2, 100, 10, "");
        this.forme = new TextFieldWidget(this.font, this.width - 101, yOffset + 73 - yOffset / 2, 100, 10, "");
        this.dyeColour = new TextFieldWidget(this.font, this.width - 21, yOffset + 28 - yOffset / 2, 20, 10, "");
        this.forme.setText(AnimationGui.mob);
        this.anim.setText("idle");
        this.addButton(this.anim);
        this.addButton(this.state_g);
        this.addButton(this.state_c);
        this.addButton(this.state_l);
        this.addButton(this.forme);
        this.addButton(this.dyeColour);

        this.addButton(new Button(this.width / 2 - xOffset, yOffset, 40, 20, "next", b ->
        {
            final PokedexEntry num = Pokedex.getInstance().getNext(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
            AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
            this.forme.setText(AnimationGui.mob);
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
            this.onUpdated();
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset - 20, 40, 20, "prev", b ->
        {
            final PokedexEntry num = Pokedex.getInstance().getPrevious(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getLastEntry();
            AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
            this.forme.setText(AnimationGui.mob);
            this.onUpdated();
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset - 40, 40, 20, "ground", b ->
        {
            this.ground = !this.ground;
            b.setMessage(this.ground ? "ground" : "float");
        }));
        this.addButton(new Button(this.width / 2 - xOffset, yOffset + 80, 40, 20, "F5", b ->
        {
            RenderPokemob.reloadModel(AnimationGui.entry);
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
            this.onUpdated();
        }));
        this.addButton(new Button(this.width - 101 + 20, yOffset + 85 - yOffset / 2, 20, 20, "\u25b6", b ->
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
                        this.forme.setText(AnimationGui.mob);
                        break;
                    }
            }
            this.onUpdated();
        }));
        this.addButton(new Button(this.width - 101, yOffset + 85 - yOffset / 2, 20, 20, "\u25c0", b ->
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
                        this.forme.setText(AnimationGui.mob);
                        break;
                    }
            }
            this.onUpdated();
        }));

        this.onUpdated();
    }

    @Override
    public boolean keyPressed(final int code, final int unk1, final int unk2)
    {
        if (code == GLFW.GLFW_KEY_ENTER) this.onUpdated();
        return super.keyPressed(code, unk1, unk2);
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
