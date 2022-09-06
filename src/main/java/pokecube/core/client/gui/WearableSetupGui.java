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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
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
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.client.render.mobs.RenderPokemob;
import pokecube.core.client.render.mobs.RenderPokemob.Holder;
import pokecube.core.database.Database;
import pokecube.core.impl.capabilities.DefaultPokemob;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.EntityTools;
import thut.api.maths.Vector3;
import thut.bling.BlingItem;
import thut.core.client.render.animation.AnimationChanger;
import thut.core.client.render.animation.AnimationLoader;
import thut.core.client.render.animation.IAnimationChanger.WornOffsets;
import thut.core.common.ThutCore;
import thut.core.common.network.EntityUpdate;
import thut.lib.RegHelper;
import thut.lib.TComponent;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class WearableSetupGui extends Screen
{

    private static Map<PokedexEntry, IPokemob> renderMobs = Maps.newHashMap();

    public static IPokemob getRenderMob(final PokedexEntry entry)
    {
        IPokemob ret = WearableSetupGui.renderMobs.get(entry);
        if (ret == null)
        {
            final Mob mob = PokecubeCore.createPokemob(entry, PokecubeCore.proxy.getWorld());
            ret = PokemobCaps.getPokemobFor(mob);
            WearableSetupGui.renderMobs.put(entry, ret);
        }
        return ret;
    }

    public static IPokemob getRenderMob(final IPokemob realMob)
    {
        final IPokemob ret = WearableSetupGui.getRenderMob(realMob.getPokedexEntry());
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

    static String mob = "";

    public static PokedexEntry entry;

    EditBox anim;
    EditBox state_g;
    EditBox state_c;
    EditBox state_l;
    EditBox forme;
    EditBox forme_alt;
    EditBox dyeColour;
    EditBox rngValue;

    EditBox worn_slot;
    EditBox worn_part;
    EditBox worn_item;
    EditBox rX;
    EditBox rY;
    EditBox rZ;
    EditBox dX;
    EditBox dY;
    EditBox dZ;
    EditBox scaleS;

    IPokemob toRender;
    Holder renderHolder;
    FormeHolder holder = null;

    List<PokedexEntry> entries = Lists.newArrayList();
    List<FormeHolder> formes = Lists.newArrayList();

    Set<PokedexEntry> doneEntries = Sets.newHashSet();

    Set<ResourceLocation> doneLocs = Sets.newHashSet();

    int entryIndex = 0;
    int formIndex = 0;

    float xRenderAngle = 0;
    float yRenderAngle = 0;
    float yHeadRenderAngle = 0;
    float xHeadRenderAngle = 0;
    int mouseRotateControl;
    int prevX = 0;
    int prevY = 0;
    float scale = 1;
    long transitTime = 0;

    int[] shift =
    { 0, 0 };

    boolean ground = true;
    boolean bg = false;
    byte sexe = IPokemob.NOSEXE;
    boolean shiny = false;

    boolean[] genders =
    { false, false };

    List<String> components;

    Map<String, EnumWearable> wearableNames = EnumWearable.wearableNames;
    Map<String, Integer> slots = EnumWearable.slotsNames;
    List<String> sortedSlots = Lists.newArrayList();
    int worn_index = 0;

    public WearableSetupGui()
    {
        super(TComponent.translatable("pokecube.model_reloader"));
        sortedSlots.addAll(slots.keySet());
        sortedSlots.sort(null);
    }

    void onUpdated()
    {
        WearableSetupGui.entry = Database.getEntry(this.forme.getValue());
        if (WearableSetupGui.entry == null) WearableSetupGui.entry = Database.getEntry(WearableSetupGui.mob);
        WearableSetupGui.mob = WearableSetupGui.entry.getName();
        this.forme.setValue(WearableSetupGui.mob);
        this.holder = WearableSetupGui.entry.getModel(this.sexe);

        if (!this.forme_alt.getValue().isEmpty()) try
        {
            final ResourceLocation key = PokecubeItems.toPokecubeResource(this.forme_alt.getValue());
            this.holder = Database.formeHolders.get(key);
        }
        catch (final Exception e)
        {
            this.holder = WearableSetupGui.entry.getModel(this.sexe);
        }
        this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());

        this.toRender = WearableSetupGui.getRenderMob(WearableSetupGui.entry);
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
        this.renderHolder = RenderPokemob.holders.get(WearableSetupGui.entry);
        if (this.holder != null)
            this.renderHolder = RenderPokemob.customs.getOrDefault(this.holder.key, this.renderHolder);

        PacketPokedex.updateWatchEntry(WearableSetupGui.entry);

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

        if (!slots.containsKey(worn_slot.getValue()))
        {
            worn_slot.setValue(sortedSlots.get(worn_index));
            resetWearableValues();
        }
        else
        {
            try
            {
                updateWearableRender();
            }
            catch (Exception err)
            {
                err.printStackTrace();
            }
        }
    }

    @Override
    public void render(final PoseStack mat, final int unk1, final int unk2, final float partialTicks)
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
            mat.pushPose();
            final Mob entity = this.toRender.getEntity();
            final IPokemob pokemob = this.toRender;
            pokemob.setSize(1);

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
            entity.yHeadRotO = -entity.yHeadRot;
            entity.xRotO = entity.xRot;
            entity.tickCount = Minecraft.getInstance().player.tickCount;
            entity.animationPosition += 0.0125;
            final float zoom = this.scale;
            if (this.renderHolder != null)
            {
                this.renderHolder.overrideAnim = true;
                this.renderHolder.anim = ThutCore.trim(this.anim.getValue());
            }
            RenderSystem.setShaderLights(com.mojang.math.Vector3f.YN, com.mojang.math.Vector3f.ZN);
            final float l = WearableSetupGui.entry.getModelSize().lengthSquared();
            // Sometimes things go bad and this happens
            if (l <= 0.0001 || l > 1e10) WearableSetupGui.entry.getModelSize().set(1, 1, 1);
            GuiPokemobBase.autoScale = false;
            GuiPokemobBase.renderMob(mat, entity, j, k, this.yRenderAngle, this.xRenderAngle, this.yHeadRenderAngle,
                    this.xHeadRenderAngle, zoom);
            GuiPokemobBase.autoScale = true;
            if (this.renderHolder != null) this.renderHolder.overrideAnim = false;
            mat.popPose();
        }
    }

    @Override
    protected void init()
    {
        super.init();
        int yOffset = this.height / 2;
        int xOffset = this.width / 2;
        this.sexe = IPokemob.MALE;
        this.bg = true;
        if (GuiPokedex.pokedexEntry != null) WearableSetupGui.mob = GuiPokedex.pokedexEntry.getName();
        WearableSetupGui.entry = Database.getEntry(WearableSetupGui.mob);
        if (WearableSetupGui.entry == null) WearableSetupGui.entry = Pokedex.getInstance().getFirstEntry();
        if (WearableSetupGui.entry != null) WearableSetupGui.mob = WearableSetupGui.entry.getName();

        final Component blank = TComponent.literal("");

        this.anim = new EditBox(this.font, this.width - 101, yOffset + 43 - yOffset / 2, 100, 10, blank);
        this.state_g = new EditBox(this.font, this.width - 101, yOffset - 33 - yOffset / 2, 100, 10, blank);
        this.state_c = new EditBox(this.font, this.width - 101, yOffset - 13 - yOffset / 2, 100, 10, blank);
        this.state_l = new EditBox(this.font, this.width - 101, yOffset + 07 - yOffset / 2, 100, 10, blank);
        this.forme = new EditBox(this.font, this.width - 101, yOffset + 73 - yOffset / 2, 100, 10, blank);
        this.forme_alt = new EditBox(this.font, this.width - 101, yOffset + 97 - yOffset / 2, 100, 10, blank);
        this.rngValue = new EditBox(this.font, this.width - 101, yOffset + 123 - yOffset / 2, 100, 10, blank);
        this.dyeColour = new EditBox(this.font, this.width - 21, yOffset + 28 - yOffset / 2, 20, 10, blank);
        this.forme.setValue(WearableSetupGui.mob);
        this.dyeColour.setValue(WearableSetupGui.entry.defaultSpecial + "");
        this.anim.setValue("idle");

        this.addRenderableWidget(this.anim);
        this.addRenderableWidget(this.state_g);
        this.addRenderableWidget(this.state_c);
        this.addRenderableWidget(this.state_l);
        this.addRenderableWidget(this.forme);
        this.addRenderableWidget(this.forme_alt);
        this.addRenderableWidget(this.rngValue);
        this.addRenderableWidget(this.dyeColour);

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
            final PokedexEntry num = Pokedex.getInstance().getPrevious(WearableSetupGui.entry, 1);
            if (num != WearableSetupGui.entry) WearableSetupGui.entry = num;
            else WearableSetupGui.entry = Pokedex.getInstance().getLastEntry();
            WearableSetupGui.mob = WearableSetupGui.entry.getForGender(this.sexe).getName();
            this.forme.setValue(WearableSetupGui.mob);
            this.holder = WearableSetupGui.entry.getModel(this.sexe);
            this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());
            PacketPokedex.updateWatchEntry(WearableSetupGui.entry);
            this.onUpdated();
        }));
        dy += 20;
        this.addRenderableWidget(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, next, b -> {
            final PokedexEntry num = Pokedex.getInstance().getNext(WearableSetupGui.entry, 1);
            if (num != WearableSetupGui.entry) WearableSetupGui.entry = num;
            else WearableSetupGui.entry = Pokedex.getInstance().getFirstEntry();
            WearableSetupGui.mob = WearableSetupGui.entry.getForGender(this.sexe).getName();
            this.forme.setValue(WearableSetupGui.mob);
            this.holder = WearableSetupGui.entry.getModel(this.sexe);
            this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());
            PacketPokedex.updateWatchEntry(WearableSetupGui.entry);
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
                    this.holder = WearableSetupGui.entry.getModel(this.sexe);
                    this.forme_alt.setValue("");
                    this.onUpdated();
                }));
        dy += 20;
        this.addRenderableWidget(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, f5, b -> {
            WearableSetupGui.renderMobs.clear();
            RenderPokemob.reloadModel(WearableSetupGui.entry);
            this.onUpdated();
            this.renderHolder.wrapper.lastInit = Long.MIN_VALUE;
            this.renderHolder.init();
        }));
        dy += 20;
        this.addRenderableWidget(new Button(this.width / 2 - xOffset, yOffset + dy, 40, 20, bg, b -> {
            this.bg = !this.bg;
        }));
        dy += 20;
        this.addRenderableWidget(
                new Button(this.width / 2 - xOffset, yOffset + dy, 40, 10, TComponent.literal("SEE"), b ->
                {
                    int a = this.toRender.getRGBA()[3];
                    if (a > 100) this.toRender.setRGBA(255, 255, 255, a = 55);
                    else this.toRender.setRGBA(255, 255, 255, a = 255);
                }));

        // Buttons from here down are on the right side of the screen
        this.addRenderableWidget(new Button(this.width - 101 + 20, yOffset + 85 - yOffset / 2, 10, 10, right, b -> {
            WearableSetupGui.entry = Database.getEntry(WearableSetupGui.mob);
            if (WearableSetupGui.entry != null)
            {
                final List<PokedexEntry> formes = Lists.newArrayList(Database.getFormes(WearableSetupGui.entry));
                if (!formes.contains(WearableSetupGui.entry)) formes.add(WearableSetupGui.entry);
                Collections.sort(formes, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                for (int i = 0; i < formes.size(); i++) if (formes.get(i) == WearableSetupGui.entry)
                {
                    WearableSetupGui.entry = i + 1 < formes.size() ? formes.get(i + 1) : formes.get(0);
                    WearableSetupGui.mob = WearableSetupGui.entry.getName();
                    this.holder = WearableSetupGui.entry.getModel(this.sexe);
                    this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());
                    this.forme.setValue(WearableSetupGui.mob);
                    break;
                }
            }
            this.onUpdated();
        }));
        this.addRenderableWidget(new Button(this.width - 101, yOffset + 85 - yOffset / 2, 10, 10, left, b -> {
            WearableSetupGui.entry = Database.getEntry(WearableSetupGui.mob);
            if (WearableSetupGui.entry != null)
            {
                final List<PokedexEntry> formes = Lists.newArrayList(Database.getFormes(WearableSetupGui.entry));
                if (!formes.contains(WearableSetupGui.entry)) formes.add(WearableSetupGui.entry);
                Collections.sort(formes, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                for (int i = 0; i < formes.size(); i++) if (formes.get(i) == WearableSetupGui.entry)
                {
                    WearableSetupGui.entry = i - 1 >= 0 ? formes.get(i - 1) : formes.get(formes.size() - 1);
                    WearableSetupGui.mob = WearableSetupGui.entry.getName();
                    this.holder = WearableSetupGui.entry.getModel(this.sexe);
                    this.forme_alt.setValue(this.holder == null ? "" : this.holder.key.toString());
                    this.forme.setValue(WearableSetupGui.mob);
                    break;
                }
            }
            this.onUpdated();
        }));
        this.addRenderableWidget(new Button(this.width - 101 + 20, yOffset + 108 - yOffset / 2, 10, 10, right, b -> {
            WearableSetupGui.entry = Database.getEntry(WearableSetupGui.mob);
            if (WearableSetupGui.entry != null)
            {
                final List<FormeHolder> holders = Database.customModels.get(WearableSetupGui.entry);
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
            WearableSetupGui.entry = Database.getEntry(WearableSetupGui.mob);
            if (WearableSetupGui.entry != null)
            {
                final List<FormeHolder> holders = Database.customModels.get(WearableSetupGui.entry);
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

        final Component zero = TComponent.literal("0");
        final Component one = TComponent.literal("1");

        yOffset -= yOffset / 2;
        yOffset += 35;

        int dx = 210;
        this.worn_item = new EditBox(this.font, this.width - dx, yOffset - 90, 100, 10, blank);
        this.worn_slot = new EditBox(this.font, this.width - dx, yOffset - 80, 100, 10, blank);
        this.worn_part = new EditBox(this.font, this.width - dx, yOffset - 70, 100, 10, blank);

        yOffset += 10;
        this.scaleS = new EditBox(this.font, this.width - dx, yOffset - 50, 100, 10, one);
        this.rZ = new EditBox(this.font, this.width - dx, yOffset - 20, 50, 10, zero);
        this.rY = new EditBox(this.font, this.width - dx, yOffset - 30, 50, 10, zero);
        this.rX = new EditBox(this.font, this.width - dx, yOffset - 40, 50, 10, zero);
        dx -= 50;
        this.dZ = new EditBox(this.font, this.width - dx, yOffset - 20, 50, 10, zero);
        this.dY = new EditBox(this.font, this.width - dx, yOffset - 30, 50, 10, zero);
        this.dX = new EditBox(this.font, this.width - dx, yOffset - 40, 50, 10, zero);
        yOffset -= 10;

        final Component copy = TComponent.literal("copy");
        dx += 30;
        this.addRenderableWidget(new Button(this.width - dx, yOffset - 60, 30, 10, copy, b -> {
            String xml = "<worn id=\"%s\" parent=\"%s\" offset=\"%s,%s,%s\" angles=\"%s,%s,%s\" scale=\"%s\"/>";
            String key = this.worn_slot.getValue();
            String part = this.worn_part.getValue();

            dX.setValue(dX.value.trim());
            while (dX.value.endsWith("0") && dX.value.contains("."))
            {
                dX.setValue(dX.value.substring(0, dX.value.length() - 1));
            }
            dY.setValue(dY.value.trim());
            while (dY.value.endsWith("0") && dY.value.contains("."))
            {
                dY.setValue(dY.value.substring(0, dY.value.length() - 1));
            }
            dZ.setValue(dZ.value.trim());
            while (dZ.value.endsWith("0") && dZ.value.contains("."))
            {
                dZ.setValue(dZ.value.substring(0, dZ.value.length() - 1));
            }

            rX.setValue(rX.value.trim());
            while (rX.value.endsWith("0") && rX.value.contains("."))
            {
                rX.setValue(rX.value.substring(0, rX.value.length() - 1));
            }
            rY.setValue(rY.value.trim());
            while (rY.value.endsWith("0") && rY.value.contains("."))
            {
                rY.setValue(rY.value.substring(0, rY.value.length() - 1));
            }
            rZ.setValue(rZ.value.trim());
            while (rZ.value.endsWith("0") && rZ.value.contains("."))
            {
                rZ.setValue(rZ.value.substring(0, rZ.value.length() - 1));
            }

            xml = xml.formatted(key, part, dX.value, dY.value, dZ.value, rX.value, rY.value, rZ.value, scaleS.value);
            Minecraft.getInstance().keyboardHandler.setClipboard(xml);
            Minecraft.getInstance().player.displayClientMessage(TComponent.literal("Copied XML to clipboard"), true);
        }));
        dx -= 30;
        this.addRenderableWidget(new Button(this.width - dx, yOffset - 60, 30, 10, reset, b -> {
            resetWearableValues();
        }));

        dx = 220;
        dy = -80;
        this.addRenderableWidget(new Button(this.width - dx, yOffset + dy, 10, 10, right, b -> {
            this.worn_index++;
            this.worn_index = this.worn_index % sortedSlots.size();
            worn_slot.setValue(sortedSlots.get(worn_index));
            this.resetWearableValues();
        }));
        dx += 10;
        this.addRenderableWidget(new Button(this.width - dx, yOffset + dy, 10, 10, left, b -> {
            this.worn_index--;
            if (worn_index < 0) worn_index = sortedSlots.size() - 1;
            this.worn_index = this.worn_index % sortedSlots.size();
            worn_slot.setValue(sortedSlots.get(worn_index));
            this.resetWearableValues();
        }));

        this.rX.setValue("0");
        this.rY.setValue("0");
        this.rZ.setValue("0");
        this.dX.setValue("0");
        this.dY.setValue("0");
        this.dZ.setValue("0");
        this.scaleS.setValue("1");

        this.addRenderableWidget(this.worn_item);
        this.addRenderableWidget(this.worn_part);
        this.addRenderableWidget(this.worn_slot);

        this.addRenderableWidget(this.rX);
        this.addRenderableWidget(this.rY);
        this.addRenderableWidget(this.rZ);
        this.addRenderableWidget(this.dX);
        this.addRenderableWidget(this.dY);
        this.addRenderableWidget(this.dZ);
        this.addRenderableWidget(this.scaleS);

        this.onUpdated();
    }

    private void resetWearableValues()
    {
        String key = worn_slot.getValue();
        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        Object ren = manager.getRenderer(toRender.getEntity());

        if (ren instanceof RenderPokemob renderer
                && renderer.getModel().renderer.getAnimationChanger() instanceof AnimationChanger changer)
        {
            WornOffsets old = changer.wornOffsets.get(key);
            if (old != null)
            {
                this.worn_part.setValue(old.parent);
                dX.setValue("%.3f".formatted(old.offset.x));
                dY.setValue("%.3f".formatted(old.offset.y));
                dZ.setValue("%.3f".formatted(old.offset.z));

                rX.setValue("%.1f".formatted(old.angles.x));
                rY.setValue("%.1f".formatted(old.angles.y));
                rZ.setValue("%.1f".formatted(old.angles.z));

                double sx = old.scale.x;
                double sy = old.scale.y;
                double sz = old.scale.z;

                if (sx == sy && sy == sz) scaleS.setValue("%.3f".formatted(sx));
                else scaleS.setValue("%.3f,%.3f,%.3f".formatted(sx, sy, sz));
            }
        }

        final PlayerWearables wearables = ThutWearables.getWearables(toRender.getEntity());
        for (final EnumWearable w : EnumWearable.values()) if (w.slots == 2)
        {
            wearables.setWearable(w, ItemStack.EMPTY, 0);
            wearables.setWearable(w, ItemStack.EMPTY, 1);
        }
        else
        {
            wearables.setWearable(w, ItemStack.EMPTY, 0);
        }

        ItemStack stack = ItemStack.EMPTY;
        EnumWearable slot = wearableNames.get(key);
        if (stack.isEmpty())
        {
            for (Item i2 : BlingItem.bling)
            {
                if (BlingItem.defaults.get(i2) == slot)
                {
                    stack = new ItemStack(i2);
                    break;
                }
            }
        }
        wearables.setWearable(slot, stack.copy(), slots.get(key));
        if (!stack.isEmpty())
        {
            this.worn_item.setValue(RegHelper.getKey(stack) + "");
        }
    }

    private void updateWearableRender()
    {
        String key = worn_slot.getValue();
        String part = worn_part.getValue();
        String worn = worn_item.getValue();

        final PlayerWearables wearables = ThutWearables.getWearables(toRender.getEntity());
        for (final EnumWearable w : EnumWearable.values()) if (w.slots == 2)
        {
            wearables.setWearable(w, ItemStack.EMPTY, 0);
            wearables.setWearable(w, ItemStack.EMPTY, 1);
        }
        else
        {
            wearables.setWearable(w, ItemStack.EMPTY, 0);
        }

        Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(worn));
        ItemStack stack = new ItemStack(i);
        EnumWearable slot = wearableNames.get(key);
        if (stack.isEmpty())
        {
            for (Item i2 : BlingItem.bling)
            {
                if (BlingItem.defaults.get(i2) == slot)
                {
                    stack = new ItemStack(i2);
                    break;
                }
            }
        }
        wearables.setWearable(slot, stack, slots.get(key));

        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        Object ren = manager.getRenderer(toRender.getEntity());
        if (ren instanceof RenderPokemob renderer
                && renderer.getModel().renderer.getAnimationChanger() instanceof AnimationChanger changer)
        {
            Vector3 w_offset = new Vector3(Float.parseFloat(dX.getValue()), Float.parseFloat(dY.getValue()),
                    Float.parseFloat(dZ.getValue()));
            Vector3 w_angles = new Vector3(Float.parseFloat(rX.getValue()), Float.parseFloat(rY.getValue()),
                    Float.parseFloat(rZ.getValue()));
            Vector3 w_scale = AnimationLoader.getVector3(this.scaleS.getValue(), null);
            WornOffsets replace = new WornOffsets(part, w_offset, w_scale, w_angles);
            changer.wornOffsets.put(key, replace);
        }
    }

    @Override
    public boolean keyPressed(final int code, final int unk1, final int unk2)
    {
        if (code == GLFW.GLFW_KEY_UP || code == GLFW.GLFW_KEY_DOWN)
        {
            float dv = code == GLFW.GLFW_KEY_UP ? 1 : -1;
            if (Screen.hasShiftDown()) dv *= 0.1f;
            if (Screen.hasControlDown()) dv *= 10f;

            if (rX.isFocused())
            {
                try
                {
                    float value = Float.parseFloat(rX.value);
                    value += dv;
                    rX.setValue("%.1f".formatted(value));
                }
                catch (Exception e)
                {
                    rX.setValue("0");
                }

            }
            else if (rY.isFocused())
            {

                try
                {
                    float value = Float.parseFloat(rY.value);
                    value += dv;
                    rY.setValue("%.1f".formatted(value));
                }
                catch (Exception e)
                {
                    rY.setValue("0");
                }
            }
            else if (rZ.isFocused())
            {
                try
                {
                    float value = Float.parseFloat(rZ.value);
                    value += dv;
                    rZ.setValue("%.1f".formatted(value));
                }
                catch (Exception e)
                {
                    rZ.setValue("0");
                }
            }

            if (dX.isFocused())
            {
                try
                {
                    float value = Float.parseFloat(dX.value);
                    value += 0.01f * dv;
                    dX.setValue("%.3f".formatted(value));
                }
                catch (Exception e)
                {
                    dX.setValue("0");
                }
            }
            else if (dY.isFocused())
            {

                try
                {
                    float value = Float.parseFloat(dY.value);
                    value += 0.01f * dv;
                    dY.setValue("%.3f".formatted(value));
                }
                catch (Exception e)
                {
                    dY.setValue("0");
                }
            }
            else if (dZ.isFocused())
            {
                try
                {
                    float value = Float.parseFloat(dZ.value);
                    value += 0.01f * dv;
                    dZ.setValue("%.3f".formatted(value));
                }
                catch (Exception e)
                {
                    dZ.setValue("0");
                }
            }
            else if (scaleS.isFocused() && !scaleS.getValue().contains(","))
            {
                try
                {
                    float value = Float.parseFloat(scaleS.value);
                    value += 0.01f * dv;
                    scaleS.setValue("%.3f".formatted(value));
                }
                catch (Exception e)
                {
                    scaleS.setValue("1");
                }
            }
            this.onUpdated();
        }
        if (code == GLFW.GLFW_KEY_ENTER || code == GLFW.GLFW_KEY_KP_ENTER)
        {
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
        return toRender != null && toRender.getRGBA()[3] < 100;
    }
}
