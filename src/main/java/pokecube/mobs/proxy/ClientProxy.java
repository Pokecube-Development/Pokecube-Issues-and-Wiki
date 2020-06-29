package pokecube.mobs.proxy;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
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
import pokecube.mobs.client.smd.SMDModel;
import thut.bling.client.render.Util;
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
            X3dModel                       model;

            // Textures for each hat layer.
            private final ResourceLocation keystone = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/keystone.png");
            private final ResourceLocation metal    = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/megatiara_2.png");

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                    final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.HAT) return;
                if (this.model == null)
                    this.model = new X3dModel(new ResourceLocation(PokecubeMod.ID, "models/worn/megatiara.x3d"));
                if (!this.model.isLoaded() || !this.model.isValid()) return;
                final float dx = -0.0f, dy = 0.25f, dz = -0.25f;
                mat.rotate(Vector3f.XP.rotationDegrees(-90));
                mat.translate(dx, dy, dz);
                IVertexBuilder buf0 = Util.makeBuilder(buff, this.keystone);
                this.model.renderOnly(mat, buf0, "stone");
                DyeColor ret = DyeColor.BLUE;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.getTextColor() + 0xFF000000);
                this.model.getParts().get("tiara").setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), 255,
                        brightness, overlay);
                buf0 = Util.makeBuilder(buff, this.metal);
                this.model.renderOnly(mat, buf0, "tiara");
            }
        });

        // Mega Anklet like one worn by Zinnia
        WearablesCompat.renderers.put("ankletzinnia", new WearablesRenderer()
        {
            // 2 layers of hat rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            X3dModel                       model;

            // Textures for each hat layer.
            private final ResourceLocation keystone = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/keystone.png");
            private final ResourceLocation texture  = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/megaankletzinnia_2.png");

            @Override
            public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                    final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.ANKLE) return;
                if (this.model == null)
                    this.model = new X3dModel(new ResourceLocation(PokecubeMod.ID, "models/worn/megaankletzinnia.x3d"));
                if (!this.model.isLoaded() || !this.model.isValid()) return;
                float s, dx, dy, dz;
                dx = 0.f;
                dy = .06f;
                dz = 0.f;
                s = 1.f;
                mat.rotate(net.minecraft.client.renderer.Vector3f.XP.rotationDegrees(90));
                mat.rotate(net.minecraft.client.renderer.Vector3f.ZP.rotationDegrees(180));
                mat.translate(dx, dy, dz);
                mat.scale(s, s, s);
                IVertexBuilder buf0 = Util.makeBuilder(buff, this.keystone);
                this.model.renderOnly(mat, buf0, "stone");
                DyeColor ret = DyeColor.CYAN;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.getTextColor() + 0xFF000000);
                this.model.getParts().get("anklet").setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(),
                        255, brightness, overlay);
                buf0 = Util.makeBuilder(buff, this.texture);
                this.model.renderOnly(mat, buf0, "anklet");
            }
        });
        // a Pendant
        WearablesCompat.renderers.put("pendant", new WearablesRenderer()
        {
            // 2 layers of hat rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            X3dModel                       model;

            // Textures for each hat layer.
            private final ResourceLocation keystone = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/keystone.png");
            private final ResourceLocation pendant  = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/megapendant_2.png");

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                    final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.NECK) return;
                if (this.model == null)
                    this.model = new X3dModel(new ResourceLocation(PokecubeMod.ID, "models/worn/megapendant.x3d"));
                if (!this.model.isLoaded() || !this.model.isValid()) return;
                float  dx, dy, dz;
                dx = 0;
                dy = -.0f;
                dz = 0.01f;
                mat.rotate(Vector3f.XP.rotationDegrees(90));
                mat.rotate(Vector3f.ZP.rotationDegrees(180));
                mat.translate(dx, dy, dz);
                IVertexBuilder buf0 = Util.makeBuilder(buff, this.keystone);
                this.model.renderOnly(mat, buf0, "keystone");
                DyeColor ret = DyeColor.YELLOW;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.getTextColor() + 0xFF000000);
                this.model.getParts().get("pendant").setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(),
                        255, brightness, overlay);
                buf0 = Util.makeBuilder(buff, this.pendant);
                this.model.renderOnly(mat, buf0, "pendant");

            }
        });
        // Earrings
        WearablesCompat.renderers.put("earring", new WearablesRenderer()
        {
            // 2 layers of hat rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            X3dModel                       model;

            // Textures for each hat layer.
            private final ResourceLocation keystone = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/keystone.png");
            private final ResourceLocation loop     = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/megaearring_2.png");

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                    final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.EAR) return;
                if (this.model == null)
                    this.model = new X3dModel(new ResourceLocation(PokecubeMod.ID, "models/worn/megaearring.x3d"));
                if (!this.model.isLoaded() || !this.model.isValid()) return;
                final Minecraft minecraft = Minecraft.getInstance();
                float dx, dy, dz;
                dx = 0.0f;
                dy = index == 0 ? 0.01f : -0.01f;
                dz = -0.25f;
                mat.rotate(net.minecraft.client.renderer.Vector3f.ZP.rotationDegrees(180));
                mat.translate(dx, dy, dz);
                IVertexBuilder buf0 = Util.makeBuilder(buff, this.keystone);
                this.model.renderOnly(mat, buf0, "keystone");

                minecraft.getTextureManager().bindTexture(this.loop);
                DyeColor ret = DyeColor.YELLOW;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.getTextColor() + 0xFF000000);
                this.model.getParts().get("earing").setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(),
                        255, brightness, overlay);
                buf0 = Util.makeBuilder(buff, this.loop);
                this.model.renderOnly(mat, buf0, "earing");

            }
        });
        // Glasses
        WearablesCompat.renderers.put("glasses", new WearablesRenderer()
        {
            // 2 layers of hat rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            X3dModel                       model;

            // Textures for each hat layer.
            private final ResourceLocation keystone = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/keystone.png");
            private final ResourceLocation loop     = new ResourceLocation(PokecubeCore.MODID,
                    "textures/worn/megaglasses_2.png");

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                    final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.EYE) return;
                if (this.model == null)
                    this.model = new X3dModel(new ResourceLocation(PokecubeMod.ID, "models/worn/megaglasses.x3d"));
                if (!this.model.isLoaded() || !this.model.isValid()) return;

                final float dx = -0.0f, dy = 0.0f, dz = -0.25f;
                mat.rotate(Vector3f.XP.rotationDegrees(90));
                mat.rotate(Vector3f.ZP.rotationDegrees(180));
                mat.translate(dx, dy, dz);
                IVertexBuilder buf0 = Util.makeBuilder(buff, this.keystone);
                this.model.renderOnly(mat, buf0, "stone");
                DyeColor ret = DyeColor.GRAY;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.getTextColor() + 0xFF000000);
                this.model.getParts().get("glasses").setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(),
                        255, brightness, overlay);
                buf0 = Util.makeBuilder(buff, this.loop);
                this.model.renderOnly(mat, buf0, "glasses");
            }
        });
    }
}
