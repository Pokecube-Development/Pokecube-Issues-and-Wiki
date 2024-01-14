package pokecube.core.client.gui.watch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.utils.PokeType;
import pokecube.core.client.gui.AnimationGui;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.ShiftedTooltip;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.client.gui.watch.pokemob.Bonus;
import pokecube.core.client.gui.watch.pokemob.Breeding;
import pokecube.core.client.gui.watch.pokemob.Description;
import pokecube.core.client.gui.watch.pokemob.Moves;
import pokecube.core.client.gui.watch.pokemob.PokeInfoPage;
import pokecube.core.client.gui.watch.pokemob.Spawns;
import pokecube.core.client.gui.watch.pokemob.StatsInfo;
import pokecube.core.client.gui.watch.util.PageWithSubPages;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.StatsCollector;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.network.packets.PacketPokedex;
import thut.core.common.ThutCore;
import thut.core.common.handlers.PlayerDataHandler;
import thut.lib.TComponent;

public class PokemobInfoPage extends PageWithSubPages<PokeInfoPage>
{
    public static int savedIndex = 0;

    public static List<Class<? extends PokeInfoPage>> PAGELIST = Lists.newArrayList();

    static
    {
        PokemobInfoPage.PAGELIST.add(StatsInfo.class);
        PokemobInfoPage.PAGELIST.add(Bonus.class);
        PokemobInfoPage.PAGELIST.add(Moves.class);
        PokemobInfoPage.PAGELIST.add(Spawns.class);
        PokemobInfoPage.PAGELIST.add(Breeding.class);
        PokemobInfoPage.PAGELIST.add(Description.class);
    }

    public static class UVHolder
    {
        private int uOffset = 0;
        private int vOffset = 0;

        private int buttonX = 0;
        private int buttonY = 0;

        private int index = 0;

        public UVHolder(int x, int y, int u, int v, int index)
        {
            this.buttonX = x;
            this.buttonY = y;
            this.uOffset = u;
            this.vOffset = v;
            this.index = index;
        }

        public void makeButton(PokemobInfoPage gui)
        {
            final int x = gui.watch.width / 2;
            final int y = gui.watch.height / 2 - 5;
            WatchPage page = gui.createPage(index);
            gui.addRenderableWidget(new TexButton(x + buttonX, y + buttonY, 17, 17, page.getTitle(), b -> {
                gui.changePage(this.index);
                PokemobInfoPage.savedIndex = this.index;
            }, new ShiftedTooltip(-buttonX, -95 - buttonY)).setTex(GuiPokeWatch.getWidgetTex()).noName()
                    .setRender(new UVImgRender(uOffset, vOffset, 17, 17)));
        }
    }

    private static PokeInfoPage makePage(final Class<? extends PokeInfoPage> clazz, final PokemobInfoPage parent)
    {
        try
        {
            return clazz.getConstructor(PokemobInfoPage.class).newInstance(parent);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error with making a page for watch", e);
            return null;
        }
    }

    public IPokemob pokemob;

    EditBox search;

    public PokemobInfoPage(final GuiPokeWatch watch)
    {
        super(TComponent.translatable("pokewatch.title.pokeinfo"), watch, GuiPokeWatch.TEX_DM, GuiPokeWatch.TEX_NM);
        this.pokemob = watch.pokemob;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (this.search.isFocused())
            if (keyCode == GLFW.GLFW_KEY_RIGHT && this.search.getCursorPosition() == this.search.value.length())
        {
            String text = this.search.getValue();
            text = Database.trim(text);
            final List<String> ret = new ArrayList<>();
            for (final PokedexEntry entry : Database.getSortedFormes())
            {
                final String check = entry.getTrimmedName();
                if (check.startsWith(text))
                {
                    String name = entry.getName();
                    if (name.contains(" ")) name = "\'" + name + "\'";
                    ret.add(name);
                }
            }
            Collections.sort(ret, (o1, o2) -> {
                if (o1.startsWith("'") && !o2.startsWith("'")) return 1;
                else if (o2.startsWith("'") && !o1.startsWith("'")) return -1;
                return o1.compareToIgnoreCase(o2);
            });
            ret.replaceAll(t -> {
                if (t.startsWith("'") && t.endsWith("'")) t = t.substring(1, t.length() - 1);
                return t;
            });
            String match = text;
            for (final String name : ret) if (ThutCore.trim(name).startsWith(ThutCore.trim(match)))
            {
                match = name;
                break;
            }
            if (!ret.isEmpty()) this.search.setValue(ret.get(0));
            return true;
        }
            else if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            PokedexEntry entry = this.pokemob.getPokedexEntry();
            PokedexEntry newEntry = Database.getEntry(this.search.getValue());
            // Search to see if maybe it was a translated name put into the
            // search.
            if (newEntry == null)
            {
                for (final PokedexEntry e : Database.getSortedFormes())
                {
                    final String translated = I18n.get(e.getUnlocalizedName());
                    if (translated.equalsIgnoreCase(this.search.getValue()))
                    {
                        Database.data2.put(translated, e);
                        entry = e;
                        break;
                    }
                }
                // If the pokedex entry is not actually registered, use
                // old
                // entry.
                if (Pokedex.getInstance().getIndex(entry) == null) entry = null;
            }
            if (newEntry != null)
            {
                boolean skip = newEntry.default_holder != null && newEntry.default_holder._entry != newEntry;
                if (skip) newEntry = newEntry.default_holder._entry;
            }
            if (newEntry != null)
            {
                this.search.setValue(newEntry.getName());
                this.pokemob = AnimationGui.getRenderMob(newEntry);
                this.initPages(this.pokemob);
            }
            else this.search.setValue(entry.getName());
            return true;
        }
        return super.keyPressed(keyCode, b, c);
    }

    @Override
    protected PokeInfoPage createPage(final int index)
    {
        return PokemobInfoPage.makePage(PokemobInfoPage.PAGELIST.get(index), this);
    }

    // Search Bar
    @Override
    public void init()
    {
        super.init();
        final int x = this.watch.width / 2 + 90;
        final int y = this.watch.height / 2 + 30;
        this.search = new EditBox(this.font, x - 205, y - 113, 100, 10, TComponent.literal(""));
        this.addRenderableWidget(this.search);
        this.index = PokemobInfoPage.savedIndex;
    }

    public void initPages(IPokemob pokemob)
    {
        if (pokemob == null)
        {
            final String name = PokecubePlayerDataHandler.getCustomDataTag(this.watch.player).getString("WEntry");
            PokedexEntry entry = Database.getEntry(name);
            if (entry == null) entry = Pokedex.getInstance().getFirstEntry();
            pokemob = AnimationGui.getRenderMob(entry);
        }
        this.pokemob = pokemob;
        var entry = pokemob.getPokedexEntry();
        if (!pokemob.getEntity().isAddedToWorld())
        {
            if (entry.isGenderForme)
            {
                pokemob.setSexe(entry.isMaleForme ? IPokemob.MALE : IPokemob.FEMALE);
            }
            else if (entry.getSexeRatio() == 0) pokemob.setSexe(IPokemob.MALE);
            else if (entry.getSexeRatio() == 254) pokemob.setSexe(IPokemob.FEMALE);
            pokemob.onGenesChanged();
        }
        this.addRenderableWidget(this.search);
        this.search.setVisible(!this.watch.canEdit(pokemob));
        this.search.setValue(I18n.get(entry.getUnlocalizedName()));
        PacketPokedex.sendSpecificSpawnsRequest(entry);
        PacketPokedex.updateWatchEntry(entry);
        // Force close and open the page to update.
        this.changePage(this.index);
    }

    @Override
    protected int pageCount()
    {
        return PokemobInfoPage.PAGELIST.size();
    }

    @Override
    public void postPageDraw(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 80;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 8;
        if (this.pokemob != null)
        {
            // Draw hovored tooltip with pokemob's name
            int mx = mouseX - x;
            int my = mouseY - y;
            if (mx > -30 && mx < -30 + 50) if (my > 25 && my < 25 + 70)
            {
                final List<String> text = Lists.newArrayList();
                text.add(this.pokemob.getPokedexEntry().getTranslatedName().getString());
                if (!this.pokemob.getPokemonNickname().isEmpty())
                    text.add("\"" + this.pokemob.getPokemonNickname() + "\"");
                GlStateManager._disableDepthTest();
                mx = -18; // 50
                my = 20;
                final int dy = this.font.lineHeight;
                int box = 0;
                for (final String s : text) box = Math.max(box, this.font.width(s) + 2);
                mx -= box/2;

                GuiComponent.fill(mat, x + mx - 2, y + my - 2,
                        x + mx + box + 2, y + my + dy * text.size() + 2, 0xD92E0A65);

                for (final String s : text)
                {
                    GuiComponent.fill(mat, x + mx - 1, y + my - 1,
                            x + mx + box + 1, y + my + dy + 1, 0xD91E0F1E);
                    if (this.pokemob.isShiny())
                        this.font.draw(mat, s, x + mx + 1, y + my + 1, 0xFFFFBB6F);
                    else this.font.draw(mat, s, x + mx + 1, y + my + 1, 0xFFFFFFFF);
                    my += dy;
                }
                GlStateManager._enableDepthTest();
            }
        }
    }

    @Override
    public void prePageDraw(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        if (this.current_page != null) this.current_page.renderBackground(mat);
        if (!this.watch.canEdit(this.pokemob))
        {
            final String name = PokecubePlayerDataHandler.getCustomDataTag(this.watch.player).getString("WEntry");
            if (!name.equals(this.pokemob.getPokedexEntry().getName()))
            {
                this.search.setValue(name);
                final PokedexEntry entry = this.pokemob.getPokedexEntry();
                final PokedexEntry newEntry = Database.getEntry(this.search.getValue());
                if (newEntry != null && newEntry != entry)
                {
                    this.search.setValue(newEntry.getName());
                    this.pokemob = AnimationGui.getRenderMob(newEntry);
                    this.initPages(this.pokemob);
                }
                else this.search.setValue(entry.getName());
            }
        }

        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 - 5;
        int colour = 0x185100;
        // Draw Subtitle Page
        var title = this.current_page.getTitle();
        this.font.draw(mat, title, x + 103 - this.font.width(title) / 2, y + 30, colour);
        int dx = -76;
        int dy = 10;

        // We only want to draw the level if we are actually inspecting a
        // pokemob.
        // Otherwise this will just show as lvl 1
        boolean drawLevel = this.watch.pokemob != null && this.watch.pokemob.getEntity().isAddedToWorld();

        // Draw Pokemob
        if (this.pokemob != null)
        {
            if (drawLevel) drawLevel = this.watch.pokemob.getPokedexEntry() == this.pokemob.getPokedexEntry();

            // Draw the icon indicating capture/inspect status.
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderTexture(0, GuiPokeWatch.TEXTURE_BASE);

            final PokedexEntry pokedexEntry = this.pokemob.getPokedexEntry();
            final PokecubePlayerStats stats = PlayerDataHandler.getInstance()
                    .getPlayerData(Minecraft.getInstance().player).getData(PokecubePlayerStats.class);
            boolean fullColour = StatsCollector.getCaptured(pokedexEntry, Minecraft.getInstance().player) > 0
                    || StatsCollector.getHatched(pokedexEntry, Minecraft.getInstance().player) > 0
                    || this.minecraft.player.getAbilities().instabuild;

            // Megas Inherit colouring from the base form.
            if (!fullColour && pokedexEntry.isMega())
                fullColour = StatsCollector.getCaptured(pokedexEntry.getBaseForme(), Minecraft.getInstance().player) > 0
                        || StatsCollector.getHatched(pokedexEntry.getBaseForme(), Minecraft.getInstance().player) > 0;

            IPokemob pokemob = this.pokemob;

            PokeType _type1 = pokemob.getType1();
            PokeType _type2 = pokemob.getType2();

            // Copy the stuff to the render mob if this mob is in world
            if (pokemob.getEntity().isAddedToWorld())
            {
                final IPokemob newMob = AnimationGui.getRenderMob(pokemob);
                pokemob = newMob;
            }

            pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
            pokemob.setGeneralState(GeneralStates.EVOLVING, false);

            // Set colouring accordingly.
            if (fullColour) pokemob.setRGBA(255, 255, 255, 255);
            else if (stats.hasInspected(pokedexEntry)) pokemob.setRGBA(127, 127, 127, 255);
            else pokemob.setRGBA(15, 15, 15, 255);

            pokemob.setSize(1);

            final float yaw = Util.getMillis() / 20;
            dx = -80; // 90
            dy = 45;

            // Draw the actual pokemob
            GuiPokemobHelper.renderMob(pokemob.getEntity(), x + dx, y + dy, 0, yaw, 0, yaw, 2f, partialTicks);

            final String level = "Lvl " + this.pokemob.getLevel();
            dx = -77;
            dy = 32;
            // Only draw the lvl if it is a real mob, otherwise it will just say
            // L.1
            final int lvlColour = 0x444444;
            if (drawLevel) this.font.draw(mat, level, x + dx, y + dy, lvlColour);


            final String type1 = PokeType.getTranslatedName(_type1).getString();
            dx = -77; // 72
            dy = 117;
            colour = _type1.colour;
            this.font.draw(mat, type1, x + dx, y + dy, colour);
            dy = 117;
            if (_type2 != PokeType.unknown)
            {
                final String slash = "/";
                colour = _type2.colour;
                dx += this.font.width(type1);
                this.font.draw(mat, slash, x + dx, y + dy, 0x444444);
                final String type2 = PokeType.getTranslatedName(_type2).getString();
                dx += this.font.width(slash);
                this.font.draw(mat, type2, x + dx, y + dy, colour);
            }
        }
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;

        this.addRenderableWidget(new TexButton(x - 108, y + 102, 17, 17,
                TComponent.literal(""), b ->
        {
            GuiPokeWatch.nightMode = !GuiPokeWatch.nightMode;
            this.watch.init();
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new TexButton.UVImgRender(110, 72, 17, 17)));
    }

    @Override
    public void preSubOpened()
    {
        this.children().clear();
        this.initPages(this.pokemob);

        List<UVHolder> buttons = new ArrayList<>();
        buttons.add(new UVHolder(9, -80, 127, 72, 0)); // Stats
        buttons.add(new UVHolder(27, -80, 144, 72, 1)); // Battle
        buttons.add(new UVHolder(45, -80, 161, 72, 2)); // Moves
        buttons.add(new UVHolder(63, -80, 178, 72, 3)); // Spawn
        buttons.add(new UVHolder(81, -80, 195, 72, 4)); // Breeding
        buttons.add(new UVHolder(99, -80, 212, 72, 5)); // Description
        buttons.forEach(uv -> uv.makeButton(this));
    }
}
