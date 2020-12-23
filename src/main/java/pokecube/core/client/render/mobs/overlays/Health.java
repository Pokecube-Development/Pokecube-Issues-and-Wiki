package pokecube.core.client.render.mobs.overlays;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.IFormattableTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.Tools;
import thut.core.common.handlers.PlayerDataHandler;

/**
 * This health renderer is directly based on Neat vy Vaziki, which can be found
 * here: https://github.com/Vazkii/Neat This version has been modified to only
 * apply to pokemobs, as well as to show level, gender and exp. I have also
 * modified the nametags to indicate ownership
 */
public class Health
{
    static List<LivingEntity> renderedEntities = new ArrayList<>();

    private static final RenderType TYPE       = RenderType.getText(Resources.GUI_BATTLE);
    private static final RenderType BACKGROUND = RenderType.getTextSeeThrough(Resources.GUI_BATTLE);

    public static boolean fullNameColour(final IPokemob pokemob)
    {
        final boolean nametag = pokemob.getGeneralState(GeneralStates.TAMED);
        // Always full name if owned
        if (nametag) return true;
        final PokedexEntry name_entry = pokemob.getPokedexEntry();
        final boolean captureOrHatch = StatsCollector.getCaptured(name_entry, Minecraft.getInstance().player) > 0
                || StatsCollector.getHatched(name_entry, Minecraft.getInstance().player) > 0;
        return captureOrHatch;
    }

    public static boolean obfuscateName(final IPokemob pokemob)
    {
        boolean nametag = Health.fullNameColour(pokemob);
        final PokecubePlayerStats stats = PlayerDataHandler.getInstance().getPlayerData(Minecraft.getInstance().player)
                .getData(PokecubePlayerStats.class);
        nametag = nametag || stats.hasInspected(pokemob.getPokedexEntry());
        return !nametag;
    }

    public static Entity getEntityLookedAt(final Entity e)
    {
        return Tools.getPointedEntity(e, 32);
    }

    private static void blit(final IVertexBuilder buffer, final Matrix4f pos, final float x1, final float y1,
            final float x2, final float y2, final float z, final int r, final int g, final int b, final int a,
            final int brightness)
    {
        final float u0 = 0;
        final float u1 = 90f / 256f;
        final float v0 = 48f / 256f;
        final float v1 = 64f / 256f;
        buffer.pos(pos, x1, y1, z).color(r, g, b, a).tex(u0, v1).lightmap(brightness).endVertex();
        buffer.pos(pos, x1, y2, z).color(r, g, b, a).tex(u1, v1).lightmap(brightness).endVertex();
        buffer.pos(pos, x2, y2, z).color(r, g, b, a).tex(u1, v0).lightmap(brightness).endVertex();
        buffer.pos(pos, x2, y1, z).color(r, g, b, a).tex(u0, v0).lightmap(brightness).endVertex();
    }

    public static void renderHealthBar(final LivingEntity passedEntity, final MatrixStack mat,
            final IRenderTypeBuffer buf, final float partialTicks, final Entity viewPoint, final int br)
    {
        final Stack<LivingEntity> ridingStack = new Stack<>();

        LivingEntity entity = passedEntity;

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null || !entity.addedToChunk) return;
        if (entity.getDistance(viewPoint) > PokecubeCore.getConfig().maxDistance) return;
        final Config config = PokecubeCore.getConfig();
        final Minecraft mc = Minecraft.getInstance();
        final EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
        if (renderManager == null || renderManager.info == null) return;
        if (PokecubeCore.getConfig().showOnlyFocused && entity != renderManager.pointedEntity) return;
        final ActiveRenderInfo viewer = renderManager.info;

        final boolean background = config.drawBackground && entity.canEntityBeSeen(viewer.getRenderViewEntity());

        if (entity.getPassengers().contains(viewer.getRenderViewEntity())) return;

        final UUID viewerID = viewer.getRenderViewEntity().getUniqueID();

        ridingStack.push(entity);

        while (entity.getRidingEntity() != null && entity.getRidingEntity() instanceof LivingEntity)
        {
            entity = (LivingEntity) entity.getRidingEntity();
            ridingStack.push(entity);
        }

        IVertexBuilder buffer;
        Matrix4f pos;

        mat.push();
        processing:
        {

            final float scale = .02f;
            final float maxHealth = entity.getMaxHealth();
            final float health = Math.min(maxHealth, entity.getHealth());

            if (maxHealth <= 0) break processing;

            final double dy = pokemob.getCombatState(CombatStates.DYNAMAX) ? config.dynamax_scale
                    : pokemob.getPokedexEntry().height * pokemob.getSize();
            mat.translate(0, dy + config.heightAbove, 0);
            Quaternion quaternion;
            quaternion = viewer.getRotation();
            mat.rotate(quaternion);
            mat.scale(scale, scale, scale);

            final float padding = config.backgroundPadding;
            final int bgHeight = config.backgroundHeight;
            final int barHeight1 = config.barHeight;
            float size = config.plateSize;

            float zlevel = 0.0f;
            int r = 0;
            int g = 220;
            int b = 0;
            ItemStack stack = ItemStack.EMPTY;
            if (pokemob.getOwner() == viewer.getRenderViewEntity()) stack = entity.getHeldItemMainhand();
            final float hue = Math.max(0F, health / maxHealth / 3F - 0.07F);
            final Color color = Color.getHSBColor(hue, 0.8F, 0.8F);
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
            IFormattableTextComponent nameComp = (IFormattableTextComponent) pokemob.getDisplayName();
            final boolean obfuscated = Health.obfuscateName(pokemob);
            if (obfuscated) nameComp.setStyle(nameComp.getStyle().setObfuscated(true));
            if (entity instanceof MobEntity && ((MobEntity) entity).hasCustomName())
                nameComp = (IFormattableTextComponent) ((MobEntity) entity).getCustomName();
            final float s = 0.5F;
            final String name = nameComp.getString();
            final float namel = mc.fontRenderer.getStringWidth(name) * s;
            if (namel + 20 > size * 2) size = namel / 2F + 10F;
            float healthSize = size * (health / maxHealth);
            mat.rotate(Vector3f.YP.rotationDegrees(180));
            mat.rotate(Vector3f.XP.rotationDegrees(180));

            pos = mat.getLast().getMatrix();
            // Background
            if (background)
            {
                buffer = Utils.makeBuilder(Health.BACKGROUND, buf);
                final int a = 32;
                Health.blit(buffer, pos, -size - padding, -bgHeight, size + padding, barHeight1 + padding, zlevel, 0, 0,
                        0, a, br);
                zlevel += 0.001f;
            }
            buffer = Utils.makeBuilder(Health.TYPE, buf);

            // Health bar
            // Gray Space
            healthSize = healthSize * 2 - size;
            Health.blit(buffer, pos, healthSize, 0, size, barHeight1, zlevel, 100, 127, 100, 255, br);
            zlevel += 0.001f;
            // Health Bar Fill
            Health.blit(buffer, pos, -size, 0, healthSize, barHeight1, zlevel, r, g, b, 255, br);
            zlevel += 0.001f;

            // Exp Bar
            r = 64;
            g = 64;
            b = 220;

            float exp = pokemob.getExp() - Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel());
            float maxExp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel() + 1) - Tools.levelToXp(
                    pokemob.getExperienceMode(), pokemob.getLevel());
            if (pokemob.getLevel() == 100) maxExp = exp = 1;
            if (exp < 0 || !pokemob.getGeneralState(GeneralStates.TAMED)) exp = 0;

            float expSize = size * (exp / maxExp);

            expSize = expSize * 2 - size;
            // Gray Space
            Health.blit(buffer, pos, expSize, barHeight1, size, barHeight1 + 1, zlevel, 100, 100, 127, 255, br);
            zlevel += 0.001f;

            // Exp Bar Fill
            Health.blit(buffer, pos, -size, barHeight1, expSize, barHeight1 + 1, zlevel, r, g, b, 255, br);
            zlevel += 0.001f;

            mat.push();
            mat.translate(-size, -4.5F, 0F);
            mat.scale(s, s, s);

            final UUID owner = pokemob.getOwnerId();
            final boolean isOwner = viewerID.equals(owner);
            final boolean fullColour = Health.fullNameColour(pokemob) && !isOwner;
            int colour = config.unknownNameColour;

            if (fullColour) colour = owner != null ? config.otherOwnedNameColour : config.caughtNamedColour;
            else if (isOwner) colour = config.ownedNameColour;
            else if (!obfuscated) colour = config.scannedNameColour;

            mat.push();
            float s1 = 0.75F;
            mat.scale(s1, s1, s1);

            mat.push();
            s1 = 1.5F;
            mat.scale(s1, s1, s1);
            pos = mat.getLast().getMatrix();
            final IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance()
                    .getBuffer());
            mc.fontRenderer.func_238416_a_(nameComp.func_241878_f(), 0, 0, colour, false, pos, irendertypebuffer$impl,
                    false, 0, 15728880);
            irendertypebuffer$impl.finish();
            s1 = 0.75F;
            mat.pop();

            final int h = config.hpTextHeight;
            String maxHpStr = "" + (int) (Math.round(maxHealth * 100.0) / 100.0);
            String hpStr = "" + (int) (Math.round(health * 100.0) / 100.0);
            final String healthStr = hpStr + "/" + maxHpStr;
            final String gender = pokemob.getSexe() == IPokemob.MALE ? "\u2642"
                    : pokemob.getSexe() == IPokemob.FEMALE ? "\u2640" : "";
            final String lvlStr = "L." + pokemob.getLevel();

            if (maxHpStr.endsWith(".0")) maxHpStr = maxHpStr.substring(0, maxHpStr.length() - 2);
            if (hpStr.endsWith(".0")) hpStr = hpStr.substring(0, hpStr.length() - 2);
            colour = 0xBBBBBB;
            if (pokemob.getSexe() == IPokemob.MALE) colour = 0x0011CC;
            else if (pokemob.getSexe() == IPokemob.FEMALE) colour = 0xCC5555;
            if (isOwner) mc.fontRenderer.drawString(mat, healthStr, (int) (size / (s * s1)) - mc.fontRenderer
                    .getStringWidth(healthStr) / 2, h, 0xFFFFFFFF);

            pos = mat.getLast().getMatrix();
            mc.fontRenderer.renderString(lvlStr, 2, h, 0xFFFFFF, false, pos, buf, false, 0, br);
            mc.fontRenderer.renderString(gender, (int) (size / (s * s1) * 2) - 2 - mc.fontRenderer.getStringWidth(
                    gender), h - 1, colour, false, pos, buf, false, 0, br);

            if (PokecubeCore.getConfig().enableDebugInfo && mc.gameSettings.showDebugInfo)
            {
                final String entityID = entity.getEntityString().toString();
                mc.fontRenderer.drawString(mat, "ID: \"" + entityID + "\"" + "(" + entity.getEntityId() + ")", 0, h
                        + 16, 0xFFFFFFFF);
            }
            mat.pop();

            int off = 0;
            s1 = 0.5F;
            mat.scale(s1, s1, s1);
            mat.translate(size / (s * s1) * 2 - 16, 0F, 0F);

            if (!stack.isEmpty() && config.showHeldItem) Health.renderIcon(entity, mat, buf, off, 0, stack, 16, 16, br);
            off -= 16;

            final int armor = entity.getTotalArmorValue();
            if (armor > 0 && config.showArmor)
            {
                final int ironArmor = armor % 5;
                final int diamondArmor = armor / 5;
                stack = new ItemStack(Items.IRON_CHESTPLATE);
                for (int i = 0; i < ironArmor; i++)
                    Health.renderIcon(entity, mat, buf, off, 0, stack, 16, 16, br);
                off -= 4;

                stack = new ItemStack(Items.DIAMOND_CHESTPLATE);
                for (int i = 0; i < diamondArmor; i++)
                    Health.renderIcon(entity, mat, buf, off, 0, stack, 16, 16, br);
                off -= 4;
            }

            mat.pop();
        }
        mat.pop();
    }

    public static void renderIcon(final LivingEntity mob, final MatrixStack mat, final IRenderTypeBuffer buf,
            final int vertexX, final int vertexY, final ItemStack stack, final int intU, final int intV, final int br)
    {
        mat.push();
        try
        {
            mat.translate(vertexX, vertexY + 7, 0);
            mat.scale(20, -20, -1);
            Minecraft.getInstance().getItemRenderer().renderItem(mob, stack,
                    net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.GUI, false, mat, buf, mob
                            .getEntityWorld(), br, OverlayTexture.NO_OVERLAY);
        }
        catch (final Exception e)
        {
        }
        mat.pop();
    }
}
