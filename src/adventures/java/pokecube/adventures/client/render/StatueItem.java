package pokecube.adventures.client.render;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.database.Database;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.PokemobType;
import thut.api.ThutCaps;
import thut.api.attachments.CopyMob;
import thut.api.entity.ICopyMob;

public class StatueItem extends BlockEntityWithoutLevelRenderer implements IClientItemExtensions
{
    public StatueItem()
    {
        super(null, null);
    }

    public static Map<UUID, LivingEntity> CACHE = Maps.newHashMap();

    private LivingEntity getMob(ItemStack stack, final ItemDisplayContext displayContext)
    {
        LivingEntity mob = null;
        ICopyMob copy = new CopyMob.Impl();

        if (stack.has(CopyMob.COPY_STORE))
        {
            var info = stack.get(CopyMob.COPY_STORE);
            copy = info.copy();
            copy.recreateMob(Minecraft.getInstance().level);
            mob = copy.getCopiedMob();
        }
        else
        {
            Thread.dumpStack();
        }

        boolean initMob = false;

        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        if (initMob && pokemob != null)
        {
            float mobScale = 1;
            if (displayContext == ItemDisplayContext.GUI)
            {
                final Float value = GuiPokemobHelper.sizeMap.get(pokemob.getPokedexEntry());
                if (value != null) mobScale = value * 8.0f;
                else
                {
                    final boolean stock = pokemob.getPokedexEntry().stock;
                    if (stock)
                    {
                        final thut.api.maths.vecmath.Vec3f dims = pokemob.getPokedexEntry().getModelSize();
                        mobScale = Math.max(dims.z, Math.max(dims.y, dims.x));
                    }
                    else mobScale = Math.max(mob.getBbHeight(), mob.getBbWidth());
                }
                pokemob.setSize(0.55f / mobScale);
            }
            else pokemob.setSize(1);

            if (copy.getCopiedMob().getType() instanceof PokemobType<?> t)
            {
                if (pokemob != null && pokemob.getPokedexEntry() == Database.missingno
                        && t.getEntry() != Database.missingno)
                {
                    pokemob.setPokedexEntry(t.getEntry());
                    pokemob.setBasePokedexEntry(t.getEntry());
                }
            }
        }

        mob.setPos(0, 0, 0);
        mob.setXRot(0);
        mob.yHeadRot = 0;
        mob.yBodyRot = 0;
        mob.setYRot(0);
        return mob;
    }

    @Override
    public void renderByItem(final ItemStack stack, final ItemDisplayContext displayContext, final PoseStack mat,
            final MultiBufferSource bufs, final int light, final int overlay)
    {
        LivingEntity mob = getMob(stack, displayContext);
        var pokemob = PokemobCaps.getPokemobFor(mob);
        var genes = ThutCaps.getGenetics(mob);
        if (pokemob != null && genes != null)
        {
            if (!mob.getPersistentData().contains("pokecube:__gui__size"))
                mob.getPersistentData().putFloat("pokecube:__gui__size", pokemob.getSize());
            if (displayContext != ItemDisplayContext.GROUND)
            {
                float size = GuiPokemobHelper.sizeMap.getOrDefault(pokemob.getPokedexEntry(), 1.0f);
                pokemob.setSize(0.15f / size);
            }
            else pokemob.setSize(mob.getPersistentData().getFloat("pokecube:__gui__size"));
            var size_gene = genes.getAlleles(GeneticsManager.SIZEGENE);
            if (size_gene != null) size_gene.getExpressed().onUpdateTick(mob);
        }
        StatueBlock.renderStatue(mob, 0, mat, bufs, light, overlay);
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer()
    {
        return this;
    }
}
