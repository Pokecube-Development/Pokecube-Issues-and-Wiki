package pokecube.legends.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class CobbleUltraBlock extends BlockBase
{
    public CobbleUltraBlock(final String name, final Material material)
    {
        super(name, Block.Properties.create(material).hardnessAndResistance(6, 10).harvestTool(ToolType.PICKAXE)
                .harvestLevel(1).sound(SoundType.STONE));
    }

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
            CobbleUltraBlock.executeProcedure($_dependencies);
        }
    }

    public static void executeProcedure(final java.util.HashMap<String, Object> dependencies)
    {
        if (dependencies.get("entity") == null) return;
        final Entity entity = (Entity) dependencies.get("entity");
        if (entity instanceof PlayerEntity) if (entity.dimension != 0) if (entity instanceof PlayerEntity)
            ((PlayerEntity) entity).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, (int) 120, (int) 1));
    }
}