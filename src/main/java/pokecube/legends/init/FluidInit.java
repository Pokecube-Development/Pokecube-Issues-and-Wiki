package pokecube.legends.init;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.fluids.DistorticWaterType;
import pokecube.legends.fluids.MoltenMeteoriteType;

public class FluidInit
{
    public static RegistryObject<FlowingFluid> DISTORTIC_WATER;
    public static RegistryObject<FlowingFluid> DISTORTIC_WATER_FLOWING;
    public static RegistryObject<LiquidBlock> DISTORTIC_WATER_BLOCK;

    public static RegistryObject<FlowingFluid> MOLTEN_METEORITE;
    public static RegistryObject<FlowingFluid> MOLTEN_METEORITE_FLOWING;

    static
    {
        DISTORTIC_WATER = PokecubeLegends.FLUIDS.register("distortic_water", () ->
                        new ForgeFlowingFluid.Source(DistorticWaterType.makeProperties()));

        DISTORTIC_WATER_FLOWING = PokecubeLegends.FLUIDS.register("distortic_water_flowing", () ->
                        new ForgeFlowingFluid.Flowing(DistorticWaterType.makeProperties()));

        DISTORTIC_WATER_BLOCK = PokecubeLegends.BLOCKS.register("distortic_water_block", () ->
                        new LiquidBlock(DISTORTIC_WATER, BlockBehaviour.Properties.of()
                                .mapColor(MapColor.COLOR_LIGHT_BLUE).noCollission().strength(100.0F).noLootTable()));


        MOLTEN_METEORITE = PokecubeLegends.FLUIDS.register("molten_meteorite", () ->
                new ForgeFlowingFluid.Source(MoltenMeteoriteType.makeProperties()));

        MOLTEN_METEORITE_FLOWING = PokecubeLegends.FLUIDS.register("molten_meteorite_flowing", () ->
                new ForgeFlowingFluid.Flowing(MoltenMeteoriteType.makeProperties()));
    }

    public static void init()
    {}
}
