package pokecube.core.client.render.mobs;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.interfaces.capabilities.TextureableCaps;
import pokecube.core.interfaces.capabilities.TextureableCaps.NPCCap;
import thut.api.entity.IMobTexturable;

public class RenderNPC<T extends NpcMob> extends LivingEntityRenderer<T, PlayerModel<T>>
{
    final PlayerModel<T> slim;
    final PlayerModel<T> normal;

    protected final List<RenderLayer<T, PlayerModel<T>>> layers_slim   = Lists.newArrayList();
    protected final List<RenderLayer<T, PlayerModel<T>>> layers_normal = Lists.newArrayList();

    public RenderNPC(final EntityRendererProvider.Context context)
    {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.normal = this.getModel();
        this.slim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);

        this.layers_slim.add(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(
                ModelLayers.PLAYER_SLIM_INNER_ARMOR)), new HumanoidModel<>(context.bakeLayer(
                        ModelLayers.PLAYER_SLIM_OUTER_ARMOR))));

        this.layers_normal.add(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(
                ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidModel<>(context.bakeLayer(
                        ModelLayers.PLAYER_OUTER_ARMOR))));

        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new ArrowLayer<>(context, this));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet()));
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
        // this.addLayer(new ParrotOnShoulderLayer(this,
        // context.getModelSet()));
        this.addLayer(new SpinAttackEffectLayer<>(this, context.getModelSet()));
        this.addLayer(new BeeStingerLayer<>(this));
    }

    @Override
    public void render(final T entityIn, final float entityYaw, final float partialTicks, final PoseStack matrixStackIn,
            final MultiBufferSource bufferIn, final int packedLightIn)
    {
        final IMobTexturable mob = TextureableCaps.forMob(entityIn);
        if (mob instanceof NPCCap<?>) this.model = ((NPCCap<?>) mob).slim.apply(entityIn) ? this.slim : this.normal;
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(final T entity)
    {
        final IMobTexturable mob = TextureableCaps.forMob(entity);
        if (mob instanceof NPCCap) return ((NPCCap<?>) mob).texGetter.apply(entity);
        return new ResourceLocation("empty");
    }

    @Override
    protected boolean shouldShowName(final T entity)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        return PokecubeCore.getConfig().npcNameTags && entity.hasLineOfSight(minecraft.getCameraEntity())
                && super.shouldShowName(entity);
    }

}
