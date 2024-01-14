package pokecube.core.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.MovesUtils.AbleStatus;
import pokecube.core.utils.Resources;

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
        final IPokemob pokemob = getMob();
        if (pokemob != null)
        {
            final int dir = PokecubeCore.getConfig().guiDown ? 1 : -1;
            final int nameOffsetX = dir == 1 ? 43 : 43;
            final int nameOffsetY = dir == 1 ? 0 : 23;
            final int movesOffsetX = 42;
            final int movesOffsetY = dir == 1 ? 23 : 10;
            final int hpOffsetX = 42;
            final int hpOffsetY = 14;
            final int xpOffsetX = 42;
            final int xpOffsetY = 21;
            final int statusOffsetX = 0;
            final int statusOffsetY = 27;
            final int confuseOffsetX = 12;
            final int confuseOffsetY = 1;

            float total, ratio;
            int width, height, u, v;

            int moveIndex = 0;
            int moveCount = 0;
            int x = hpOffsetX + 1;
            int y = hpOffsetY + 1;

            var gui = evt.getGui();

            FormattedCharSequence displayName = pokemob.getDisplayName().getVisualOrderText();
            if (gui.getFont().width(displayName) > 70)
            {
                displayName = gui.getFont().split(pokemob.getDisplayName(), 70).get(0);
            }
            total = pokemob.getMaxHealth();
            ratio = pokemob.getHealth() / total;
            final int currentMoveIndex = pokemob.getMoveIndex();
            evt.getMat().pushPose();
            evt.getMat().translate(this.pos.x0, this.pos.y0, 0);

            // If gui is upwards, translate accordingly
            if (!PokecubeCore.getConfig().guiDown)
            {
                evt.getMat().translate(0, 40, 0);
            }

            // Render HP
            RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
            width = (int) (90 * ratio);
            height = 5;
            u = 0;
            v = 85;
            gui.blit(evt.getMat(), hpOffsetX, hpOffsetY, 43, 12, 90, 7);
            gui.blit(evt.getMat(), x, y, u, v, width, height);

            // Render XP
            gui.blit(evt.getMat(), xpOffsetX, xpOffsetY, 43, 19, 90, 5);
            int current = pokemob.getExp();
            int level = pokemob.getLevel();
            int prev = Tools.levelToXp(pokemob.getExperienceMode(), level);
            int next = Tools.levelToXp(pokemob.getExperienceMode(), level + 1);
            int levelDiff = next - prev;
            int diff = current - prev;
            ratio = diff / (float) levelDiff;
            if (level == 100) ratio = 1;
            x = xpOffsetX + 1;
            y = xpOffsetY;
            width = (int) (90 * ratio);
            height = 2;
            u = 0;
            v = 97;
            gui.blit(evt.getMat(), x, y, u, v, width, height);

            // Render Hunger before status (Status will render over it)
            final float full_hunger = PokecubeCore.getConfig().pokemobLifeSpan / 4
                    + PokecubeCore.getConfig().pokemobLifeSpan;
            float current_hunger = -(pokemob.getHungerTime() - PokecubeCore.getConfig().pokemobLifeSpan);
            final float scale = 100f / full_hunger;
            current_hunger *= scale / 100f;
            current_hunger = Math.min(1, current_hunger);
            if (current_hunger < 0.5)
            {
                int dv = -1 * 14;
                gui.blit(evt.getMat(), statusOffsetX, statusOffsetY, 0, 138 + dv, 15, 15);
            }

            // Render Status
            final int status = pokemob.getStatus();
            if (status != IMoveConstants.STATUS_NON)
            {
                int dv = 0;
                if ((status & IMoveConstants.STATUS_FRZ) != 0) dv = 1 * 14;
                if ((status & IMoveConstants.STATUS_BRN) != 0) dv = 2 * 14;
                if ((status & IMoveConstants.STATUS_PAR) != 0) dv = 3 * 14;
                if ((status & IMoveConstants.STATUS_PSN) != 0) dv = 4 * 14;
                gui.blit(evt.getMat(), statusOffsetX, statusOffsetY, 0, 138 + dv, 15, 15);
            }
            if ((pokemob.getChanges() & IMoveConstants.CHANGE_CONFUSED) != 0)
            {
                evt.getMat().translate(0, 0, 100);
                gui.blit(evt.getMat(), confuseOffsetX, confuseOffsetY, 0, 211, 24, 16);
                evt.getMat().translate(0, 0, -100);
            }

            // Render Name
            if (currentMoveIndex == 5) RenderSystem.setShaderColor(0.0F, 1.0F, 0.4F, 1.0F);
            gui.blit(evt.getMat(), nameOffsetX, nameOffsetY, 44, 0, 89, 13);

            gui.getFont().draw(evt.getMat(), displayName, nameOffsetX + 3, nameOffsetY + 3,
                    GuiDisplayPokecubeInfo.lightGrey);

            // Render level
            gui.getFont().draw(evt.getMat(), "Lvl " + level, nameOffsetX + 82 - gui.getFont().width("Lvl" + level),
                    nameOffsetY + 3, GuiDisplayPokecubeInfo.lightGrey);

            // Draw number of pokemon
            RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
            RenderSystem.enableBlend();
            var info = GuiDisplayPokecubeInfo.instance();
            final int n = info.getPokemobsToDisplay().length;
            final int n2 = info.indexPokemob + 1;
            String txt = n == 1 ? n + "" : n2 + "/" + n;
            final int num = gui.getFont().width(txt);
            evt.getMat().pushPose();
            evt.getMat().translate(nameOffsetX + 89, nameOffsetY, 0);

            if (num > 8) gui.blit(evt.getMat(), 1, 0, 150, 0, 30, 15);
            else gui.blit(evt.getMat(), 1, 0, 134, 0, 15, 15);

            evt.getMat().popPose();
            if (num > 8) gui.getFont().draw(evt.getMat(), txt, nameOffsetX + 99 - num / 4, nameOffsetY + 4,
                    GuiDisplayPokecubeInfo.lightGrey);
            else gui.getFont().draw(evt.getMat(), txt, nameOffsetX + 95 - num / 4, nameOffsetY + 4,
                    GuiDisplayPokecubeInfo.lightGrey);

            // Render Moves
            for (moveCount = 0; moveCount < 4; moveCount++) if (pokemob.getMove(moveCount) == null) break;
            int h = 0;
            if (dir == -1) h -= 14 + 12 * (moveCount - 1) - (4 - moveCount) * 2;
            for (moveIndex = 0; moveIndex < 4; moveIndex++)
            {
                final int index = moveIndex;

                final MoveEntry move = MovesUtils.getMove(pokemob.getMove(index));
                final boolean disabled = index >= 0 && index < 4 && pokemob.getDisableTimer(index) > 0;
                if (move != null)
                {

                    // bind texture
                    evt.getMat().pushPose();

                    RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);
                    RenderSystem.enableBlend();
                    gui.blit(evt.getMat(), movesOffsetX, movesOffsetY + 13 * index + h, 43, 22, 91, 13);

                    // Render colour overlays.
                    if (currentMoveIndex == index)
                    {
                        // Draw selected indictator
                        RenderSystem.enableBlend();
                        gui.blit(evt.getMat(), movesOffsetX, movesOffsetY + 13 * index + h, 43, 65, 91, 13);
                        // Draw cooldown box
                        float timer = 1;
                        MoveEntry lastMove;
                        if (MovesUtils.isAbleToUseMoves(pokemob) != AbleStatus.ABLE) timer = 0;
                        else if ((lastMove = MovesUtils.getMove(pokemob.getLastMoveUsed())) != null)
                            timer -= pokemob.getAttackCooldown() / (float) MovesUtils.getAttackDelay(pokemob,
                                    pokemob.getLastMoveUsed(), lastMove.isRanged(pokemob), false);
                        timer = Math.max(0, Math.min(timer, 1));
                        RenderSystem.enableBlend();
                        gui.blit(evt.getMat(), movesOffsetX, movesOffsetY + 13 * index + h, 43, 35, (int) (91 * timer),
                                13);
                    }
                    if (disabled) gui.blit(evt.getMat(), movesOffsetX, movesOffsetY + 13 * index + h, 43, 65, 91, 13);

                    evt.getMat().popPose();
                    evt.getMat().pushPose();
                    gui.getFont().draw(evt.getMat(), MovesUtils.getMoveName(move.getName(), pokemob).getString(),
                            5 + movesOffsetX, index * 13 + movesOffsetY + 3 + h, move.getType(pokemob).colour);
                    evt.getMat().popPose();
                }
            }

            // Render Mob
            RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);

            int mobOffsetX = 0;
            int mobOffsetY = 0;
            RenderSystem.enableBlend();
            gui.blit(evt.getMat(), mobOffsetX, mobOffsetY, 0, 0, 42, 42);

            LivingEntity mob = pokemob.getEntity();

            float f = 30;
            float yBodyRot = mob.yBodyRot;
            float yBodyRotO = mob.yBodyRotO;
            float yHeadRot = mob.yHeadRot;
            float yHeadRotO = mob.yHeadRotO;

            mob.yBodyRot = mob.yBodyRotO = 180.0F + f * 20.0F;
            mob.yHeadRot = mob.yHeadRotO = mob.yBodyRot;

            GuiPokemobHelper.renderMob(evt.getMat(), mob, mobOffsetX - 30, mobOffsetY - 25, 0, 0, 0, 0, 0.75f,
                    Minecraft.getInstance().getFrameTime());

            mob.yBodyRot = yBodyRot;
            mob.yBodyRotO = yBodyRotO;
            mob.yHeadRot = yHeadRot;
            mob.yHeadRotO = yHeadRotO;

            LivingEntity ally = pokemob.getMoveStats().targetAlly;
            if (ally != null && ally != pokemob.getEntity())
            {
                evt.getMat().pushPose();

                evt.getMat().scale(0.5f, 0.5f, 0.5f);

                RenderSystem.setShaderTexture(0, Resources.GUI_BATTLE);

                mobOffsetX = 45;
                mobOffsetY = 80;
                RenderSystem.enableBlend();
                gui.blit(evt.getMat(), mobOffsetX, mobOffsetY, 0, 0, 42, 42);

                mob = ally;

                f = 30;
                yBodyRot = mob.yBodyRot;
                yBodyRotO = mob.yBodyRotO;
                yHeadRot = mob.yHeadRot;
                yHeadRotO = mob.yHeadRotO;

                mob.yBodyRot = mob.yBodyRotO = 180.0F + f * 20.0F;
                mob.yHeadRot = mob.yHeadRotO = mob.yBodyRot;

                GuiPokemobHelper.renderMob(evt.getMat(), mob, mobOffsetX - 30, mobOffsetY - 25, 0, 0, 0, 0, 0.75f,
                        Minecraft.getInstance().getFrameTime());

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