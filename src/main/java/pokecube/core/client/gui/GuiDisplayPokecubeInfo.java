/**
 *
 */
package pokecube.core.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Predicate;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.helper.ListHelper;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
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
import pokecube.core.network.pokemobs.PacketAIRoutine;
import pokecube.core.network.pokemobs.PacketCommand;
import pokecube.core.utils.AITools;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

public class GuiDisplayPokecubeInfo extends GuiComponent implements IIngameOverlay
{
    protected static int                 lightGrey  = 0xDDDDDD;
    public static int[]                  guiDims    = { 147, 42 };
    public static int[]                  targetDims = { 147, 42 };
    public static int[]                  teleDims   = { 147, 42 };
    public static GuiDisplayPokecubeInfo instance;

    public static int[] applyTransform(final PoseStack mat, final String ref, final List<Integer> offsets,
            final int[] dims, final float targetSize)
    {
        final Minecraft minecraft = Minecraft.getInstance();

        final Window res = minecraft.getWindow();
        int w = offsets.get(0);
        int h = offsets.get(1);
        int x = 0;
        int y = 0;
        final int scaledWidth = res.getGuiScaledWidth();
        final int scaledHeight = res.getGuiScaledHeight();
        int dx = 1;
        int dy = 1;
        switch (ref)
        {
        case "top_left":
            mat.translate(w, h, 0);
            mat.scale(targetSize, targetSize, targetSize);
            break;
        case "middle_left":
            h = scaledHeight / 2 - h - dims[1];
            mat.translate(w, h, 0);
            mat.scale(targetSize, targetSize, targetSize);
            break;
        case "bottom_left":
            h = scaledHeight - h - dims[1];
            mat.translate(w, h, 0);
            mat.scale(targetSize, targetSize, targetSize);
            dy = -1;
            break;
        case "top_right":
            w = scaledWidth - w;
            h = Math.min(h + dims[1], scaledHeight);
            mat.translate(w, h, 0);
            mat.scale(targetSize, targetSize, targetSize);
            dx = -1;
            break;
        case "right_bottom":
            w = scaledWidth - w - dims[0];
            h = scaledHeight - h - dims[1];
            mat.translate(w, h, 0);
            mat.scale(targetSize, targetSize, targetSize);
            dx = -1;
            dy = -1;
            break;
        case "right_middle":
            w = scaledWidth - w - dims[0];
            h = scaledHeight / 2 - h - dims[1];
            mat.translate(w, h, 0);
            mat.scale(targetSize, targetSize, targetSize);
            dx = -1;
            dy = -1;
            break;
        case "bottom_middle":
            x = scaledWidth / 2 - w;
            y = scaledHeight;
            w = scaledWidth / 2 - w;
            h = scaledHeight - h - dims[1];
            mat.translate(w, h, 0);
            h = (int) (-dims[1] / targetSize - offsets.get(1));
            w = 0;
            dx = -1;
            dy = -1;
            mat.scale(targetSize, targetSize, targetSize);
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

    public static void sendMoveIndexPacket(final IPokemob pokemob, final int moveIndex)
    {
        PacketCommand.sendCommand(pokemob, Command.CHANGEMOVEINDEX, new MoveIndexHandler((byte) moveIndex).setFromOwner(
                true));
    }

    private static final IPokemob[] EMPTY = new IPokemob[0];

    protected Font fontRenderer;

    protected Minecraft minecraft;

    IPokemob[] pokemobsCache = new IPokemob[0];

    int refreshCounter = 0;

    int indexPokemob = 0;

    IIngameOverlay infoOverlay = this;

    /**
     *
     */
    public GuiDisplayPokecubeInfo()
    {
        this.minecraft = Minecraft.getInstance();
        this.fontRenderer = this.minecraft.font;
        if (GuiDisplayPokecubeInfo.instance != null) MinecraftForge.EVENT_BUS.unregister(
                GuiDisplayPokecubeInfo.instance);
        GuiDisplayPokecubeInfo.instance = this;
        OverlayRegistry.registerOverlayTop("Pokecube Info", this.infoOverlay);

    }

    public void disable()
    {
        OverlayRegistry.enableOverlay(this.infoOverlay, false);
    }

    public void enable()
    {
        OverlayRegistry.enableOverlay(this.infoOverlay, true);
    }

    @Override
    public void render(final ForgeIngameGui gui, final PoseStack mStack, final float partialTicks, final int width,
            final int height)
    {
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderMoveMessages(mStack, gui));
        if (this.indexPokemob > this.getPokemobsToDisplay().length)
        {
            this.refreshCounter = 0;
            this.indexPokemob = 0;
            this.pokemobsCache = this.getPokemobsToDisplay();
        }
        if (this.getPokemobsToDisplay().length == 0) return;
        if (this.indexPokemob >= this.getPokemobsToDisplay().length) this.indexPokemob = 0;
        if (this.fontRenderer == null) this.fontRenderer = this.minecraft.font;
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderSelectedInfo(mStack, gui));
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderTargetInfo(mStack, gui));
        MinecraftForge.EVENT_BUS.post(new GuiEvent.RenderTeleports(mStack, gui));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void drawSelected(final GuiEvent.RenderSelectedInfo evt)
    {
        final int dir = PokecubeCore.getConfig().guiDown ? 1 : -1;
        final int nameOffsetX = dir == 1 ? 43 : 43;
        final int nameOffsetY = dir == 1 ? 0 : 23;
        final int movesOffsetX = 42;
        final int movesOffsetY = dir == 1 ? 22 : 10;
        final int hpOffsetX = 42;
        final int hpOffsetY = 13;
        final int xpOffsetX = 42;
        final int xpOffsetY = 20;
        final int statusOffsetX = 0;
        final int statusOffsetY = 27;
        final int confuseOffsetX = 12;
        final int confuseOffsetY = 1;

        final IPokemob pokemob = this.getCurrentPokemob();
        if (pokemob != null)
        {
            String displayName = pokemob.getDisplayName().getString();
            final int currentMoveIndex = pokemob.getMoveIndex();
            evt.getMat().pushPose();
            final float s = (float) PokecubeCore.getConfig().guiSize;
            GuiDisplayPokecubeInfo.applyTransform(evt.getMat(), PokecubeCore.getConfig().guiRef, PokecubeCore
                    .getConfig().guiPos, GuiDisplayPokecubeInfo.guiDims, s);
            // Render HP
            RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
            this.blit(evt.getMat(), hpOffsetX, hpOffsetY, 43, 12, 92, 7);
            final float total = pokemob.getMaxHealth();
            float ratio = pokemob.getHealth() / total;
            int x = hpOffsetX + 1;
            int y = hpOffsetY + 1;
            int width = (int) (92 * ratio);
            int height = 5;
            int u = 0;
            int v = 85;
            this.blit(evt.getMat(), x, y, u, v, width, height);

            // Render XP
            this.blit(evt.getMat(), xpOffsetX, xpOffsetY, 43, 19, 92, 5);

            final int current = pokemob.getExp();
            final int level = pokemob.getLevel();
            final int prev = Tools.levelToXp(pokemob.getExperienceMode(), level);
            final int next = Tools.levelToXp(pokemob.getExperienceMode(), level + 1);
            final int levelDiff = next - prev;
            final int diff = current - prev;
            ratio = diff / (float) levelDiff;
            if (level == 100) ratio = 1;
            x = xpOffsetX + 1;
            y = xpOffsetY;
            width = (int) (92 * ratio);
            height = 2;
            u = 0;
            v = 97;
            this.blit(evt.getMat(), x, y, u, v, width, height);

            // Render Status
            final byte status = pokemob.getStatus();
            if (status != IMoveConstants.STATUS_NON)
            {
                int dv = 0;
                if ((status & IMoveConstants.STATUS_BRN) != 0) dv = 2 * 14;
                if ((status & IMoveConstants.STATUS_FRZ) != 0) dv = 1 * 14;
                if ((status & IMoveConstants.STATUS_PAR) != 0) dv = 3 * 14;
                if ((status & IMoveConstants.STATUS_PSN) != 0) dv = 4 * 14;
                this.blit(evt.getMat(), statusOffsetX, statusOffsetY, 0, 138 + dv, 15, 15);
            }
            if ((pokemob.getChanges() & IMoveConstants.CHANGE_CONFUSED) != 0)
            {

                evt.getMat().translate(0, 0, 100);
                this.blit(evt.getMat(), confuseOffsetX, confuseOffsetY, 0, 211, 24, 16);
                evt.getMat().translate(0, 0, -100);
            }

            // Render Name
            if (currentMoveIndex == 5) RenderSystem.setShaderColor(0.0F, 1.0F, 0.4F, 1.0F);
            this.blit(evt.getMat(), nameOffsetX, nameOffsetY, 44, 0, 90, 13);
            if (this.fontRenderer.width(displayName) > 70)
            {
                final List<MutableComponent> list = ListHelper.splitText(new TextComponent(displayName), 70,
                        this.fontRenderer, true);
                displayName = list.get(0).getString();
            }
            this.fontRenderer.draw(evt.getMat(), displayName, nameOffsetX + 3, nameOffsetY + 3,
                    GuiDisplayPokecubeInfo.lightGrey);

            // Render level
            this.fontRenderer.draw(evt.getMat(), "L." + level, nameOffsetX + 88 - this.fontRenderer.width("L." + level),
                    nameOffsetY + 3, GuiDisplayPokecubeInfo.lightGrey);

            // Draw number of pokemon
            RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
            RenderSystem.enableBlend();
            final int n = this.getPokemobsToDisplay().length;
            final int num = this.fontRenderer.width("" + n);
            this.blit(evt.getMat(), nameOffsetX + 89, nameOffsetY, 0, 27, 15, 15);
            this.fontRenderer.draw(evt.getMat(), "" + n, nameOffsetX + 95 - num / 4, nameOffsetY + 4,
                    GuiDisplayPokecubeInfo.lightGrey);

            // Render Moves
            int moveIndex = 0;
            int moveCount = 0;
            for (moveCount = 0; moveCount < 4; moveCount++)
                if (pokemob.getMove(moveCount) == null) break;
            int h = 0;
            if (dir == -1) h -= 14 + 12 * (moveCount - 1) - (4 - moveCount) * 2;
            for (moveIndex = 0; moveIndex < 4; moveIndex++)
            {
                final int index = moveIndex;

                final Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(index));
                final boolean disabled = index >= 0 && index < 4 && pokemob.getDisableTimer(index) > 0;
                if (move != null)
                {

                    // bind texture
                    evt.getMat().pushPose();

                    RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
                    RenderSystem.enableBlend();
                    this.blit(evt.getMat(), movesOffsetX, movesOffsetY + 13 * index + h, 43, 22, 91, 13);

                    // Render colour overlays.
                    if (currentMoveIndex == index)
                    {
                        // Draw selected indictator
                        RenderSystem.enableBlend();
                        this.blit(evt.getMat(), movesOffsetX, movesOffsetY + 13 * index + h, 43, 65, 91, 13);
                        // Draw cooldown box
                        float timer = 1;
                        Move_Base lastMove;
                        if (MovesUtils.isAbleToUseMoves(pokemob) != AbleStatus.ABLE) timer = 0;
                        else if ((lastMove = MovesUtils.getMoveFromName(pokemob.getLastMoveUsed())) != null)
                            timer -= pokemob.getAttackCooldown() / (float) MovesUtils.getAttackDelay(pokemob, pokemob
                                    .getLastMoveUsed(), (lastMove.getAttackCategory()
                                            & IMoveConstants.CATEGORY_DISTANCE) > 0, false);
                        timer = Math.max(0, Math.min(timer, 1));
                        RenderSystem.enableBlend();
                        this.blit(evt.getMat(), movesOffsetX, movesOffsetY + 13 * index + h, 43, 35, (int) (91 * timer),
                                13);
                    }
                    if (disabled) this.blit(evt.getMat(), movesOffsetX, movesOffsetY + 13 * index + h, 43, 65, 91, 13);

                    evt.getMat().popPose();
                    evt.getMat().pushPose();
                    this.fontRenderer.draw(evt.getMat(), MovesUtils.getMoveName(move.getName()).getString(), 5
                            + movesOffsetX, index * 13 + movesOffsetY + 3 + h, move.getType(pokemob).colour);
                    evt.getMat().popPose();
                }
            }

            // Render Mob
            RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);

            final int mobOffsetX = 0;
            final int mobOffsetY = 0;
            RenderSystem.enableBlend();
            this.blit(evt.getMat(), mobOffsetX, mobOffsetY, 0, 0, 42, 42);

            GuiPokemobBase.renderMob(evt.getMat(), pokemob.getEntity(), mobOffsetX - 30, mobOffsetY - 25, 0, 0, 0, 0,
                    0.75f);
            evt.getMat().popPose();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void drawTarget(final GuiEvent.RenderTargetInfo evt)
    {
        final int dir = PokecubeCore.getConfig().guiDown ? 1 : -1;
        final int nameOffsetX = dir == 1 ? 43 : 43;
        final int nameOffsetY = dir == 1 ? 0 : 23;
        final int hpOffsetX = 42;
        final int hpOffsetY = 13;
        final int statusOffsetX = 0;
        final int statusOffsetY = 27;
        final int confuseOffsetX = 12;
        final int confuseOffsetY = 1;
        IPokemob pokemob = this.getCurrentPokemob();
        render:
        if (pokemob != null)
        {
            final LivingEntity entity = BrainUtils.getAttackTarget(pokemob.getEntity());
            if (entity == null || !entity.isAlive()) break render;

            evt.getMat().pushPose();
            GuiDisplayPokecubeInfo.applyTransform(evt.getMat(), PokecubeCore.getConfig().targetRef, PokecubeCore
                    .getConfig().targetPos, GuiDisplayPokecubeInfo.targetDims, (float) PokecubeCore
                            .getConfig().targetSize);
            // Render HP
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
            this.blit(evt.getMat(), hpOffsetX, hpOffsetY, 43, 12, 92, 7);
            final float total = entity.getMaxHealth();
            final float ratio = entity.getHealth() / total;
            final int x = hpOffsetX + 1;
            final int y = hpOffsetY + 1;
            final int width = (int) (92 * ratio);
            this.blit(evt.getMat(), x, y, 0, 85, width, 5);

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
                    this.blit(evt.getMat(), statusOffsetX, statusOffsetY, 0, 138 + dv, 15, 15);
                }
                if ((pokemob.getChanges() & IMoveConstants.CHANGE_CONFUSED) != 0)
                {
                    evt.getMat().translate(0, 0, 100);
                    this.blit(evt.getMat(), confuseOffsetX, confuseOffsetY, 0, 211, 24, 16);
                    evt.getMat().translate(0, 0, -100);
                }
            }

            // Render Name
            RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
            this.blit(evt.getMat(), nameOffsetX, nameOffsetY, 44, 0, 90, 13);
            final String displayName = entity.getDisplayName().getString();
            if (this.fontRenderer.width(displayName) > 70)
            {

            }
            this.fontRenderer.draw(evt.getMat(), displayName, nameOffsetX + 3, nameOffsetY + 3,
                    GuiDisplayPokecubeInfo.lightGrey);

            // Render Box behind Mob
            RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
            RenderSystem.enableBlend();
            final int mobBoxOffsetX = 0;
            final int mobBoxOffsetY = 0;
            this.blit(evt.getMat(), mobBoxOffsetX, mobBoxOffsetY, 0, 0, 42, 42);
            // Render Mob
            GuiPokemobBase.renderMob(evt.getMat(), entity, mobBoxOffsetX - 30, mobBoxOffsetY - 25, 0, 0, 0, 0, 0.75f);
            evt.getMat().popPose();
        }
    }

    /** @return the currently selected pokemob */
    public IPokemob getCurrentPokemob()
    {
        IPokemob pokemob = null;
        if (this.indexPokemob < this.pokemobsCache.length && this.indexPokemob >= 0 && this.pokemobsCache.length > 0)
            pokemob = this.pokemobsCache[this.indexPokemob];
        return pokemob;
    }

    public IPokemob[] getPokemobsToDisplay()
    {
        if (this.refreshCounter++ > 5) this.refreshCounter = 0;
        if (this.refreshCounter > 0) return this.pokemobsCache;

        final Player player = this.minecraft.player;

        if (player == null || player.getCommandSenderWorld() == null) return GuiDisplayPokecubeInfo.EMPTY;

        final List<IPokemob> pokemobs = EventsHandlerClient.getPokemobs(player, 96);
        final List<IPokemob> ret = new ArrayList<>();
        for (final IPokemob pokemob : pokemobs)
        {
            boolean owner = pokemob.getOwnerId() != null;
            if (owner) owner = player.getUUID().equals(pokemob.getOwnerId());
            if (owner && !pokemob.getLogicState(LogicStates.SITTING) && !pokemob.getGeneralState(GeneralStates.STAYING))
                ret.add(pokemob);
        }
        if (this.pokemobsCache.length != ret.size()) this.pokemobsCache = ret.toArray(new IPokemob[ret.size()]);
        else this.pokemobsCache = ret.toArray(this.pokemobsCache);
        Arrays.sort(this.pokemobsCache, (o1, o2) ->
        {
            final Entity e1 = o1.getEntity();
            final Entity e2 = o2.getEntity();

            if (e1.tickCount == e2.tickCount)
            {
                if (o2.getLevel() == o1.getLevel()) return o1.getDisplayName().getString().compareTo(o2.getDisplayName()
                        .getString());
                return o2.getLevel() - o1.getLevel();
            }
            return e1.tickCount - e2.tickCount;
        });
        return this.pokemobsCache;
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
        if (this.indexPokemob >= this.pokemobsCache.length) this.indexPokemob = 0;
    }

    /** Identifies target of attack, and sends the packet with info to server */
    public void pokemobAttack()
    {
        if (this.getCurrentPokemob() == null) return;
        final Player player = this.minecraft.player;
        final Predicate<Entity> selector = input ->
        {
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(input);
            if (!AITools.validTargets.test(input)) return false;
            if (pokemob == null) return true;
            return pokemob.getOwner() != GuiDisplayPokecubeInfo.this.getCurrentPokemob().getOwner();
        };
        Entity target = Tools.getPointedEntity(player, 32, selector);
        target = EntityTools.getCoreEntity(target);
        if (target == null && Minecraft.getInstance().crosshairPickEntity != null && selector.test(Minecraft
                .getInstance().crosshairPickEntity)) target = Minecraft.getInstance().crosshairPickEntity;
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
        if (target != null && !sameOwner && (target instanceof LivingEntity || target instanceof PartEntity<?>))
            PacketCommand.sendCommand(pokemob, Command.ATTACKENTITY, new AttackEntityHandler(target.getId())
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
                    .getOwner()), 1.0f));
            return;
        }

        // System.out.println(pokemob+":");
        if (pokemob != null) pokemob.onRecall();
        else
        {
            final Player player = this.minecraft.player;
            final Entity target = Tools.getPointedEntity(player, 32);
            final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
            if (targetMob != null && player.getUUID().equals(targetMob.getOwnerId())) targetMob.onRecall();
        }

        if (this.indexPokemob >= this.pokemobsCache.length) this.indexPokemob--;

        if (this.indexPokemob < 0) this.indexPokemob = 0;

    }

    /**
     * Sends the packet to toggle all pokemobs set to follow between sit and
     * stand
     */
    public void pokemobStance()
    {
        IPokemob pokemob;
        if ((pokemob = this.getCurrentPokemob()) != null)
        {
            final boolean isRiding = pokemob.getEntity().hasIndirectPassenger(pokemob.getOwner());
            if (!isRiding) PacketCommand.sendCommand(pokemob, Command.STANCE, new StanceHandler(!pokemob.getLogicState(
                    LogicStates.SITTING), StanceHandler.SIT).setFromOwner(true));
            else
            {
                final AIRoutine routine = AIRoutine.AIRBORNE;
                final boolean state = !pokemob.isRoutineEnabled(routine);
                pokemob.setRoutineState(routine, state);
                PacketAIRoutine.sentCommand(pokemob, routine, state);
            }
        }
        else
        {
            final Player player = this.minecraft.player;
            final Entity target = Tools.getPointedEntity(player, 32);
            final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
            if (targetMob != null && targetMob.getOwner() == player) PacketCommand.sendCommand(targetMob,
                    Command.STANCE, new StanceHandler(!targetMob.getLogicState(LogicStates.SITTING), StanceHandler.SIT)
                            .setFromOwner(true));
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
        if (this.indexPokemob < 0) this.indexPokemob = this.pokemobsCache.length - 1;
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
