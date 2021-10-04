package pokecube.core.client.render.mobs;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
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

    public RenderNPC(final EntityRenderDispatcher renderManager)
    {
        super(renderManager, new PlayerModel<>(0.0F, false), 0.5F);
        this.normal = this.getModel();
        this.slim = new PlayerModel<>(0.0f, true);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(0.5F), new HumanoidModel<>(1.0F)));
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new ArrowLayer<>(this));
        this.addLayer(new CustomHeadLayer<>(this));
        this.addLayer(new ElytraLayer<>(this));
        this.addLayer(new SpinAttackEffectLayer<>(this));
    }

    @Override
    public void render(final T entityIn, final float entityYaw, final float partialTicks,
            final PoseStack matrixStackIn, final MultiBufferSource bufferIn, final int packedLightIn)
    {
        final IMobTexturable mob = entityIn.getCapability(TextureableCaps.CAPABILITY).orElse(null);
        if (mob instanceof NPCCap<?>) this.model = ((NPCCap<?>) mob).slim.apply(entityIn) ? this.slim : this.normal;
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(final T entity)
    {
        final IMobTexturable mob = entity.getCapability(TextureableCaps.CAPABILITY).orElse(null);
        if (mob instanceof NPCCap) return ((NPCCap<?>) mob).texGetter.apply(entity);
        return new ResourceLocation("empty");
    }

    @Override
    protected boolean shouldShowName(final T entity)
    {
        final Minecraft minecraft = Minecraft.getInstance();
        return PokecubeCore.getConfig().npcNameTags && entity.canSee(minecraft.getCameraEntity())
                && super.shouldShowName(entity);
    }

}
