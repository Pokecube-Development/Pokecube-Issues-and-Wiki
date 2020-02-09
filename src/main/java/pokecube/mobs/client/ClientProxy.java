package pokecube.mobs.client;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.items.megastuff.WearablesCompat;
import pokecube.core.items.megastuff.WearablesCompat.WearablesRenderer;
import pokecube.mobs.CommonProxy;
import pokecube.mobs.client.smd.SMDModel;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.x3d.X3dModel;
import thut.wearables.EnumWearable;

public class ClientProxy extends CommonProxy
{
    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        // // Register smd format for models
        ModelFactory.registerIModel("smd", SMDModel::new);
    }

    @Override
    public void initWearables()
    {
        super.initWearables();
        // Tiara like worn by Lisia, but rotated to be centered on head instead
        // of at angle.
        WearablesCompat.renderers.put("tiara", new WearablesRenderer()
        {
            // 2 layers of hat rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            X3dModel model;

            // Textures for each hat layer.
            private final ResourceLocation keystone = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/keystone.png");
            private final ResourceLocation metal    = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/megatiara_2.png");

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final EnumWearable slot, final int index, final LivingEntity wearer,
                    final ItemStack stack, final float partialTicks)
            {
                if (slot != EnumWearable.HAT) return;
                if (this.model == null) this.model = new X3dModel(new ResourceLocation(PokecubeMod.ID,
                        "models/worn/megatiara.x3d"));
                final Minecraft minecraft = Minecraft.getInstance();
                GL11.glRotatef(90, 1, 0, 0);
                GL11.glRotatef(180, 0, 0, 1);
                final float dx = -0.0f, dy = .235f, dz = 0.25f;
                GL11.glTranslatef(dx, dy, dz);
                GlStateManager.pushMatrix();
                final int brightness = wearer.getBrightnessForRender();
                final int[] col = new int[] { 255, 255, 255, 255, brightness };
                minecraft.getTextureManager().bindTexture(this.keystone);
                this.model.renderOnly("stone");
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                minecraft.getTextureManager().bindTexture(this.metal);
                DyeColor ret = DyeColor.BLUE;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.getTextColor() + 0xFF000000);
                col[0] = colour.getRed();
                col[2] = colour.getGreen();
                col[1] = colour.getBlue();
                this.model.getParts().get("tiara").setRGBAB(col);
                this.model.renderOnly("tiara");
                GL11.glColor3f(1, 1, 1);
                GlStateManager.popMatrix();

            }
        });

        // Mega Anklet like one worn by Zinnia
        WearablesCompat.renderers.put("ankletzinnia", new WearablesRenderer()
        {
            // 2 layers of hat rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            X3dModel model;

            // Textures for each hat layer.
            private final ResourceLocation keystone = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/keystone.png");
            private final ResourceLocation texture  = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/megaankletzinnia_2.png");

            @Override
            public void renderWearable(final EnumWearable slot, final int index, final LivingEntity wearer,
                    final ItemStack stack, final float partialTicks)
            {
                if (slot != EnumWearable.ANKLE) return;

                if (this.model == null) this.model = new X3dModel(new ResourceLocation(PokecubeMod.ID,
                        "models/worn/megaankletzinnia.x3d"));
                final Minecraft minecraft = Minecraft.getInstance();
                GlStateManager.pushMatrix();
                GL11.glRotatef(90, 1, 0, 0);
                GL11.glRotatef(180, 0, 0, 1);
                final float dx = -0.0f, dy = .05f, dz = 0.f;
                final double s = 1;
                GL11.glScaled(s, s, s);
                GL11.glTranslatef(dx, dy, dz);
                final int brightness = wearer.getBrightnessForRender();
                final int[] col = new int[] { 255, 255, 255, 255, brightness };
                minecraft.getTextureManager().bindTexture(this.keystone);
                this.model.renderOnly("stone");
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GL11.glRotatef(90, 1, 0, 0);
                GL11.glRotatef(180, 0, 0, 1);
                GL11.glScaled(s, s, s);
                GL11.glTranslatef(dx, dy, dz);
                minecraft.getTextureManager().bindTexture(this.texture);
                DyeColor ret = DyeColor.CYAN;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.getTextColor() + 0xFF000000);
                col[0] = colour.getRed();
                col[2] = colour.getGreen();
                col[1] = colour.getBlue();
                this.model.getParts().get("anklet").setRGBAB(col);
                this.model.renderOnly("anklet");
                GL11.glColor3f(1, 1, 1);
                GlStateManager.popMatrix();
            }
        });
        // a Pendant
        WearablesCompat.renderers.put("pendant", new WearablesRenderer()
        {
            // 2 layers of hat rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            X3dModel model;

            // Textures for each hat layer.
            private final ResourceLocation keystone = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/keystone.png");
            private final ResourceLocation pendant  = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/megapendant_2.png");

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final EnumWearable slot, final int index, final LivingEntity wearer,
                    final ItemStack stack, final float partialTicks)
            {
                if (slot != EnumWearable.NECK) return;
                if (this.model == null) this.model = new X3dModel(new ResourceLocation(PokecubeMod.ID,
                        "models/worn/megapendant.x3d"));
                final Minecraft minecraft = Minecraft.getInstance();
                GL11.glRotatef(90, 1, 0, 0);
                GL11.glRotatef(180, 0, 0, 1);
                final float dx = -0.0f, dy = .0f, dz = 0.01f;
                GL11.glTranslatef(dx, dy, dz);
                GlStateManager.pushMatrix();
                final int brightness = wearer.getBrightnessForRender();
                final int[] col = new int[] { 255, 255, 255, 255, brightness };
                minecraft.getTextureManager().bindTexture(this.keystone);
                this.model.renderOnly("keystone");
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                minecraft.getTextureManager().bindTexture(this.pendant);
                DyeColor ret = DyeColor.YELLOW;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.getTextColor() + 0xFF000000);
                col[0] = colour.getRed();
                col[2] = colour.getGreen();
                col[1] = colour.getBlue();
                this.model.getParts().get("pendant").setRGBAB(col);
                this.model.renderOnly("pendant");
                GL11.glColor3f(1, 1, 1);
                GlStateManager.popMatrix();

            }
        });
        // Earrings
        WearablesCompat.renderers.put("earring", new WearablesRenderer()
        {
            // 2 layers of hat rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            X3dModel model;

            // Textures for each hat layer.
            private final ResourceLocation keystone = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/keystone.png");
            private final ResourceLocation loop     = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/megaearring_2.png");

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final EnumWearable slot, final int index, final LivingEntity wearer,
                    final ItemStack stack, final float partialTicks)
            {
                if (slot != EnumWearable.EAR) return;
                if (this.model == null) this.model = new X3dModel(new ResourceLocation(PokecubeMod.ID,
                        "models/worn/megaearring.x3d"));
                final Minecraft minecraft = Minecraft.getInstance();
                GL11.glRotatef(180, 0, 0, 1);
                final float dx = -0.0f, dy = index == 0 ? .01f : -.01f, dz = -0.3f;
                GL11.glTranslatef(dx, dy, dz);
                GlStateManager.pushMatrix();
                final int brightness = wearer.getBrightnessForRender();
                final int[] col = new int[] { 255, 255, 255, 255, brightness };
                minecraft.getTextureManager().bindTexture(this.keystone);
                this.model.renderOnly("keystone");
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                minecraft.getTextureManager().bindTexture(this.loop);
                DyeColor ret = DyeColor.YELLOW;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.getTextColor() + 0xFF000000);
                col[0] = colour.getRed();
                col[2] = colour.getGreen();
                col[1] = colour.getBlue();
                this.model.getParts().get("earing").setRGBAB(col);
                this.model.renderOnly("earing");
                GL11.glColor3f(1, 1, 1);
                GlStateManager.popMatrix();

            }
        });
        // Glasses
        WearablesCompat.renderers.put("glasses", new WearablesRenderer()
        {
            // 2 layers of hat rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            X3dModel model;

            // Textures for each hat layer.
            private final ResourceLocation keystone = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/keystone.png");
            private final ResourceLocation loop     = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/megaglasses_2.png");

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final EnumWearable slot, final int index, final LivingEntity wearer,
                    final ItemStack stack, final float partialTicks)
            {
                if (slot != EnumWearable.EYE) return;
                if (this.model == null) this.model = new X3dModel(new ResourceLocation(PokecubeMod.ID,
                        "models/worn/megaglasses.x3d"));
                final Minecraft minecraft = Minecraft.getInstance();
                GL11.glRotatef(90, 1, 0, 0);
                GL11.glRotatef(180, 0, 0, 1);
                final float dx = -0.0f, dy = .01f, dz = -0.25f;
                GL11.glTranslatef(dx, dy, dz);
                GlStateManager.pushMatrix();
                final int brightness = wearer.getBrightnessForRender();
                final int[] col = new int[] { 255, 255, 255, 255, brightness };
                minecraft.getTextureManager().bindTexture(this.keystone);
                this.model.renderOnly("keystone");
                GlStateManager.popMatrix();

                GlStateManager.pushMatrix();
                minecraft.getTextureManager().bindTexture(this.loop);
                DyeColor ret = DyeColor.GRAY;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.getTextColor() + 0xFF000000);
                col[0] = colour.getRed();
                col[2] = colour.getGreen();
                col[1] = colour.getBlue();
                this.model.getParts().get("glasses").setRGBAB(col);
                this.model.renderOnly("glasses");
                GL11.glColor3f(1, 1, 1);
                GlStateManager.popMatrix();

            }
        });
    }
}
