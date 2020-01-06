package pokecube.core.client.render.mobs;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.mobs.overlays.Evolution;
import pokecube.core.client.render.mobs.overlays.ExitCube;
import pokecube.core.client.render.mobs.overlays.Health;
import pokecube.core.client.render.mobs.overlays.Status;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.wrappers.ModelWrapper;

@Mod.EventBusSubscriber
public class RenderMobOverlays
{
    public static boolean enabled = true;

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void renderSpecial(@SuppressWarnings("rawtypes") final RenderLivingEvent.Specials.Pre event)
    {
        if (!RenderMobOverlays.enabled) return;
        final Minecraft mc = Minecraft.getInstance();
        if (!PokecubeCore.getConfig().renderInF1 && !Minecraft.isGuiEnabled()) return;
        final Entity cameraEntity = mc.getRenderViewEntity();
        final float partialTicks = event.getPartialRenderTick();
        if (cameraEntity == null || !event.getEntity().isAlive()) return;
        final Vec3d pos = new Vec3d(event.getX(), event.getY(), event.getZ());

        if (PokecubeCore.getConfig().doHealthBars) Health.renderHealthBar(event.getEntity(), partialTicks, cameraEntity,
                pos);

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            Evolution.render(pokemob, pos, partialTicks);
            ExitCube.render(pokemob, pos, partialTicks);

            if (event.getRenderer().getEntityModel() instanceof ModelWrapper)
            {
                final ModelWrapper<?> wrapper = (ModelWrapper<?>) event.getRenderer().getEntityModel();
                Status.render((IModelRenderer<MobEntity>) wrapper.renderer, pos, pokemob.getEntity(), 0, 0, 0, 1,
                        partialTicks);
            }
        }
    }
}
