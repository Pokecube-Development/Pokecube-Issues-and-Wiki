package pokecube.legends.fluids;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import pokecube.legends.init.FluidInit;
import pokecube.legends.init.ItemInit;

public class DistorticWaterType extends FluidType
{
    public static final ResourceLocation UNDERWATER_LOCATION = ResourceLocation.parse(
            "pokecube_legends:textures/misc/distortic_underwater.png");
    public static final ResourceLocation DISTORTIC_WATER_STILL = ResourceLocation.parse(
            "pokecube_legends:block/distortic_water_still");
    public static final ResourceLocation DISTORTIC_WATER_FLOWING = ResourceLocation.parse(
            "pokecube_legends:block/distortic_water_flowing");
    public static final ResourceLocation DISTORTIC_WATER_OVERLAY = ResourceLocation.parse(
            "pokecube_legends:block/distortic_water_overlay");

    public DistorticWaterType(Properties properties)
    {
        super(properties);
    }

    public static BaseFlowingFluid.Properties makeProperties()
    {
        return new BaseFlowingFluid.Properties(FluidInit.DISTORTIC_WATER_TYPE, FluidInit.DISTORTIC_WATER,
                FluidInit.DISTORTIC_WATER_FLOWING).bucket(ItemInit.DISTORTIC_WATER_BUCKET)
                .block(FluidInit.DISTORTIC_WATER_BLOCK);
    }

}