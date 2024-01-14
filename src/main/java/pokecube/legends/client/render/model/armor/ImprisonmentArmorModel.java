package pokecube.legends.client.render.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public class ImprisonmentArmorModel<T extends Entity> extends EntityModel<T>
{
    public final ModelPart head;

    public ImprisonmentArmorModel(ModelPart root) {
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F,
                                new CubeDeformation(1.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition brace = head.addOrReplaceChild("brace",
                CubeListBuilder.create()
                        .texOffs(0, 17)
                        .addBox(-5.0F, -1.25F, -5.0F, 10.0F, 2.0F, 9.0F,
                                new CubeDeformation(1.0F, 0.25F, 1.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_bolt = head.addOrReplaceChild("left_bolt",
                CubeListBuilder.create()
                        .texOffs(47, 16)
                        .addBox(5.0F, -4.75F, -2.0F, 1.0F, 4.0F, 4.0F,
                                new CubeDeformation(0.25F, 0.5F, 0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_bolt = head.addOrReplaceChild("right_bolt",
                CubeListBuilder.create()
                        .texOffs(47, 25)
                        .addBox(-6.0F, -4.75F, -2.0F, 1.0F, 4.0F, 4.0F,
                                new CubeDeformation(0.25F, 0.5F, 0.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition spikes = brace.addOrReplaceChild("spikes",
                CubeListBuilder.create()
                        .texOffs(0, 43)
                        .addBox(-9.0F, 2.6F, -9.0F, 18.0F, 0.0F, 18.0F,
                                new CubeDeformation(1.5F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition chin_spike = brace.addOrReplaceChild("chin_spike",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .addBox(-1.5F, 1.25F, -5.7F, 3.0F, 4.0F, 1.0F,
                                new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear",
                CubeListBuilder.create()
                        .texOffs(30, 15)
                        .addBox(1.6F, -12.4F, -3.4F, 3.0F, 3.0F, 1.0F,
                                new CubeDeformation(0.4F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear",
                CubeListBuilder.create()
                        .texOffs(30, 20)
                        .addBox(-4.6F, -12.4F, -3.4F, 3.0F, 3.0F, 1.0F,
                                new CubeDeformation(0.4F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition axe = head.addOrReplaceChild("axe",
                CubeListBuilder.create()
                        .texOffs(24, 16)
                        .addBox(-1.0F, -17.9F, -2.0F, 2.0F, 8.0F, 18.0F,
                                new CubeDeformation(0.25F, 1.0F, 1.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition axe_piece = axe.addOrReplaceChild("axe_piece",
                CubeListBuilder.create()
                        .texOffs(0, 46)
                        .addBox(-1.0F, -18.25F, -6.25F, 2.0F, 5.0F, 3.0F,
                                new CubeDeformation(0.25F, 0.65F, 0.25F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition axe_sheath = axe.addOrReplaceChild("axe_sheath",
                CubeListBuilder.create()
                        .texOffs(0, 29)
                        .addBox(-2.0F, -18.9F, 4.0F, 4.0F, 10.0F, 5.0F,
                                new CubeDeformation(0.25F, 1.0F, 0.25F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));


        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green,
                               float blue, float alpha) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
