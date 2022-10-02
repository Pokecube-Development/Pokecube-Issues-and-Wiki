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
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.FormeHolder;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.client.gui.animation.AnimModule;
import pokecube.core.client.gui.animation.IconModule;
import pokecube.core.client.gui.animation.WornOffsetModule;
import pokecube.core.client.gui.helper.ListEditBox;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.client.render.mobs.RenderPokemob;
import pokecube.core.client.render.mobs.RenderPokemob.Holder;
import pokecube.core.database.Database;
import pokecube.core.impl.capabilities.DefaultPokemob;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.EntityTools;
import thut.api.entity.animation.Animation;
import thut.api.entity.animation.Animators.FunctionAnimation;
import thut.core.common.ThutCore;
import thut.core.common.network.EntityUpdate;
import thut.lib.RegHelper;
import thut.lib.TComponent;

public class AnimationGui extends Screen
{
    private static Map<PokedexEntry, IPokemob> renderMobs = Maps.newHashMap();

    public static Object2FloatOpenHashMap<PokedexEntry> sizes = new Object2FloatOpenHashMap<>();

    public static IPokemob getRenderMob(final PokedexEntry entry)
    {
        IPokemob ret = AnimationGui.renderMobs.get(entry);
        if (ret == null)
        {
            final Mob mob = PokecubeCore.createPokemob(entry, PokecubeCore.proxy.getWorld());
            ret = PokemobCaps.getPokemobFor(mob);
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
                if (ret instanceof DefaultPokemob to && realMob instanceof DefaultPokemob from)
                    to.genes.deserializeNBT(from.genes.serializeNBT());
                if (!realMob.getPokedexEntry().stock)
                {
                    final CompoundTag tag = new CompoundTag();
                    try
                    {
                        realMob.getEntity().saveWithoutId(tag);
                        EntityUpdate.readMob(ret.getEntity(), tag);
                    }
                    catch (final Exception e)
                    {
                        PokecubeAPI.LOGGER
                                .error("Error with syncing tag for " + RegHelper.getKey(realMob.getEntity().getType()));
                        e.printStackTrace();
                    }
                }
            }
        }
        if (ret != null) ret.setCopiedMob(realMob.getCopiedMob());
        return ret;
    }

    public static String mob = "";

    public static PokedexEntry entry;

    public EditBox anim;
    public EditBox state_g;
    public EditBox state_c;
    public EditBox state_l;
    public EditBox forme;
    public EditBox forme_alt;
    public EditBox dyeColour;
    public EditBox rngValue;

    public IPokemob toRender;
    public Holder renderHolder;
    public FormeHolder holder = null;

    public List<PokedexEntry> entries = Lists.newArrayList();
    public List<FormeHolder> formes = Lists.newArrayList();

    public int entryIndex = 0;
    public int formIndex = 0;

    float xRenderAngle = 0;
    float yRenderAngle = 0;
    float yHeadRenderAngle = 0;
    float xHeadRenderAngle = 0;
    int mouseRotateControl;
    int prevX = 0;
    int prevY = 0;
    public float scale = 1;

    int[] shift =
    { 0, 0 };

    public boolean ground = true;
    public boolean bg = false;
    public byte sexe = IPokemob.NOSEXE;
    public boolean shiny = false;

    public boolean[] genders =
    { false, false };

    List<AnimModule> modules = Lists.newArrayList();
    int moduleIndex = -1;

    public String testAnimation = "";

    public AnimationGui()
    {
        super(TComponent.translatable("pokecube.model_reloader"));
        modules.add(new WornOffsetModule(this));
        modules.add(new IconModule(this));
    }

    public void onUpdated()
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
        {}

        this.toRender.onGenesChanged();
        this.dyeColour.setValue("" + this.toRender.getDyeColour());
        this.renderHolder = RenderPokemob.holders.get(AnimationGui.entry);
        if (this.holder != null)
            this.renderHolder = RenderPokemob.customs.getOrDefault(this.holder.key, this.renderHolder);

        PacketPokedex.updateWatchEntry(AnimationGui.entry);

        this.forme.moveCursorToStart();
        this.forme_alt.moveCursorToStart();

        Set<Object> states = Sets.newHashSet();
        String[] args = this.state_g.getValue().split(" ");
        for (final String s : args) try
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
        for (final String s : args) try
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
        for (final String s : args) try
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
        modules.forEach(m -> {
            if (m.active) m.onUpdated();
        });
    }

    @Override
    public void render(final PoseStack mat, final int unk1, final int unk2, float partialTicks)
    {
        if (this.bg)
        {
            mat.pushPose();
            mat.translate(0, 0, -900);
            GuiComponent.fill(mat, 0, 0, this.width, this.height, 0xFF121314);
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
            final Mob entity = this.toRender.getEntity();
            final IPokemob pokemob = this.toRender;
            pokemob.setSize(1);
            pokemob.setRGBA(255, 255, 255, 255);

            modules.forEach(m -> {
                if (m.active) m.preRender();
            });

            mat.pushPose();

            final float xSize = this.width / 2;
            final float dx = xSize / 3 + this.shift[0];
            final float dy = 00 + this.shift[1];

            final float yaw = 0;

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
            entity.yHeadRot = this.xHeadRenderAngle;
            entity.yHeadRotO = entity.yHeadRot;
            entity.xRotO = entity.xRot;
            entity.tickCount = Minecraft.getInstance().player.tickCount;
            entity.animationPosition += 0.0125;

            partialTicks = minecraft.getFrameTime();
            if (this.isPauseScreen())
            {
                entity.tickCount = 0;
                entity.animationPosition = 0;
                partialTicks = 0;
            }

            final float zoom = this.scale;
            if (this.renderHolder != null)
            {
                this.renderHolder.overrideAnim = true;
                this.renderHolder.anim = ThutCore.trim(this.anim.getValue());

                if (this.testAnimation.startsWith("f::")) try
                {
                    String[] args = testAnimation.split("::");

                    String part = ThutCore.trim(args[1]);
                    String function = args[2];
                    this.renderHolder.anim = "test_anim";

                    FunctionAnimation animation;

                    if (function.startsWith("d")) animation = FunctionAnimation.makeOffsetTest(function);
                    else animation = FunctionAnimation.makeRotationTest(function);

                    Animation anim = new Animation();
                    anim.sets.put(part, animation);
                    anim.sets.put("test_anim", animation);
                    anim.name = "test_anim";

                    EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
                    Object ren = manager.getRenderer(toRender.getEntity());
                    if (ren instanceof RenderPokemob renderer)
                    {
                        renderer.getModel().preProcessAnimations(Lists.newArrayList(anim));
                        renderer.getModel().renderer.getAnimations().put("test_anim", Lists.newArrayList(anim));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            RenderSystem.setShaderLights(com.mojang.math.Vector3f.YN, com.mojang.math.Vector3f.ZN);
            final float l = AnimationGui.entry.getModelSize().lengthSquared();
            // Sometimes things go bad and this happens
            if (l <= 0.0001 || l > 1e10) AnimationGui.entry.getModelSize().set(1, 1, 1);
            GuiPokemobHelper.autoScale = false;
            GuiPokemobHelper.renderMob(mat, entity, j, k, this.yRenderAngle, this.xRenderAngle, this.yHeadRenderAngle,
                    this.xHeadRenderAngle, zoom, partialTicks);
            GuiPokemobHelper.autoScale = true;
            if (this.renderHolder != null) this.renderHolder.overrideAnim = false;
            mat.popPose();

            modules.forEach(m -> {
                if (m.active) m.postRender();
            });
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
        AnimationGui.entry = Database.getEntry(AnimationGui.mob);
        if (AnimationGui.entry == null) AnimationGui.entry = Pokedex.getInstance().getFirstEntry();
        if (AnimationGui.entry != null) AnimationGui.mob = AnimationGui.entry.getName();

        final Component blank = TComponent.literal("");

        this.anim = new ListEditBox(this.font, this.width - 101, yOffset + 43 - yOffset / 2, 100, 10, blank);
        this.state_g = new ListEditBox(this.font, this.width - 101, yOffset - 33 - yOffset / 2, 100, 10, blank);
        this.state_c = new ListEditBox(this.font, this.width - 101, yOffset - 13 - yOffset / 2, 100, 10, blank);
        this.state_l = new ListEditBox(this.font, this.width - 101, yOffset + 07 - yOffset / 2, 100, 10, blank);
        this.forme = new ListEditBox(this.font, this.width - 101, yOffset + 73 - yOffset / 2, 100, 10, blank);
        this.forme_alt = new ListEditBox(this.font, this.width - 101, yOffset + 97 - yOffset / 2, 100, 10, blank);
        this.rngValue = new ListEditBox(this.font, this.width - 101, yOffset + 123 - yOffset / 2, 100, 10, blank);
        this.dyeColour = new ListEditBox(this.font, this.width - 21, yOffset + 28 - yOffset / 2, 20, 10, blank);
        this.forme.setValue(AnimationGui.mob);
        this.dyeColour.setValue(AnimationGui.entry.defaultSpecial + "");
        this.anim.setValue("idle");
        this.anim.maxLength = 999;
        this.addRenderableWidget(this.anim);
        this.addRenderableWidget(this.state_g);
        this.addRenderableWidget(this.state_c);
        this.addRenderableWidget(this.state_l);
        this.addRenderableWidget(this.forme);
        this.addRenderableWidget(this.forme_alt);
        this.addRenderableWidget(this.rngValue);
        this.addRenderableWidget(this.dyeColour);

        for (var o : this.children)
        {
            if (o instanceof ListEditBox box && box.preFocusGain == null)
            {
                box.registerPreFocus(this);
            }
        }

        final Component up = TComponent.literal("\u25bc");
        final Component down = TComponent.literal("\u25b2");
        final Component right = TComponent.literal("\u25b6");
        final Component left = TComponent.literal("\u25c0");
        final Component next = TComponent.literal("next");
        final Component prev = TComponent.literal("prev");
        final Component plus = TComponent.literal("+");
        final Component minus = TComponent.literal("-");

        final Component reset = TComponent.literal("reset");
        final Component f5 = TComponent.literal("f5");
        final Component bg = TComponent.literal("bg");
        final Component module = TComponent.literal("module");

        int dy = -120;
        dy += 20;
        this.addRenderableWidget(new Button(this.width / 2 - xOffset + 20, yOffset + dy, 20, 20, up, b -> {
            this.shift[1] += Screen.hasShiftDown() ? 10 : 1;
        }));
        this.addRenderableWidget(new Button(this.width / 2 - xOffset, yOffset + dy, 20, 20, down, b -> {
            this.shift[1] -= Screen.hasShiftDown() ? 10 : 1;
        }));
        dy += 20;
        this.addRenderableWidget(new Button(this.width / 2 - xOffset + 20, yOffset + dy, 20, 20, right, b -> {
            this.shift[0] += Screen.hasShiftDown() ? 10 : 1;
        }));
        this.addRenderableWidget(new Button(this.width / 2 - xOffset, yOffset + dy, 20, 20, left, b -> {
            this.shift[0] -= Screen.hasShiftDown() ? 10 : 1;
        }));
        dy += 20;
        this.addRenderableWidget(new Button(this.width / 2 - xOffset + 20, yOffset + dy, 20, 20, plus, b -> {
            this.scale += Screen.hasShiftDown() ? 1 : 0.1;
        }));
        this.addRenderableWidget(new Button(this.width / 2 - xOffset, yOffset + dy, 20, 20, minus, b -> {
            this.scale -= Screen.hasShiftDown() ? 1 : 0.1;
        }));
        dy += 20;
        this.addRenderableWidget(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, prev, b -> {
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
        this.addRenderableWidget(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, next, b -> {
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
        this.addRenderableWidget(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, reset, b -> {
            this.xRenderAngle = 0;
            this.yRenderAngle = 0;
            this.yHeadRenderAngle = 0;
            this.xHeadRenderAngle = 0;
            this.scale = 1;
            this.shift[0] = 0;
            this.shift[1] = 0;
        }));
        dy += 20;
        this.addRenderableWidget(
                new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, TComponent.literal("normal"), b ->
                {
                    this.shiny = !this.shiny;
                    b.setMessage(TComponent.literal(this.shiny ? "shiny" : "normal"));
                    this.onUpdated();
                }));
        dy += 20;
        this.addRenderableWidget(
                new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, TComponent.literal("sexe:M"), b ->
                {
                    final String[] gender = b.getMessage().getString().split(":");
                    if (gender[1].equalsIgnoreCase("f"))
                    {
                        this.sexe = IPokemob.MALE;
                        b.setMessage(TComponent.literal("sexe:M"));
                    }
                    else if (gender[1].equalsIgnoreCase("m"))
            {
                this.sexe = IPokemob.FEMALE;
                b.setMessage(TComponent.literal("sexe:F"));
            }
                    this.holder = AnimationGui.entry.getModel(this.sexe);
                    this.forme_alt.setValue("");
                    this.onUpdated();
                }));
        dy += 20;
        this.addRenderableWidget(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, f5, b -> {
            AnimationGui.renderMobs.clear();
            RenderPokemob.reloadModel(AnimationGui.entry);
            this.onUpdated();
            this.renderHolder.wrapper.lastInit = Long.MIN_VALUE;
            this.renderHolder.init();
        }));
        dy += 20;
        this.addRenderableWidget(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, bg, b -> {
            this.bg = !this.bg;
        }));
        dy += 20;
        this.addRenderableWidget(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 10, module, b -> {
            int mod = this.moduleIndex + 1;
            modules.forEach(m -> {
                m.setEnabled(false);
            });
            if (mod < this.modules.size())
            {
                var modul = this.modules.get(mod);
                modul.setEnabled(true);
            }
            else mod = -1;
            this.moduleIndex = mod;
        }));

        // Buttons from here down are on the right side of the screen
        this.addRenderableWidget(new Button(this.width - 101 + 20, yOffset + 85 - yOffset / 2, 10, 10, right, b -> {
            AnimationGui.entry = Database.getEntry(AnimationGui.mob);
            if (AnimationGui.entry != null)
            {
                final List<PokedexEntry> formes = Lists.newArrayList(Database.getFormes(AnimationGui.entry));
                if (!formes.contains(AnimationGui.entry)) formes.add(AnimationGui.entry);
                Collections.sort(formes, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                for (int i = 0; i < formes.size(); i++) if (formes.get(i) == AnimationGui.entry)
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
        this.addRenderableWidget(new Button(this.width - 101, yOffset + 85 - yOffset / 2, 10, 10, left, b -> {
            AnimationGui.entry = Database.getEntry(AnimationGui.mob);
            if (AnimationGui.entry != null)
            {
                final List<PokedexEntry> formes = Lists.newArrayList(Database.getFormes(AnimationGui.entry));
                if (!formes.contains(AnimationGui.entry)) formes.add(AnimationGui.entry);
                Collections.sort(formes, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                for (int i = 0; i < formes.size(); i++) if (formes.get(i) == AnimationGui.entry)
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
        this.addRenderableWidget(new Button(this.width - 101 + 20, yOffset + 108 - yOffset / 2, 10, 10, right, b -> {
            AnimationGui.entry = Database.getEntry(AnimationGui.mob);
            if (AnimationGui.entry != null)
            {
                final List<FormeHolder> holders = Database.customModels.get(AnimationGui.entry);
                if (holders != null) try
                {
                    final ResourceLocation key = this.forme_alt.getValue().isEmpty() ? null
                            : PokecubeItems.toPokecubeResource(this.forme_alt.getValue());
                    for (int i = 0; i < holders.size(); i++) if (key == null || holders.get(i).key.equals(key))
                    {
                        final FormeHolder holder = i + 1 < holders.size() ? holders.get(i + 1) : holders.get(0);
                        this.forme_alt.setValue(holder.key.toString());
                        break;
                    }
                }
                catch (final Exception e)
                {
                    PokecubeAPI.LOGGER.error("Error cycling forme holder!");
                    this.forme_alt.setValue("");
                }
                else this.forme_alt.setValue("");
            }
            this.onUpdated();
        }));
        this.addRenderableWidget(new Button(this.width - 101, yOffset + 108 - yOffset / 2, 10, 10, left, b -> {
            AnimationGui.entry = Database.getEntry(AnimationGui.mob);
            if (AnimationGui.entry != null)
            {
                final List<FormeHolder> holders = Database.customModels.get(AnimationGui.entry);
                if (holders != null) try
                {
                    final ResourceLocation key = this.forme_alt.getValue().isEmpty() ? null
                            : PokecubeItems.toPokecubeResource(this.forme_alt.getValue());
                    for (int i = 0; i < holders.size(); i++) if (key == null || holders.get(i).key.equals(key))
                    {
                        final FormeHolder holder = i - 1 >= 0 ? holders.get(i - 1) : holders.get(holders.size() - 1);
                        this.forme_alt.setValue(holder.key.toString());
                        break;
                    }
                }
                catch (final Exception e)
                {
                    PokecubeAPI.LOGGER.error("Error cycling forme holder!");
                    this.forme_alt.setValue("");
                }
                else this.forme_alt.setValue("");
            }
            this.onUpdated();
        }));

        modules.forEach(m -> {
            m.init();
        });

        this.onUpdated();
    }

    @Override
    public boolean keyPressed(final int code, final int unk1, final int unk2)
    {
        for (var m : modules)
        {
            if (m.active && m.updateOnButtonPress(code))
            {
                this.onUpdated();
                break;
            }
        }
        if (code == GLFW.GLFW_KEY_ENTER || code == GLFW.GLFW_KEY_KP_ENTER) this.onUpdated();
        return super.keyPressed(code, unk1, unk2);
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
        for (var m : modules)
        {
            if (m.active && m.isPauseScreen())
            {
                return true;
            }
        }
        return false;
    }

}
