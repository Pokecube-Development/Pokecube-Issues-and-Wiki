package pokecube.legends.init;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.fluids.DistorticWater;

public class FluidInit
{
    public static RegistryObject<FluidType> DISTORTED_WATER_TYPE;
    public static RegistryObject<FlowingFluid> DISTORTED_WATER;
    public static RegistryObject<FlowingFluid> DISTORTED_WATER_FLOWING;
    public static RegistryObject<LiquidBlock> DISTORTED_WATER_BLOCK;

//  FluidAttributes.builder(DISTORTIC_WATER_STILL_TEXTURE, DISTORTIC_WATER_FLOWING_TEXTURE)
//  .overlay(DISTORTIC_WATER_OVERLAY_TEXTURE)
//  .density(-500)
//  .temperature(20)
//  .viscosity(20)
//  .gaseous()
//  .luminosity(10)
//  .sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY)

    static
    {
        DISTORTED_WATER_TYPE = PokecubeLegends.FLUID_TYPES.register("distortic_water", () -> new FluidType(
                FluidType.Properties.create().density(-500).temperature(20).viscosity(20).lightLevel(10)));

        DISTORTED_WATER = PokecubeLegends.FLUIDS.register("distortic_water",
                () -> new ForgeFlowingFluid.Source(DistorticWater.makeProperties()));
        DISTORTED_WATER_FLOWING = PokecubeLegends.FLUIDS.register("distortic_water_flowing",
                () -> new ForgeFlowingFluid.Flowing(DistorticWater.makeProperties()));
        DISTORTED_WATER_BLOCK = PokecubeLegends.DIMENSIONS_TAB.register("distortic_water_block",
                () -> new LiquidBlock(FluidInit.DISTORTED_WATER, BlockBehaviour.Properties
                        .of(Material.LAVA, MaterialColor.COLOR_LIGHT_BLUE).noCollission().strength(100f)));
    }

    public static void init()
    {}
}
