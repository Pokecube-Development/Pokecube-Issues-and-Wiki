package pokecube.core.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.GuiEvent;
import pokecube.core.client.gui.GuiDisplayPokecubeInfo;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.init.Config;
import pokecube.core.moves.damage.effects.StatusEffects;
import pokecube.core.network.packets.PacketSyncBattle;
import pokecube.core.network.pokemobs.PacketBattleTargets;
import thut.api.Tracker;

import java.util.UUID;

public class TargetInfo extends GuiEventComponent
{
    public static LivingEntity lastViewedTarget;

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
        final int statusOffsetX = 4;
        final int statusOffsetY = 30;
        final int confuseOffsetX = 14;
        final int confuseOffsetY = 1;
        IPokemob pokemob = info.getCurrentPokemob();
        final Config config = PokecubeCore.getConfig();

        var renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        var viewer = renderManager.camera;
        var viewerID = viewer.getEntity().getUUID();

        var nameBGSprite = ICON_MOVE_FRAMES[0];
        int n = 1, n2 = 1;
        LivingEntity target = null;
        boolean combatTarget = false;
        {
            var list = PacketSyncBattle.getEnemies();
            if (!list.isEmpty())
            {
                combatTarget = true;
                n = list.size();
                n2 = PacketBattleTargets.manualTargetIndex % n;
                if (n2 < 0)
                {
                    n2 += n;
                    n2 %= n;
                }
                target = list.get(n2);
                if (pokemob != null)
                {
                    var other = pokemob.getMoveStats().targetEnemy;
                    if (other != target)
                    {
                        var packet = new PacketBattleTargets(pokemob.getEntity().getId(), (byte) 3, target.getId());
                        PokecubeCore.packets.sendToServer(packet);
                        pokemob.getMoveStats().targetEnemy = target;
                    }
                    if (other != null) other.getBbWidth();
                }
                n2++;
                nameBGSprite = ICON_MOVE_FRAMES[2];
            }
        }
        if (config.displayViewedInfo && target == null)
        {
            if (EventsHandlerClient.hovorTarget instanceof LivingEntity living && (pokemob == null
                    || living != pokemob.getEntity()))
            {
                target = living;
            }
            else
            {
                if (renderManager.crosshairPickEntity instanceof LivingEntity living && (pokemob == null
                        || living != pokemob.getEntity()))
                {
                    target = living;
                }
            }
        }
        // Return if no target, dead target, or same as our sent out mob
        if (target == null || !target.isAlive() || (pokemob != null && target == pokemob.getTrackedEntity()))
        {
            lastViewedTarget = null;
            return;
        }

        pokemob = PokemobCaps.getPokemobFor(target);
        if (pokemob == null && !combatTarget && target.shouldShowName())
        {
            lastViewedTarget = null;
            return;
        }
        if (pokemob == null && config.onlyDisplayViewedPokemob)
        {
            lastViewedTarget = null;
            return;
        }

        lastViewedTarget = target;

        MutableComponent nameComp;
        if (target.getDisplayName() instanceof MutableComponent c) nameComp = c;
        else nameComp = MutableComponent.create(target.getDisplayName().getContents());

        evt.getMat().pushPose();
        // global translate
        evt.getMat().translate(this.pos.x0, this.pos.y0, 0);
        // Now translate us to the box itself
        evt.getMat().translate(18, 0, 0);

        RenderSystem.enableBlend();
        // Render Box behind Mob
        graphics.blitSprite(ICON_MOB_FRAME, 1, 0, -2, 42, 42);

        // Render HP
        graphics.blitSprite(ICON_HEALTH_EXP[0], hpOffsetX, hpOffsetY, 89, 7);
        final float total = target.getMaxHealth();
        final float ratio = target.getHealth() / total;
        final int width = (int) (89 * ratio);
        graphics.blitSprite(ICON_HEALTH_EXP[1], hpOffsetX, hpOffsetY, width, 7);

        // Render number of enemies
        RenderSystem.enableBlend();
        if (n > 1)
        {
            String txt = n2 + "/" + n;
            int num = gui.getFont().width(txt);
            graphics.blitSprite(ICON_NUMBER_FRAME, -num + 2, 0, 1, num + 4, 15);
            graphics.drawString(gui.getFont(), txt, nameOffsetX - 39 - num, nameOffsetY + 4,
                    GuiDisplayPokecubeInfo.lightGrey);
        }

        // Bar behind the name
        graphics.blitSprite(nameBGSprite, nameOffsetX, nameOffsetY, 89, 13);

        float mobScale = 0.8f / target.getBbHeight();
        float scale2 = 0.5f / target.getBbWidth();

        mobScale = Math.min(mobScale, scale2);

        // Render Status
        int colour = config.unknownNameColour;
        float nameMaxLen = 85;
        if (pokemob != null)
        {
            mobScale = 0.75f;
            boolean obfuscated = PokecubePlayerStats.obfuscateName(pokemob);

            UUID owner = pokemob.getOwnerId();
            boolean isOwner = viewerID.equals(owner);
            boolean fullColour = PokecubePlayerStats.fullNameColour(pokemob) && !isOwner;

            if (fullColour) colour = owner != null ? config.otherOwnedNameColour : config.caughtNamedColour;
            else if (isOwner) colour = config.ownedNameColour;
            else if (!obfuscated) colour = config.scannedNameColour;
            if(obfuscated) nameComp = PokecubePlayerStats.obfuscate(nameComp);

            if (combatTarget)
            {
                // Render status icons if present
                RenderSystem.enableBlend();
                var status = StatusEffects.getStatusEffect(pokemob.getEntity());
                int dv = 0;
                if (status != null && status.getDuration() != 0)
                {
                    if (StatusEffects.BURN.is(status.getEffect().getKey())) dv = 0;
                    if (StatusEffects.FREEZE.is(status.getEffect().getKey())) dv = 1;
                    if (StatusEffects.PARALYSIS.is(status.getEffect().getKey())) dv = 2;
                    if (StatusEffects.POISON.is(status.getEffect().getKey())) dv = 3;
                    if (StatusEffects.SLEEP.is(status.getEffect().getKey())) dv = 4;
                    graphics.blitSprite(STATUS_ICONS[dv], statusOffsetX, statusOffsetY, 0, 10, 10);
                }
                if (pokemob.getEntity().hasEffect(StatusEffects.CONFUSE))
                {
                    dv = 5;
                    graphics.blitSprite(STATUS_ICONS[dv], confuseOffsetX, confuseOffsetY, 100, 20, 14);
                }
            }
            // Otherwise we render held item in the status icon spot
            else if (isOwner && config.showHeldItem)
            {
                ItemStack stack = target.getMainHandItem();
                if(!stack.isEmpty())
                {
                    evt.getMat().pushPose();
                    float scale = 0.75f;
                    evt.getMat().scale(scale, scale, scale);
                    graphics.renderItem(stack, (int) (statusOffsetX / scale) - 2, (int) (statusOffsetY / scale) - 2);
                    evt.getMat().popPose();
                }
            }

            // Render Pokemob relevant info

            // Level
            String lvlStr = "L." + pokemob.getLevel();
            nameMaxLen -= gui.getFont().width(lvlStr);
            graphics.drawString(gui.getFont(), lvlStr, (int) (nameOffsetX + nameMaxLen + 2), nameOffsetY + 3, colour);

            // Sex
            String sexStr = pokemob.getSexe() == IPokemob.MALE ? "♂" : pokemob.getSexe() == IPokemob.FEMALE ? "♀" : "";
            if (!sexStr.isEmpty())
            {
                int colour2 = colour;
                if (pokemob.getSexe() == IPokemob.MALE) colour2 = 0x0011CC;
                else if (pokemob.getSexe() == IPokemob.FEMALE) colour2 = 0xCC5555;
                nameMaxLen -= gui.getFont().width(sexStr);
                graphics.drawString(gui.getFont(), sexStr, (int) (nameOffsetX + nameMaxLen + 1), nameOffsetY + 3,
                        colour2);
            }

            nameMaxLen -= 3; // Add some space between this and actual name
        }

        var displayName = nameComp.getString();
        if (gui.getFont().width(nameComp) > nameMaxLen)
        {
            float _ratio = nameMaxLen / gui.getFont().width(nameComp);
            int total_len = displayName.length();
            int maxLen = (int) (total_len * _ratio);
            int missing = total_len - maxLen;
            if (missing > 0)
            {
                int offset = (int) ((Tracker.instance().getTick() / 5) % (missing + 1));
                displayName = displayName.substring(offset, maxLen + offset);
            }
        }
        // Render Name
        graphics.drawString(gui.getFont(), displayName, nameOffsetX + 3, nameOffsetY + 3, colour);

        RenderSystem.enableBlend();
        // Render Mob

        float f = 30;
        float yBodyRot = target.yBodyRot;
        float yBodyRotO = target.yBodyRotO;
        float yHeadRot = target.yHeadRot;
        float yHeadRotO = target.yHeadRotO;

        target.yBodyRot = target.yBodyRotO = 180.0F + f * 20.0F;
        target.yHeadRot = target.yHeadRotO = target.yBodyRot;

        float tick = evt.getTick();
        GuiPokemobHelper.renderMob(evt.getMat(), target, -30, -25, 0, 0, mobScale, tick, true);

        target.yBodyRot = yBodyRot;
        target.yBodyRotO = yBodyRotO;
        target.yHeadRot = yHeadRot;
        target.yHeadRotO = yHeadRotO;

        evt.getMat().popPose();

    }

}
