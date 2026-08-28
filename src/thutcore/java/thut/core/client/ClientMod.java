package thut.core.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lwjgl.opengl.GL11;
import thut.core.client.render.model.parts.Mesh;
import thut.core.common.ThutCore;

@Mod(value = ThutCore.MODID, dist = Dist.CLIENT)
public class ClientMod
{
    public ClientMod(ModContainer container)
    {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        Mesh.QUAD_FMT = GL11.GL_QUADS;
        Mesh.TRIANGLE_FMT = GL11.GL_TRIANGLES;
    }
}
