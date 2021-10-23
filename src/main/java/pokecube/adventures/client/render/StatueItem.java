package pokecube.adventures.client.render;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.pokemob.GuiPokemobBase;
import pokecube.core.database.Database;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;

public class StatueItem extends BlockEntityWithoutLevelRenderer implements IItemRenderProperties
{
    public StatueItem()
    {
        super(null, null);
    }

    static Map<UUID, LivingEntity> CACHE = Maps.newHashMap();

    @Override
    public void renderByItem(final ItemStack stack, final ItemTransforms.TransformType transform, final PoseStack mat,
            final MultiBufferSource bufs, final int light, final int overlay)
    {
        final boolean flag = stack.getTagElement("BlockEntityTag") != null;
        LivingEntity mob = null;
        final Minecraft mc = Minecraft.getInstance();
        tag:
        if (flag)
        {
            final CompoundTag blockTag = stack.getTagElement("BlockEntityTag");
            if (blockTag.contains("ForgeCaps"))
            {
                final CompoundTag capsTag = blockTag.getCompound("ForgeCaps");
                if (capsTag.contains("thutcore:copymob"))
                {
                    final CompoundTag copyTag = capsTag.getCompound("thutcore:copymob");
                    final ICopyMob copy = new CopyCaps.Impl();
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

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
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
        mob.setPos(0, 0, 0);
        mob.xRot = 0;
        mob.yHeadRot = 0;
        mob.yBodyRot = 0;
        mob.yRot = 0;
        mc.getEntityRenderDispatcher().setRenderShadow(false);
        mc.getEntityRenderDispatcher().render(mob, 0.5f, 0, 0.5f, 0, 0, mat, bufs, light);
    }

    @Override
    public BlockEntityWithoutLevelRenderer getItemStackRenderer()
    {
        return this;
    }
}
