package pokecube.core.client.gui.watch;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
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

    long lastClick = 0;

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton)
    {
        final boolean ret = super.mouseClicked(mouseX, mouseY, mouseButton);

        // Implement some flood control for this.
        if (System.currentTimeMillis() - this.lastClick < 30) return ret;
        this.lastClick = System.currentTimeMillis();

        // change gender if clicking on the gender, and shininess otherwise
        if (!this.watch.canEdit(this.pokemob))
        {
            // If it is actually a real mob, swap it out for the fake one.
            if (this.pokemob.getEntity().isAddedToWorld()) this.pokemob = AnimationGui.getRenderMob(this.pokemob);

            final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
            final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;
            final int mx = (int) (mouseX - x);
            final int my = (int) (mouseY - y);

            // The box to click goes from (ox, oy) -> (ox + dx, oy + dy)
            int ox = 10;
            int oy = 29;
            int dx = 14;
            int dy = 14;

            // Click for toggling if it is male or female
            if (mx > ox && mx < ox + dx && my > oy && my < oy + dy)
            {
                switch (this.pokemob.getSexe())
                {
                case IPokemob.MALE:
                    this.pokemob.setSexe(IPokemob.FEMALE);
                    break;
                case IPokemob.FEMALE:
                    this.pokemob.setSexe(IPokemob.MALE);
                    break;
                }
                this.pokemob.onGenesChanged();
                return ret;
            }
        }
        return ret;
    }

    @Override
    protected int pageCount()
    {
        return StartWatch.PAGELIST.size();
    }

    @Override
    public void postPageDraw(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {}

    @SuppressWarnings("resource")
	@Override
    public void prePageDraw(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        if (this.current_page != null) this.current_page.renderBackground(graphics);
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
            dx = -6; //90
            dy = 65;

            // Draw the actual pokemob
            GuiPokemobHelper.renderMob(pokemob.getEntity(), x + dx, y + dy, 0, yaw, 0, yaw, 3.3f, partialTicks);

            // Draw gender
            int genderColor = 0xBBBBBB;
            String gender = "";
            if (pokemob.getSexe() == IPokemob.MALE)
            {
                genderColor = 0x0011CC;
                gender = "\u2642";
            }
            else if (pokemob.getSexe() == IPokemob.FEMALE)
            {
                genderColor = 0xCC5555;
                gender = "\u2640";
            }
            dx = -73;
            dy = 40;
            graphics.drawString(this.font, gender, x + dx, y + dy, genderColor, false);
        }
    }

    @Override
    public void preSubOpened()
    {
        this.children().clear();
        this.initPages(this.pokemob);
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;

        //Shiny Button
       this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
        	if (this.pokemob.getPokedexEntry().hasShiny) {
        		this.pokemob.setShiny(!this.pokemob.isShiny());
        		this.pokemob.onGenesChanged();
        	}
        }).bounds(x - 78, y + 93, 12, 12).setTexture(GuiPokeWatch.getWidgetTex())
    		   .setRender(new UVImgRender(241, 36, 12, 12)).build());
       
       this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
           this.watch.player.playSound(this.pokemob.getSound(), 0.5f, 1.0F);
       }).bounds(x - 78, y + 79, 12, 12).setTexture(GuiPokeWatch.getWidgetTex())
    		   .setRender(new UVImgRender(229, 72, 12, 12)).build());
       
       this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b ->
               {
                   GuiPokeWatch.nightMode = !GuiPokeWatch.nightMode;
                   this.watch.init();
        }).bounds(x + 137, y + 7, 17, 17).setTexture(GuiPokeWatch.getWidgetTex())
    		   .setRender(new UVImgRender(110, 72, 17, 17)).build());
    }
}
