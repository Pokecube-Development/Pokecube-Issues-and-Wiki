package pokecube.core.client.gui;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.client.render.mobs.RenderPokemob;
import pokecube.core.client.render.mobs.RenderPokemob.Holder;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.network.packets.PacketPokedex;
import thut.api.entity.IMobColourable;
import thut.core.common.ThutCore;

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

    static String              mob              = "";

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

    float                      xRenderAngle     = 0;
    float                      yRenderAngle     = 0;
    float                      yHeadRenderAngle = 0;
    float                      xHeadRenderAngle = 0;
    int                        mouseRotateControl;
    int                        prevX            = 0;
    int                        prevY            = 0;
    float                      scale            = 1;

    int[]                      shift            = { 0, 0 };

    boolean                    ground           = true;
    byte                       sexe             = IPokemob.NOSEXE;
    boolean                    shiny            = false;

    List<String>               components;

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
    public void render(final int unk1, final int unk2, final float partialTicks)
    {
        final int yOffset = this.height / 2;
        this.font.drawString("State-General", this.width - 101, yOffset - 42 - yOffset / 2, 0xFFFFFF);
        this.font.drawString("State-Combat", this.width - 101, yOffset - 22 - yOffset / 2, 0xFFFFFF);
        this.font.drawString("State-Logic", this.width - 101, yOffset - 02 - yOffset / 2, 0xFFFFFF);

        this.font.drawString("Animation", this.width - 101, yOffset + 30 - yOffset / 2, 0xFFFFFF);
        this.font.drawString("              Info:", this.width - 101, yOffset + 30 - yOffset / 2, 0xFFFFFF);
        this.font.drawString("Forme", this.width - 101, yOffset + 60 - yOffset / 2, 0xFFFFFF);

        if (this.toRender != null)
        {
            final MobEntity entity = this.toRender.getEntity();
            final IPokemob pokemob = this.toRender;
            pokemob.setSize(1);

            final float xSize = this.width / 2;
            final float ySize = this.height / 2;
            final float dx = xSize / 3 + this.shift[0];
            final float dy = 00 + this.shift[1];

            final float yaw = 0;

            final IMobColourable colourable = pokemob.getEntity() instanceof IMobColourable
                    ? (IMobColourable) pokemob.getEntity()
                    : pokemob instanceof IMobColourable ? (IMobColourable) pokemob : null;
            if (colourable != null)
            {
                colourable.setRGBA(255, 255, 255, 255);
            }
            // Reset some things that add special effects to rendered mobs.
            pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
            pokemob.setGeneralState(GeneralStates.EVOLVING, false);
            final int j = (int) ((this.width - xSize) / 2 + dx);
            final int k = (int) ((this.height - ySize) / 2 + dy);

            entity.prevRenderYawOffset = yaw;
            entity.renderYawOffset = yaw;
            entity.rotationYaw = yaw;
            entity.prevRotationYaw = entity.rotationYaw;
            entity.rotationPitch = this.yHeadRenderAngle;
            entity.rotationYawHead = -this.xHeadRenderAngle;
            entity.prevRotationYawHead = entity.rotationYawHead;
            entity.prevRotationPitch = entity.rotationPitch;
            entity.ticksExisted = Minecraft.getInstance().player.ticksExisted;
            entity.limbSwing += 0.125;
            final float zoom =  this.scale;

            GuiPokemobBase.renderMob(entity, j, k + 30, yRenderAngle, xRenderAngle + 180, yHeadRenderAngle,
                    xHeadRenderAngle, zoom);
        }
        super.render(unk1, unk2, partialTicks);
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
            this.holder = null;
            this.forme_alt.setText("");
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
            this.holder = null;
            this.forme_alt.setText("");
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
                        this.holder = null;
                        this.forme_alt.setText("");
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
                        this.holder = null;
                        this.forme_alt.setText("");
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
        if (code == GLFW.GLFW_KEY_RIGHT)
        {
            final PokedexEntry num = Pokedex.getInstance().getNext(AnimationGui.entry, 1);
            if (num != AnimationGui.entry) AnimationGui.entry = num;
            else AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
            AnimationGui.mob = AnimationGui.entry.getForGender(this.sexe).getName();
            this.forme.setText(AnimationGui.mob);
            PacketPokedex.updateWatchEntry(AnimationGui.entry);
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
            this.onUpdated();
        }
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
