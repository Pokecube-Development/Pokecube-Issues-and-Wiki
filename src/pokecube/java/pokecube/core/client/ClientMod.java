package pokecube.core.client;

import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.damage.effects.StatusEffects;

import java.lang.reflect.Field;

@Mod(value = PokecubeCore.MODID, dist = Dist.CLIENT)
public class ClientMod
{
    public ClientMod(ModContainer container)
    {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mc, parent) -> new ConfigurationScreen(container, parent));
        container.getEventBus().addListener(ClientMod::onClientExtensions);
    }

    public static void onClientExtensions(RegisterClientExtensionsEvent event)
    {
        // TODO decide on if we want custom mob effect icons here?

        // anyways adds a blindness effect for sleep status.
        FogRenderer.MOB_EFFECT_FOG.add(new FogRenderer.MobEffectFogFunction()
        {

            @Override
            public Holder<MobEffect> getMobEffect()
            {
                return StatusEffects.SLEEP;
            }

            @Override
            public void setupFog(FogRenderer.FogData fogData, LivingEntity entity, MobEffectInstance effectInstance,
                    float farPlaneDistance, float p_234216_)
            {
                // Below is copied from the BlindnessFogFunction
                float f = effectInstance.isInfiniteDuration()
                        ? 5.0F
                        : Mth.lerp(Math.min(1.0F, (float) effectInstance.getDuration() / 20.0F), farPlaneDistance,
                                5.0F);
                if (fogData.mode == FogRenderer.FogMode.FOG_SKY)
                {
                    fogData.start = 0.0F;
                    fogData.end = f * 0.8F;
                }
                else
                {
                    fogData.start = f * 0.25F;
                    fogData.end = f;
                }
            }
        });
    }
}
