package pokecube.adventures.client.gui.trainer.editor;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
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

import java.util.List;

public class EditorGui extends Screen
{
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
        super(Component.literal(""));
        this.entity = mob;
        this.trainer = TrainerCaps.getHasPokemobs(mob);
        this.rewards = TrainerCaps.getHasRewards(mob);
        this.messages = TrainerCaps.getMessages(mob);
        this.aiStates = TrainerCaps.getNPCAIStates(mob);
        this.pokemob = PokemobCaps.getPokemobFor(mob);
        if (this.entity != null) this.guard = CapHolders.getGuardAI(entity);
        else this.guard = null;

        this.renderables.add((graphics, mouseX, mouseY, partialTicks) -> {
            try
            {
                this.current_page.render(graphics, mouseX, mouseY, partialTicks);
            }
            catch (final Exception e)
            {
                this.handleError(e);
            }
        });
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        final int j2 = (this.width - 256) / 2;
        final int k2 = (this.height - 160) / 2;
        this.renderBlurredBackground(partialTick);
        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(PokecubeAdv.MODID, "textures/gui/traineredit.png"), j2,
                k2, 0, 0, 256, 160);
    }

    @Override
    public void init()
    {
        EditorGui.lastPage = 0;
        super.init();
        if (this.current_page != null)
        {
            this.children.remove(this.current_page);
            this.current_page.onPageClosed();
        }
        // Here we just init current, it will then decide on what to do.
        this.current_page = this.createPage(EditorGui.lastPage);
        this.current_page.init(mc, width, height);
        this.current_page.onPageOpened();
        this.children.add(this.current_page);
    }

    private void handleError(final Exception e)
    {
        if (this.current_page != null) PokecubeAPI.LOGGER.warn("Error with page {}", this.current_page.getTitle(), e);
        else
        {
            PokecubeAPI.LOGGER.warn("Error with null page", e);
            return;
        }
        if (this.current_page != null)
        {
            this.children.remove(this.current_page);
            this.current_page.onPageClosed();
        }
        this.current_page.init();
        this.current_page.onPageOpened();
        this.children.add(this.current_page);
    }

    public void changePage(final int newIndex)
    {
        if (newIndex == this.index) return;
        Page newPage = this.createPage(newIndex);
        if (!newPage.isValid())
        {
            return;
        }
        if (this.current_page != null)
        {
            this.children.remove(this.current_page);
            this.current_page.onPageClosed();
        }
        this.index = newIndex;
        this.current_page = newPage;
        this.current_page.init(this.minecraft, this.width, this.height);
        this.children.add(this.current_page);
    }

    public Page createPage(final int index)
    {
        if (this.entity == null) return new Spawn(this);
        if (this.pokemob != null) return new LivePokemob(this);
        return EditorGui.makePage(EditorGui.PAGELIST.get(index), this);
    }
}
