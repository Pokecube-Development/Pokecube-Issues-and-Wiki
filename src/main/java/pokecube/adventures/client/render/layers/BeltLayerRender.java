package pokecube.adventures.client.render.layers;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.trainers.IHasPokemobs;
import pokecube.api.entity.trainers.TrainerCaps;
import thut.lib.AxisAngles;
import thut.wearables.EnumWearable;
import thut.wearables.ThutWearables;
import thut.wearables.inventory.PlayerWearables;

public class BeltLayerRender<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M>
{
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
        mat.translate(0, 0.785, -.125);
        mat.mulPose(AxisAngles.XP.rotationDegrees(180));
        mat.mulPose(AxisAngles.YP.rotationDegrees(180));
        mat.scale(0.6f, 0.6f, 0.6f);

        float scale = 0.3f;
        float[][] offsets =
        {
                // 1st slot, front left
                { -0.95f, 0, 0 },
                // 2nd slot, front right
                { 0.95f, 0, 0 },

                // 3rd slot, side left
                { -1.5f, 0, 0.7f },
                // 4th slot, side right
                { 1.5f, 0, 0.7f },

                // 5th slot, back left
                { -0.95f, 0, 1.4f },
                // 6th slot, back right
                { 0.95f, 0, 1.4f }
        };

        float[][] rotates =
        {
                // 1st slot, front left
                { -20, 0, 0 },
                // 2nd slot, front right
                { -20, 0, 0 },

                // 3rd slot, side left
                { 90, -70, 90 },
                // 4th slot, side right
                { -90, -70, -90 },

                // 5th slot, back left
                { 20, 180, 0 },
                // 6th slot, back right
                { 20, 180, 0 }
        };

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
            if (rots[0] != 0) mat.mulPose(AxisAngles.XP.rotationDegrees(rots[0]));
            if (rots[1] != 0) mat.mulPose(AxisAngles.YP.rotationDegrees(rots[1]));
            if (rots[2] != 0) mat.mulPose(AxisAngles.ZP.rotationDegrees(rots[2]));
            Minecraft.getInstance().getItemInHandRenderer().renderItem(wearer, stack, TransformType.GROUND, false, mat,
                    buff, packedLightIn);
            mat.popPose();
        }
        mat.popPose();
    }
}
