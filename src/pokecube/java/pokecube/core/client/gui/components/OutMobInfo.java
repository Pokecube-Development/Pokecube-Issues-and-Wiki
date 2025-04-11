package pokecube.core.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.MovesUtils.AbleStatus;

public class OutMobInfo extends GuiEventComponent
{
    @Override
    protected void onMovedGui()
    {
        PokecubeCore.getConfig().guiSelectedPos.set(0, this.bounds.x0);
        PokecubeCore.getConfig().guiSelectedPos.set(1, this.bounds.y0);
        super.onMovedGui();
    }

    @Override
    protected void preDraw(GuiEvent event)
    {
        if (clickA == 0)
        {
            int x0 = PokecubeCore.getConfig().guiSelectedPos.get(0);
            int y0 = PokecubeCore.getConfig().guiSelectedPos.get(1);
            if (x0 != bounds.x0 || y0 != bounds.y0 || bounds.h == 0) this.bounds.setBox(x0, y0, 154, 82);
            this.ref = PokecubeCore.getConfig().guiRef;
        }
    }

    protected IPokemob getMob()
    {
        var info = GuiDisplayPokecubeInfo.instance();
        return info.getCurrentPokemob();
    }

    @Override
    public void _drawGui(GuiEvent evt)
    {
        IPokemob pokemob = getMob();

        if (pokemob != null)
        {
            final int dir = PokecubeCore.getConfig().guiDown ? 1 : -1;
            final int nameOffsetX = 42;
            final int nameOffsetY = dir == 1 ? 0 : 23;
            final int movesOffsetX = 42;
            final int movesOffsetY = dir == 1 ? 23 : 10;
            final int hpOffsetX = 42;
            final int hpOffsetY = 13;
            final int xpOffsetX = 42;
            final int xpOffsetY = 19;
            final int statusOffsetX = 0;
            final int statusOffsetY = 27;
            final int confuseOffsetX = 12;
            final int confuseOffsetY = 1;

            float total, ratio;
            int width;

            int moveIndex;
            int moveCount;

            var graphics = evt.getGraphics();
            var gui = Minecraft.getInstance().gui;

            FormattedCharSequence displayName = pokemob.getDisplayName().getVisualOrderText();
            if (gui.getFont().width(displayName) > 70)
            {
                displayName = gui.getFont().split(pokemob.getDisplayName(), 70).getFirst();
            }
            final int currentMoveIndex = pokemob.getMoveIndex();
            evt.getMat().pushPose();
            evt.getMat().translate(this.pos.x0, this.pos.y0, 0);

            // If gui is upwards, translate accordingly
            if (!PokecubeCore.getConfig().guiDown)
            {
                evt.getMat().translate(0, 40, 0);
            }

            // Render HP
            total = pokemob.getMaxHealth();
            ratio = pokemob.getHealth() / total;
            width = (int) (89 * ratio);
            graphics.blitSprite(ICON_HEALTH_EXP[0], hpOffsetX, hpOffsetY, 89, 7);
            graphics.blitSprite(ICON_HEALTH_EXP[1], hpOffsetX, hpOffsetY, width, 7);

            // Render XP
            int current = pokemob.getExp();
            int level = pokemob.getLevel();
            int prev = Tools.levelToXp(pokemob.getExperienceMode(), level);
            int next = Tools.levelToXp(pokemob.getExperienceMode(), level + 1);
            int levelDiff = next - prev;
            int diff = current - prev;
            ratio = diff / (float) levelDiff;
            if (level == 100) ratio = 1;
            width = (int) (89 * ratio);
            graphics.blitSprite(ICON_HEALTH_EXP[2], xpOffsetX, xpOffsetY, 89, 4);
            graphics.blitSprite(ICON_HEALTH_EXP[3], xpOffsetX, xpOffsetY, width, 4);

            // Render Hunger before status (Status will render over it)
            int maxT = PokecubeCore.getConfig().pokemobLifeSpan;
            final float full_hunger = maxT / 4f + maxT;
            float current_hunger = -(pokemob.getHungerTime() - maxT);
            final float scale = 100f / full_hunger;
            current_hunger *= scale / 100f;
            current_hunger = Math.min(1, current_hunger);
            if (current_hunger < 0.5)
            {
                int dv = 5;
                graphics.blitSprite(STATUS_ICONS[dv], statusOffsetX, statusOffsetY, -1, 13, 13);
            }

            // Render Status
            final int status = pokemob.getStatus();
            if (status != IMoveConstants.STATUS_NON)
            {
                int dv = 0;
                if ((status & IMoveConstants.STATUS_BRN) != 0) dv = 0;
                if ((status & IMoveConstants.STATUS_FRZ) != 0) dv = 1;
                if ((status & IMoveConstants.STATUS_PAR) != 0) dv = 2;
                if ((status & IMoveConstants.STATUS_PSN) != 0) dv = 3;
                graphics.blitSprite(STATUS_ICONS[dv], statusOffsetX, statusOffsetY, -1, 13, 13);
            }
            if ((pokemob.getChanges() & IMoveConstants.CHANGE_CONFUSED) != 0)
            {
                graphics.blitSprite(STATUS_ICONS[4], confuseOffsetX, confuseOffsetY, 100, 20, 14);
            }

            // Render Name
            ResourceLocation plate = ICON_MOVE_FRAMES[currentMoveIndex == 5 ? 1 : 0];
            graphics.blitSprite(plate, nameOffsetX, nameOffsetY, 89, 13);
            graphics.drawString(gui.getFont(), displayName, nameOffsetX + 3, nameOffsetY + 3,
                    GuiDisplayPokecubeInfo.lightGrey);

            // Render level
            graphics.drawString(gui.getFont(), "Lvl" + level, nameOffsetX + 88 - gui.getFont().width("Lvl " + level),
                    nameOffsetY + 3, GuiDisplayPokecubeInfo.lightGrey);

            // Draw number of pokemon
            RenderSystem.enableBlend();
            var info = GuiDisplayPokecubeInfo.instance();
            int n = info.getPokemobsToDisplay().length;
            int n2 = info.indexPokemob + 1;
            String txt = n == 1 ? n + "" : n2 + "/" + n;
            int num = gui.getFont().width(txt);

            graphics.blitSprite(ICON_NUMBER_FRAME, nameOffsetX + 89, nameOffsetY, -1, num + 4, 15);
            graphics.drawString(gui.getFont(), txt, nameOffsetX + 91, nameOffsetY + 4,
                    GuiDisplayPokecubeInfo.lightGrey);

            // Render Moves
            RenderSystem.enableBlend();
            for (moveCount = 0; moveCount < 4; moveCount++) if (pokemob.getMove(moveCount) == null) break;
            int h = 0;
            if (dir == -1) h -= 14 + 12 * (moveCount - 1) - (4 - moveCount) * 2;
            for (moveIndex = 0; moveIndex < 4; moveIndex++)
            {
                final MoveEntry move = MovesUtils.getMove(pokemob.getMove(moveIndex));
                final boolean disabled = moveIndex >= 0 && moveIndex < 4 && pokemob.getDisableTimer(moveIndex) > 0;
                if (move != null)
                {
                    // Select background plate colour
                    plate = ICON_MOVE_FRAMES[disabled ? 2 : currentMoveIndex == moveIndex ? 2 : 0];
                    // Draw background plate
                    graphics.blitSprite(plate, movesOffsetX, movesOffsetY + 13 * moveIndex + h, -2, 89, 13);

                    // Render colour overlays.
                    if (currentMoveIndex == moveIndex && !disabled)
                    {
                        // Draw cooldown box
                        float timer = 1;
                        MoveEntry lastMove;
                        if (MovesUtils.isAbleToUseMoves(pokemob) != AbleStatus.ABLE) timer = 0;
                        else if ((lastMove = MovesUtils.getMove(pokemob.getLastMoveUsed())) != null) timer -=
                                pokemob.getAttackCooldown() / (float) MovesUtils.getAttackDelay(pokemob,
                                        pokemob.getLastMoveUsed(), lastMove.isRanged(pokemob), false);
                        timer = Math.max(0, Math.min(timer, 1));
                        width = (int) (89 * timer);
                        plate = ICON_MOVE_FRAMES[1];
                        graphics.blitSprite(plate, movesOffsetX, movesOffsetY + 13 * moveIndex + h, -1, width, 13);
                    }
                    // Finally draw the name
                    graphics.drawString(gui.getFont(), MovesUtils.getMoveName(move.getName(), pokemob).getString(),
                            5 + movesOffsetX, moveIndex * 13 + movesOffsetY + 3 + h, move.getType(pokemob).colour);
                }
            }

            // Render Mob
            int mobOffsetX = 0;
            int mobOffsetY = 0;
            RenderSystem.enableBlend();
            // Render Box behind Mob
            graphics.blitSprite(ICON_MOB_FRAME, 1, 0, -2, 42, 42);
            // Render Mob

            LivingEntity mob = pokemob.getEntity();

            float f = 30;
            float yBodyRot = mob.yBodyRot;
            float yBodyRotO = mob.yBodyRotO;
            float yHeadRot = mob.yHeadRot;
            float yHeadRotO = mob.yHeadRotO;

            mob.yBodyRot = mob.yBodyRotO = 180.0F + f * 20.0F;
            mob.yHeadRot = mob.yHeadRotO = mob.yBodyRot;

            float renderScale = 1.0f;
            float tick = evt.getTick();

            GuiPokemobHelper.renderMob(evt.getMat(), mob, mobOffsetX - 30, mobOffsetY - 25, 0, 0, 0, 0, renderScale,
                    tick);

            mob.yBodyRot = yBodyRot;
            mob.yBodyRotO = yBodyRotO;
            mob.yHeadRot = yHeadRot;
            mob.yHeadRotO = yHeadRotO;

            LivingEntity ally = pokemob.getMoveStats().targetAlly;
            if (ally != null && ally != pokemob.getEntity())
            {
                evt.getMat().pushPose();

                evt.getMat().scale(0.5f, 0.5f, 0.5f);

                mobOffsetX = 45;
                mobOffsetY = 80;
                RenderSystem.enableBlend();
                graphics.blitSprite(ICON_MOB_FRAME, mobOffsetX, mobOffsetY, -2, 42, 42);

                mob = ally;

                f = 30;
                yBodyRot = mob.yBodyRot;
                yBodyRotO = mob.yBodyRotO;
                yHeadRot = mob.yHeadRot;
                yHeadRotO = mob.yHeadRotO;

                mob.yBodyRot = mob.yBodyRotO = 180.0F + f * 20.0F;
                mob.yHeadRot = mob.yHeadRotO = mob.yBodyRot;

                GuiPokemobHelper.renderMob(evt.getMat(), mob, mobOffsetX - 30, mobOffsetY - 25, 0, 0, 0, 0, renderScale,
                        tick);

                mob.yBodyRot = yBodyRot;
                mob.yBodyRotO = yBodyRotO;
                mob.yHeadRot = yHeadRot;
                mob.yHeadRotO = yHeadRotO;
                evt.getMat().popPose();
            }
            evt.getMat().popPose();
        }
    }

}