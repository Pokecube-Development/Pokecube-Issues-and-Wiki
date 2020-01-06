package pokecube.core.client.gui.watch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.GuiPokedex;
import pokecube.core.client.gui.watch.pokemob.Breeding;
import pokecube.core.client.gui.watch.pokemob.Description;
import pokecube.core.client.gui.watch.pokemob.Moves;
import pokecube.core.client.gui.watch.pokemob.PokeInfoPage;
import pokecube.core.client.gui.watch.pokemob.Spawns;
import pokecube.core.client.gui.watch.pokemob.StatsInfo;
import pokecube.core.client.gui.watch.util.PageWithSubPages;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.PokeType;
import thut.core.common.handlers.PlayerDataHandler;

public class PokemobInfoPage extends PageWithSubPages<PokeInfoPage>
{
    public static int savedIndex = 0;

    public static List<Class<? extends PokeInfoPage>> PAGELIST = Lists.newArrayList();

    static
    {
        PokemobInfoPage.PAGELIST.add(StatsInfo.class);
        PokemobInfoPage.PAGELIST.add(Moves.class);
        PokemobInfoPage.PAGELIST.add(Spawns.class);
        PokemobInfoPage.PAGELIST.add(Breeding.class);
        PokemobInfoPage.PAGELIST.add(Description.class);
    }

    private static PokeInfoPage makePage(final Class<? extends PokeInfoPage> clazz, final PokemobInfoPage parent)
    {
        try
        {
            return clazz.getConstructor(PokemobInfoPage.class).newInstance(parent);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with making a page for watch", e);
            return null;
        }
    }

    public IPokemob pokemob;
    IPokemob        renderMob;
    TextFieldWidget search;

    public PokemobInfoPage(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent("pokewatch.title.pokeinfo"), watch);
        this.pokemob = watch.pokemob;
    }

    @Override
    public boolean charTyped(final char typedChar, final int keyCode)
    {
        final boolean typed = super.charTyped(typedChar, keyCode);
        if (this.search.isFocused()) if (keyCode == GLFW.GLFW_KEY_TAB)
        {
            String text = this.search.getText();
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
            Collections.sort(ret, (o1, o2) ->
            {
                if (o1.startsWith("'") && !o2.startsWith("'")) return 1;
                else if (o2.startsWith("'") && !o1.startsWith("'")) return -1;
                return o1.compareToIgnoreCase(o2);
            });
            ret.replaceAll(t ->
            {
                if (t.startsWith("'") && t.endsWith("'")) t = t.substring(1, t.length() - 1);
                return t;
            });
            // TODO Tab completetion
            // final String[] args = { text };
            // ret = CommandBase.getListOfStringsMatchingLastWord(args,
            // ret);
            // if (!ret.isEmpty()) this.search.setText(ret.get(0));
            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            PokedexEntry entry = this.pokemob.getPokedexEntry();
            final PokedexEntry newEntry = Database.getEntry(this.search.getText());
            // Search to see if maybe it was a translated name put into the
            // search.
            if (newEntry == null)
            {
                for (final PokedexEntry e : Database.getSortedFormes())
                {
                    final String translated = I18n.format(e.getUnlocalizedName());
                    if (translated.equalsIgnoreCase(this.search.getText()))
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
                this.search.setText(newEntry.getName());
                this.pokemob = EventsHandlerClient.getRenderMob(newEntry, this.watch.player.getEntityWorld());
                this.initPages(this.pokemob);
            }
            else this.search.setText(entry.getName());
            return true;
        }
        return typed;
    }

    @Override
    protected PokeInfoPage createPage(final int index)
    {
        return PokemobInfoPage.makePage(PokemobInfoPage.PAGELIST.get(index), this);
    }

    @Override
    public void init()
    {
        super.init();
        final int x = this.watch.width / 2 - 70;
        final int y = this.watch.height / 2 + 53;
        this.search = new TextFieldWidget(this.font, x, y, 140, 10, "");
        this.addButton(this.search);
        this.index = PokemobInfoPage.savedIndex;
    }

    public void initPages(IPokemob pokemob)
    {
        if (pokemob == null)
        {
            final String name = PokecubePlayerDataHandler.getCustomDataTag(this.watch.player).getString("WEntry");
            PokedexEntry entry = Database.getEntry(name);
            if (entry == null) entry = Pokedex.getInstance().getFirstEntry();
            pokemob = EventsHandlerClient.getRenderMob(entry, this.watch.player.getEntityWorld());
        }
        this.pokemob = pokemob;
        this.renderMob = pokemob;
        this.search.setVisible(!this.watch.canEdit(pokemob));
        this.search.setText(pokemob.getPokedexEntry().getName());
        PacketPokedex.sendSpecificSpawnsRequest(pokemob.getPokedexEntry());
        PacketPokedex.updateWatchEntry(pokemob.getPokedexEntry());
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton)
    {
        final boolean ret = super.mouseClicked(mouseX, mouseY, mouseButton);
        // change gender if clicking on the gender, and shininess otherwise
        if (!new Exception().getStackTrace()[2].getClassName().equals(
                "pokecube.core.client.gui.watch.util.PageWithSubPages")) if (!this.watch.canEdit(this.pokemob))
        {
            // If it is actually a real mob, swap it out for the fake one.
            if (this.pokemob.getEntity().addedToChunk) this.pokemob = this.renderMob = EventsHandlerClient.getRenderMob(
                    this.pokemob.getPokedexEntry(), this.watch.player.getEntityWorld());

            final int x = (this.watch.width - 160) / 2 + 80;
            final int y = (this.watch.height - 160) / 2 + 8;
            final int mx = (int) (mouseX - x);
            final int my = (int) (mouseY - y);
            if (mx > -43 && mx < -43 + 76 && my > 42 && my < 42 + 7) switch (this.pokemob.getSexe())
            {
            case IPokemob.MALE:
                this.pokemob.setSexe(IPokemob.FEMALE);
                break;
            case IPokemob.FEMALE:
                this.pokemob.setSexe(IPokemob.MALE);
                break;
            }
            else if (mx > -75 && mx < -75 + 40 && my > 10 && my < 10 + 40) if (this.pokemob.getPokedexEntry().hasShiny)
                this.pokemob.setShiny(!this.pokemob.isShiny());
        }
        return ret;
    }

    @Override
    protected int pageCount()
    {
        return PokemobInfoPage.PAGELIST.size();
    }

    @Override
    public void postPageDraw(final int mouseX, final int mouseY, final float partialTicks)
    {
        final int x = (this.watch.width - 160) / 2 + 80;
        final int y = (this.watch.height - 160) / 2 + 8;
        if (this.pokemob != null)
        {
            // Draw hovored tooltip with pokemob's name
            int mx = mouseX - x;
            int my = mouseY - y;
            if (mx > -75 && mx < -75 + 40) if (my > 10 && my < 10 + 40)
            {
                final List<String> text = Lists.newArrayList();
                text.add(this.pokemob.getPokedexEntry().getTranslatedName());
                if (!this.pokemob.getPokemonNickname().isEmpty()) text.add("\"" + this.pokemob.getPokemonNickname()
                        + "\"");
                GlStateManager.disableDepthTest();
                mx = -35;
                my = 20;
                final int dy = this.font.FONT_HEIGHT;
                int box = 0;
                for (final String s : text)
                    box = Math.max(box, this.font.getStringWidth(s) + 2);

                AbstractGui.fill(x + mx - 1, y + my - 1, x + mx + box + 1, y + my + dy * text.size() + 1, 0xFF78C850);
                for (final String s : text)
                {
                    AbstractGui.fill(x + mx, y + my, x + mx + box, y + my + dy, 0xFF000000);
                    this.font.drawString(s, x + mx + 1, y + my, 0xFFFFFFFF);
                    my += dy;
                }
                GlStateManager.enableDepthTest();
            }
        }
    }

    @Override
    public void prePageDraw(final int mouseX, final int mouseY, final float partialTicks)
    {
        if (!this.watch.canEdit(this.pokemob))
        {
            final String name = PokecubePlayerDataHandler.getCustomDataTag(this.watch.player).getString("WEntry");
            if (!name.equals(this.pokemob.getPokedexEntry().getName()))
            {
                this.search.setText(name);
                final PokedexEntry entry = this.pokemob.getPokedexEntry();
                final PokedexEntry newEntry = Database.getEntry(this.search.getText());
                if (newEntry != null)
                {
                    this.search.setText(newEntry.getName());
                    this.pokemob = EventsHandlerClient.getRenderMob(newEntry, this.watch.player.getEntityWorld());
                    this.initPages(this.pokemob);
                }
                else this.search.setText(entry.getName());
            }
        }

        final int x = (this.watch.width - 160) / 2 + 80;
        final int y = (this.watch.height - 160) / 2 + 8;
        this.drawCenteredString(this.font, this.getTitle().getFormattedText(), x, y, 0xFF78C850);
        this.drawCenteredString(this.font, this.current_page.getTitle().getFormattedText(), x, y + 10, 0xFF78C850);
        int dx = -76;
        int dy = 10;
        int dr = 40;
        int colour = 0xFF78C850;

        // Draw a box around where pokemob displays
        this.vLine(x + dx, y + dy, y + dy + dr, colour);
        this.vLine(x + dx + dr, y + dy, y + dy + dr, colour);
        this.hLine(x + dx, x + dx + dr, y + dy, colour);
        this.hLine(x + dx, x + dx + dr, y + dy + dr, colour);

        // Draw Pokemob
        if (this.pokemob != null)
        {

            // Draw the icon indicating capture/inspect status.
            this.minecraft.getTextureManager().bindTexture(GuiPokeWatch.TEXTURE);
            final PokedexEntry pokedexEntry = this.pokemob.getPokedexEntry();
            final PokecubePlayerStats stats = PlayerDataHandler.getInstance().getPlayerData(Minecraft
                    .getInstance().player).getData(PokecubePlayerStats.class);
            boolean fullColour = StatsCollector.getCaptured(pokedexEntry, Minecraft.getInstance().player) > 0
                    || StatsCollector.getHatched(pokedexEntry, Minecraft.getInstance().player) > 0
                    || this.minecraft.player.abilities.isCreativeMode;

            // Megas Inherit colouring from the base form.
            if (!fullColour && pokedexEntry.isMega) fullColour = StatsCollector.getCaptured(pokedexEntry.getBaseForme(),
                    Minecraft.getInstance().player) > 0 || StatsCollector.getHatched(pokedexEntry.getBaseForme(),
                            Minecraft.getInstance().player) > 0;

            // Select which box to draw via position.
            if (fullColour) dr = 0;
            else if (stats.hasInspected(pokedexEntry)) dr = 9;
            else dr = 18;
            GL11.glColor3f(1, 1, 1);
            dx = -75;
            dy = 11;
            // Draw the box.
            this.blit(x + dx, y + dy, dr, 247, 9, 9);

            IPokemob pokemob = this.renderMob;
            // Copy the stuff to the render mob if this mob is in world
            if (pokemob.getEntity().addedToChunk)
            {
                final IPokemob newMob = EventsHandlerClient.getRenderMob(pokemob.getPokedexEntry(), pokemob.getEntity()
                        .getEntityWorld());
                newMob.read(pokemob.write());
                pokemob = this.renderMob = newMob;
            }

            if (!pokemob.getEntity().addedToChunk)
            {
                final LivingEntity player = this.watch.player;
                EntityTools.copyEntityTransforms(pokemob.getEntity(), player);
            }

            dx = -35;
            dy = -55;
            // Draw the actual pokemob
            GuiPokedex.renderMob(pokemob.getEntity(), this.minecraft, dx, dy, 0.75f, this.watch.height,
                    this.watch.width, 160, 160, 0, 45, -45);
            // Draw gender, types and lvl
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
            final String level = "L. " + pokemob.getLevel();
            dx = -74;
            dy = 42;
            this.drawString(this.font, level, x + dx, y + dy, 0xffffff);
            dx = -40;
            this.drawCenteredString(this.font, gender, x + dx, y + dy, genderColor);
            pokemob.getType1();
            final String type1 = PokeType.getTranslatedName(pokemob.getType1());
            final String type2 = PokeType.getTranslatedName(pokemob.getType2());
            dx = -74;
            dy = 52;
            colour = pokemob.getType1().colour;
            this.drawString(this.font, type1, x + dx, y + dy, colour);
            colour = pokemob.getType2().colour;
            dy = 62;
            if (pokemob.getType2() != PokeType.unknown) this.drawString(this.font, type2, x + dx, y + dy, colour);

            // Draw box around where type displays
            dx = -76;
            dy = 50;
            dr = 20;
            colour = 0xFF78C850;
            this.vLine(x + dx, y + dy, y + dy + dr, colour);
            this.vLine(x + dx + 2 * dr, y + dy, y + dy + dr, colour);
            dr = 40;
            this.hLine(x + dx, x + dx + dr, y + dy + dr / 2, colour);

        }
    }

    @Override
    public void preSubOpened()
    {
        this.initPages(this.pokemob);
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;
        final String next = ">";
        final String prev = "<";
        this.addButton(new Button(x + 64, y - 70, 12, 12, next, b ->
        {
            this.changePage(this.index + 1);
            PokemobInfoPage.savedIndex = this.index;
        }));
        this.addButton(new Button(x - 76, y - 70, 12, 12, prev, b ->
        {
            this.changePage(this.index - 1);
            PokemobInfoPage.savedIndex = this.index;
        }));
    }
}
