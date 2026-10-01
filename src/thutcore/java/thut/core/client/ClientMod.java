package thut.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lwjgl.opengl.GL11;
import thut.api.ThutAPI;
import thut.api.entity.multipart.GenericPartEntity;
import thut.api.model.Material;
import thut.api.model.render.MaterialRenderable;
import thut.api.model.Mesh;
import thut.api.model.render.MeshRenderable;
import thut.core.common.ThutCore;
import thut.api.util.ResourceHelper;
import thut.core.common.network.PartInteract;

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

        var oldSupply = ThutAPI.REGISTRY_SUPPLY;
        ThutAPI.REGISTRY_SUPPLY = () -> {
            final net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            // This is null on single player, so we have an integrated server
            if (mc.getCurrentServer() == null) return oldSupply.get();
            if (mc.level == null) return null;
            return mc.level.registryAccess();
        };

        GenericPartEntity.SENDER = new GenericPartEntity.InteractSender()
        {
            @Override
            public void onHurt(String name, Entity entityIn, boolean sneak)
            {
                final PartInteract packet = new PartInteract(name, entityIn, sneak);
                ThutCore.packets.sendToServer(packet);
            }

            @Override
            public void onInteract(String name, Entity entityIn, InteractionHand handIn, Vec3 hitVecIn, boolean sneak)
            {
                final PartInteract packet = new PartInteract(name, entityIn, handIn, hitVecIn, sneak);
                ThutCore.packets.sendToServer(packet);
            }

            @Override
            public void onInteract(String name, Entity entityIn, InteractionHand handIn, boolean sneak)
            {

                final PartInteract packet = new PartInteract(name, entityIn, handIn, sneak);
                ThutCore.packets.sendToServer(packet);
            }
        };
    }
}
