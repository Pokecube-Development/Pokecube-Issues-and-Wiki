package pokecube.core.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.PokecubeCore;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;

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
        var gui = Minecraft.getInstance().gui;
        var info = GuiDisplayPokecubeInfo.instance();

        final int dir = PokecubeCore.getConfig().guiDown ? 1 : -1;
        final int nameOffsetX = 43;
        final int nameOffsetY = dir == 1 ? 0 : 23;
        final int hpOffsetX = 42;
        final int hpOffsetY = 13;
        final int statusOffsetX = 2;
        final int statusOffsetY = 27;
        final int confuseOffsetX = 14;
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
            graphics.blitSprite(ICON_HEALTH_EXP[0], hpOffsetX, hpOffsetY, 89, 7);
            final float total = entity.getMaxHealth();
            final float ratio = entity.getHealth() / total;
            final int width = (int) (89 * ratio);
            graphics.blitSprite(ICON_HEALTH_EXP[1], hpOffsetX, hpOffsetY, width, 7);

            // Render number of enemies
            RenderSystem.enableBlend();
            final int n = pokemob.getEnemyNumber();
            if (n > 1)
            {
                final int n2 = pokemob.getMoveStats().enemyIndex + 1;
                String txt = n == 1 ? n + "" : n2 + "/" + n;
                int num = gui.getFont().width(txt);

                graphics.blitSprite(ICON_NUMBER_FRAME, -num+2, 0, 1, num + 4, 15);
                graphics.drawString(gui.getFont(), txt, nameOffsetX - 39 - num, nameOffsetY + 4,
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
            }

            // Bar behind the name
            graphics.blitSprite(ICON_MOVE_FRAMES[2], nameOffsetX, nameOffsetY, 89, 13);
            String displayName = entity.getDisplayName().getString();
            if (gui.getFont().width(displayName) > 70)
            {
                float _ratio = 70f / gui.getFont().width(displayName);
                // TODO scroll the name isntead of culling it?
                displayName = displayName.substring(0, (int) (displayName.length() * _ratio));
            }
            // Render Name
            graphics.drawString(gui.getFont(), displayName, nameOffsetX + 3, nameOffsetY + 3,
                    GuiDisplayPokecubeInfo.lightGrey);

            RenderSystem.enableBlend();
            // Render Box behind Mob
            graphics.blitSprite(ICON_MOB_FRAME, 1, 0, -2, 42, 42);
            // Render Mob

            float f = 30;
            float yBodyRot = entity.yBodyRot;
            float yBodyRotO = entity.yBodyRotO;
            float yHeadRot = entity.yHeadRot;
            float yHeadRotO = entity.yHeadRotO;

            entity.yBodyRot = entity.yBodyRotO = 180.0F + f * 20.0F;
            entity.yHeadRot = entity.yHeadRotO = entity.yBodyRot;

            float tick = evt.getTick();
            GuiPokemobHelper.renderMob(evt.getMat(), entity, -30, -25, 0, 0, 0, 0, 0.75f, tick);

            entity.yBodyRot = yBodyRot;
            entity.yBodyRotO = yBodyRotO;
            entity.yHeadRot = yHeadRot;
            entity.yHeadRotO = yHeadRotO;

            evt.getMat().popPose();
        }
    }

}
