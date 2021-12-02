package pokecube.legends.init;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.fluids.DistorticWater;

public class FluidInit
{
    public static RegistryObject<FlowingFluid>      DISTORTED_WATER;
    public static RegistryObject<FlowingFluid>      DISTORTED_WATER_FLOWING;
    public static RegistryObject<LiquidBlock> DISTORTED_WATER_BLOCK;

    static
    {
        FluidInit.DISTORTED_WATER = PokecubeLegends.FLUIDS.register("distortic_water", () -> new ForgeFlowingFluid.Source(
                DistorticWater.makeProperties().canMultiply()));
        FluidInit.DISTORTED_WATER_FLOWING = PokecubeLegends.FLUIDS.register("distortic_water_flowing",
                () -> new ForgeFlowingFluid.Flowing(DistorticWater.makeProperties()));
        FluidInit.DISTORTED_WATER_BLOCK = PokecubeLegends.DIMENSIONS_TAB.register("distortic_water_block",
                () -> new LiquidBlock(FluidInit.DISTORTED_WATER, BlockBehaviour.Properties.of(Material.LAVA,
                        MaterialColor.COLOR_LIGHT_BLUE).noCollission().strength(100f).noDrops()));
    }

    public static void init()
    {
    }

    // FIXME remove this when forge fixes fluids crash
    public static void finish()
    {
        FluidInit.DISTORTED_WATER_BLOCK.get().fluid = FluidInit.DISTORTED_WATER.get();
    }
}
