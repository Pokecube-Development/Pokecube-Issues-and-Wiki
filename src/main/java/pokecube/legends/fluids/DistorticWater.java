package pokecube.legends.fluids;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import pokecube.legends.init.FluidInit;
import pokecube.legends.init.ItemInit;

public class DistorticWater
{
    public static final ResourceLocation DISTORTIC_WATER_STILL_TEXTURE = new ResourceLocation("pokecube_legends:blocks/distortic_water_still");
    public static final ResourceLocation DISTORTIC_WATER_FLOWING_TEXTURE = new ResourceLocation("pokecube_legends:blocks/distortic_water_flow");
    public static final ResourceLocation DISTORTIC_WATER_OVERLAY_TEXTURE = new ResourceLocation("pokecube_legends:blocks/distortic_water_overlay");

    public static ForgeFlowingFluid.Properties makeProperties(){
        return new ForgeFlowingFluid.Properties(FluidInit.DISTORTED_WATER, FluidInit.DISTORTED_WATER_FLOWING,
                FluidAttributes.builder(DISTORTIC_WATER_STILL_TEXTURE, DISTORTIC_WATER_FLOWING_TEXTURE)
                .overlay(DISTORTIC_WATER_OVERLAY_TEXTURE)
                .density(-500)
                .temperature(20)
                .viscosity(20)
                .gaseous()
                .luminosity(10)
                .sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY))
                .bucket(ItemInit.DISTORTED_WATER_BUCKET)
                .block(FluidInit.DISTORTED_WATER_BLOCK);
    }
}