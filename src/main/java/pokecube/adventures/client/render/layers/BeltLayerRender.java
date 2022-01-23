package pokecube.adventures.client.render.layers;

import java.util.Set;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.TrainerCaps;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class BeltLayerRender<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M>
{

    private static final Set<RenderLayerParent<?, ?>> addedLayers = Sets.newHashSet();

    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    @SubscribeEvent
    public static void renderSpecial(final RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event)
    {
        // Only apply to model bipeds.
        if (!(event.getRenderer().getModel() instanceof HumanoidModel<?>)) return;
        // Only one layer per renderer.
        if (addedLayers.contains(event.getRenderer())) return;
        event.getRenderer().addLayer(new BeltLayerRender(event.getRenderer()));

        addedLayers.add(event.getRenderer());
    }

    private final RenderLayerParent<?, ?> parent;

    public BeltLayerRender(RenderLayerParent<T, M> parent)
    {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void render(final PoseStack mat, final MultiBufferSource buff, final int packedLightIn, final T wearer,
            final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks,
            final float netHeadYaw, final float headPitch)
    {
        // No Render invisible.
        if (wearer.getEffect(MobEffects.INVISIBILITY) != null) return;
        // Only applies to bipeds, anyone else needs to write their own renderer
        if (!(this.parent.getModel() instanceof HumanoidModel<?> theModel)) return;

        IHasPokemobs trainer = TrainerCaps.getHasPokemobs(wearer);
        if (trainer == null || trainer.getMaxPokemobCount() > 6) return;
        final PlayerWearables worn = ThutWearables.getWearables(wearer);
        if (worn == null) return;
        ItemStack stack = worn.getWearable(EnumWearable.WAIST);
        if (stack.isEmpty()) return;

        mat.pushPose();
        theModel.body.translateAndRotate(mat);
        mat.translate(0, 0.65, -.125);
        mat.mulPose(Vector3f.XP.rotationDegrees(180));
        mat.mulPose(Vector3f.YP.rotationDegrees(180));
        mat.scale(0.25f, 0.25f, 0.25f);

        float scale = 0.3f;
        float[][] offsets =
        {
                { -1.5f, 0, 0 },
                { -2.5f, 0, 0 },
                { -3.0f, 0, 0.5f },

                { 1.5f, 0, 0 },
                { 2.5f, 0, 0 },
                { 3.0f, 0, 0.5f } };
        float[][] rotates =
        {
                { 0, 0, 0 },
                { 0, 0, 0 },
                { 0, 90, 0 },

                { 0, 0, 0 },
                { 0, 0, 0 },
                { 0, -90, 0 } };

        int n = trainer.getMaxPokemobCount();
        for (int i = 0; i < n; i++)
        {
            stack = trainer.getPokemob(i);
            if (stack.isEmpty()) continue;
            mat.pushPose();

            float x = offsets[i][0] * scale;
            float y = offsets[i][1] * scale;
            float z = offsets[i][2] * scale;
            mat.translate(x, y, z);
            float[] rots = rotates[i];
            if (rots[0] != 0) mat.mulPose(Vector3f.XP.rotationDegrees(rots[0]));
            if (rots[1] != 0) mat.mulPose(Vector3f.YP.rotationDegrees(rots[1]));
            if (rots[2] != 0) mat.mulPose(Vector3f.ZP.rotationDegrees(rots[2]));
            Minecraft.getInstance().getItemInHandRenderer().renderItem(wearer, stack, TransformType.GROUND, false, mat,
                    buff, packedLightIn);
            mat.popPose();
        }

        mat.popPose();
    }
}
