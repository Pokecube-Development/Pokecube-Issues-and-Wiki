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
        float f = (float) (random.nextInt(4) + 4);

        Map<BlockPos, BlockState> toPlace = Maps.newHashMap();

        for (int y = 0 + random.nextInt(2); f > 0.5F; --y)
        {
            for (int x = Mth.floor(-f); x <= Mth.ceil(f); ++x)
            {
                for (int z = Mth.floor(-f); z <= Mth.ceil(f); ++z)
                {
                    if ((float) (x * x + z * z) <= (f + 1.0F) * (f + 1.0F))
                    {
                        toPlace.put(new BlockPos(x, y, z), BlockInit.DISTORTIC_STONE.get().defaultBlockState());
                        toPlace.put(new BlockPos(x, y + 1, z),
                                BlockInit.DISTORTIC_GRASS_BLOCK.get().defaultBlockState());
                        toPlace.put(new BlockPos(x, y - 1, z), BlockInit.CRACKED_DISTORTIC_STONE.get()
                                .defaultBlockState().setValue(DirectionalBlock.FACING, Direction.DOWN));
                        toPlace.put(new BlockPos(x, y - 2, z), BlockInit.DISTORTIC_GLOWSTONE.get().defaultBlockState());
                    }
                }
            }
            f = (float) ((double) f - ((double) random.nextInt(2) + 0.5D));
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