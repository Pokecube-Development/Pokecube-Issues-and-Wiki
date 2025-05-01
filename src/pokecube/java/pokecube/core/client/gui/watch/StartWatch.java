package pokecube.core.client.gui.watch;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
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
import pokecube.core.entity.genetics.genes.SizeGene;
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
    public static TexButton gender;

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
    public void onPageOpened()
    {
        super.onPageOpened();

        if (shiny.active)
            shiny.setTooltip(Tooltip.create(Component.translatable("button.pokecube.pokewatch.shiny.tooltip")));
        else shiny.setTooltip(Tooltip.create(Component.literal("")));

        if (formChanger.active)
            formChanger.setTooltip(Tooltip.create(Component.translatable("button.pokecube.pokewatch.forms.tooltip")));
        else formChanger.setTooltip(Tooltip.create(Component.literal("")));

        if (gender.active)
            gender.setTooltip(Tooltip.create(Component.translatable("button.pokecube.pokewatch.gender.tooltip")));
        else gender.setTooltip(Tooltip.create(Component.literal("")));

        StartWatch.gender.active =
                !this.pokemob.getEntity().isAddedToLevel() && (this.pokemob.getSexe() == IPokemob.MALE
                        || this.pokemob.getSexe() == IPokemob.FEMALE);
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
    public void postPageDraw(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 - 5;
        int dx;
        int dy;

        // Draw Pokemob
        if (this.pokemob != null)
        {
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
            if (!fullColour && pokedexEntry.isMega()) fullColour =
                    StatsCollector.getCaptured(pokedexEntry.getBaseForme(), Minecraft.getInstance().player) > 0
                            || StatsCollector.getHatched(pokedexEntry.getBaseForme(), Minecraft.getInstance().player)
                            > 0;

            IPokemob pokemob = this.pokemob;
            // Copy the stuff to the render mob if this mob is in world
            if (pokemob.getEntity().isAddedToLevel())
            {
                pokemob = AnimationGui.getRenderMob(pokemob);
            }

            pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
            pokemob.setGeneralState(GeneralStates.EVOLVING, false);

            // Set colouring accordingly.
            if (fullColour) pokemob.setRGBA(255, 255, 255, 255);
            else if (stats.hasInspected(pokedexEntry)) pokemob.setRGBA(127, 127, 127, 255);
            else pokemob.setRGBA(15, 15, 15, 255);

            SizeGene.setScale(pokemob, 1);

            final float yaw = Util.getMillis() / 20f;
            dx = -15; //90
            dy = 65;

            // Draw the actual pokemob
            GuiPokemobHelper.renderMob(pokemob.getEntity(), x + dx, y + dy, 0, yaw, 0, yaw, 3.0F, partialTicks);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        if (this.current_page != null) this.current_page.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void prePageDraw(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        if (!this.watch.canEdit(this.pokemob))
        {
            final String name = PokecubePlayerDataHandler.getCustomDataTag(this.watch.player).getString("WEntry");
            if (!name.equals(this.pokemob.getPokedexEntry().getName()))
            {
                final PokedexEntry entry = this.pokemob.getPokedexEntry();
                final PokedexEntry newEntry = Database.getEntry(name);
                if (newEntry != null && newEntry != entry)
                {
                    this.pokemob = AnimationGui.getRenderMob(newEntry);
                    this.initPages(this.pokemob);
                }
            }
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
        this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""),
                b -> this.watch.player.playSound(this.pokemob.getSound(), 0.5f, 1.0F)).bounds(x - 78, y + 95, 12, 12)
                .setRender(new UVImgRender(229, 72, 12, 12)).setTexture(GuiPokeWatch.getWidgetTex())
                .tooltip(Tooltip.create(Component.translatable("button.pokecube.pokewatch.sound.tooltip")))
                .createNarration(supplier -> Component.translatable("button.pokecube.pokewatch.sound.narrate"))
                .build());

        // Shiny Button
        shiny = this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            if (this.pokemob.getPokedexEntry().hasShiny && !this.pokemob.getEntity().isAddedToLevel())
            {
                this.pokemob.setShiny(!this.pokemob.isShiny());
            }
        }).bounds(x - 65, y + 95, 12, 12).setRender(new UVImgRender(241, 36, 12, 12))
                .setTexture(GuiPokeWatch.getWidgetTex())
                .createNarration(supplier -> Component.translatable("button.pokecube.pokewatch.shiny.narrate"))
                .build());

        shiny.active = this.pokemob.getPokedexEntry().hasShiny && !this.pokemob.getEntity().isAddedToLevel();

        // Change Forms Button
        formChanger = this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            if (this.pokemob.getEntity().isAddedToLevel()) return;
            PokedexEntry entry = this.pokemob.getPokedexEntry();
            PokedexEntry nextEntry = Pokedex.getInstance().getNextForm(entry);
            if (nextEntry == entry) nextEntry = Pokedex.getInstance().getFirstForm(entry);
            this.pokemob.setPokedexEntry(nextEntry);
            this.pokemob.setBasePokedexEntry(nextEntry);
            this.initPages(this.pokemob);
        }).bounds(x - 52, y + 95, 12, 12).setRender(new UVImgRender(241, 72, 12, 12))
                .setTexture(GuiPokeWatch.getWidgetTex())
                .createNarration(supplier -> Component.translatable("button.pokecube.pokewatch.forms.narrate"))
                .build());

        PokedexEntry entry = this.pokemob.getPokedexEntry();
        PokedexEntry firstEntry = Pokedex.getInstance().getFirstForm(entry);
        PokedexEntry nextEntry = Pokedex.getInstance().getNextForm(entry);
        PokedexEntry previousEntry = Pokedex.getInstance().getPreviousForm(entry);
        formChanger.active =
                (firstEntry != nextEntry && previousEntry != firstEntry) && !this.pokemob.getEntity().isAddedToLevel();

        // Gender Button
        Component genderText = TComponent.literal("");
        if (this.pokemob.getSexe() == IPokemob.MALE)
        {
            genderText = TComponent.literal("♂");
        }
        else if (this.pokemob.getSexe() == IPokemob.FEMALE)
        {
            genderText = TComponent.literal("♀");
        }

        StartWatch.gender = this.addRenderableWidget(new TexButton.Builder(genderText, b -> {
            var old = this.pokemob.getPokedexEntry();
            var e = old;
            switch (this.pokemob.getSexe())
            {
            case IPokemob.MALE:
                e = old.getForGender(IPokemob.FEMALE);
                this.pokemob.setSexe(IPokemob.FEMALE);
                b.setMessage(TComponent.literal("♀"));
                b.setFGColor(ChatFormatting.DARK_RED.getColor());
                if (e != old)
                {
                    this.pokemob.setPokedexEntry(e);
                    this.pokemob.setBasePokedexEntry(e);
                }
                this.initPages(this.pokemob);
                break;
            case IPokemob.FEMALE:
                e = old.getForGender(IPokemob.MALE);
                this.pokemob.setSexe(IPokemob.MALE);
                b.setMessage(TComponent.literal("♂"));
                b.setFGColor(ChatFormatting.DARK_BLUE.getColor());
                if (e != old)
                {
                    this.pokemob.setPokedexEntry(e);
                    this.pokemob.setBasePokedexEntry(e);
                }
                this.initPages(this.pokemob);
                break;
            }
        }).bounds(x - 39, y + 95, 12, 12).setRender(new UVImgRender(200, 0, 12, 12))
                .setTexture(GuiPokeWatch.getWidgetTex())
                .createNarration(supplier -> Component.translatable("button.pokecube.pokewatch.gender.narrate"))
                .build());

        StartWatch.gender.active =
                !this.pokemob.getEntity().isAddedToLevel() && (this.pokemob.getSexe() == IPokemob.MALE
                        || this.pokemob.getSexe() == IPokemob.FEMALE);
        if (this.pokemob.getSexe() == IPokemob.MALE) gender.setFGColor(ChatFormatting.DARK_BLUE.getColor());
        else if (this.pokemob.getSexe() == IPokemob.FEMALE) gender.setFGColor(ChatFormatting.DARK_RED.getColor());
    }
}
