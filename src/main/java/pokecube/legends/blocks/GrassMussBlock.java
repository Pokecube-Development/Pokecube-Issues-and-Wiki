package pokecube.legends.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolType;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class GrassMussBlock extends BlockBase
{
    public GrassMussBlock(final String name, final Material material)
    {
        super(name, Properties.create(material).sound(SoundType.PLANT).hardnessAndResistance(1, 2).harvestTool(
                ToolType.SHOVEL).harvestLevel(1));
    }

    @Override
    public boolean canSustainPlant(final BlockState state, final IBlockReader world, final BlockPos pos,
            final Direction direction, final IPlantable plantable)
    {
        return true;
    }

    @SuppressWarnings("unused")
    @Override
    public void onEntityWalk(final World world, final BlockPos pos, final Entity entity)
    {
        super.onEntityWalk(world, pos, entity);
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        final Block block = this;
        {
            final java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
            $_dependencies.put("entity", entity);
            GrassMussBlock.executeProcedure($_dependencies);
        }
    }

    public static void executeProcedure(final java.util.HashMap<String, Object> dependencies)
    {
        if (dependencies.get("entity") == null)
        {
            System.err.println("Failed to WalkGrassEffect!");
            return;
        }
        final Entity entity = (Entity) dependencies.get("entity");
        if (entity instanceof ServerPlayerEntity) if (entity.dimension.getId() == ModDimensions.DIMENSION_TYPE.getId())
            ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 60, 1));
    }
}
