package pokecube.legends.blocks.normalblocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ToolType;
import pokecube.legends.blocks.BlockBase;

public class MagneticBlock extends BlockBase
{
    public MagneticBlock(final String name, final Material material, MaterialColor color)
    {
        super(name, Properties.of(material).sound(SoundType.STONE).strength(3, 8).harvestTool(
                ToolType.PICKAXE).harvestLevel(1));
    }

    @SuppressWarnings("deprecation")
	@Override
	public ActionResultType use(final BlockState state, final World world, final BlockPos pos, final PlayerEntity entity, final Hand hand,
			final BlockRayTraceResult hit) {
		super.use(state, world, pos, entity, hand, hit);
		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();
		@SuppressWarnings("unused")
        final
		Direction direction = hit.getDirection();
		{
			final java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
			$_dependencies.put("entity", entity);
			$_dependencies.put("x", x);
			$_dependencies.put("y", y);
			$_dependencies.put("z", z);
			$_dependencies.put("world", world);
			MagneticBlock.executeProcedure($_dependencies);
		}
		return ActionResultType.SUCCESS;
	}

    public static void executeProcedure(final java.util.HashMap<String, Object> dependencies)
    {
        if (dependencies.get("entity") == null)
        {
            System.err.println("Failed to WalkEffect!");
            return;
        }
        final int x = (int) dependencies.get("x");
		final int y = (int) dependencies.get("y");
		final int z = (int) dependencies.get("z");

        final World world = (World) dependencies.get("world");
        final Entity entity = (Entity) dependencies.get("entity");
        if (entity instanceof ServerPlayerEntity) {
        	if (!world.isClientSide) world.explode(null, x, y, z, 3, Explosion.Mode.BREAK);

        	if (world instanceof ServerWorld) {
				//((ServerWorld) world).addEntity(new LightningBoltEntity(null, world));
        	}
        }
    }
}
