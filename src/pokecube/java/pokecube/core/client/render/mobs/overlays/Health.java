package pokecube.core.client.render.mobs.overlays;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
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
import thut.api.entity.multipart.GenericPartEntity;
import thut.core.common.ThutCore;
import thut.core.common.handlers.PlayerDataHandler;
import thut.lib.TComponent;

import java.awt.*;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * This health renderer is directly based on Neat by Vaziki, which can be found here: <a
 * href="https://github.com/Vazkii/Neat">...</a> This version has been modified to only apply to pokemobs, as well as to
 * show level, gender and exp. I have also modified the nametags to indicate ownership
 */
public class Health
{
    public static BiFunction<LivingEntity, Entity, Boolean> RENDER_HEALTH = (entity, viewPoint) -> {
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        // Only apply to pokemobs in world
        if (pokemob == null || !entity.isAddedToLevel()) return false;
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
        return !(entity.getVehicle() instanceof Player);
    };

    public static boolean fullNameColour(final IPokemob pokemob)
    {
        final boolean nametag = pokemob.getGeneralState(GeneralStates.TAMED);
        // Always full name if owned
        if (nametag) return true;
        final PokedexEntry name_entry = pokemob.getPokedexEntry();
        return StatsCollector.getCaptured(name_entry, Minecraft.getInstance().player) > 0
                || StatsCollector.getHatched(name_entry, Minecraft.getInstance().player) > 0;
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
        String val = compIn.getString();
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
        return TComponent.literal(val).setStyle(compIn.getStyle());
    }

    private static void blit(final VertexConsumer buffer, final Matrix4f pos, final float x1, final float y1,
            final float x2, final float y2, final float z, final int r, final int g, final int b, final int a,
            final int brightness)
    {
        buffer.addVertex(pos, x1, y1, z).setColor(r, g, b, a).setLight(brightness);
        buffer.addVertex(pos, x1, y2, z).setColor(r, g, b, a).setLight(brightness);
        buffer.addVertex(pos, x2, y2, z).setColor(r, g, b, a).setLight(brightness);
        buffer.addVertex(pos, x2, y1, z).setColor(r, g, b, a).setLight(brightness);
    }

    public static void renderHealthBar(final LivingEntity entity, PoseStack mat, final MultiBufferSource buf,
            final float partialTick, final Entity viewPoint, final int br)
    {
        if (!RENDER_HEALTH.apply(entity, viewPoint)) return;

        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        final Config config = PokecubeCore.getConfig();
        final Minecraft mc = Minecraft.getInstance();
        final EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        final Camera viewer = renderManager.camera;
        final UUID viewerID = viewer.getEntity().getUUID();

        final boolean background = config.drawBackground;

        Vec3 vec3 = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));
        if (vec3 == null) return;

        VertexConsumer buffer;
        Matrix4f pos;

        mat.pushPose();
        processing:
        {
            float scale = .02f;

            final float maxHealth = entity.getMaxHealth();
            final float health = Math.min(maxHealth, entity.getHealth());

            if (maxHealth <= 0) break processing;

            double dy = config.heightAbove;

            mat.translate(vec3.x, vec3.y + dy, vec3.z);
            mat.mulPose(renderManager.cameraOrientation());
            mat.scale(scale, -scale, scale);

            final float padding = config.backgroundPadding;
            final int bgHeight = config.backgroundHeight;
            final int barHeight1 = config.barHeight;
            float size = config.plateSize;

            float zlevel = 0.0f;
            int r, g, b;
            ItemStack stack = ItemStack.EMPTY;
            if (pokemob.getOwner() == viewer.getEntity()) stack = entity.getMainHandItem();
            final float hue = Math.max(0F, health / maxHealth / 3F - 0.07F);
            final Color color = Color.getHSBColor(hue, 0.8F, 0.8F);
            r = color.getRed();
            g = color.getGreen();
            b = color.getBlue();
            MutableComponent nameComp = (MutableComponent) pokemob.getDisplayName();
            final float s = 0.5F;
            final float namel = mc.font.width(nameComp.getString()) * s;
            final boolean obfuscated = Health.obfuscateName(pokemob);
            if (obfuscated) nameComp = Health.obfuscate(nameComp);
            if (entity instanceof Mob mob && mob.hasCustomName()) nameComp = (MutableComponent) mob.getCustomName();
            if (namel + 20 > size * 2) size = namel / 2f + 10F;
            float healthSize = size * (health / maxHealth);
            int barA = 255;

            pos = mat.last().pose();
            // Background
            if (background)
            {
                buffer = Utils.makeBuilder(RenderType.textBackgroundSeeThrough(), buf);
                int a = 32;
                float z_offset = zlevel + .006f;
                float x1 = -size - padding;
                float x2 = size + padding;
                float y1 = -bgHeight;
                float y2 = barHeight1 + padding;
                Health.blit(buffer, pos, x1, y1, x2, y2, z_offset, 0, 0, 0, a, br);
                zlevel += 0.001f;
            }
            buffer = Utils.makeBuilder(RenderType.textBackground(), buf);

            // Health bar
            // Gray Space
            healthSize = healthSize * 2 - size;
            Health.blit(buffer, pos, healthSize, 0, size, barHeight1, zlevel, 127, 100, 100, barA, br);
            zlevel += 0.001f;
            // Health Bar Fill
            Health.blit(buffer, pos, -size, 0, healthSize, barHeight1, zlevel, r, g, b, barA, br);
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
            Health.blit(buffer, pos, expSize, barHeight1, size, barHeight1 + 1, zlevel, 100, 100, 127, barA, br);
            zlevel += 0.001f;

            // Exp Bar Fill
            Health.blit(buffer, pos, -size, barHeight1, expSize, barHeight1 + 1, zlevel, r, g, b, barA, br);
            zlevel += 0.001f;

            {
                int colour = config.unknownNameColour;

                UUID owner = pokemob.getOwnerId();
                boolean isOwner = viewerID.equals(owner);
                boolean fullColour = Health.fullNameColour(pokemob) && !isOwner;

                if (fullColour) colour = owner != null ? config.otherOwnedNameColour : config.caughtNamedColour;
                else if (isOwner) colour = config.ownedNameColour;
                else if (!obfuscated) colour = config.scannedNameColour;

                // Decorations
                mat.pushPose();
                {
                    mat.translate(-size, -barHeight1-2, zlevel);
                    mat.scale(s, s, s);
                    pos = mat.last().pose();
                    // Name
                    mc.font.drawInBatch(nameComp.getString(), 0, 2, colour, false, pos, buf,
                            Font.DisplayMode.SEE_THROUGH, 0, br);

                    int h = config.hpTextHeight;

                    float s1 = 0.75f;
                    float x = 1, y = 0;

                    mat.translate(0, h, 0);

                    mat.pushPose();
                    {
                        mat.scale(s1, s1, s1);
                        pos = mat.last().pose();
                        colour = 0xFFFFFFFF;

                        // Level
                        final String lvlStr = "L." + pokemob.getLevel();
                        mc.font.drawInBatch(lvlStr, x, y, colour, false, pos, buf, Font.DisplayMode.NORMAL, 0, br);

                        // HP
                        String maxHpStr = "" + (int) (Math.round(maxHealth * 100.0) / 100.0);
                        String hpStr = "" + (int) (Math.round(health * 100.0) / 100.0);
                        if (maxHpStr.endsWith(".0")) maxHpStr = maxHpStr.substring(0, maxHpStr.length() - 2);
                        if (hpStr.endsWith(".0")) hpStr = hpStr.substring(0, hpStr.length() - 2);
                        String healthStr = hpStr + "/" + maxHpStr;
                        x = size / (s * s1) - mc.font.width(healthStr) / 2f;
                        if (isOwner)
                            mc.font.drawInBatch(healthStr, x, y, colour, false, pos, buf, Font.DisplayMode.NORMAL, 0,
                                    br);

                        // Sex
                        final String sexStr = pokemob.getSexe() == IPokemob.MALE
                                ? "♂"
                                : pokemob.getSexe() == IPokemob.FEMALE ? "♀" : "";
                        if (pokemob.getSexe() == IPokemob.MALE) colour = 0x0011CC;
                        else if (pokemob.getSexe() == IPokemob.FEMALE) colour = 0xCC5555;
                        x = 2 * size / (s * s1) - mc.font.width(sexStr);
                        mc.font.drawInBatch(sexStr, x, y, colour, false, pos, buf, Font.DisplayMode.NORMAL, 0, br);

                        // Extras
                        if (PokecubeCore.getConfig().enableDebugInfo && mc.gui.getDebugOverlay().showDebugScreen())
                        {
                            final String entityID = entity.getEncodeId();
                            mc.font.drawInBatch("ID: \"" + entityID + "\"" + "(" + entity.getId() + ")", 0, h + 16,
                                    0xFFFFFFFF, false, pos, buf, Font.DisplayMode.NORMAL, 0, br);
                        }
                    }
                    mat.popPose();

                    x = size * 2 / s - 4;
                    if (!stack.isEmpty() && config.showHeldItem)
                        Health.renderIcon(entity, mat, buf, x, 0, 0, stack, br);

                    final int armor = entity.getArmorValue();

                    if (armor > 0 && config.showArmor)
                    {
                        int ironArmor = armor % 5;
                        int diamondArmor = armor / 5;
                        stack = new ItemStack(Items.IRON_CHESTPLATE);
                        int zOrder = 0;
                        x -= 16;
                        for (int i = 0; i < ironArmor; i++)
                            Health.renderIcon(entity, mat, buf, x + i * 1.5f, 0, zOrder--, stack, br);

                        stack = new ItemStack(Items.DIAMOND_CHESTPLATE);
                        x -= 16;
                        for (int i = 0; i < diamondArmor; i++)
                            Health.renderIcon(entity, mat, buf, x + i * 1.5f, 0, zOrder--, stack, br);
                    }
                }
                mat.popPose();
            }
        }
        mat.popPose();

        if (PokecubeCore.getConfig().enableDebugInfo && mc.gui.getDebugOverlay().showDebugScreen()
                && entity.isMultipartEntity() && entity.getParts() != null)
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
                Quaternionf quaternion;
                quaternion = viewer.rotation();
                mat.mulPose(quaternion);
                mat.scale(scale, scale, scale);
                mat.mulPose(Axis.YP.rotationDegrees(180));
                mat.mulPose(Axis.XP.rotationDegrees(180));
                mat.translate(0, -2.5F, 0F);
                mat.scale(s, s, s);

                mc.font.drawInBatch(g.id, 0, 16, 0xFFFFFFFF, false, mat.last().pose(), buf, Font.DisplayMode.NORMAL, 0,
                        br);

                mat.popPose();
            }
        }
    }

    public static void renderIcon(LivingEntity mob, PoseStack mat, MultiBufferSource buf, float x, float y, int zOrder,
            ItemStack stack, int br)
    {
        mat.pushPose();
        mat.translate(x, y - 7, 0.1f * zOrder);
        mat.scale(10, -10, 10);
        mat.mulPose(Axis.YP.rotationDegrees(180));
        Minecraft.getInstance().getItemRenderer()
                .renderStatic(stack, ItemDisplayContext.FIXED, br, OverlayTexture.NO_OVERLAY, mat, buf, mob.level, 0);
        mat.popPose();
    }
}
