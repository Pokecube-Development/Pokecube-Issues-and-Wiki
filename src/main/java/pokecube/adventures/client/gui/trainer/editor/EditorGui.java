package pokecube.adventures.client.gui.trainer.editor;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.client.gui.trainer.editor.pages.AI;
import pokecube.adventures.client.gui.trainer.editor.pages.LivePokemob;
import pokecube.adventures.client.gui.trainer.editor.pages.Messages;
import pokecube.adventures.client.gui.trainer.editor.pages.Pokemob;
import pokecube.adventures.client.gui.trainer.editor.pages.Rewards;
import pokecube.adventures.client.gui.trainer.editor.pages.Spawn;
import pokecube.adventures.client.gui.trainer.editor.pages.Trainer;
import pokecube.adventures.client.gui.trainer.editor.pages.util.Page;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.trainers.IHasMessages;
import pokecube.api.entity.trainers.IHasNPCAIStates;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.IHasRewards;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.utils.CapHolders;
import thut.lib.TComponent;

public class EditorGui extends Screen
{
    public static class MissingPage extends Page
    {

        public MissingPage(final EditorGui watch)
        {
            super(TComponent.translatable("pokewatch.title.blank"), watch);
            this.font = Minecraft.getInstance().font;
        }

        @Override
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
        {
            final int x = (this.parent.width - 160) / 2 + 80;
            final int y = (this.parent.height - 160) / 2 + 70;
            graphics.drawCenteredString(this.font, I18n.get("pokewatch.title.blank"), x, y, 0xFFFFFFFF);
            super.render(graphics, mouseX, mouseY, partialTicks);
        }

    }

    public static final ResourceLocation TEXTURE = new ResourceLocation(PokecubeAdv.MODID,
            "textures/gui/traineredit.png");
    public static List<Class<? extends Page>> PAGELIST = Lists.newArrayList();

    static
    {
        // We start with this as it will be replaced based on why this gui is
        // opened.
        EditorGui.PAGELIST.add(Trainer.class);
        EditorGui.PAGELIST.add(AI.class);
        EditorGui.PAGELIST.add(Messages.class);
        EditorGui.PAGELIST.add(Pokemob.class);
        EditorGui.PAGELIST.add(Rewards.class);
    }

    public static int lastPage = 0;

    private static Page makePage(final Class<? extends Page> clazz, final EditorGui parent)
    {
        try
        {
            return clazz.getConstructor(EditorGui.class).newInstance(parent);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error with making a page for watch", e);
            return null;
        }
    }

    public Page current_page = null;

    public final Entity entity;
    public final Minecraft mc = Minecraft.getInstance();
    public final IGuardAICapability guard;
    public final IHasPokemobs trainer;
    public final IHasRewards rewards;
    public final IHasMessages messages;
    public final IHasNPCAIStates aiStates;
    public final IPokemob pokemob;
    public int index = 0;

    public EditorGui(final Entity mob)
    {
        super(TComponent.literal(""));
        this.entity = mob;
        this.trainer = TrainerCaps.getHasPokemobs(mob);
        this.rewards = TrainerCaps.getHasRewards(mob);
        this.messages = TrainerCaps.getMessages(mob);
        this.aiStates = TrainerCaps.getNPCAIStates(mob);
        this.pokemob = PokemobCaps.getPokemobFor(mob);
        if (this.entity != null) this.guard = this.entity.getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);
        else this.guard = null;
    }

    @Override
    public void init(final Minecraft mc, final int width, final int height)
    {
        this.children.clear();
        this.renderables.clear();
        super.init(mc, width, height);
        EditorGui.lastPage = 0;
        // Here we just init current, it will then decide on what to do.
        this.current_page = this.createPage(EditorGui.lastPage);
        this.current_page.init(mc, width, height);
        this.current_page.onPageOpened();
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(graphics, mouseX, mouseY, partialTicks);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderTexture(0, new ResourceLocation(PokecubeAdv.MODID, "textures/gui/traineredit.png"));

        final int j2 = (this.width - 256) / 2;
        final int k2 = (this.height - 160) / 2;
        // TODO: Check this
        graphics.blit(BACKGROUND_LOCATION, j2, k2, 0, 0, 256, 160);
        try
        {
            this.current_page.render(graphics, mouseX, mouseY, partialTicks);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.warn("Error with page " + this.current_page, e);
        }
    }

    boolean resizing = false;

    @Override
    public void init()
    {
        super.init();
    }

    @Override
    public void resize(final Minecraft minecraft, final int width, final int height)
    {
        this.resizing = true;
        super.resize(minecraft, width, height);
        this.init(this.mc, width, height);
    }

    public void changePage(final int newIndex)
    {
        if (newIndex == this.index) return;
        Page newPage = this.createPage(newIndex);
        if (!newPage.isValid())
        {
            return;
        }
        if (this.current_page != null) this.current_page.onPageClosed();
        this.index = newIndex;
        this.current_page = newPage;
        this.current_page.init(this.minecraft, this.width, this.height);
        this.current_page.onPageOpened();

    }

    public Page createPage(final int index)
    {
        if (this.entity == null) return new Spawn(this);
        if (this.pokemob != null) return new LivePokemob(this);
        return EditorGui.makePage(EditorGui.PAGELIST.get(index), this);
    }

}
