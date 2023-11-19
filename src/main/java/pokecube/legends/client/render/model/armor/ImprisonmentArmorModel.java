package pokecube.legends.client.render.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class ImprisonmentArmorModel extends HumanoidModel<LivingEntity>
{
    public ImprisonmentArmorModel(ModelPart root) {
        super(root);
    }

    public static MeshDefinition setup(CubeDeformation deformation) {
        MeshDefinition mesh = HumanoidModel.createMesh(deformation, 0);
        PartDefinition part = mesh.getRoot();

        var head = part.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, deformation),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var brace = head.addOrReplaceChild("brace",
                CubeListBuilder.create()
                        .texOffs(0, 17)
                        .addBox(-5.0F, -1.25F, -5.0F, 10.0F, 2.0F, 9.0F, deformation),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var left_bolt = head.addOrReplaceChild("left_bolt",
                CubeListBuilder.create()
                        .texOffs(47, 16)
                        .addBox(4.4F, -5.0F, -2.0F, 1.0F, 4.0F, 4.0F, deformation.extend(0F, 0F, 0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var right_bolt = head.addOrReplaceChild("right_bolt",
                CubeListBuilder.create()
                        .texOffs(47, 25)
                        .addBox(-5.4F, -5.0F, -2.0F, 1.0F, 4.0F, 4.0F, deformation.extend(0F, 0F, 0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var spikes = brace.addOrReplaceChild("spikes",
                CubeListBuilder.create()
                        .texOffs(0, 43)
                        .addBox(-9.0F, 1.5F, -9.0F, 18.0F, 0.0F, 18.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var chin_spike = brace.addOrReplaceChild("chin_spike",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .addBox(-1.5F, 1.5F, -4.9F, 3.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var left_ear = head.addOrReplaceChild("left_ear",
                CubeListBuilder.create()
                        .texOffs(30, 15)
                        .addBox(1.0F, -11.5F, -4.0F, 3.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var right_ear = head.addOrReplaceChild("right_ear",
                CubeListBuilder.create()
                        .texOffs(30, 20)
                        .addBox(-4.0F, -11.5F, -4.0F, 3.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var axe = head.addOrReplaceChild("axe",
                CubeListBuilder.create()
                        .texOffs(24, 16)
                        .addBox(-1.0F, -16.5F, -2.0F, 2.0F, 8.0F, 18.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var axe_piece = axe.addOrReplaceChild("axe_piece",
                CubeListBuilder.create()
                        .texOffs(0, 46)
                        .addBox(-1.0F, -16.5F, -5.0F, 2.0F, 5.0F, 3.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var axe_sheath = axe.addOrReplaceChild("axe_sheath",
                CubeListBuilder.create()
                        .texOffs(0, 29)
                        .addBox(-2.0F, -17.5F, 4.0F, 4.0F, 10.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));


        return mesh;
    }
}
