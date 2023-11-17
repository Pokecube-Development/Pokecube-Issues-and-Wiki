package pokecube.legends.client.render.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ImprisonmentArmorModel {

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
                        .addBox(-5.0F, -2.5F, -5.0F, 10.0F, 3.0F, 9.0F, deformation),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var left_bolt = head.addOrReplaceChild("left_bolt",
                CubeListBuilder.create()
                        .texOffs(30, 17)
                        .addBox(4.5F, -6.0F, -2.0F, 0.5F, 4.0F, 4.0F, deformation),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var right_bolt = head.addOrReplaceChild("right_bolt",
                CubeListBuilder.create()
                        .texOffs(41, 17)
                        .addBox(-5.5F, -6.0F, -2.0F, 0.5F, 4.0F, 4.0F, deformation),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var spikes = head.addOrReplaceChild("spikes",
                CubeListBuilder.create()
                        .texOffs(0, 42)
                        .addBox(-8.0F, 1.1F, -8.0F, 16.0F, 0.0F, 16.0F, deformation),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        var chin_spike = head.addOrReplaceChild("chin_spike",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .addBox(-0.5F, 3.0F, -4.9F, 3.0F, 4.0F, 0.0F, deformation),
                PartPose.rotation(0F, 0F, 0F / (180F / (float) Math.PI)));

        return mesh;
    }
}
