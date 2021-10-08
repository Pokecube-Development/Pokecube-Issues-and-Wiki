package pokecube.legends.blocks.normalblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.legends.blocks.BlockBase;

public class MagneticBlock extends BlockBase
{

    public MagneticBlock(final Material material, final MaterialColor color, final float hardness, final float resistance, final SoundType sound,
            final boolean hasDrop)
    {
        super(material, color, hardness, resistance, sound, hasDrop);
    }

    @Override
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos, final Player entity,
            final InteractionHand hand, final BlockHitResult hit)
    {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        {
            final java.util.HashMap<String, Object> $_dependencies = new java.util.HashMap<>();
            $_dependencies.put("entity", entity);
            $_dependencies.put("x", x);
            $_dependencies.put("y", y);
            $_dependencies.put("z", z);
            $_dependencies.put("world", world);
            MagneticBlock.executeProcedure($_dependencies);
        }
        return InteractionResult.SUCCESS;
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

        final Level world = (Level) dependencies.get("world");
        final Entity entity = (Entity) dependencies.get("entity");
        if (entity instanceof ServerPlayer)
        {
            if (!world.isClientSide) world.explode(null, x, y, z, 3, Explosion.BlockInteraction.BREAK);

            if (world instanceof ServerLevel)
            {
                // ((ServerWorld) world).addEntity(new LightningBoltEntity(null,
                // world));
            }
        }
    }
}
