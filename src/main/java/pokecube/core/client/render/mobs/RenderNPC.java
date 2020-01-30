package pokecube.core.client.render.mobs;

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
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.capabilities.TextureableCaps;
import pokecube.core.interfaces.capabilities.TextureableCaps.NPCCap;
import thut.api.entity.IMobTexturable;

public class RenderNPC<T extends LivingEntity> extends LivingRenderer<T, PlayerModel<T>>
{
    final PlayerModel<T> slim;
    final PlayerModel<T> normal;

    public RenderNPC(final EntityRendererManager renderManager)
    {
        super(renderManager, new PlayerModel<>(0.0F, false), 0.5F);
        this.normal = this.getEntityModel();
        this.slim = new PlayerModel<>(0.0f, true);
        this.addLayer(new BipedArmorLayer<>(this, new BipedModel<>(0.5F), new BipedModel<>(1.0F)));
        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new ArrowLayer<>(this));
        this.addLayer(new HeadLayer<>(this));
        this.addLayer(new ElytraLayer<>(this));
        this.addLayer(new SpinAttackEffectLayer<>(this));
    }

    @Override
    public void doRender(final T entity, final double x, final double y, final double z, final float entityYaw,
            final float partialTicks)
    {
        final IMobTexturable mob = entity.getCapability(TextureableCaps.CAPABILITY).orElse(null);
        if (mob instanceof NPCCap<?>) this.entityModel = ((NPCCap<?>) mob).slim.apply(entity) ? this.slim : this.normal;
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Override
    public ResourceLocation getEntityTexture(final T entity)
    {
        final IMobTexturable mob = entity.getCapability(TextureableCaps.CAPABILITY).orElse(null);
        if (mob instanceof NPCCap) return ((NPCCap<?>) mob).texGetter.apply(entity);
        return entity.func_213346_cF();
    }

    @Override
    protected boolean canRenderName(final T entity)
    {
        return PokecubeCore.getConfig().npcNameTags && super.canRenderName(entity);
    }

}
