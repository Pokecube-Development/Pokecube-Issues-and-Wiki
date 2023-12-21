package pokecube.core.client.gui.watch;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.core.client.gui.AnimationGui;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.client.gui.watch.pokemob.PlayerInfo;
import pokecube.core.client.gui.watch.pokemob.PokeStartPage;
import pokecube.core.client.gui.watch.util.PageWithSubPages;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.StatsCollector;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.network.packets.PacketPokedex;
import thut.core.common.handlers.PlayerDataHandler;
import thut.lib.TComponent;

public class StartWatch extends PageWithSubPages<PokeStartPage>
{
    public static int savedIndex = 0;
    public static TexButton shiny;
    public static TexButton formChanger;
    TexButton gender;

    public static List<Class<? extends PokeStartPage>> PAGELIST = Lists.newArrayList();

    static
    {
        StartWatch.PAGELIST.add(PlayerInfo.class);
    
    }

    private static PokeStartPage makePage(final Class<? extends PokeStartPage> clazz, final StartWatch parent)
    {
        try
        {
            return clazz.getConstructor(StartWatch.class).newInstance(parent);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error with making a page for watch", e);
            return null;
        }
    }

    public IPokemob pokemob;

    public StartWatch(final GuiPokeWatch watch)
    {
        super(TComponent.translatable(""), watch, GuiPokeWatch.TEX_DM, GuiPokeWatch.TEX_NM);
        this.pokemob = watch.pokemob;
    }

    @Override
    protected PokeStartPage createPage(final int index)
    {
        return StartWatch.makePage(StartWatch.PAGELIST.get(index), this);
    }

    // Search Bar
    @Override
    public void init()
    {
        super.init();
        this.index = StartWatch.savedIndex;
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
        this.watch.canEdit(pokemob);
        pokemob.getPokedexEntry().getName();
        PacketPokedex.sendSpecificSpawnsRequest(pokemob.getPokedexEntry());
        PacketPokedex.updateWatchEntry(pokemob.getPokedexEntry());
        // Force close and open the page to update.
        this.changePage(this.index);
    }

    @Override
    protected int pageCount()
    {
        return StartWatch.PAGELIST.size();
    }

    @Override
    public void postPageDraw(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {}

    @Override
    public void prePageDraw(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        if (this.current_page != null) this.current_page.renderBackground(mat);
        if (!this.watch.canEdit(this.pokemob))
        {
            final String name = PokecubePlayerDataHandler.getCustomDataTag(this.watch.player).getString("WEntry");
            if (!name.equals(this.pokemob.getPokedexEntry().getName()))
            {
                final PokedexEntry entry = this.pokemob.getPokedexEntry();
                final PokedexEntry newEntry = Database.getEntry(name);
                if (newEntry != null && newEntry != entry)
                {
                    newEntry.getName();
                    this.pokemob = AnimationGui.getRenderMob(newEntry);
                    this.initPages(this.pokemob);
                }
                else entry.getName();
            }
        }

        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 - 5;
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
            dx = -15; //90
            dy = 65;

            // Draw the actual pokemob
            GuiPokemobHelper.renderMob(pokemob.getEntity(), x + dx, y + dy, 0, yaw, 0, yaw, 3.0F, partialTicks);
        }
    }

    @Override
    public void preSubOpened()
    {
        this.children().clear();
        this.initPages(this.pokemob);
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;

        // Play Sound Button
        this.addRenderableWidget(new TexButton(x - 78, y + 95, 12, 12, TComponent.literal(""), b -> {
            this.watch.player.playSound(this.pokemob.getSound(), 0.5f, 1.0F);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(229, 72, 12, 12)));

        // Shiny Button
       shiny = this.addRenderableWidget(new TexButton(x - 65, y + 95, 12, 12, TComponent.literal(""), b -> {
           if (this.pokemob.getPokedexEntry().hasShiny && !this.pokemob.getEntity().isAddedToWorld()) {
        		this.pokemob.setShiny(!this.pokemob.isShiny());
        		this.pokemob.onGenesChanged();
        	}
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(241, 36, 12, 12)));

        shiny.active = this.pokemob.getPokedexEntry().hasShiny && !this.pokemob.getEntity().isAddedToWorld();

        // Change Forms Button
        formChanger = this.addRenderableWidget(new TexButton(x - 52, y + 95, 12, 12, TComponent.literal(""), b -> {
            if (this.pokemob.getEntity().isAddedToWorld()) return;
            PokedexEntry entry = this.pokemob.getPokedexEntry();
            PokedexEntry nextEntry = Pokedex.getInstance().getNextForm(entry);
            if (nextEntry == entry) nextEntry = Pokedex.getInstance().getFirstForm(entry);
            this.pokemob = this.pokemob.setPokedexEntry(nextEntry);
            this.pokemob.setBasePokedexEntry(nextEntry);
            this.initPages(this.pokemob);
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(241, 72, 12, 12)));

        PokedexEntry entry = this.pokemob.getPokedexEntry();
        PokedexEntry firstEntry = Pokedex.getInstance().getFirstForm(entry);
        PokedexEntry nextEntry = Pokedex.getInstance().getNextForm(entry);
        PokedexEntry previousEntry = Pokedex.getInstance().getPreviousForm(entry);
        formChanger.active = (firstEntry != nextEntry && previousEntry != firstEntry) && !this.pokemob.getEntity().isAddedToWorld();

        // Gender Button
        Component genderText = TComponent.literal("");
        if (this.pokemob.getSexe() == IPokemob.MALE)
        {
            genderText = TComponent.literal("\u2642");
        } else if (this.pokemob.getSexe() == IPokemob.FEMALE)
        {
            genderText = TComponent.literal("\u2640");
        }

        this.gender = this.addRenderableWidget(new TexButton(x - 39, y + 95, 12, 12, genderText, b -> {
            var old = this.pokemob.getPokedexEntry();
            var e = old;
            switch (this.pokemob.getSexe())
            {
                case IPokemob.MALE:
                    e = old.getForGender(IPokemob.FEMALE);
                    this.pokemob.setSexe(IPokemob.FEMALE);
                    if (e != old)
                    {
                        this.pokemob = this.pokemob.setPokedexEntry(e);
                        this.pokemob.setBasePokedexEntry(e);
                    }
                    this.initPages(this.pokemob);
                    break;
                case IPokemob.FEMALE:
                    e = old.getForGender(IPokemob.MALE);
                    this.pokemob.setSexe(IPokemob.MALE);
                    if (e != old)
                    {
                        this.pokemob = this.pokemob.setPokedexEntry(e);
                        this.pokemob.setBasePokedexEntry(e);
                    }
                    this.initPages(this.pokemob);
                    break;
            }
            this.pokemob.onGenesChanged();
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(200, 0, 12, 12)).shadow(true));

        this.gender.active = !this.pokemob.getEntity().isAddedToWorld() &&
                (this.pokemob.getSexe() == IPokemob.MALE || this.pokemob.getSexe() == IPokemob.FEMALE);
        if (this.pokemob.getSexe() == IPokemob.MALE) gender.setFGColor(ChatFormatting.DARK_BLUE.getColor());
        else if (this.pokemob.getSexe() == IPokemob.FEMALE) gender.setFGColor(ChatFormatting.DARK_RED.getColor());
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

        this.gender.active = !this.pokemob.getEntity().isAddedToWorld() &&
                (this.pokemob.getSexe() == IPokemob.MALE || this.pokemob.getSexe() == IPokemob.FEMALE);
    }
}
