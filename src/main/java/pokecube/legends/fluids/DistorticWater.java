package pokecube.legends.fluids;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import pokecube.legends.init.FluidInit;
import pokecube.legends.init.ItemInit;

public class DistorticWater
{
    public static final ResourceLocation DISTORTIC_WATER_STILL_TEXTURE = new ResourceLocation(
            "pokecube_legends:block/distortic_water_still");
    public static final ResourceLocation DISTORTIC_WATER_FLOWING_TEXTURE = new ResourceLocation(
            "pokecube_legends:block/distortic_water_flow");
    public static final ResourceLocation DISTORTIC_WATER_OVERLAY_TEXTURE = new ResourceLocation(
            "pokecube_legends:block/distortic_water_overlay");

    public static ForgeFlowingFluid.Properties makeProperties()
    {
        return new ForgeFlowingFluid.Properties(FluidInit.DISTORTED_WATER_TYPE, FluidInit.DISTORTED_WATER,
                FluidInit.DISTORTED_WATER_FLOWING).bucket(ItemInit.DISTORTED_WATER_BUCKET)
                        .block(FluidInit.DISTORTED_WATER_BLOCK);
    }
}