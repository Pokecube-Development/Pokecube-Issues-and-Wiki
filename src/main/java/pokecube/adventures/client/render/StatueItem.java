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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.statue.StatueEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.database.Database;
import pokecube.core.entity.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.PokemobType;
import thut.api.ThutCaps;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;

public class StatueItem extends BlockEntityWithoutLevelRenderer implements IItemRenderProperties
{
    private static class InitMob implements Runnable
    {

        private final ICopyMob copy;
        private final String id;
        private final Minecraft mc;

        public InitMob(ICopyMob copy, String id)
        {
            this.copy = copy;
            this.id = id;
            this.mc = Minecraft.getInstance();
        }

        @Override
        public void run()
        {
            if (id != null)
            {
                copy.setCopiedID(new ResourceLocation(id));
                copy.setCopiedMob(null);
                copy.onBaseTick(mc.level, null);
            }
            else
            {
                copy.setCopiedID(new ResourceLocation("pokecube:missingno"));
                copy.setCopiedMob(PokecubeCore.createPokemob(Database.missingno, mc.level));
            }
        }

    }

    private StatueEntity statue_cache = null;

    public StatueItem()
    {
        super(null, null);
    }

    public static Map<UUID, LivingEntity> CACHE = Maps.newHashMap();

    private LivingEntity getMob(ItemStack stack, final ItemTransforms.TransformType transform)
    {
        LivingEntity mob = null;
        final Minecraft mc = Minecraft.getInstance();
        ICopyMob copy = new CopyCaps.Impl();
        CompoundTag modelTag = new CompoundTag();

        if (statue_cache == null)
            statue_cache = new StatueEntity(BlockPos.ZERO, PokecubeAdv.STATUE.get().defaultBlockState());

        boolean initMob = true;

        boolean hasCache = stack.getTagElement("__id_cache__") != null;
        if (hasCache)
        {
            final UUID id = stack.getTagElement("__id_cache__").getUUID("id");
            if (StatueItem.CACHE.containsKey(id)) mob = StatueItem.CACHE.get(id);
            else hasCache = false;
            hasCache = false;
        }

        final boolean flag = !hasCache && stack.getTagElement("BlockEntityTag") != null;

        if (flag)
        {
            final CompoundTag blockTag = stack.getTagElement("BlockEntityTag");
            if (blockTag.contains("ForgeCaps"))
            {
                final CompoundTag capsTag = blockTag.getCompound("ForgeCaps");
                if (capsTag.contains("thutcore:copymob"))
                {
                    final CompoundTag copyTag = capsTag.getCompound("thutcore:copymob");
                    copy.deserializeNBT(copyTag);
                    copy.onBaseTick(mc.level, mob);
                    mob = copy.getCopiedMob();
                    if (mob != null)
                    {
                        StatueItem.CACHE.put(mob.getUUID(), mob);
                        stack.getOrCreateTagElement("__id_cache__").putUUID("id", mob.getUUID());
                        initMob = true;
                        hasCache = true;
                    }
                }
            }
            else
            {
                statue_cache.setLevel(mc.level);
                statue_cache.load(blockTag);
                copy = ThutCaps.getCopyMob(statue_cache);
                if (copy != null)
                {
                    mob = copy.getCopiedMob();
                    if (mob != null)
                    {
                        final UUID uuid = mob.getUUID();
                        StatueItem.CACHE.put(mob.getUUID(), mob);
                        stack.getOrCreateTagElement("__id_cache__").putUUID("id", uuid);
                        initMob = true;
                        hasCache = true;
                    }
                }
            }
        }

        if (mob == null) mob = StatueItem.CACHE.get(new UUID(0, 0));
        if (mob == null)
        {
            mob = PokecubeCore.createPokemob(Database.missingno, mc.level);
            StatueItem.CACHE.put(new UUID(0, 0), mob);
        }

        if (!hasCache)
        {
            String id = null;
            if (modelTag.contains("id")) id = modelTag.getString("id");
            mob = StatueEntity.initMob(copy, modelTag, new InitMob(copy, id));
            initMob = true;
            final UUID uuid = mob.getUUID();
            StatueItem.CACHE.put(mob.getUUID(), mob);
            stack.getOrCreateTagElement("__id_cache__").putUUID("id", uuid);
        }

        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        if (initMob && pokemob != null)
        {
            float mobScale = 1;
            if (transform == TransformType.GUI)
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
        mob.xRot = 0;
        mob.yHeadRot = 0;
        mob.yBodyRot = 0;
        mob.yRot = 0;
        return mob;
    }

    @Override
    public void renderByItem(final ItemStack stack, final ItemTransforms.TransformType transform, final PoseStack mat,
            final MultiBufferSource bufs, final int light, final int overlay)
    {
        LivingEntity mob = getMob(stack, transform);
        var pokemob = PokemobCaps.getPokemobFor(mob);
        var genes = ThutCaps.getGenetics(mob);
        if (pokemob != null && genes != null)
        {
            if (!mob.getPersistentData().contains("pokecube:__gui__size"))
                mob.getPersistentData().putFloat("pokecube:__gui__size", pokemob.getSize());
            if (transform != TransformType.GROUND)
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
    public BlockEntityWithoutLevelRenderer getItemStackRenderer()
    {
        return this;
    }
}
