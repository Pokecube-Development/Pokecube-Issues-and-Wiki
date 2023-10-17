package pokecube.core.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.utils.Resources;

public class TargetInfo extends GuiEventComponent
{
    @Override
    protected void onMovedGui()
    {
        PokecubeCore.getConfig().guiTargetPos.set(0, this.bounds.x0);
        PokecubeCore.getConfig().guiTargetPos.set(1, this.bounds.y0);
        super.onMovedGui();
    }

    @Override
    protected void preDraw(GuiEvent event)
    {
        if (clickA == 0)
        {
            int x0 = PokecubeCore.getConfig().guiTargetPos.get(0);
            int y0 = PokecubeCore.getConfig().guiTargetPos.get(1);
            if (x0 != bounds.x0 || y0 != bounds.y0 || bounds.h == 0) this.bounds.setBox(x0, y0, 150, 42);
            this.ref = PokecubeCore.getConfig().targetRef;
        }
    }

    @Override
    public void _drawGui(GuiEvent evt)
    {
        var graphics = evt.getGraphics();
        var gui = evt.getGui();
        var info = GuiDisplayPokecubeInfo.instance();

        final int dir = PokecubeCore.getConfig().guiDown ? 1 : -1;
        final int nameOffsetX = dir == 1 ? 43 : 43;
        final int nameOffsetY = dir == 1 ? 0 : 23;
        final int hpOffsetX = 42;
        final int hpOffsetY = 13;
        final int statusOffsetX = 0;
        final int statusOffsetY = 27;
        final int confuseOffsetX = 12;
        final int confuseOffsetY = 1;
        IPokemob pokemob = info.getCurrentPokemob();
        render:
        if (pokemob != null)
        {
            LivingEntity entity = pokemob.getMoveStats().targetEnemy;
            if (entity == null || !entity.isAlive()) break render;

            evt.getMat().pushPose();
            // global translate
            evt.getMat().translate(this.pos.x0, this.pos.y0, 0);
            // Now translate us to the box itself
            evt.getMat().translate(18, 0, 0);

            // Render HP
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            graphics.blit(Resources.GUI_BATTLE, hpOffsetX, hpOffsetY, 43, 12, 92, 7);
            final float total = entity.getMaxHealth();
            final float ratio = entity.getHealth() / total;
            final int x = hpOffsetX + 1;
            final int y = hpOffsetY + 1;
            final int width = (int) (92 * ratio);
            graphics.blit(Resources.GUI_BATTLE, x, y, 0, 85, width, 5);

            // Render number of enemies
            RenderSystem.enableBlend();
            final int n = pokemob.getEnemyNumber();
            if (n > 1)
            {
                final int n2 = pokemob.getMoveStats().enemyIndex + 1;
                String txt = n == 1 ? n + "" : n2 + "/" + n;
                final int num = gui.getFont().width(txt);
                evt.getMat().pushPose();
                evt.getMat().translate(nameOffsetX - 43 - num, nameOffsetY, 0);
                if (num > 10) evt.getMat().scale(1.5f * num / 18f, 1, 1);
                graphics.blit(Resources.GUI_BATTLE, 0, 0, 0, 27, 15, 15);
                evt.getMat().popPose();
                graphics.drawString(gui.getFont(), txt, nameOffsetX - 43 - num + 2, nameOffsetY + 4,
                        GuiDisplayPokecubeInfo.lightGrey);
            }
            // Render Status
            pokemob = PokemobCaps.getPokemobFor(entity);
            if (pokemob != null)
            {
                final int status = pokemob.getStatus();
                if (status != IMoveConstants.STATUS_NON)
                {
                    int dv = 0;
                    if ((status & IMoveConstants.STATUS_BRN) != 0) dv = 2 * 14;
                    if ((status & IMoveConstants.STATUS_FRZ) != 0) dv = 1 * 14;
                    if ((status & IMoveConstants.STATUS_PAR) != 0) dv = 3 * 14;
                    if ((status & IMoveConstants.STATUS_PSN) != 0) dv = 4 * 14;
                    graphics.blit(Resources.GUI_BATTLE, statusOffsetX, statusOffsetY, 0, 138 + dv, 15, 15);
                }
                if ((pokemob.getChanges() & IMoveConstants.CHANGE_CONFUSED) != 0)
                {
                    evt.getMat().translate(0, 0, 100);
                    graphics.blit(Resources.GUI_BATTLE, confuseOffsetX, confuseOffsetY, 0, 211, 24, 16);
                    evt.getMat().translate(0, 0, -100);
                }
            }

            // Render Name
            graphics.blit(Resources.GUI_BATTLE, nameOffsetX, nameOffsetY, 44, 0, 90, 13);
            final String displayName = entity.getDisplayName().getString();
            if (gui.getFont().width(displayName) > 70)
            {

            }
            graphics.drawString(gui.getFont(), displayName, nameOffsetX + 3, nameOffsetY + 3,
                    GuiDisplayPokecubeInfo.lightGrey);

            // Render Box behind Mob
            RenderSystem.enableBlend();
            final int mobBoxOffsetX = 0;
            final int mobBoxOffsetY = 0;
            graphics.blit(Resources.GUI_BATTLE, mobBoxOffsetX, mobBoxOffsetY, 0, 0, 42, 42);
            // Render Mob

            LivingEntity mob = entity;

            float f = 30;
            float yBodyRot = mob.yBodyRot;
            float yBodyRotO = mob.yBodyRotO;
            float yHeadRot = mob.yHeadRot;
            float yHeadRotO = mob.yHeadRotO;

            mob.yBodyRot = mob.yBodyRotO = 180.0F + f * 20.0F;
            mob.yHeadRot = mob.yHeadRotO = mob.yBodyRot;

            GuiPokemobHelper.renderMob(evt.getMat(), entity, mobBoxOffsetX - 30, mobBoxOffsetY - 25, 0, 0, 0, 0, 0.75f,
                    Minecraft.getInstance().getFrameTime());

            mob.yBodyRot = yBodyRot;
            mob.yBodyRotO = yBodyRotO;
            mob.yHeadRot = yHeadRot;
            mob.yHeadRotO = yHeadRotO;

            evt.getMat().popPose();
        }
    }

}
