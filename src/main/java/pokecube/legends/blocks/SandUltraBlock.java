package pokecube.legends.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import pokecube.legends.worldgen.dimension.ModDimensions;

public class SandUltraBlock extends BlockBase
{
    public SandUltraBlock(final String name, final Material material)
    {
        super(name, Properties.create(material).sound(SoundType.SAND).hardnessAndResistance(2, 6).harvestTool(
                ToolType.SHOVEL).harvestLevel(1));
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
            SandUltraBlock.executeProcedure($_dependencies);
        }
    }

    public static void executeProcedure(final java.util.HashMap<String, Object> dependencies)
    {
        if (dependencies.get("entity") == null)
        {
            System.err.println("Failed to WalkEffect!");
            return;
        }
        final Entity entity = (Entity) dependencies.get("entity");
        if (entity instanceof ServerPlayerEntity) if (entity.dimension.getId() == ModDimensions.DIMENSION_TYPE.getId())
            ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.LEVITATION, 60, 1));
    }
}
