package pokecube.legends.blocks.plants;

import net.minecraft.block.Block;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.PlantType;

public class PlantBase extends FlowerBlock
{
    public static final Block block = null;

    public PlantBase(final Material material, final float hardness, final float resistance, final SoundType sound)
    {
        super(Effects.SATURATION, 0, Block.Properties.create(material).hardnessAndResistance(hardness, resistance)
                .doesNotBlockMovement().sound(sound).lightValue(2));
    }

    @Override
    public PlantType getPlantType(final IBlockReader world, final BlockPos pos)
    {
        return PlantType.Plains;
    }
}
