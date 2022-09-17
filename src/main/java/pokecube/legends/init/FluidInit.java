package pokecube.legends.init;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.fluids.DistorticWaterType;

public class FluidInit
{
    public static RegistryObject<FluidType> DISTORTIC_WATER_TYPE;
    public static RegistryObject<FlowingFluid> DISTORTIC_WATER;
    public static RegistryObject<FlowingFluid> DISTORTIC_WATER_FLOWING;
    public static RegistryObject<LiquidBlock> DISTORTIC_WATER_BLOCK;

    static
    {
        DISTORTIC_WATER_TYPE = PokecubeLegends.FLUID_TYPES.register("distortic_water", () -> new DistorticWaterType(
                FluidType.Properties.create().density(-500).temperature(20).viscosity(20).lightLevel(0)));

        DISTORTIC_WATER = PokecubeLegends.FLUIDS.register("distortic_water", () ->
                        new ForgeFlowingFluid.Source(DistorticWaterType.makeProperties()));

        DISTORTIC_WATER_FLOWING = PokecubeLegends.FLUIDS.register("distortic_water_flowing", () ->
                        new ForgeFlowingFluid.Flowing(DistorticWaterType.makeProperties()));

        DISTORTIC_WATER_BLOCK = PokecubeLegends.DIMENSIONS_TAB.register("distortic_water_block", () ->
                        new LiquidBlock(DISTORTIC_WATER, BlockBehaviour.Properties.of(Material.WATER).noCollission().strength(100.0F).noLootTable()));
    }

    public static void init()
    {}
}
