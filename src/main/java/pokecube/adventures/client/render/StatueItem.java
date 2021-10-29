package pokecube.adventures.client.render;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.database.Database;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.CopyCaps;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.ICopyMob;
import thut.api.entity.animation.CapabilityAnimation;

public class StatueItem extends ItemStackTileEntityRenderer
{
    static Map<UUID, LivingEntity> CACHE = Maps.newHashMap();

    @Override
    public void renderByItem(final ItemStack stack, final TransformType transform, final MatrixStack mat,
            final IRenderTypeBuffer bufs, final int light, final int overlay)
    {
        final boolean flag = stack.getTagElement("BlockEntityTag") != null;
        LivingEntity mob = null;
        final Minecraft mc = Minecraft.getInstance();
        final ICopyMob copy = new CopyCaps.Impl();
        CompoundNBT modelTag = new CompoundNBT();
        tag:
        if (flag)
        {
            final CompoundNBT blockTag = stack.getTagElement("BlockEntityTag");
            modelTag = blockTag.getCompound("custom_model");
            if (blockTag.contains("ForgeCaps"))
            {
                final CompoundNBT capsTag = blockTag.getCompound("ForgeCaps");
                if (capsTag.contains("thutcore:copymob"))
                {
                    final CompoundNBT copyTag = capsTag.getCompound("thutcore:copymob");
                    copy.deserializeNBT(copyTag);
                    if (copy.getCopiedNBT().hasUUID("UUID"))
                    {
                        final UUID id = copy.getCopiedNBT().getUUID("UUID");
                        if ((mob = StatueItem.CACHE.get(id)) != null) break tag;
                    }
                    copy.onBaseTick(mc.level, mob);
                    mob = copy.getCopiedMob();
                    if (mob != null) StatueItem.CACHE.put(mob.getUUID(), mob);
                }
            }

        }
        if (mob == null) mob = StatueItem.CACHE.get(new UUID(0, 0));
        if (mob == null)
        {
            mob = PokecubeCore.createPokemob(Database.missingno, mc.level);
            StatueItem.CACHE.put(new UUID(0, 0), mob);
        }

        boolean hasCache = stack.getTagElement("__id_cache__") != null;
        if (hasCache)
        {
            final UUID id = stack.getTagElement("__id_cache__").getUUID("id");
            if (StatueItem.CACHE.containsKey(id)) mob = StatueItem.CACHE.get(id);
            else hasCache = false;
        }

        if (!hasCache)
        {
            String tex = null;
            String anim = null;
            String id = null;
            float size = 1;
            if (modelTag.contains("id")) id = modelTag.getString("id");
            if (modelTag.contains("tex")) tex = modelTag.getString("tex");
            if (modelTag.contains("anim")) anim = modelTag.getString("anim");
            if (modelTag.contains("size")) size = modelTag.getFloat("size");

            if (id != null)
            {
                copy.setCopiedID(new ResourceLocation(id));
                copy.setCopiedMob(null);
                copy.onBaseTick(mc.level, null);
                mob = copy.getCopiedMob();
            }
            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
            if (tex != null && pokemob != null)
            {
                final ResourceLocation texRes = new ResourceLocation(tex);
                final ResourceLocation name = new ResourceLocation(texRes.getNamespace(), pokemob.getPokedexEntry()
                        .getTrimmedName() + texRes.getPath());
                final FormeHolder old = pokemob.getCustomHolder();
                final ResourceLocation model = old != null ? old.model : null;
                final ResourceLocation animation = old != null ? old.animation : null;
                final FormeHolder holder = FormeHolder.get(model, texRes, animation, name);
                pokemob.setCustomHolder(holder);
            }
            if (pokemob != null) pokemob.setSize(size);
            final IAnimationHolder anims = mob.getCapability(CapabilityAnimation.CAPABILITY).orElse(null);
            if (anim != null && anims != null)
            {
                anims.setFixed(true);
                anims.overridePlaying(anim);
            }
            if (pokemob != null)
            {
                float mobScale = 1;

                if (transform == TransformType.GUI)
                {
                    final Float value = GuiPokemobBase.sizeMap.get(pokemob.getPokedexEntry());
                    if (value != null) mobScale = value * 2.0f;
                    else
                    {
                        final boolean stock = pokemob.getPokedexEntry().stock;
                        if (stock)
                        {
                            final thut.api.maths.vecmath.Vector3f dims = pokemob.getPokedexEntry().getModelSize();
                            mobScale = Math.max(dims.z, Math.max(dims.y, dims.x));
                        }
                        else mobScale = Math.max(mob.getBbHeight(), mob.getBbWidth());
                    }
                    pokemob.setSize(0.55f / mobScale);
                }
                else pokemob.setSize(1);
            }
            final UUID uuid = mob.getUUID();
            StatueItem.CACHE.put(mob.getUUID(), mob);
            stack.getOrCreateTagElement("__id_cache__").putUUID("id", uuid);
        }

        mob.setPos(0, 0, 0);
        mob.xRot = 0;
        mob.yHeadRot = 0;
        mob.yBodyRot = 0;
        mob.yRot = 0;
        mc.getEntityRenderDispatcher().setRenderShadow(false);
        mc.getEntityRenderDispatcher().render(mob, 0.5f, 0, 0.5f, 0, 0, mat, bufs, light);
    }
}
