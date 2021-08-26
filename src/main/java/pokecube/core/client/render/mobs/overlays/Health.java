package pokecube.core.client.render.mobs.overlays;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.entity.PartEntity;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.Tools;
import thut.core.common.ThutCore;
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

    private static final RenderType TYPE       = RenderType.text(Resources.GUI_BATTLE);
    private static final RenderType BACKGROUND = RenderType.textSeeThrough(Resources.GUI_BATTLE);

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

    public static IFormattableTextComponent obfuscate(final ITextComponent compIn)
    {
        final ITextComponent comp = compIn;
        String val = comp.getString();
        final Random rand = ThutCore.newRandom();
        final char[] chars = val.toCharArray();
        for (int i = 0; i < val.length(); i++)
            for (int j = 0; j < 10; j++)
            {
                final int rng = rand.nextInt(256);
                if (Character.isAlphabetic(rng))
                {
                    chars[i] = (char) rng;
                    break;
                }
            }
        val = new String(chars);
        return new StringTextComponent(val).setStyle(compIn.getStyle());
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
        buffer.vertex(pos, x1, y1, z).color(r, g, b, a).uv(u0, v1).uv2(brightness).endVertex();
        buffer.vertex(pos, x1, y2, z).color(r, g, b, a).uv(u1, v1).uv2(brightness).endVertex();
        buffer.vertex(pos, x2, y2, z).color(r, g, b, a).uv(u1, v0).uv2(brightness).endVertex();
        buffer.vertex(pos, x2, y1, z).color(r, g, b, a).uv(u0, v0).uv2(brightness).endVertex();
    }

    public static void renderHealthBar(final LivingEntity passedEntity, final MatrixStack mat,
            final IRenderTypeBuffer buf, final float partialTicks, final Entity viewPoint, final int br)
    {
        final Stack<LivingEntity> ridingStack = new Stack<>();

        LivingEntity entity = passedEntity;

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        if (pokemob == null || !entity.inChunk || !pokemob.getPokedexEntry().stock) return;
        if (entity.distanceTo(viewPoint) > PokecubeCore.getConfig().maxDistance) return;
        final Config config = PokecubeCore.getConfig();
        final Minecraft mc = Minecraft.getInstance();
        final EntityRendererManager renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        if (renderManager == null || renderManager.camera == null) return;
        if (PokecubeCore.getConfig().showOnlyFocused && entity != renderManager.crosshairPickEntity) return;
        final ActiveRenderInfo viewer = renderManager.camera;

        final boolean background = config.drawBackground && entity.canSee(viewer.getEntity());

        if (entity.getPassengers().contains(viewer.getEntity())) return;

        final UUID viewerID = viewer.getEntity().getUUID();

        ridingStack.push(entity);

        while (entity.getVehicle() != null && entity.getVehicle() instanceof LivingEntity)
        {
            entity = (LivingEntity) entity.getVehicle();
            ridingStack.push(entity);
        }

        IVertexBuilder buffer;
        Matrix4f pos;

        mat.pushPose();
        processing:
        {

            final float scale = .02f;
            final float maxHealth = entity.getMaxHealth();
            final float health = Math.min(maxHealth, entity.getHealth());

            if (maxHealth <= 0) break processing;

            double dy = entity.getBbHeight();

            if (entity.isMultipartEntity())
            {
                dy = 0;
                for (final PartEntity<?> part : entity.getParts())
                    dy = Math.max(dy, part.getBbHeight() + part.getY() - entity.getY());
            }

            mat.translate(0, dy + config.heightAbove, 0);
            Quaternion quaternion;
            quaternion = viewer.rotation();
            mat.mulPose(quaternion);
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
            if (pokemob.getOwner() == viewer.getEntity()) stack = entity.getMainHandItem();
            final float hue = Math.max(0F, health / maxHealth / 3F - 0.07F);
            final Color color = Color.getHSBColor(hue, 0.8F, 0.8F);
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
            IFormattableTextComponent nameComp = (IFormattableTextComponent) pokemob.getDisplayName();
            final boolean obfuscated = Health.obfuscateName(pokemob);
            if (obfuscated) nameComp = Health.obfuscate(nameComp);
            if (entity instanceof MobEntity && ((MobEntity) entity).hasCustomName())
                nameComp = (IFormattableTextComponent) ((MobEntity) entity).getCustomName();

            final float s = 0.5F;
            final String name = nameComp.getString();
            final float namel = mc.font.width(name) * s;
            if (namel + 20 > size * 2) size = namel / 2F + 10F;
            float healthSize = size * (health / maxHealth);
            mat.mulPose(Vector3f.YP.rotationDegrees(180));
            mat.mulPose(Vector3f.XP.rotationDegrees(180));

            pos = mat.last().pose();
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

            mat.pushPose();
            mat.translate(-size, -4.5F, 0F);
            mat.scale(s, s, s);

            final UUID owner = pokemob.getOwnerId();
            final boolean isOwner = viewerID.equals(owner);
            final boolean fullColour = Health.fullNameColour(pokemob) && !isOwner;
            int colour = config.unknownNameColour;

            if (fullColour) colour = owner != null ? config.otherOwnedNameColour : config.caughtNamedColour;
            else if (isOwner) colour = config.ownedNameColour;
            else if (!obfuscated) colour = config.scannedNameColour;

            mat.pushPose();
            float s1 = 0.75F;
            mat.scale(s1, s1, s1);

            mat.pushPose();
            s1 = 1.5F;
            mat.scale(s1, s1, s1);
            mc.font.draw(mat, nameComp.getString(), 0, 0, colour);
            s1 = 0.75F;
            mat.popPose();

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
            if (isOwner) mc.font.draw(mat, healthStr, (int) (size / (s * s1)) - mc.font
                    .width(healthStr) / 2, h, 0xFFFFFFFF);

            pos = mat.last().pose();
            mc.font.drawInBatch(lvlStr, 2, h, 0xFFFFFF, false, pos, buf, false, 0, br);
            mc.font.drawInBatch(gender, (int) (size / (s * s1) * 2) - 2 - mc.font.width(
                    gender), h - 1, colour, false, pos, buf, false, 0, br);

            if (PokecubeCore.getConfig().enableDebugInfo && mc.options.renderDebug)
            {
                final String entityID = entity.getEncodeId().toString();
                mc.font.draw(mat, "ID: \"" + entityID + "\"" + "(" + entity.getId() + ")", 0, h
                        + 16, 0xFFFFFFFF);
            }
            mat.popPose();

            int off = 0;
            s1 = 0.5F;
            mat.scale(s1, s1, s1);
            mat.translate(size / (s * s1) * 2 - 16, 0F, 0F);

            if (!stack.isEmpty() && config.showHeldItem) Health.renderIcon(entity, mat, buf, off, 0, stack, 16, 16, br);
            off -= 16;

            final int armor = entity.getArmorValue();
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
            mat.popPose();
        }
        mat.popPose();
    }

    public static void renderIcon(final LivingEntity mob, final MatrixStack mat, final IRenderTypeBuffer buf,
            final int vertexX, final int vertexY, final ItemStack stack, final int intU, final int intV, final int br)
    {
        mat.pushPose();
        try
        {
            mat.translate(vertexX, vertexY + 7, 0);
            mat.scale(20, -20, -1);
            Minecraft.getInstance().getItemRenderer().renderStatic(mob, stack,
                    net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType.GUI, false, mat, buf, mob
                            .getCommandSenderWorld(), br, OverlayTexture.NO_OVERLAY);
        }
        catch (final Exception e)
        {
        }
        mat.popPose();
    }
}
