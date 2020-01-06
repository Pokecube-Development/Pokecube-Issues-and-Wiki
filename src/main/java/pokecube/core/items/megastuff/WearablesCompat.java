package pokecube.core.items.megastuff;

import java.awt.Color;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.client.models.ModelRing;
import pokecube.core.interfaces.PokecubeMod;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.x3d.X3dModel;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.ThutWearables;

public class WearablesCompat
{
    public static class WearableMega implements thut.wearables.IActiveWearable, ICapabilityProvider
    {
        private final LazyOptional<IActiveWearable> holder = LazyOptional.of(() -> this);

        @Override
        public boolean dyeable(final ItemStack stack)
        {
            return true;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
        {
            return ThutWearables.WEARABLE_CAP.orEmpty(capability, this.holder);
        }

        @Override
        public thut.wearables.EnumWearable getSlot(final ItemStack stack)
        {
            return thut.wearables.EnumWearable.valueOf(this.getSlotSt(stack));
        }

        String getSlotSt(final ItemStack stack)
        {
            if (stack.getItem() instanceof ItemMegawearable) return ((ItemMegawearable) stack.getItem()).slot;
            return "";
        }

        String getVariant(final ItemStack stack)
        {
            if (stack.getItem() instanceof ItemMegawearable) return ((ItemMegawearable) stack.getItem()).name;
            return "";
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void renderWearable(final thut.wearables.EnumWearable slot, final LivingEntity wearer,
                final ItemStack stack, final float partialTicks)
        {
            final WearablesRenderer renderer = WearablesCompat.renderers.get(this.getVariant(stack));
            if (renderer != null) renderer.renderWearable(slot, wearer, stack, partialTicks);
        }
    }

    public abstract static class WearablesRenderer
    {
        @OnlyIn(Dist.CLIENT)
        public abstract void renderWearable(thut.wearables.EnumWearable slot, LivingEntity wearer, ItemStack stack,
                float partialTicks);
    }

    public static class WearableWatch implements thut.wearables.IActiveWearable, ICapabilityProvider
    {
        private final LazyOptional<IActiveWearable> holder = LazyOptional.of(() -> this);

        @Override
        public boolean dyeable(final ItemStack stack)
        {
            return true;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
        {
            return ThutWearables.WEARABLE_CAP.orEmpty(capability, this.holder);
        }

        @Override
        public thut.wearables.EnumWearable getSlot(final ItemStack stack)
        {
            return thut.wearables.EnumWearable.WRIST;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void renderWearable(final thut.wearables.EnumWearable slot, final LivingEntity wearer,
                final ItemStack stack, final float partialTicks)
        {
            final WearablesRenderer renderer = WearablesCompat.renderers.get("pokewatch");
            if (renderer != null) renderer.renderWearable(slot, wearer, stack, partialTicks);
        }
    }

    private static final ResourceLocation WEARABLESKEY = new ResourceLocation("pokecube:wearable");

    public static Map<String, WearablesRenderer> renderers = Maps.newHashMap();

    static
    {
        WearablesCompat.renderers.put("pokewatch", new WearablesRenderer()
        {
            // 2 layers of belt rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            private X3dModel model;

            // Textures for each belt layer.
            private ResourceLocation strap;
            private ResourceLocation watch;

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final EnumWearable slot, final LivingEntity wearer, final ItemStack stack,
                    final float partialTicks)
            {
                if (slot != EnumWearable.WRIST) return;
                if (this.model == null)
                {
                    this.model = new X3dModel(new ResourceLocation(PokecubeMod.ID, "models/worn/pokewatch.x3d"));
                    this.strap = new ResourceLocation(PokecubeMod.ID, "textures/worn/megabelt_2.png");
                    this.watch = new ResourceLocation(PokecubeMod.ID, "textures/worn/watch.png");
                }
                final int brightness = wearer.getBrightnessForRender();
                final int[] col = new int[] { 255, 255, 255, 255, brightness };
                float s, sy, sx, sz, dx, dy, dz;
                dx = 0.f;
                dy = .06f;
                dz = -0.075f;
                s = .65f;
                sx = s;
                sy = s;
                sz = s * 1.5f;
                GL11.glPushMatrix();
                GL11.glTranslatef(dx, dy, dz);
                GL11.glScalef(sx * 0.75f, sy, sz);
                GL11.glRotatef(-90, 1, 0, 0);
                Minecraft.getInstance().getTextureManager().bindTexture(this.strap);
                DyeColor ret = DyeColor.GRAY;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.func_218388_g() + 0xFF000000);
                col[0] = colour.getRed();
                col[1] = colour.getGreen();
                col[2] = colour.getBlue();
                this.model.getParts().get("strap").setRGBAB(col);
                this.model.renderOnly("strap");
                GL11.glPopMatrix();
                GL11.glPushMatrix();
                GL11.glTranslatef(dx, dy, dz);
                GL11.glScalef(sx, sy, sz);
                GL11.glRotatef(-90, 1, 0, 0);
                Minecraft.getInstance().getTextureManager().bindTexture(this.watch);
                this.model.renderOnly("watch");
                GL11.glPopMatrix();

            }
        });
        WearablesCompat.renderers.put("ring", new WearablesRenderer()
        {
            // rings use own model, so only 1 layer here, ring model handles own
            // textures.
            @OnlyIn(Dist.CLIENT)
            private ModelRing ring;

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final EnumWearable slot, final LivingEntity wearer, final ItemStack stack,
                    final float partialTicks)
            {
                if (slot != EnumWearable.FINGER) return;
                if (this.ring == null) this.ring = new ModelRing();
                this.ring.stack = stack;
                this.ring.render(wearer, 0, 0, partialTicks, 0, 0, 0.0625f);
            }
        });
        WearablesCompat.renderers.put("belt", new WearablesRenderer()
        {
            // 2 layers of belt rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            private X3dModel belt;

            // Textures for each belt layer.
            private ResourceLocation keystone;
            private ResourceLocation belt_2;

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final EnumWearable slot, final LivingEntity wearer, final ItemStack stack,
                    final float partialTicks)
            {
                if (slot != EnumWearable.WAIST) return;
                if (this.belt == null)
                {
                    this.belt = new X3dModel(new ResourceLocation(PokecubeMod.ID, "models/worn/megabelt.x3d"));
                    this.keystone = new ResourceLocation(PokecubeMod.ID, "textures/worn/keystone.png");
                    this.belt_2 = new ResourceLocation(PokecubeMod.ID, "textures/worn/megabelt_2.png");
                }
                final int brightness = wearer.getBrightnessForRender();
                final int[] col = new int[] { 255, 255, 255, 255, brightness };
                GL11.glPushMatrix();
                final float dx = 0, dy = .6f, dz = -0.f;
                GL11.glTranslatef(dx, dy, dz);
                float s = 1.1f;
                if (wearer.getItemStackFromSlot(EquipmentSlotType.LEGS).isEmpty()) s = 0.95f;
                GL11.glScalef(s, s, s);
                GL11.glRotatef(90, 1, 0, 0);
                GL11.glRotatef(180, 0, 1, 0);
                Minecraft.getInstance().getTextureManager().bindTexture(this.keystone);
                GL11.glRotatef(90, 1, 0, 0);
                this.belt.renderOnly("stone");
                GL11.glPopMatrix();

                GL11.glPushMatrix();
                GL11.glTranslatef(dx, dy, dz);
                GL11.glScalef(s, s, s);
                GL11.glRotatef(90, 1, 0, 0);
                GL11.glRotatef(180, 0, 1, 0);
                Minecraft.getInstance().getTextureManager().bindTexture(this.belt_2);
                DyeColor ret = DyeColor.GRAY;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.func_218388_g() + 0xFF000000);
                col[0] = colour.getRed();
                col[1] = colour.getGreen();
                col[2] = colour.getBlue();
                this.belt.getParts().get("belt").setRGBAB(col);
                this.belt.renderOnly("belt");
                GL11.glPopMatrix();
            }
        });
        WearablesCompat.renderers.put("hat", new WearablesRenderer()
        {
            // 2 layers of hat rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            X3dModel hat;

            // Textures for each hat layer.
            private final ResourceLocation hat_1 = new ResourceLocation(PokecubeCore.MODID, "textures/worn/hat.png");
            private final ResourceLocation hat_2 = new ResourceLocation(PokecubeCore.MODID, "textures/worn/hat2.png");

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final EnumWearable slot, final LivingEntity wearer, final ItemStack stack,
                    final float partialTicks)
            {
                if (slot != EnumWearable.HAT) return;

                if (this.hat == null) this.hat = new X3dModel(new ResourceLocation(PokecubeMod.ID,
                        "models/worn/hat.x3d"));

                final Minecraft minecraft = Minecraft.getInstance();
                GlStateManager.pushMatrix();
                final float s = 0.285f;
                GL11.glScaled(s, -s, -s);
                final int brightness = wearer.getBrightnessForRender();
                final int[] col = new int[] { 255, 255, 255, 255, brightness };
                minecraft.getTextureManager().bindTexture(this.hat_1);
                for (final IExtendedModelPart part1 : this.hat.getParts().values())
                    part1.setRGBAB(col);
                this.hat.renderAll();
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GL11.glScaled(s * 0.995f, -s * 0.995f, -s * 0.995f);
                minecraft.getTextureManager().bindTexture(this.hat_2);
                DyeColor ret = DyeColor.RED;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.func_218388_g() + 0xFF000000);
                col[0] = colour.getRed();
                col[1] = colour.getGreen();
                col[2] = colour.getBlue();
                for (final IExtendedModelPart part : this.hat.getParts().values())
                    part.setRGBAB(col);
                this.hat.renderAll();
                GL11.glColor3f(1, 1, 1);
                GlStateManager.popMatrix();
            }
        });
    }

    public WearablesCompat()
    {
        MegaCapability.checker = (player, toEvolve) ->
        {
            final Set<ItemStack> worn = thut.wearables.ThutWearables.getWearables(player).getWearables();
            for (final ItemStack stack1 : worn)
                if (stack1 != null) if (MegaCapability.matches(stack1, toEvolve)) return true;
            for (int i = 0; i < player.inventory.armorInventory.size(); i++)
            {
                final ItemStack stack2 = player.inventory.armorInventory.get(i);
                if (stack2 != null) if (MegaCapability.matches(stack2, toEvolve)) return true;
            }
            return false;
        };
    }

    @SubscribeEvent
    public void onItemCapabilityAttach(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getObject().getItem() instanceof ItemMegawearable) event.addCapability(WearablesCompat.WEARABLESKEY,
                new WearableMega());
        else if (event.getObject().getItem() == PokecubeItems.POKEWATCH) event.addCapability(
                WearablesCompat.WEARABLESKEY, new WearableWatch());
    }
}
