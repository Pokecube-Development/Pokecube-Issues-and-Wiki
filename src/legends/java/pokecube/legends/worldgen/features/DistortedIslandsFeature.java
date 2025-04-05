package pokecube.legends.worldgen.features;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import pokecube.legends.init.BlockInit;

public class DistortedIslandsFeature extends Feature<NoneFeatureConfiguration>
{
    public DistortedIslandsFeature(Codec<NoneFeatureConfiguration> config)
    {
        super(config);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context)
    {
        WorldGenLevel worldLevel = context.level();
        RandomSource random = context.random();
        BlockPos pos = context.origin();
        float f = random.nextInt(4) + 4;

        Map<BlockPos, BlockState> toPlace = Maps.newHashMap();

        // This results in a circle.
        for (int x = Mth.floor(-f); x <= Mth.ceil(f); ++x)
        {
            for (int z = Mth.floor(-f); z <= Mth.ceil(f); ++z)
            {
                double r = Math.sqrt(z * z + x * x);
                if (r > f) continue;

                // This results in a semi-random height, but average larger in
                // the middle.
                int yMax = (int) (f);
                r -= random.nextInt(3);
                int yMin = (int) r;
                if (yMax - yMin < 3) continue;
                for (int y = yMax; y >= yMin; y--)
                {
                    BlockState state = null;
                    if (y == yMax) state = BlockInit.DISTORTIC_GRASS_BLOCK.get().defaultBlockState();
                    else if (y == yMin) state = BlockInit.DISTORTIC_GLOWSTONE.get().defaultBlockState();
                    else if (y == yMin + 1) state = BlockInit.CRACKED_DISTORTIC_STONE.get().defaultBlockState()
                            .setValue(DirectionalBlock.FACING, Direction.DOWN);
                    else state = BlockInit.DISTORTIC_STONE.get().defaultBlockState();
                    toPlace.put(new BlockPos(x, y, z), state);
                }
            }
        }

        Direction dir = Direction.getRandom(random);

        toPlace.forEach((pos1, state) -> {

            int x = pos1.getX();
            int y = pos1.getY();
            int z = pos1.getZ();

            if (state.hasProperty(DirectionalBlock.FACING))
            {
                state = state.setValue(DirectionalBlock.FACING,
                        state.getValue(DirectionalBlock.FACING) == Direction.UP ? dir : dir.getOpposite());
            }

            switch (dir)
            {
            case DOWN:
                y = -pos1.getY();
                break;
            case EAST:
                // X is up
                x = pos1.getY();
                y = pos1.getX();
                break;
            case NORTH:
                z = -pos1.getY();
                y = pos1.getZ();
                break;
            case SOUTH:
                z = pos1.getY();
                y = pos1.getZ();
                break;
            case UP:
                // This is default case.
                break;
            case WEST:
                x = -pos1.getY();
                y = pos1.getX();
                break;
            default:
                break;
            }
            BlockPos newPos = pos.offset(x, y, z);
            this.setBlock(worldLevel, newPos, state);
        });
        return true;
    }
}