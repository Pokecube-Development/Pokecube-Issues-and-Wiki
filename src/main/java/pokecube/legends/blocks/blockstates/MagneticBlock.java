package pokecube.legends.blocks.blockstates;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.LightningBoltEntity;
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
    public MagneticBlock(final String name, final Material material)
    {
        super(name, Properties.create(material).sound(SoundType.STONE).hardnessAndResistance(3, 8).harvestTool(
                ToolType.PICKAXE).harvestLevel(1));
    }

    @SuppressWarnings("deprecation")
	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity entity, Hand hand,
			BlockRayTraceResult hit) {
		super.onBlockActivated(state, world, pos, entity, hand, hit);
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		@SuppressWarnings("unused")
		Direction direction = hit.getFace();
		{
			java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
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
        int x = (int) dependencies.get("x");
		int y = (int) dependencies.get("y");
		int z = (int) dependencies.get("z");

        World world = (World) dependencies.get("world");
        final Entity entity = (Entity) dependencies.get("entity");
        if (entity instanceof ServerPlayerEntity) {
        	if (!world.isRemote) {
				world.createExplosion(null, (int) x, (int) y, (int) z, (float) 3, Explosion.Mode.BREAK);
			}

        	if (world instanceof ServerWorld) {
				//((ServerWorld) world).addEntity(new LightningBoltEntity(null, world));
        	}
        }
    }
}
