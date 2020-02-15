/**
 *
 */
package pokecube.core.client.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.IHasCommands.Command;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackEntityHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackLocationHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackNothingHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.MoveIndexHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.MoveToHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.StanceHandler;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.MovesUtils.AbleStatus;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class GuiDisplayPokecubeInfo extends AbstractGui
{
    protected static int                 lightGrey  = 0xDDDDDD;
    public static int[]                  guiDims    = { 147, 42 };
    public static int[]                  targetDims = { 147, 42 };
    public static int[]                  teleDims   = { 147, 42 };
    public static GuiDisplayPokecubeInfo instance;

    public static int[] applyTransform(final String ref, final List<Integer> offsets, final int[] dims,
            final float targetSize)
    {
        final Minecraft minecraft = Minecraft.getInstance();

        final MainWindow res = minecraft.mainWindow;
        int w = offsets.get(0);
        int h = offsets.get(1);
        int x = 0;
        int y = 0;
        final int scaledWidth = res.getScaledWidth();
        final int scaledHeight = res.getScaledHeight();
        int dx = 1;
        int dy = 1;
        switch (ref)
        {
        case "top_left":
            GlStateManager.translatef(w, h, 0);
            GlStateManager.scalef(targetSize, targetSize, targetSize);
            break;
        case "middle_left":
            h = scaledHeight / 2 - h - dims[1];
            GlStateManager.translatef(w, h, 0);
            GlStateManager.scalef(targetSize, targetSize, targetSize);
            break;
        case "bottom_left":
            h = scaledHeight - h - dims[1];
            GlStateManager.translatef(w, h, 0);
            GlStateManager.scalef(targetSize, targetSize, targetSize);
            dy = -1;
            break;
        case "top_right":
            w = scaledWidth - w;
            h = Math.min(h + dims[1], scaledHeight);
            GlStateManager.translatef(w, h, 0);
            GlStateManager.scalef(targetSize, targetSize, targetSize);
            dx = -1;
            break;
        case "right_bottom":
            w = scaledWidth - w - dims[0];
            h = scaledHeight - h - dims[1];
            GlStateManager.translatef(w, h, 0);
            GlStateManager.scalef(targetSize, targetSize, targetSize);
            dx = -1;
            dy = -1;
            break;
        case "right_middle":
            w = scaledWidth - w - dims[0];
            h = scaledHeight / 2 - h - dims[1];
            GlStateManager.translatef(w, h, 0);
            GlStateManager.scalef(targetSize, targetSize, targetSize);
            dx = -1;
            dy = -1;
            break;
        case "bottom_middle":
            x = scaledWidth / 2 - w;
            y = scaledHeight;
            w = scaledWidth / 2 - w;
            h = scaledHeight - h - dims[1];
            GlStateManager.translatef(w, h, 0);
            h = (int) (-dims[1] / targetSize - offsets.get(1));
            w = 0;
            dx = -1;
            dy = -1;
            GlStateManager.scalef(targetSize, targetSize, targetSize);
            break;
        }
        final int[] ret = { x, y, w, h, dx, dy };
        return ret;
    }

    public static GuiDisplayPokecubeInfo instance()
    {
        if (GuiDisplayPokecubeInfo.instance == null) GuiDisplayPokecubeInfo.instance = new GuiDisplayPokecubeInfo();
        return GuiDisplayPokecubeInfo.instance;
    }

    public static float scale(final float scaled, final boolean apply)
    {
        if (PokecubeCore.getConfig().guiAutoScale) return 1;

        final Minecraft mc = Minecraft.getInstance();
        float scaleFactor = 1;
        final boolean flag = mc.getForceUnicodeFont();
        int i = mc.gameSettings.guiScale;
        final int scaledWidth = Minecraft.getInstance().mainWindow.getScaledWidth();
        final int scaledHeight = Minecraft.getInstance().mainWindow.getScaledHeight();
        if (i == 0) i = 1000;
        while (scaleFactor < i && scaledWidth / (scaleFactor + 1) >= 320 && scaledHeight / (scaleFactor + 1) >= 240)
            ++scaleFactor;

        if (flag && scaleFactor % 2 != 0 && scaleFactor != 1) --scaleFactor;
        float scaleFactor2 = 1;
        i = 1000;
        while (scaleFactor2 < i && scaledWidth / (scaleFactor2 + 1) >= 320 && scaledHeight / (scaleFactor2 + 1) >= 240)
            ++scaleFactor2;

        if (flag && scaleFactor2 % 2 != 0 && scaleFactor2 != 1) --scaleFactor2;
        scaleFactor2 *= scaled;
        if (apply) GL11.glScaled(scaleFactor2 / scaleFactor, scaleFactor2 / scaleFactor, scaleFactor2 / scaleFactor);
        return scaleFactor2;
    }

    public static void sendMoveIndexPacket(final IPokemob pokemob, final int moveIndex)
    {
        PacketCommand.sendCommand(pokemob, Command.CHANGEMOVEINDEX, new MoveIndexHandler((byte) moveIndex).setFromOwner(
                true));
    }

    protected FontRenderer fontRenderer;

    protected Minecraft minecraft;

    IPokemob[] arrayRet = new IPokemob[0];

    int refreshCounter = 0;

    int        indexPokemob     = 0;
    public int currentMoveIndex = 0;

    /**
     *
     */
    public GuiDisplayPokecubeInfo()
    {
        this.minecraft = Minecraft.getInstance();
        this.fontRenderer = this.minecraft.fontRenderer;
        if (GuiDisplayPokecubeInfo.instance != null) MinecraftForge.EVENT_BUS.unregister(
                GuiDisplayPokecubeInfo.instance);
        GuiDisplayPokecubeInfo.instance = this;
        MinecraftForge.EVENT_BUS.register(GuiDisplayPokecubeInfo.instance);
    }

    protected void draw(final RenderGameOverlayEvent.Post event)
    {
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderMoveMessages(event.getType()));
        if (this.indexPokemob > this.getPokemobsToDisplay().length)
        {
            this.refreshCounter = 0;
            this.indexPokemob = 0;
            this.arrayRet = this.getPokemobsToDisplay();
        }
        if (this.getPokemobsToDisplay().length == 0) return;
        if (this.indexPokemob >= this.getPokemobsToDisplay().length) this.indexPokemob = 0;
        if (this.fontRenderer == null) this.fontRenderer = this.minecraft.fontRenderer;
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderSelectedInfo());
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderTargetInfo());
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderTeleports());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void drawSelected(final GuiEvent.RenderSelectedInfo evt)
    {
        final int dir = PokecubeCore.getConfig().guiDown ? 1 : -1;
        final int nameOffsetX = dir == 1 ? 43 : 43;
        final int nameOffsetY = dir == 1 ? 0 : 23;
        final int movesOffsetX = 42;
        final int movesOffsetY = dir == 1 ? 22 : 10;
        final int mobOffsetX = 0;
        final int mobOffsetY = 0;
        final int hpOffsetX = 42;
        final int hpOffsetY = 13;
        final int xpOffsetX = 42;
        final int xpOffsetY = 20;
        final int statusOffsetX = 0;
        final int statusOffsetY = 27;
        final int confuseOffsetX = 12;
        final int confuseOffsetY = 1;
        GL11.glPushMatrix();
        GL11.glColor4f(1, 1, 1, 1);

        GuiDisplayPokecubeInfo.applyTransform(PokecubeCore.getConfig().guiRef, PokecubeCore.getConfig().guiPos,
                GuiDisplayPokecubeInfo.guiDims, (float) PokecubeCore.getConfig().guiSize);
        final int w = 0;// trans[0];
        int h = 0;// trans[1];
        final IPokemob pokemob = this.getCurrentPokemob();
        if (pokemob != null)
        {
            String displayName = pokemob.getDisplayName().getFormattedText();
            final int currentMoveIndex = pokemob.getMoveIndex();
            GlStateManager.setProfile(GlStateManager.Profile.PLAYER_SKIN);
            // Render HP
            this.minecraft.getTextureManager().bindTexture(Resources.GUI_BATTLE);
            this.blit(hpOffsetX + w, hpOffsetY + h, 43, 12, 92, 7);
            final float total = pokemob.getMaxHealth();
            float ratio = pokemob.getHealth() / total;
            final float f = 0.00390625F;
            final float f1 = 0.00390625F;
            float x = hpOffsetX + 1 + w;
            float y = hpOffsetY + 1 + h;
            float width = 92 * ratio;
            float height = 5;
            int u = 0;
            int v = 85;
            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(x + 0, y + height, this.blitOffset).tex(u * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + height, this.blitOffset).tex((u + width) * f, (v + height) * f1)
                    .endVertex();
            vertexbuffer.pos(x + width, y + 0, this.blitOffset).tex((u + width) * f, v * f1).endVertex();
            vertexbuffer.pos(x + 0, y + 0, this.blitOffset).tex(u * f, v * f1).endVertex();
            tessellator.draw();

            // Render XP
            this.blit(xpOffsetX + w, xpOffsetY + h, 43, 19, 92, 5);

            final int current = pokemob.getExp();
            final int level = pokemob.getLevel();
            final int prev = Tools.levelToXp(pokemob.getExperienceMode(), level);
            final int next = Tools.levelToXp(pokemob.getExperienceMode(), level + 1);
            final int levelDiff = next - prev;
            final int diff = current - prev;
            ratio = diff / (float) levelDiff;
            if (level == 100) ratio = 1;
            x = xpOffsetX + 1 + w;
            y = xpOffsetY + h;
            width = 92 * ratio;
            height = 2;
            u = 0;
            v = 97;
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(x + 0, y + height, this.blitOffset).tex(u * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + height, this.blitOffset).tex((u + width) * f, (v + height) * f1)
                    .endVertex();
            vertexbuffer.pos(x + width, y + 0, this.blitOffset).tex((u + width) * f, v * f1).endVertex();
            vertexbuffer.pos(x + 0, y + 0, this.blitOffset).tex(u * f, v * f1).endVertex();
            tessellator.draw();

            // Render Status
            final byte status = pokemob.getStatus();
            if (status != IMoveConstants.STATUS_NON)
            {
                int dv = 0;
                if ((status & IMoveConstants.STATUS_BRN) != 0) dv = 2 * 14;
                if ((status & IMoveConstants.STATUS_FRZ) != 0) dv = 1 * 14;
                if ((status & IMoveConstants.STATUS_PAR) != 0) dv = 3 * 14;
                if ((status & IMoveConstants.STATUS_PSN) != 0) dv = 4 * 14;
                this.blit(statusOffsetX + w, statusOffsetY + h, 0, 138 + dv, 15, 15);
            }
            if ((pokemob.getChanges() & IMoveConstants.CHANGE_CONFUSED) != 0)
            {
                GlStateManager.translatef(0, 0, 100);
                this.blit(confuseOffsetX + w, confuseOffsetY + h, 0, 211, 24, 16);
                GlStateManager.translatef(0, 0, -100);
            }

            // Render Name
            if (currentMoveIndex == 5) GL11.glColor4f(0.0F, 1.0F, 0.4F, 1.0F);
            this.minecraft.getTextureManager().bindTexture(Resources.GUI_BATTLE);
            this.blit(nameOffsetX + w, nameOffsetY + h, 44, 0, 90, 13);
            if (this.fontRenderer.getStringWidth(displayName) > 70) displayName = this.fontRenderer.trimStringToWidth(
                    displayName, 70);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.fontRenderer.drawString(displayName, nameOffsetX + 3 + w, nameOffsetY + 3 + h,
                    GuiDisplayPokecubeInfo.lightGrey);

            // Render level
            GL11.glColor4f(1.0F, 0.5F, 0.0F, 1.0F);
            this.fontRenderer.drawString("L." + level, nameOffsetX + 88 + w - this.fontRenderer.getStringWidth("L."
                    + level), nameOffsetY + 3 + h, GuiDisplayPokecubeInfo.lightGrey);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            // Draw number of pokemon
            this.minecraft.getTextureManager().bindTexture(Resources.GUI_BATTLE);
            final int n = this.getPokemobsToDisplay().length;
            final int num = this.fontRenderer.getStringWidth("" + n);
            this.blit(nameOffsetX + 89 + w, nameOffsetY + h, 0, 27, 15, 15);
            this.fontRenderer.drawString("" + n, nameOffsetX + 95 - num / 4 + w, nameOffsetY + 4 + h,
                    GuiDisplayPokecubeInfo.lightGrey);

            // Render Moves
            // GlStateManager.setProfile(GlStateManager.Profile.PLAYER_SKIN);
            final int h1 = 1;
            int moveIndex = 0;
            int moveCount = 0;
            for (moveCount = 0; moveCount < 4; moveCount++)
                if (pokemob.getMove(moveCount) == null) break;
            if (dir == -1) h -= 14 + 12 * (moveCount - 1) - (4 - moveCount) * 2;
            for (moveIndex = 0; moveIndex < 4; moveIndex++)
            {
                final int index = moveIndex;

                final Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(index));
                final boolean disabled = index >= 0 && index < 4 && pokemob.getDisableTimer(index) > 0;
                if (move != null)
                {// TODO find out why both needed
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    // bind texture
                    GL11.glPushMatrix();
                    this.minecraft.getTextureManager().bindTexture(Resources.GUI_BATTLE);
                    this.blit(movesOffsetX + w, movesOffsetY + 13 * index + h, 43, 21 + h1, 91, 13);

                    // Render colour overlays.
                    if (currentMoveIndex == index)
                    {
                        // Draw selected indictator
                        GL11.glColor4f(0F, 1F, 1F, 0.5F);
                        this.blit(movesOffsetX + w, movesOffsetY + 13 * index + h, 43, 21 + h1, 91, 13);
                        GL11.glColor4f(0F, 1.0F, 1.0F, 1.0F);
                        // Draw cooldown box
                        float timer = 1;
                        Move_Base lastMove;
                        if (MovesUtils.isAbleToUseMoves(pokemob) != AbleStatus.ABLE) timer = 0;
                        else if ((lastMove = MovesUtils.getMoveFromName(pokemob.getLastMoveUsed())) != null)
                            timer -= pokemob.getAttackCooldown() / (float) MovesUtils.getAttackDelay(pokemob, pokemob
                                    .getLastMoveUsed(), (lastMove.getAttackCategory()
                                            & IMoveConstants.CATEGORY_DISTANCE) > 0, false);
                        timer = Math.max(0, Math.min(timer, 1));
                        GL11.glColor4f(0F, 0.1F, 1.0F, 0.5F);
                        this.blit(movesOffsetX + w, movesOffsetY + 13 * index + h, 43, 21 + h1, (int) (91 * timer), 13);
                        GL11.glColor4f(0F, 1.0F, 1.0F, 1.0F);
                    }
                    if (disabled)
                    {
                        GL11.glColor4f(1F, 0.0F, 0.0F, 0.5F);
                        this.blit(movesOffsetX + w, movesOffsetY + 13 * index + h, 43, 21 + h1, 91, 13);
                        GL11.glColor4f(0F, 1.0F, 1.0F, 1.0F);
                    }

                    GL11.glPopMatrix();
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glPushMatrix();
                    final Color moveColor = new Color(move.getType(pokemob).colour);
                    GL11.glColor4f(moveColor.getRed() / 255f, moveColor.getGreen() / 255f, moveColor.getBlue() / 255f,
                            1.0F);
                    this.fontRenderer.drawString(MovesUtils.getMoveName(move.getName()).getFormattedText(), 5
                            + movesOffsetX + w, index * 13 + movesOffsetY + 3 + h, move.getType(pokemob).colour);
                    GL11.glPopMatrix();
                }
            }

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            // Render Mob
            this.minecraft.getTextureManager().bindTexture(Resources.GUI_BATTLE);
            this.blit(mobOffsetX + w, mobOffsetY + h, 0, 0, 42, 42);
            GL11.glColor4f(1, 1, 1, 1);
            pokemob.getEntity().addedToChunk = false;
            GuiPokemobBase.renderMob(pokemob.getEntity(), -30, -25, 0, 0, 0, 0, 0, 0.75f);
            pokemob.getEntity().addedToChunk = true;
        }
        GL11.glPopMatrix();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void drawTarget(final GuiEvent.RenderTargetInfo evt)
    {
        final int dir = PokecubeCore.getConfig().guiDown ? 1 : -1;
        final int nameOffsetX = dir == 1 ? 43 : 43;
        final int nameOffsetY = dir == 1 ? 0 : 23;
        final int mobOffsetX = 0;
        final int mobOffsetY = 0;
        final int hpOffsetX = 42;
        final int hpOffsetY = 13;
        final int statusOffsetX = 0;
        final int statusOffsetY = 27;
        final int confuseOffsetX = 12;
        final int confuseOffsetY = 1;
        GL11.glPushMatrix();
        GL11.glColor4f(1, 1, 1, 1);
        GuiDisplayPokecubeInfo.applyTransform(PokecubeCore.getConfig().targetRef, PokecubeCore.getConfig().targetPos,
                GuiDisplayPokecubeInfo.targetDims, (float) PokecubeCore.getConfig().targetSize);
        final int w = 0;
        final int h = 0;
        IPokemob pokemob = this.getCurrentPokemob();
        render:
        if (pokemob != null)
        {
            final LivingEntity entity = pokemob.getEntity().getAttackTarget();
            if (entity == null || !entity.isAlive()) break render;

            GlStateManager.setProfile(GlStateManager.Profile.PLAYER_SKIN);
            // Render HP
            this.minecraft.getTextureManager().bindTexture(Resources.GUI_BATTLE);
            this.blit(hpOffsetX + w, hpOffsetY + h, 43, 12, 92, 7);
            final float total = entity.getMaxHealth();
            final float ratio = entity.getHealth() / total;
            final float f = 0.00390625F;
            final float f1 = 0.00390625F;
            final float x = hpOffsetX + 1 + w;
            final float y = hpOffsetY + 1 + h;
            final float width = 92 * ratio;
            final float height = 5;
            final int u = 0;
            final int v = 85;
            final Tessellator tessellator = Tessellator.getInstance();
            final BufferBuilder vertexbuffer = tessellator.getBuffer();
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            vertexbuffer.pos(x + 0, y + height, this.blitOffset).tex(u * f, (v + height) * f1).endVertex();
            vertexbuffer.pos(x + width, y + height, this.blitOffset).tex((u + width) * f, (v + height) * f1)
                    .endVertex();
            vertexbuffer.pos(x + width, y + 0, this.blitOffset).tex((u + width) * f, v * f1).endVertex();
            vertexbuffer.pos(x + 0, y + 0, this.blitOffset).tex(u * f, v * f1).endVertex();
            tessellator.draw();

            // Render Status
            pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (pokemob != null)
            {
                final byte status = pokemob.getStatus();
                if (status != IMoveConstants.STATUS_NON)
                {
                    int dv = 0;
                    if ((status & IMoveConstants.STATUS_BRN) != 0) dv = 2 * 14;
                    if ((status & IMoveConstants.STATUS_FRZ) != 0) dv = 1 * 14;
                    if ((status & IMoveConstants.STATUS_PAR) != 0) dv = 3 * 14;
                    if ((status & IMoveConstants.STATUS_PSN) != 0) dv = 4 * 14;
                    this.blit(statusOffsetX + w, statusOffsetY + h, 0, 138 + dv, 15, 15);
                }
                if ((pokemob.getChanges() & IMoveConstants.CHANGE_CONFUSED) != 0)
                {
                    GlStateManager.translatef(0, 0, 100);
                    this.blit(confuseOffsetX + w, confuseOffsetY + h, 0, 211, 24, 16);
                    GlStateManager.translatef(0, 0, -100);
                }
            }

            // Render Name
            GL11.glColor4f(1.0F, 0.4F, 0.4F, 1.0F);
            this.minecraft.getTextureManager().bindTexture(Resources.GUI_BATTLE);
            this.blit(nameOffsetX + w, nameOffsetY + h, 44, 0, 90, 13);
            final String displayName = entity.getDisplayName().getFormattedText();
            if (this.fontRenderer.getStringWidth(displayName) > 70)
            {

            }
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.fontRenderer.drawString(displayName, nameOffsetX + 3 + w, nameOffsetY + 3 + h,
                    GuiDisplayPokecubeInfo.lightGrey);

            // Render Box behind Mob
            this.minecraft.getTextureManager().bindTexture(Resources.GUI_BATTLE);
            this.blit(mobOffsetX + w, mobOffsetY + h, 0, 0, 42, 42);

            // Render Mob
            GL11.glColor4f(1, 1, 1, 1);
            GuiPokemobBase.renderMob(entity, -30, -25, 0, 0, 0, 0, 0, 0.75f);

        }
        GL11.glPopMatrix();
    }

    /** @return the currently selected pokemob */
    public IPokemob getCurrentPokemob()
    {
        IPokemob pokemob = null;
        if (this.indexPokemob < this.arrayRet.length && this.indexPokemob >= 0 && this.arrayRet.length > 0)
            pokemob = this.arrayRet[this.indexPokemob];
        return pokemob;
    }

    public IPokemob[] getPokemobsToDisplay()
    {
        if (this.refreshCounter++ > 5) this.refreshCounter = 0;
        if (this.refreshCounter > 0) return this.arrayRet;

        final PlayerEntity player = this.minecraft.player;

        if (player == null || player.getEntityWorld() == null) return new IPokemob[0];

        final ClientWorld world = this.minecraft.world;
        final List<Entity> pokemobs = world.getEntitiesInAABBexcluding(player, player.getBoundingBox().grow(96, 96, 96),
                c -> CapabilityPokemob.getPokemobFor(c) != null);
        final List<IPokemob> ret = new ArrayList<>();
        final Set<Integer> added = new HashSet<>();
        for (final Entity object : pokemobs)
        {
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(object);
            if (pokemob == null) continue;

            boolean owner = pokemob.getOwnerId() != null;

            if (owner) owner = player.getUniqueID().equals(pokemob.getOwnerId());
            final int id = pokemob.getPokemonUID();

            if (owner && !pokemob.getLogicState(LogicStates.SITTING) && !pokemob.getGeneralState(GeneralStates.STAYING)
                    && !added.contains(id))
            {
                ret.add(pokemob);
                added.add(id);
            }

        }
        this.arrayRet = ret.toArray(new IPokemob[ret.size()]);
        Arrays.sort(this.arrayRet, (o1, o2) ->
        {
            final Entity e1 = o1.getEntity();
            final Entity e2 = o2.getEntity();

            if (e1.ticksExisted == e2.ticksExisted)
            {
                if (o2.getLevel() == o1.getLevel()) return o1.getDisplayName().getFormattedText().compareTo(o2
                        .getDisplayName().getFormattedText());
                return o2.getLevel() - o1.getLevel();
            }
            return e1.ticksExisted - e2.ticksExisted;
        });
        return this.arrayRet;
    }

    /**
     * Shifts the gui by x and y
     *
     * @param x
     * @param y
     */
    public void moveGui(final int x, final int y)
    {
        PokecubeCore.getConfig().guiPos.set(0, PokecubeCore.getConfig().guiPos.get(0) + x);
        PokecubeCore.getConfig().guiPos.set(1, PokecubeCore.getConfig().guiPos.get(1) + y);
        this.saveConfig();
    }

    /**
     * Incremenrs pokemob move index
     *
     * @param i
     */
    public void nextMove(final int i)
    {
        final IPokemob pokemob = this.getCurrentPokemob();
        if (pokemob != null)
        {
            int index = pokemob.getMoveIndex() + i;
            int max = 0;
            for (max = 0; max < 4; max++)
                if (pokemob.getMove(max) == null) break;
            if (index >= 5) index = 0;
            if (index >= max) index = 5;
            this.setMove(index);
        }
    }

    /** Select next pokemob */
    public void nextPokemob()
    {
        this.indexPokemob++;
        if (this.indexPokemob >= this.arrayRet.length) this.indexPokemob = 0;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
    public void onRenderHotbar(final RenderGameOverlayEvent.Post event)
    {
        try
        {
            if ((this.minecraft.currentScreen == null || GuiArranger.toggle) && !Minecraft
                    .getInstance().gameSettings.hideGUI && event.getType() == ElementType.HOTBAR || event
                            .getType() == ElementType.CHAT) this.draw(event);
        }
        catch (final Throwable e)
        {
            e.printStackTrace();
        }
    }

    /** Identifies target of attack, and sends the packet with info to server */
    public void pokemobAttack()
    {
        if (this.getCurrentPokemob() == null) return;
        final PlayerEntity player = this.minecraft.player;
        final Predicate<Entity> selector = input ->
        {
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(input);
            if (pokemob == null) return true;
            return pokemob.getOwner() != GuiDisplayPokecubeInfo.this.getCurrentPokemob().getOwner();
        };
        final Entity target = Tools.getPointedEntity(player, 32, selector);
        final Vector3 targetLocation = Tools.getPointedLocation(player, 32);
        boolean sameOwner = false;
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob != null) sameOwner = targetMob.getOwner() == player;
        final IPokemob pokemob = this.getCurrentPokemob();
        if (pokemob != null)
        {
            if (pokemob.getMove(pokemob.getMoveIndex()) == null) return;
            if (pokemob.getMove(pokemob.getMoveIndex()).equalsIgnoreCase(IMoveNames.MOVE_TELEPORT))
            {
                if (!GuiTeleport.instance().getState())
                {
                    GuiTeleport.instance().setState(true);
                    return;
                }
                GuiTeleport.instance().setState(false);
                PacketCommand.sendCommand(pokemob, Command.TELEPORT, new TeleportHandler().setFromOwner(true));
                return;
            }
        }
        if (target != null && !sameOwner && (target instanceof LivingEntity || target instanceof EnderDragonPartEntity))
            PacketCommand.sendCommand(pokemob, Command.ATTACKENTITY, new AttackEntityHandler(target.getEntityId())
                    .setFromOwner(true));
        else if (targetLocation != null) PacketCommand.sendCommand(pokemob, Command.ATTACKLOCATION,
                new AttackLocationHandler(targetLocation).setFromOwner(true));
        else PacketCommand.sendCommand(pokemob, Command.ATTACKNOTHING, new AttackNothingHandler().setFromOwner(true));
    }

    /**
     * Recalls selected pokemob, if none selected, will try to identify a
     * pokemob being looked at, and recalls that
     */
    public void pokemobBack()
    {
        final IPokemob pokemob = this.getCurrentPokemob();

        if (Screen.hasShiftDown() && pokemob != null && pokemob.getOwner() != null)
        {
            PacketCommand.sendCommand(pokemob, Command.MOVETO, new MoveToHandler(Vector3.getNewVector().set(pokemob
                    .getOwner()), (float) pokemob.getEntity().getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                            .getValue()));
            return;
        }

        // System.out.println(pokemob+":");
        if (pokemob != null) pokemob.onRecall();
        else
        {
            final PlayerEntity player = this.minecraft.player;
            Entity target = null;
            final Vector3 look = Vector3.getNewVector().set(player.getLook(1));
            final Vector3 temp = Vector3.getNewVector().set(player).addTo(0, player.getEyeHeight(), 0);
            target = temp.firstEntityExcluding(32, look, player.getEntityWorld(), player.isSneaking(), player);
            final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
            if (targetMob != null && player.getUniqueID().equals(targetMob.getOwnerId())) targetMob.onRecall();
        }

        if (this.indexPokemob >= this.arrayRet.length) this.indexPokemob--;

        if (this.indexPokemob < 0) this.indexPokemob = 0;

    }

    /**
     * Sends the packet to toggle all pokemobs set to follow between sit and
     * stand
     */
    public void pokemobStance()
    {
        IPokemob pokemob;
        if ((pokemob = this.getCurrentPokemob()) != null) PacketCommand.sendCommand(pokemob, Command.STANCE,
                new StanceHandler(!pokemob.getLogicState(LogicStates.SITTING), StanceHandler.BUTTONTOGGLESIT)
                        .setFromOwner(true));
        else
        {
            final PlayerEntity player = this.minecraft.player;
            Entity target = null;
            final Vector3 look = Vector3.getNewVector().set(player.getLook(1));
            final Vector3 temp = Vector3.getNewVector().set(player).addTo(0, player.getEyeHeight(), 0);
            target = temp.firstEntityExcluding(32, look, player.getEntityWorld(), player.isSneaking(), player);
            final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
            if (targetMob != null && targetMob.getOwner() == player) PacketCommand.sendCommand(targetMob,
                    Command.STANCE, new StanceHandler(!targetMob.getLogicState(LogicStates.SITTING),
                            StanceHandler.BUTTONTOGGLESIT).setFromOwner(true));
        }
    }

    /**
     * Decrements pokemob move index
     *
     * @param j
     */
    public void previousMove(final int j)
    {
        final IPokemob pokemob = this.getCurrentPokemob();
        if (pokemob != null)
        {
            int index = pokemob.getMoveIndex();
            if (index == 5)
            {
                for (int i = 3; i > 0; i -= j)
                    if (pokemob.getMove(i) != null)
                    {
                        index = i;
                        break;
                    }
            }
            else index -= j;

            if (index % 5 >= 0) index = index % 5;
            else index = 5;
            this.setMove(index);
        }
    }

    /** Select previous pokemob */
    public void previousPokemob()
    {
        this.indexPokemob--;
        if (this.indexPokemob < 0) this.indexPokemob = this.arrayRet.length - 1;
    }

    private void saveConfig()
    {// TODO save the configs
     // PokecubeCore.getConfig().setSettings();
    }

    /**
     * Sets pokemob's move index.
     *
     * @param num
     */
    public void setMove(final int num)
    {
        final IPokemob pokemob = this.getCurrentPokemob();
        if (pokemob != null)
        {
            int index = num;
            if (index > 5) index = index % 6;
            GuiDisplayPokecubeInfo.sendMoveIndexPacket(pokemob, index);
        }
    }
}
