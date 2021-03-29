package pokecube.core.client.render.mobs;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.interfaces.capabilities.TextureableCaps;
import pokecube.core.interfaces.capabilities.TextureableCaps.NPCCap;
import thut.api.entity.IMobTexturable;

public class RenderNPC<T extends NpcMob> extends LivingRenderer<T, PlayerModel<T>>
{
    final PlayerModel<T> slim;
    final PlayerModel<T> normal;

    public RenderNPC(final EntityRendererManager renderManager)
    {
        super(renderManager, new PlayerModel<>(0.0F, false), 0.5F);
        this.normal = this.getModel();
        this.slim = new PlayerModel<>(0.0f, true);
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel<>(0.5F), new BipedModel<>(1.0F)));
        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new ArrowLayer<>(this));
        this.addLayer(new HeadLayer<>(this));
        this.addLayer(new ElytraLayer<>(this));
        this.addLayer(new SpinAttackEffectLayer<>(this));
    }

    @Override
    public void render(final T entityIn, final float entityYaw, final float partialTicks,
            final MatrixStack matrixStackIn, final IRenderTypeBuffer bufferIn, final int packedLightIn)
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
