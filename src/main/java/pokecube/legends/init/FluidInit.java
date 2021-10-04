package pokecube.legends.init;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fmllegacy.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.fluids.DistorticWater;

public class FluidInit
{
    public static RegistryObject<FlowingFluid>      DISTORTED_WATER;
    public static RegistryObject<FlowingFluid>      DISTORTED_WATER_FLOWING;
    public static RegistryObject<LiquidBlock> DISTORTED_WATER_BLOCK;
    public static RegistryObject<Item>              DISTORTED_WATER_BUCKET;

    static
    {
        FluidInit.DISTORTED_WATER = PokecubeLegends.FLUIDS.register("distortic_water", () -> new ForgeFlowingFluid.Source(
                DistorticWater.makeProperties().canMultiply()));
        FluidInit.DISTORTED_WATER_FLOWING = PokecubeLegends.FLUIDS.register("distortic_water_flowing",
                () -> new ForgeFlowingFluid.Flowing(DistorticWater.makeProperties()));
        FluidInit.DISTORTED_WATER_BLOCK = PokecubeLegends.BLOCKS_TAB.register("distortic_water_block",
                () -> new LiquidBlock(FluidInit.DISTORTED_WATER, BlockBehaviour.Properties.of(Material.LAVA,
                        MaterialColor.COLOR_LIGHT_BLUE).noCollission().strength(100f).noDrops()));
        FluidInit.DISTORTED_WATER_BUCKET = PokecubeLegends.ITEMS.register("distortic_water_bucket", () -> new BucketItem(
                FluidInit.DISTORTED_WATER, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(
                        PokecubeLegends.DECO_TAB).fireResistant()));
    }

    public static void init()
    {
    }
}
