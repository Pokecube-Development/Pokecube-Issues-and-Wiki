package thut.core.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lwjgl.opengl.GL11;
import thut.core.client.render.model.parts.Material;
import thut.core.client.render.model.parts.MaterialRenderable;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.model.parts.MeshRenderable;
import thut.core.common.ThutCore;
import thut.lib.ResourceHelper;

@Mod(value = ThutCore.MODID, dist = Dist.CLIENT)
public class ClientMod
{
    public ClientMod(ModContainer container)
    {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        Mesh.QUAD_FMT = GL11.GL_QUADS;
        Mesh.TRIANGLE_FMT = GL11.GL_TRIANGLES;

        ResourceHelper.RESOURCE_SOURCE = () -> Minecraft.getInstance().getResourceManager();

        Mesh.MESH_FACTORY = MeshRenderable::new;
        Mesh.MERGE_FACTORY = MeshRenderable::new;

        Material.MATERIAL_FACTORY = MaterialRenderable::new;
    }
}
