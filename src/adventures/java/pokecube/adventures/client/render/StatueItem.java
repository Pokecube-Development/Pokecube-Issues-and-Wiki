package pokecube.adventures.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import pokecube.adventures.blocks.statue.StatueEntity;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.entity.genetics.GeneticsManager;
import thut.api.ThutCaps;
import thut.api.attachments.CopyMob;

public class StatueItem extends BlockEntityWithoutLevelRenderer implements IClientItemExtensions
{
    public StatueItem()
    {
        super(null, null);
    }

    private LivingEntity getMob(ItemStack stack, final ItemDisplayContext displayContext)
    {
        LivingEntity mob = null;
        var info = StatueEntity.unpackStatue(stack, Minecraft.getInstance().level);
        if (info == null) return null;
        var copy = info.copy();
        if (copy != null) mob = copy.getCopiedMob();
        if (mob == null) return null;

        if (!info.tag().contains("statue_item:edited"))
        {
            CompoundTag tag = info.tag().copy();
            tag.putBoolean("statue_item:edited", true);
            info = new CopyMob.CopyInfo(tag);
            stack.set(CopyMob.COPY_STORE, info);
            info = StatueEntity.unpackStatue(stack, Minecraft.getInstance().level);
            mob = info.copy().getCopiedMob();
            mob.setPos(0, 0, 0);
            mob.setXRot(0);
            mob.yHeadRot = 0;
            mob.yBodyRot = 0;
            mob.setYRot(0);
        }
        return mob;
    }

    @Override
    public void renderByItem(final ItemStack stack, final ItemDisplayContext displayContext, final PoseStack mat,
            final MultiBufferSource bufs, final int light, final int overlay)
    {
        LivingEntity mob = getMob(stack, displayContext);
        if (mob == null)
        {
            return;
        }
        var info = StatueEntity.unpackStatue(stack, Minecraft.getInstance().level);
        var pokemob = PokemobCaps.getPokemobFor(mob);
        var genes = ThutCaps.getGenetics(mob);
        if (pokemob != null && genes != null)
        {
            CompoundTag tag = info.tag();

            if (!tag.contains("pokecube:__gui__size"))
            {
                tag = tag.copy();
                tag.putFloat("pokecube:__gui__size", pokemob.getSize());
                info = new CopyMob.CopyInfo(tag);
                stack.set(CopyMob.COPY_STORE, info);
                mob = getMob(stack, displayContext);
                info = StatueEntity.unpackStatue(stack, Minecraft.getInstance().level);
                pokemob = PokemobCaps.getPokemobFor(mob);
                genes = ThutCaps.getGenetics(mob);
            }

            if (displayContext != ItemDisplayContext.GROUND)
            {
                float size = GuiPokemobHelper.sizeMap.getOrDefault(pokemob.getPokedexEntry(), 1.0f);
                pokemob.setSize(0.15f / size);
            }
            else pokemob.setSize(tag.getFloat("pokecube:__gui__size"));
            var size_gene = genes.getAlleles(GeneticsManager.SIZEGENE);
            if (size_gene != null) size_gene.getExpressed().onUpdateTick(mob);
        }
        StatueBlock.renderStatue(info, 0, mat, bufs, light, overlay);
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer()
    {
        return this;
    }
}
