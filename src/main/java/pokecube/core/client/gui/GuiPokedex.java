/**
 *
 */
package pokecube.core.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.PokeType;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.StatsCollector;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.Resources;
import thut.core.common.handlers.PlayerDataHandler;
import thut.lib.TComponent;

public class GuiPokedex extends Screen
{
    public static PokedexEntry pokedexEntry = null;

    public IPokemob pokemob = null;
    protected Player PlayerEntity = null;
    protected ScrollGui<LineEntry> list;
    protected EditBox pokemobTextField;
    /** The X size of the inventory window in pixels. */
    protected int xSize;

    /** The Y size of the inventory window in pixels. */
    protected int ySize;
    int prevX = 0;

    int prevY = 0;
    TexButton soundButton;
    TexButton upButton;
    TexButton downButton;
    TexButton prevButton;
    TexButton nextButton;

    public GuiPokedex(final IPokemob pokemob, final Player PlayerEntity)
    {
        super(TComponent.translatable("pokecube.pokedex.gui"));
        this.xSize = 256;
        this.ySize = 192;
        this.pokemob = pokemob;
        this.PlayerEntity = PlayerEntity;

        if (pokemob != null) GuiPokedex.pokedexEntry = pokemob.getPokedexEntry();
        else
        {
            final String name = PokecubePlayerDataHandler.getCustomDataTag(PlayerEntity).getString("WEntry");
            GuiPokedex.pokedexEntry = Database.getEntry(name);
            if (GuiPokedex.pokedexEntry == null) GuiPokedex.pokedexEntry = Pokedex.getInstance().getFirstEntry();
        }
    }

    @Override
    public boolean charTyped(final char par1, final int par2)
    {
        if (Screen.hasAltDown()) return false;
        return super.charTyped(par1, par2);
    }

    @Override
    public void init()
    {
        super.init();

        final int yOffset = this.height / 2 + 1;
        final int xOffset = this.width / 2;

        this.pokemobTextField = new EditBox(this.font, xOffset - 60, yOffset + 40, 103, 12, TComponent.literal(""));
        this.pokemobTextField.setBordered(false);
        this.pokemobTextField.setEditable(true);

        // Play Sound Button
        this.soundButton = this.addRenderableWidget(new TexButton(xOffset - 122, yOffset + 66, 16, 18, TComponent.literal(""), b -> {
            float volume = 1F;
            this.minecraft.player.playSound(GuiPokedex.pokedexEntry.getSoundEvent(), volume, 1.0F);
        }).setTex(Resources.WIDGETS_POKEDEX).setRender(new TexButton.UVImgRender(0, 0, 16, 18)));

        // Previous Button
        this.prevButton = this.addRenderableWidget(new TexButton(xOffset - 33, yOffset + 62, 10, 18, TComponent.literal(""), b -> {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getPrevious(GuiPokedex.pokedexEntry, 1);
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }).setTex(Resources.WIDGETS_POKEDEX).setRender(new TexButton.UVImgRender(16, 0, 10, 18)));

        // Next Button
        this.nextButton = this.addRenderableWidget(new TexButton(xOffset - 19, yOffset + 62, 10, 18, TComponent.literal(""), b -> {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getNext(GuiPokedex.pokedexEntry, 1);
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }).setTex(Resources.WIDGETS_POKEDEX).setRender(new TexButton.UVImgRender(26, 0, 10, 18)));

        // Down Button
        this.downButton = this.addRenderableWidget(new TexButton(xOffset - 25, yOffset + 70, 8, 18, TComponent.literal(""), b -> {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getPrevious(GuiPokedex.pokedexEntry, 10);
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }).setTex(Resources.WIDGETS_POKEDEX).setRender(new TexButton.UVImgRender(44, 0, 8, 18)));

        // Up Button
        this.upButton = this.addRenderableWidget(new TexButton(xOffset - 25, yOffset + 58, 8, 12, TComponent.literal(""), b -> {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getNext(GuiPokedex.pokedexEntry, 10);
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }).setTex(Resources.WIDGETS_POKEDEX).setRender(new TexButton.UVImgRender(36, 0, 8, 12)));

        if (GuiPokedex.pokedexEntry != null)
            this.pokemobTextField.setValue(I18n.get(GuiPokedex.pokedexEntry.getUnlocalizedName()));
        this.addRenderableWidget(this.pokemobTextField);
        this.initList();
    }

    private void initList()
    {
        if (this.list != null) this.children.remove(this.list);
        final int offsetX = (this.width - 160) / 2 + 92;
        final int offsetY = (this.height - 160) / 2 + 22;
        final int height = 15 * this.font.lineHeight;

        this.list = new ScrollGui<LineEntry>(this, this.minecraft, 108, height, this.font.lineHeight, offsetX, offsetY + 1)
                .setScrollBarColor(255, 12, 53)
                .setScrollBarDarkBorder(107, 6, 24)
                .setScrollBarGrayBorder(193, 9, 43)
                .setScrollBarLightBorder(255, 150, 169)
                .setScrollColor(193, 9, 43)
                .setScrollDarkBorder(107, 6, 24)
                .setScrollLightBorder(255, 150, 169);

        MutableComponent page;
        String key = "entity.pokecube." + GuiPokedex.pokedexEntry.getTrimmedName() + ".dexDesc";
        page = TComponent.translatable(key);
        // No description
        if (page.getString().equals(key))
        {
            // Check if we have a base form
            if (GuiPokedex.pokedexEntry.generated)
            {
                key = "entity.pokecube." + GuiPokedex.pokedexEntry.getBaseForme().getTrimmedName() + ".dexDesc";
                page = TComponent.translatable(key);
            }
            else page = TComponent.literal("");
        }
        this.pokemobTextField.setValue(I18n.get(GuiPokedex.pokedexEntry.getUnlocalizedName()));
        var list = Lists.newArrayList(this.font.split(page, 98));
        if (page.getString().isBlank()) list.clear();
        list.add(TComponent.literal("").getVisualOrderText());
        var holder = this.pokemob != null ? this.pokemob.getCustomHolder() : null;
        page = pokedexEntry.getDescription(pokemob, holder);
        list.addAll(this.font.split(page, 98));

        final IClickListener listen = new IClickListener()
        {
            @Override
            public boolean handleClick(final Style component)
            {
                if (component != null)
                {
                    final ClickEvent clickevent = component.getClickEvent();
                    if (clickevent != null) if (clickevent.getAction() == Action.CHANGE_PAGE)
                    {
                        final PokedexEntry entry = Database.getEntry(clickevent.getValue());
                        if (entry != null)
                        {
                            GuiPokedex.pokedexEntry = entry;
                            GuiPokedex.this.initList();
                            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
                        }
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void handleHovor(final PoseStack mat, final Style component, final int x, final int y)
            {}
        };
        for (var line : list)
            this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, line, 0xFFFFFF).setClickListner(listen));

        this.children.add(this.list);
    }

    @Override
    public boolean keyPressed(final int key, final int unk1, final int unk2)
    {
        if (key == GLFW.GLFW_KEY_ENTER && this.pokemobTextField.isFocused())
        {
            PokedexEntry entry = Database.getEntry(this.pokemobTextField.getValue());
            if (entry == null)
            {
                for (final PokedexEntry e : Database.getSortedFormes())
                {
                    final String translated = I18n.get(e.getUnlocalizedName());
                    if (translated.equalsIgnoreCase(this.pokemobTextField.getValue()))
                    {
                        Database.data2.put(translated, e);
                        entry = e;
                        break;
                    }
                }
                // If the pokedex entry is not actually registered, use old
                // entry.
                if (Pokedex.getInstance().getIndex(entry) == null) entry = null;
            }
            if (entry != null)
            {
                GuiPokedex.pokedexEntry = entry;
                this.initList();
                PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
            }
            else this.pokemobTextField.setValue(I18n.get(GuiPokedex.pokedexEntry.getUnlocalizedName()));
        }
        else if (key == GLFW.GLFW_KEY_UP)
        {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getNext(GuiPokedex.pokedexEntry, 10);
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }
        else if (key == GLFW.GLFW_KEY_DOWN)
        {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getPrevious(GuiPokedex.pokedexEntry, 10);
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }
        else if (key == GLFW.GLFW_KEY_LEFT)
        {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getPrevious(GuiPokedex.pokedexEntry, 1);
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }
        else if (key == GLFW.GLFW_KEY_RIGHT)
        {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getNext(GuiPokedex.pokedexEntry, 1);
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }
        return super.keyPressed(key, unk1, unk2);
    }

    @Override
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float partialTick)
    {
        // Draw background
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderTexture(0, Resources.GUI_POKEDEX);
        final int j2 = (this.width - this.xSize) / 2;
        final int k2 = (this.height - this.ySize) / 2;
        this.blit(mat, j2, k2, 0, 0, this.xSize, this.ySize);

        // Draw mob
        final IPokemob renderMob = EventsHandlerClient.getRenderMob(GuiPokedex.pokedexEntry,
                this.PlayerEntity.getLevel());
        if (!renderMob.getEntity().isAddedToWorld())
            EntityTools.copyEntityTransforms(renderMob.getEntity(), this.PlayerEntity);

        final PokedexEntry pokedexEntry = renderMob.getPokedexEntry();
        final PokecubePlayerStats stats = PlayerDataHandler.getInstance().getPlayerData(Minecraft.getInstance().player)
                .getData(PokecubePlayerStats.class);
        boolean fullColour = StatsCollector.getCaptured(pokedexEntry, Minecraft.getInstance().player) > 0
                || StatsCollector.getHatched(pokedexEntry, Minecraft.getInstance().player) > 0
                || this.minecraft.player.getAbilities().instabuild;

        // Megas Inherit colouring from the base form.
        if (!fullColour && pokedexEntry.isMega())
            fullColour = StatsCollector.getCaptured(pokedexEntry.getBaseForme(), Minecraft.getInstance().player) > 0
                    || StatsCollector.getHatched(pokedexEntry.getBaseForme(), Minecraft.getInstance().player) > 0;
        // Set colouring accordingly.
        if (fullColour) renderMob.setRGBA(255, 255, 255, 255);
        else if (stats.hasInspected(pokedexEntry)) renderMob.setRGBA(127, 127, 127, 255);
        else renderMob.setRGBA(15, 15, 15, 255);

        GlStateManager._enableDepthTest();
        final float yaw = Util.getMillis() / 20;
        final float pitch = 0;
        final float hx = 0;
        final float hy = yaw;
        GuiPokemobHelper.renderMob(renderMob.getEntity(), j2 + 5, k2 + 50, pitch, yaw, hx, hy, 2.0F, partialTick);

        // Draw info about mob
        final int yOffset = this.height / 2 - 82;
        int xOffset = this.width / 2;
        final int nb = GuiPokedex.pokedexEntry != null ? GuiPokedex.pokedexEntry.getPokedexNb() : 0;
        final String pokemobNum = "#" + nb;
        final PokeType type1 = this.pokemob != null && GuiPokedex.pokedexEntry == this.pokemob.getPokedexEntry() ? this.pokemob.getType1()
                : GuiPokedex.pokedexEntry != null ? GuiPokedex.pokedexEntry.getType1() : PokeType.unknown;
        final PokeType type2 = this.pokemob != null && GuiPokedex.pokedexEntry == this.pokemob.getPokedexEntry() ? this.pokemob.getType2()
                : GuiPokedex.pokedexEntry != null ? GuiPokedex.pokedexEntry.getType2() : PokeType.unknown;
        GuiComponent.drawCenteredString(mat, this.font, pokemobNum, xOffset - 28 - pokemobNum.length()/2, yOffset + 8, 0xffffff);
        try
        {
            GuiComponent.drawCenteredString(mat, this.font, PokeType.getTranslatedName(type1), xOffset - 88,
                    yOffset + 140, type1.colour);
            GuiComponent.drawCenteredString(mat, this.font, PokeType.getTranslatedName(type2), xOffset - 44,
                    yOffset + 140, type2.colour);
        }
        catch (final Exception e)
        {}

        // Draw default gui stuff.
        final int length = this.font.width(this.pokemobTextField.getValue()) / 2;
        xOffset = this.width / 2 - 65;
        this.pokemobTextField.x = xOffset - length;
        super.render(mat, mouseX, mouseY, partialTick);

        // Draw description
        this.list.render(mat, mouseX, mouseY, partialTick);
    }
}
