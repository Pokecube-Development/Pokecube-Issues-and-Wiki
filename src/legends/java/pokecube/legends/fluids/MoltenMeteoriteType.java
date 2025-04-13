package pokecube.legends.fluids;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import pokecube.legends.init.FluidInit;

public class MoltenMeteoriteType extends FluidType
{
    public static final ResourceLocation MOLTEN_METEORITE_STILL = ResourceLocation.parse(
            "pokecube_legends:block/molten_meteorite");
    public static final ResourceLocation MOLTEN_METEORITE_FLOWING = ResourceLocation.parse(
            "pokecube_legends:block/molten_meteorite_flowing");
    public static final ResourceLocation MOLTEN_METEORITE_OVERLAY = ResourceLocation.parse(
            "pokecube_legends:block/molten_meteorite_overlay");

    public MoltenMeteoriteType(Properties properties)
    {
        super(properties);
    }

    public static BaseFlowingFluid.Properties makeProperties()
    {
        return new BaseFlowingFluid.Properties(FluidInit.MOLTEN_METEORITE_TYPE, FluidInit.MOLTEN_METEORITE,
                FluidInit.MOLTEN_METEORITE_FLOWING);
    }
}