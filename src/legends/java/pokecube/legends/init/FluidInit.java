package pokecube.legends.init;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.fluids.DistorticWaterType;
import pokecube.legends.fluids.MoltenMeteoriteType;

import java.util.function.Supplier;

public class FluidInit
{
    public static Supplier<FlowingFluid> DISTORTIC_WATER;
    public static Supplier<FlowingFluid> DISTORTIC_WATER_FLOWING;
    public static Supplier<LiquidBlock> DISTORTIC_WATER_BLOCK;

    public static Supplier<FlowingFluid> MOLTEN_METEORITE;
    public static Supplier<FlowingFluid> MOLTEN_METEORITE_FLOWING;

    static
    {
        DISTORTIC_WATER = PokecubeLegends.FLUIDS.register("distortic_water", () ->
                        new BaseFlowingFluid.Source(DistorticWaterType.makeProperties()));

        DISTORTIC_WATER_FLOWING = PokecubeLegends.FLUIDS.register("distortic_water_flowing", () ->
                        new BaseFlowingFluid.Flowing(DistorticWaterType.makeProperties()));

        DISTORTIC_WATER_BLOCK = PokecubeLegends.BLOCKS.register("distortic_water_block", () ->
                        new LiquidBlock(DISTORTIC_WATER.get(), BlockBehaviour.Properties.of()
                                .mapColor(MapColor.COLOR_LIGHT_BLUE).noCollission().strength(100.0F).noLootTable()));


        MOLTEN_METEORITE = PokecubeLegends.FLUIDS.register("molten_meteorite", () ->
                new BaseFlowingFluid.Source(MoltenMeteoriteType.makeProperties()));

        MOLTEN_METEORITE_FLOWING = PokecubeLegends.FLUIDS.register("molten_meteorite_flowing", () ->
                new BaseFlowingFluid.Flowing(MoltenMeteoriteType.makeProperties()));
    }

    public static void init()
    {}
}
