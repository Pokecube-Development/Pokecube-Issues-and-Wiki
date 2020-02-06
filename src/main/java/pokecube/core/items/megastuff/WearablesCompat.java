package pokecube.core.items.megastuff;

import java.awt.Color;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
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
import thut.api.maths.vecmath.Vector3f;
import thut.bling.client.render.Hat;
import thut.bling.client.render.Util;
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
        public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                final int brightness, final int overlay)
        {
            final WearablesRenderer renderer = WearablesCompat.renderers.get(this.getVariant(stack));
            if (renderer != null)
                renderer.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
        }
    }

    public abstract static class WearablesRenderer
    {
        @OnlyIn(Dist.CLIENT)
        public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                final LivingEntity wearer, final ItemStack stack, final float partialTicks, final int brightness,
                final int overlay)
        {
            this.renderWearable(mat, buff, slot, 0, wearer, stack, partialTicks, brightness, overlay);
        }

        @OnlyIn(Dist.CLIENT)
        public abstract void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff,
                final EnumWearable slot, final int index, final LivingEntity wearer, final ItemStack stack,
                final float partialTicks, int brightness, int overlay);
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
        public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                final int brightness, final int overlay)
        {
            final WearablesRenderer renderer = WearablesCompat.renderers.get("pokewatch");
            if (renderer != null)
                renderer.renderWearable(mat, buff, slot, index, wearer, stack, partialTicks, brightness, overlay);
        }
    }

    private static final ResourceLocation        WEARABLESKEY = new ResourceLocation("pokecube:wearable");

    public static Map<String, WearablesRenderer> renderers    = Maps.newHashMap();

    static
    {
        WearablesCompat.renderers.put("pokewatch", new WearablesRenderer()
        {
            // 2 layers of belt rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            private X3dModel         model;

            // Textures for each belt layer.
            private ResourceLocation strap;
            private ResourceLocation watch;

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                    final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.WRIST) return;
                if (this.model == null)
                {
                    this.model = new X3dModel(new ResourceLocation(PokecubeMod.ID, "models/worn/pokewatch.x3d"));
                    this.strap = new ResourceLocation(PokecubeMod.ID, "textures/worn/megabelt_2.png");
                    this.watch = new ResourceLocation(PokecubeMod.ID, "textures/worn/watch.png");
                }
                float s, sy, sx, sz, dx, dy, dz;
                dx = 0.f;
                dy = .06f;
                dz = 0.f;
                s = 0.475f;
                sx = 1.05f * s / 2;
                sy = s * 1.8f / 2;
                sz = s / 2;
                mat.push();
                mat.rotate(net.minecraft.client.renderer.Vector3f.XP.rotationDegrees(90));
                mat.rotate(net.minecraft.client.renderer.Vector3f.ZP.rotationDegrees(180));
                mat.translate(dx, dy, dz);
                mat.scale(sx, sy, sz);
                DyeColor ret = DyeColor.GRAY;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                IVertexBuilder buf = Util.makeBuilder(buff, this.strap);
                final Color colour = new Color(ret.getTextColor() + 0xFF000000);
                this.model.getParts().get("strap").setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), 255,
                        brightness, overlay);
                this.model.renderOnly(mat, buf, "strap");
                buf = Util.makeBuilder(buff, this.watch);
                this.model.renderOnly(mat, buf, "watch");
                mat.pop();

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
            public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                    final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.FINGER) return;
                if (this.ring == null) this.ring = new ModelRing();
                this.ring.stack = stack;
                this.ring.render(wearer, 0, 0, partialTicks, 0, 0);
            }
        });
        WearablesCompat.renderers.put("belt", new WearablesRenderer()
        {
            // 2 layers of belt rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            private X3dModel         belt;

            // Textures for each belt layer.
            private ResourceLocation keystone;
            private ResourceLocation belt_2;

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                    final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.WAIST) return;
                if (this.belt == null)
                {
                    this.belt = new X3dModel(new ResourceLocation(PokecubeMod.ID, "models/worn/megabelt.x3d"));
                    this.keystone = new ResourceLocation(PokecubeMod.ID, "textures/worn/keystone.png");
                    this.belt_2 = new ResourceLocation(PokecubeMod.ID, "textures/worn/megabelt_2.png");
                }
                float s, dx, dy, dz;
                dx = 0;
                dy = -.0f;
                dz = -0.6f;
                s = 0.525f;
                if (wearer.getItemStackFromSlot(EquipmentSlotType.LEGS) == null) s = 0.465f;
                final Vector3f dr = new Vector3f(dx, dy, dz);
                final Vector3f ds = new Vector3f(s, s, s);
                mat.rotate(net.minecraft.client.renderer.Vector3f.XP.rotationDegrees(90));
                mat.rotate(net.minecraft.client.renderer.Vector3f.ZP.rotationDegrees(180));

                mat.push();
                mat.translate(dr.x, dr.y, dr.z);
                mat.scale(ds.x, ds.y, ds.z);
                IVertexBuilder buf = Util.makeBuilder(buff, this.keystone);
                this.belt.renderOnly(mat, buf, "stone");
                DyeColor ret = DyeColor.GRAY;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                final Color colour = new Color(ret.getTextColor() + 0xFF000000);
                this.belt.getParts().get("belt").setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), 255,
                        brightness, overlay);
                buf = Util.makeBuilder(buff, this.belt_2);
                this.belt.renderOnly(mat, buf, "belt");
                mat.pop();
            }
        });
        WearablesCompat.renderers.put("hat", new WearablesRenderer()
        {
            // 2 layers of hat rendering for the different colours.
            @OnlyIn(Dist.CLIENT)
            X3dModel                         hat;

            // Textures for each hat layer.
            private final ResourceLocation   hat_1 = new ResourceLocation(PokecubeCore.MODID, "textures/worn/hat.png");
            private final ResourceLocation   hat_2 = new ResourceLocation(PokecubeCore.MODID, "textures/worn/hat2.png");
            private final ResourceLocation[] TEX   = { this.hat_1, this.hat_2 };

            @OnlyIn(Dist.CLIENT)
            @Override
            public void renderWearable(final MatrixStack mat, final IRenderTypeBuffer buff, final EnumWearable slot,
                    final int index, final LivingEntity wearer, final ItemStack stack, final float partialTicks,
                    final int brightness, final int overlay)
            {
                if (slot != EnumWearable.HAT) return;
                if (this.hat == null)
                    this.hat = new X3dModel(new ResourceLocation(PokecubeMod.ID, "models/worn/hat.x3d"));
                Hat.renderHat(mat, buff, wearer, stack, this.hat, this.TEX, brightness, overlay);
            }
        });
    }

    public static void init()
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
    public static void onItemCapabilityAttach(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(WearablesCompat.WEARABLESKEY)) return;
        if (event.getObject().getItem() instanceof ItemMegawearable)
            event.addCapability(WearablesCompat.WEARABLESKEY, new WearableMega());
        else if (event.getObject().getItem() == PokecubeItems.POKEWATCH)
            event.addCapability(WearablesCompat.WEARABLESKEY, new WearableWatch());
    }
}
