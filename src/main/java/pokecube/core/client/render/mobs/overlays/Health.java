package pokecube.core.client.render.mobs.overlays;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiFunction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.entity.PartEntity;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.eventhandlers.StatsCollector;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.init.Config;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.Resources;
import thut.api.entity.multipart.GenericPartEntity;
import thut.core.common.ThutCore;
import thut.core.common.handlers.PlayerDataHandler;
import thut.lib.AxisAngles;
import thut.lib.TComponent;

/**
 * This health renderer is directly based on Neat by Vaziki, which can be found
 * here: https://github.com/Vazkii/Neat This version has been modified to only
 * apply to pokemobs, as well as to show level, gender and exp. I have also
 * modified the nametags to indicate ownership
 */
public class Health
{
    static List<LivingEntity> renderedEntities = new ArrayList<>();

    public static final RenderType TYPE = RenderType.text(Resources.GUI_BATTLE);
    public static final RenderType BACKGROUND = RenderType.textSeeThrough(Resources.GUI_BATTLE);

    public static BiFunction<LivingEntity, Entity, Boolean> RENDER_HEALTH = (entity, viewPoint) -> {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        // Only apply to pokemobs in world
        if (pokemob == null || !entity.isAddedToWorld()) return false;
        final EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        // Some sanity checks
        if (renderManager == null || renderManager.camera == null) return false;
        // Only apply to stock ones, unless otherwise configured
        if (PokecubeCore.getConfig().nonStockHealthbars && !pokemob.getPokedexEntry().stock) return false;
        // Only apply if in range
        if (entity.distanceTo(viewPoint) > PokecubeCore.getConfig().maxDistance) return false;

        // For hovor target, or cross-hair stuff, only consider the root entity
        var rootHovor = EntityTools.getCoreEntity(EventsHandlerClient.hovorTarget);
        var rootCross = EntityTools.getCoreEntity(renderManager.crosshairPickEntity);
        boolean inCombat = pokemob.inCombat();

        // If we are set to only show focused, then only show that, only do this
        // if it isn't in combat.
        if (!inCombat && PokecubeCore.getConfig().showOnlyFocused && (entity != rootHovor && entity != rootCross))
            return false;
        final Camera viewer = renderManager.camera;
        // If viewer is riding us, do not show
        if (entity.getPassengers().contains(viewer.getEntity())) return false;
        // If we are riding a player, do not show
        if (entity.getVehicle() instanceof Player) return false;
        // Otherwise show.
        return true;
    };

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

    public static MutableComponent obfuscate(final Component compIn)
    {
        final Component comp = compIn;
        String val = comp.getString();
        final Random rand = ThutCore.newRandom();
        final char[] chars = val.toCharArray();
        for (int i = 0; i < val.length(); i++) for (int j = 0; j < 10; j++)
        {
            final int rng = rand.nextInt(256);
            if (Character.isAlphabetic(rng))
            {
                chars[i] = (char) rng;
                break;
            }
        }
        val = new String(chars);
        return TComponent.literal(val).setStyle(compIn.getStyle());
    }

    public static Entity getEntityLookedAt(final Entity e)
    {
        return Tools.getPointedEntity(e, 32);
    }

    private static void blit(final VertexConsumer buffer, final Matrix4f pos, final float x1, final float y1,
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

    public static void renderHealthBar(final LivingEntity passedEntity, final PoseStack mat,
            final MultiBufferSource buf, final float partialTicks, final Entity viewPoint, final int br)
    {
        LivingEntity entity = passedEntity;

        if (!RENDER_HEALTH.apply(passedEntity, viewPoint)) return;

        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        final Config config = PokecubeCore.getConfig();
        final Minecraft mc = Minecraft.getInstance();
        final EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        final Camera viewer = renderManager.camera;
        final UUID viewerID = viewer.getEntity().getUUID();

        final boolean background = config.drawBackground;
        VertexConsumer buffer;
        Matrix4f pos;

        mat.pushPose();
        processing:
        {

            float scale = .02f;

            final float maxHealth = entity.getMaxHealth();
            final float health = Math.min(maxHealth, entity.getHealth());

            if (maxHealth <= 0) break processing;

            double dy = entity.getBoundingBox().getYsize();

            if (entity.isMultipartEntity())
            {
                dy = 0;
                for (final PartEntity<?> part : entity.getParts())
                    dy = Math.max(dy, part.getBoundingBox().getYsize() + part.getY() - entity.getY());
            }

            dy += config.heightAbove;

            mat.translate(0, dy, 0);
            Quaternion quaternion;
            quaternion = viewer.rotation();
            mat.mulPose(quaternion);
            mat.scale(scale, scale, scale);
            mat.mulPose(AxisAngles.YP.rotationDegrees(180));
            mat.mulPose(AxisAngles.XP.rotationDegrees(180));

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
            MutableComponent nameComp = (MutableComponent) pokemob.getDisplayName();
            final boolean obfuscated = Health.obfuscateName(pokemob);
            if (obfuscated) nameComp = Health.obfuscate(nameComp);
            if (entity instanceof Mob mob && mob.hasCustomName()) nameComp = (MutableComponent) mob.getCustomName();

            final float s = 0.5F;
            final String name = nameComp.getString();
            final float namel = mc.font.width(name) * s;
            if (namel + 20 > size * 2) size = namel / 2F + 10F;
            float healthSize = size * (health / maxHealth);

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
            float maxExp = Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel() + 1)
                    - Tools.levelToXp(pokemob.getExperienceMode(), pokemob.getLevel());
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
            if (isOwner)
                mc.font.draw(mat, healthStr, (int) (size / (s * s1)) - mc.font.width(healthStr) / 2, h, 0xFFFFFFFF);

            pos = mat.last().pose();
            mc.font.drawInBatch(lvlStr, 2, h, 0xFFFFFF, false, pos, buf, false, 0, br);
            mc.font.drawInBatch(gender, (int) (size / (s * s1) * 2) - 2 - mc.font.width(gender), h - 1, colour, false,
                    pos, buf, false, 0, br);

            if (PokecubeCore.getConfig().enableDebugInfo && mc.options.renderDebug)
            {
                final String entityID = entity.getEncodeId().toString();
                mc.font.draw(mat, "ID: \"" + entityID + "\"" + "(" + entity.getId() + ")", 0, h + 16, 0xFFFFFFFF);
            }
            mat.popPose();

            int off = 0;
            s1 = 0.5F;
            mat.scale(s1, s1, s1);
            mat.translate(size / (s * s1) * 2 - 16, 0F, 0F);

            if (!stack.isEmpty() && config.showHeldItem)
                Health.renderIcon(entity, mat, buf, off, 0, 0, stack, 16, 16, br);
            off -= 16;

            final int armor = entity.getArmorValue();

            if (armor > 0 && config.showArmor)
            {
                final int ironArmor = armor % 5;
                final int diamondArmor = armor / 5;
                stack = new ItemStack(Items.IRON_CHESTPLATE);
                int zOrder = 0;
                for (int i = 0; i < ironArmor; i++)
                    Health.renderIcon(entity, mat, buf, off, 0, zOrder--, stack, 16, 16, br);
                off -= 4;

                stack = new ItemStack(Items.DIAMOND_CHESTPLATE);
                for (int i = 0; i < diamondArmor; i++)
                    Health.renderIcon(entity, mat, buf, off, 0, zOrder--, stack, 16, 16, br);
                off -= 4;
            }
            mat.popPose();
        }
        mat.popPose();

        if (PokecubeCore.getConfig().enableDebugInfo && mc.options.renderDebug && entity.isMultipartEntity()
                && entity.getParts() != null)
        {
            float scale = 0.02f;
            float s = 0.5f;
            for (var p : entity.getParts())
            {
                if (!(p instanceof GenericPartEntity<?> g)) continue;

                mat.pushPose();

                double dx = p.getX() - entity.getX();
                double dy = p.getY() - entity.getY();
                double dz = p.getZ() - entity.getZ();
                mat.translate(dx, dy + p.getBbHeight(), dz);
                Quaternion quaternion;
                quaternion = viewer.rotation();
                mat.mulPose(quaternion);
                mat.scale(scale, scale, scale);
                mat.mulPose(AxisAngles.YP.rotationDegrees(180));
                mat.mulPose(AxisAngles.XP.rotationDegrees(180));
                mat.translate(0, -2.5F, 0F);
                mat.scale(s, s, s);

                mc.font.draw(mat, g.id, 0, 16, 0xFFFFFFFF);

                mat.popPose();
            }
        }
    }

    public static void renderIcon(final LivingEntity mob, final PoseStack mat, final MultiBufferSource buf,
            final int vertexX, final int vertexY, int zOrder, final ItemStack stack, final int intU, final int intV,
            final int br)
    {
        mat.pushPose();
        mat.translate(vertexX, vertexY + 7, 0.1f * zOrder);
        mat.scale(20, 20, 20);
        mat.mulPose(AxisAngles.YP.rotationDegrees(180));
        mat.mulPose(AxisAngles.ZP.rotationDegrees(180));
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, TransformType.NONE, br, OverlayTexture.NO_OVERLAY,
                mat, buf, 0);
        mat.popPose();
    }
}
