package thut.core.client.render.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import thut.core.client.render.model.parts.Material;

public interface IModelCustom
{
    default void render(final PoseStack mat, final VertexConsumer buffer)
    {

    }

    default void renderAll(final PoseStack mat, final VertexConsumer buffer)
    {

    }

    default void renderAllExcept(final PoseStack mat, final VertexConsumer buffer, final Collection<String> excluded)
    {

    }

    default void renderOnly(final PoseStack mat, final VertexConsumer buffer, final Collection<String> groupNames)
    {

    }

    default void renderPart(final PoseStack mat, final VertexConsumer buffer, final String partName)
    {

    }

    default void prepareRender()
    {

    }

    List<Material> getMaterials();

    /**
     * This is used to ensure all sub parts share the same set of materials,
     * for use with sharing render types, etc
     */
    void updateMaterials(Collection<Material> materials);
}
