package pokecube.legends.fluids;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import pokecube.legends.init.FluidInit;
import pokecube.legends.init.ItemInit;

public class DistorticWaterType extends FluidType
{
    public static final ResourceLocation DISTORTIC_WATER_STILL = new ResourceLocation("pokecube_legends:block/distortic_water_still");
    public static final ResourceLocation DISTORTIC_WATER_FLOWING = new ResourceLocation("pokecube_legends:block/distortic_water_flowing");
    public static final ResourceLocation DISTORTIC_WATER_OVERLAY = new ResourceLocation("pokecube_legends:block/distortic_water_overlay");

    public DistorticWaterType(Properties properties)
    {
        super(properties);
    }

    public static ForgeFlowingFluid.Properties makeProperties()
    {
        return new ForgeFlowingFluid.Properties(FluidInit.DISTORTIC_WATER_TYPE, FluidInit.DISTORTIC_WATER,
                FluidInit.DISTORTIC_WATER_FLOWING).bucket(ItemInit.DISTORTIC_WATER_BUCKET)
                        .block(FluidInit.DISTORTIC_WATER_BLOCK);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
    {
        consumer.accept(new IClientFluidTypeExtensions()
        {
            @Override
            public ResourceLocation getStillTexture()
            {
                return DISTORTIC_WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture()
            {
                return DISTORTIC_WATER_FLOWING;
            }

            @Nullable
            @Override
            public ResourceLocation getOverlayTexture()
            {
                return DISTORTIC_WATER_OVERLAY;
            }
        });
    }
}