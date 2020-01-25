package pokecube.legends.init.function;

import net.minecraft.block.Blocks;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
// import pokecube.core.interfaces.IPokemob;
// import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Uses player interact here to also prevent opening of inventories.
 *
 * @param dependencies
 */
public class WormHoleActiveFunction
{
    public static void executeProcedure(final java.util.HashMap<String, Object> dependencies)
    {
        if (dependencies.get("x") == null)
        {
            System.err.println("Failed to load dependency x for PortalActive!");
            return;
        }
        if (dependencies.get("y") == null)
        {
            System.err.println("Failed to load dependency y for PortalActive!");
            return;
        }
        if (dependencies.get("z") == null)
        {
            System.err.println("Failed to load dependency z for PortalActive!");
            return;
        }
        if (dependencies.get("world") == null)
        {
            System.err.println("Failed to load dependency world for PortalActive!");
            return;
        }
        final int x = (int) dependencies.get("x");
        final int y = (int) dependencies.get("y");
        final int z = (int) dependencies.get("z");
        final World world = (World) dependencies.get("world");
        world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState());
        if (world instanceof ServerWorld)
        {
            final ServerWorld sworld = (ServerWorld) world;
            sworld.spawnParticle(ParticleTypes.FIREWORK, x, y + 1, z, 15, 6, 6, 6, 0.4);
            sworld.playSound(x, y, z, SoundEvents.ENTITY_WITHER_DEATH, SoundCategory.NEUTRAL, 1, 1, false);
        }
    }
}